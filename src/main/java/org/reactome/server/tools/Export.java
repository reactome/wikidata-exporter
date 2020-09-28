package org.reactome.server.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martiansoftware.jsap.*;
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


/**
 * @author Yusra Haider (yhaider@ebi.ac.uk)
 **/

public class Export {

    private static Set<WDPathway> wdPathways;
    private static Set<WDReaction> wdReactions;
    private static Set<WDPhysicalEntity> wdPhysicalEntities;
    private static Set<WDModifiedProtein> wdModifiedProteins;
    private static Map<String, Set> wdParents;

    private static String pathwayFile = "pathway.json";
    private static String reactionFile = "reaction.json";
    private static String physicalEntityFile = "physicalEntity.json";
    private static String modifiedProteinFile = "modifiedProtein.json";
    private static String parentFile = "parent.json";


    public static void main(String[] args) throws JSAPException {

        SimpleJSAP jsap = new SimpleJSAP(Export.class.getName(), "A tool to export data for Wikidata",
                new Parameter[]{
                        new FlaggedOption("host", JSAP.STRING_PARSER, "localhost", JSAP.REQUIRED, 'h', "host", "The neo4j host"),
                        new FlaggedOption("port", JSAP.STRING_PARSER, "7474", JSAP.NOT_REQUIRED, 'b', "port", "The neo4j port"),
                        new FlaggedOption("user", JSAP.STRING_PARSER, "neo4j", JSAP.REQUIRED, 'u', "user", "The neo4j user"),
                        new FlaggedOption("password", JSAP.STRING_PARSER, "reactome", JSAP.REQUIRED, 'p', "password", "The neo4j password"),
                        new FlaggedOption("outputdirectory", JSAP.STRING_PARSER, ".", JSAP.REQUIRED, 'o', "outputdirectory", "The output directory")
                }
        );

        JSAPResult config = jsap.parse(args);
        if (jsap.messagePrinted()) System.exit(1);

        ReactomeGraphCore.initialise(config.getString("host"), config.getString("port"), config.getString("user"), config.getString("password"), GraphNeo4jConfig.class);

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
        wdParents = new HashMap<>();

        Collection<SimpleDatabaseObject> topLevelPathways = schemaService.getSimpleDatabaseObjectByClass(TopLevelPathway.class, species);
        Iterator<SimpleDatabaseObject> iterator = topLevelPathways.iterator();
        while (iterator.hasNext()) {
            Pathway pathway = databaseObjectService.findByIdNoRelations(iterator.next().getStId());
            traversePathway(pathway);
        }

        System.out.println(wdPathways.size());
        System.out.println(wdReactions.size());
        System.out.println(wdPhysicalEntities.size());
        System.out.println(wdModifiedProteins.size());
        System.out.println(wdParents.size());

        ObjectMapper objectMapper = new ObjectMapper();
        String outputDirectory = config.getString("outputdirectory");

        try {
            objectMapper.writeValue(new File(outputDirectory, pathwayFile), wdPathways);
            objectMapper.writeValue(new File(outputDirectory, reactionFile), wdReactions);
            objectMapper.writeValue(new File(outputDirectory, physicalEntityFile), wdPhysicalEntities);
            objectMapper.writeValue(new File(outputDirectory, modifiedProteinFile), wdModifiedProteins);

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
                add_parent_child_link(event.getStId(), parent.getStId());
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
                WDLinks link = new WDLinks(so.getObject(), so.getStoichiometry());
                input.add(link);
                if (link.getIdType() == "REACTOME") {
                    add_parent_child_link(link.getId(), reaction.getStId());
                }
                traverseEntity((PhysicalEntity) so.getObject());
            }
        }

        loe = reaction.getOutput();
        List<WDLinks> output = null;
        if (loe != null && loe.size() > 0) {
            for (PhysicalEntity pe: loe) {
                output = new ArrayList<>();
                for (StoichiometryObject so : reaction.fetchOutput()) {
                    WDLinks link= new WDLinks(so.getObject(), so.getStoichiometry());
                    output.add(link);
                    if (link.getIdType() == "REACTOME") {
                        add_parent_child_link(link.getId(), reaction.getStId());
                    }
                    traverseEntity((PhysicalEntity) so.getObject());
                }
                traverseEntity(pe);
            }
        }

        List<WDLinks> modifier = new ArrayList<>();
        if (reaction.getCatalystActivity() != null){
            for (CatalystActivity catalystActivity: reaction.getCatalystActivity() ){
                //TODO figure out / ask about stoichiometry here
                WDLinks link = new WDLinks(catalystActivity.getPhysicalEntity(), 1);
                modifier.add(link);
                if (link.getIdType() == "REACTOME") {
                    add_parent_child_link(link.getId(), reaction.getStId());
                }
                traverseEntity(catalystActivity.getPhysicalEntity());
            }
        }

        if (reaction.getRegulatedBy() != null) {
            for (Regulation reg : reaction.getRegulatedBy()) {
                DatabaseObject pe = reg.getRegulator();
                if (pe instanceof PhysicalEntity) {
                    //TODO figure out / ask about stoichiometry here
                    WDLinks link = new WDLinks(pe, 1);
                    modifier.add(link);
                    if (link.getIdType() == "REACTOME") {
                        add_parent_child_link(link.getId(), reaction.getStId());
                    }
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
                    WDLinks link = new WDLinks((PhysicalEntity) stoichiometryObject.getObject(), stoichiometryObject.getStoichiometry());
                    parts.add(link);
                    if (link.getIdType() == "REACTOME") {
                        add_parent_child_link(link.getId(), physicalEntity.getStId());
                    }
                    traverseEntity(stoichiometryObject.getObject());
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
                // todo check for missing data here
                // todo for example, we aren't adding the candidates of candidate sets here
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
                    if (link.getIdType() == "REACTOME") {
                        add_parent_child_link(link.getId(), physicalEntity.getStId());
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

    private static void add_parent_child_link(String key, String val) {
        if (wdParents.containsKey(key)) {
            Set set = wdParents.get(key);
            set.add(val);
        }
        else {
            Set set = new HashSet();
            set.add(val);
            wdParents.put(key, set);
        }
    }
}
