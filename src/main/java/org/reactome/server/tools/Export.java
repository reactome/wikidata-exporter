package org.reactome.server.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.domain.result.SimpleDatabaseObject;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.SchemaService;
import org.reactome.server.graph.service.helper.StoichiometryObject;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.config.GraphNeo4jConfig;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class Export {

    private static Set<WDPathway> wdPathways;
    private static Set<WDReaction> wdReactions;
    private static Set<WDPhysicalEntity> wdPhysicalEntities;
    private static Set<WDModifiedProtein> wdModifiedProteins;

    public static void main(String[] args) {


        ReactomeGraphCore.initialise("localhost", "7474", "neo4j", "Software1o1", GraphNeo4jConfig.class);

        DatabaseObjectService databaseObjectService = ReactomeGraphCore.getService(DatabaseObjectService.class);
        SchemaService schemaService = ReactomeGraphCore.getService(SchemaService.class);
        // hard coded to homosapiens
        // we are presently only fetching Reactome entities for the homosapiens species
        long speciesId = 48887;
        Species species = databaseObjectService.findByIdNoRelations(speciesId);

        // We have used a linked hash set to maintain the insertion order
        // this is important here as our code adds all the "children"
        // to the list and then the parent
        // the bot code relies on this ordering
        // it ensures that the children have been traversed and added to Wikidata
        // before the parents
        wdPathways = new LinkedHashSet<>();
        wdReactions = new LinkedHashSet<>();
        wdPhysicalEntities = new LinkedHashSet<>();
        wdModifiedProteins = new LinkedHashSet<>();


        Collection<SimpleDatabaseObject> topLevelPathways = schemaService.getSimpleDatabaseObjectByClass(TopLevelPathway.class, species);
        Iterator<SimpleDatabaseObject> iterator = topLevelPathways.iterator();
        while (iterator.hasNext()) {
            // todo: remove the commented stuff before commiting
           // Pathway pathway = databaseObjectService.findByIdNoRelations("R-HSA-9615710"); // Late Endosomal Autophagy
            //Pathway pathway = databaseObjectService.findByIdNoRelations("R-HSA-9612973"); // Autophagy Top level pathway
            //Pathway pathway = databaseObjectService.findByIdNoRelations("R-HSA-1640170"); // Cell Cycle top level pathway
            //Pathway pathway = databaseObjectService.findByIdNoRelations("R-HSA-1500931"); // Cell-Cell communication top level pathway
            //Pathway pathway = databaseObjectService.findByIdNoRelations("R-HSA-8953897"); //  Cellular responses to external stimuli top level pathway
            Pathway pathway = databaseObjectService.findByIdNoRelations(iterator.next().getStId());
            traversePathway(pathway);
        }

        System.out.println(wdPathways.size());
        System.out.println(wdReactions.size());
        System.out.println(wdPhysicalEntities.size());
        System.out.println(wdModifiedProteins.size());

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File("/Users/yhaider/dev/reactome/wikidata-exporter/outputdir", "pathway.json"), wdPathways);
            objectMapper.writeValue(new File("/Users/yhaider/dev/reactome/wikidata-exporter/outputdir", "reaction.json"), wdReactions);
            objectMapper.writeValue(new File("/Users/yhaider/dev/reactome/wikidata-exporter/outputdir", "physicalEntities.json"), wdPhysicalEntities);
            objectMapper.writeValue(new File("/Users/yhaider/dev/reactome/wikidata-exporter/outputdir", "modifiedProteins.json"), wdModifiedProteins);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void traversePathway(Pathway parent) {
        List<Event> loe = parent.getHasEvent();

        WDPathway wdPathway = new WDPathway(parent);
        List<WDLinks> parts = null;

        if (parent.getHasEvent() != null || parent.getHasEvent().size() > 0) {

            parts = new ArrayList<>();
            for (Event event : loe) {
                // adding and traversing the parts of a pathway
                parts.add(new WDLinks(event, null));
                if (event instanceof Pathway) {
                    Pathway child = (Pathway) (event);
                    traversePathway(child);
                } else if (event instanceof ReactionLikeEvent) {
                    ReactionLikeEvent child = (ReactionLikeEvent) (event);
                    traverseReaction(child);
                }
            }
        }
        // setting parts of the pathway
        wdPathway.setParts(parts);
        // and then adding pathway to the list
        wdPathways.add(wdPathway);
    }


    private static void traverseReaction(ReactionLikeEvent reaction) {

        WDReaction wdReaction = new WDReaction(reaction);

        List<PhysicalEntity> loe = reaction.getInput();
        List<WDLinks> input = null;
        if (loe != null && loe.size() > 0) {
            input = new ArrayList<>();
            for (StoichiometryObject so : reaction.fetchInput()) {
                input.add(new WDLinks((PhysicalEntity)so.getObject(), so.getStoichiometry()));
                traverseEntity((PhysicalEntity) so.getObject());
            }
        }

        loe = reaction.getOutput();
        List<WDLinks> output = null;
        if (loe != null && loe.size() > 0) {
            for (PhysicalEntity pe: loe) {
                output = new ArrayList<>();
                for (StoichiometryObject so : reaction.fetchOutput()) {
                    output.add(new WDLinks((PhysicalEntity) so.getObject(), so.getStoichiometry()));
                    traverseEntity((PhysicalEntity) so.getObject());
                }
                traverseEntity(pe);
            }
        }

        List<WDLinks> modifier = new ArrayList<>();
        if (reaction.getCatalystActivity() != null){
            for (CatalystActivity catalystActivity: reaction.getCatalystActivity() ){
                //TODO figure out / ask about stoichiometry here
                modifier.add(new WDLinks(catalystActivity.getPhysicalEntity(), 1));
                traverseEntity(catalystActivity.getPhysicalEntity());
            }
        }

        if (reaction.getRegulatedBy() != null) {
            for (Regulation reg : reaction.getRegulatedBy()) {
                DatabaseObject pe = reg.getRegulator();
                if (pe instanceof PhysicalEntity) {
                    //TODO figure out / ask about stoichiometry here
                    modifier.add(new WDLinks((PhysicalEntity)(pe), 1));
                    traverseEntity((PhysicalEntity)(pe));
                }
            }
        }

        wdReaction.setInput(input);
        wdReaction.setOutput(output);
        wdReaction.setModifier(modifier);
        wdReactions.add(wdReaction);
    }


    private static void traverseEntity(PhysicalEntity physicalEntity) {

        if (physicalEntity instanceof Complex) {
            Complex complex = (Complex) (physicalEntity);
            WDComplex wdComplex = new WDComplex(complex);
            List<WDLinks> parts = null;

            if (complex.getHasComponent() != null) {
                parts = new ArrayList<>();
                for (StoichiometryObject stoichiometryObject : complex.fetchHasComponent()) {
                    parts.add(new WDLinks((PhysicalEntity) stoichiometryObject.getObject(), stoichiometryObject.getStoichiometry()));
                    traverseEntity((PhysicalEntity) stoichiometryObject.getObject());
                }
            }

                wdComplex.setParts(parts);
                wdPhysicalEntities.add(wdComplex);
        }

        else if (physicalEntity instanceof EntitySet) {
            EntitySet entitySet = (EntitySet) (physicalEntity);
            WDEntitySet wdEntitySet = new WDEntitySet(entitySet);
            List<WDLinks> parts = null;

            if (entitySet.getHasMember() != null) {
                parts = new ArrayList<>();
//                 todo check for missing data here
//                 todo for example, we aren't adding the candidates of candidate sets here
                for (PhysicalEntity member : entitySet.getHasMember()) {
                    WDLinks link = new WDLinks(member, 1);
                    int linkIndex = parts.indexOf(link);
                    if (linkIndex != -1) {
                        WDLinks part = parts.get(linkIndex);
                        part.setQty(part.getQty() + 1);
                    }
                    else {
                        parts.add(link);
                    }
                    traverseEntity(member);
                }
            }


            wdEntitySet.setParts(parts);
            wdPhysicalEntities.add(wdEntitySet);
        }
        else if (isModifiedProtein(physicalEntity)) {
            WDModifiedProtein wdModifiedProtein = new WDModifiedProtein((EntityWithAccessionedSequence)(physicalEntity));
            wdModifiedProteins.add(wdModifiedProtein);
        }
    }

    private static boolean isModifiedProtein(PhysicalEntity physicalEntity) {
        if (physicalEntity instanceof EntityWithAccessionedSequence) {
            List<AbstractModifiedResidue> mods = ((EntityWithAccessionedSequence) physicalEntity).getHasModifiedResidue();
            if (mods != null && mods.size() > 0) {
                return true;
            }
        }
        return false;
    }

}
