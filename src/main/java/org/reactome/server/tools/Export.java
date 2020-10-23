package org.reactome.server.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martiansoftware.jsap.*;
import org.apache.log4j.Logger;
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

    static Logger log = Logger.getLogger(Export.class);

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

        // hard coded to homo sapiens
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
        // hash map that maintains "child" key : "list of parents of child" value
        wdParents = new HashMap<>();

        Collection<SimpleDatabaseObject> topLevelPathways = schemaService.getSimpleDatabaseObjectByClass(TopLevelPathway.class, species);
        Iterator<SimpleDatabaseObject> iterator = topLevelPathways.iterator();
        while (iterator.hasNext()) {
            Pathway pathway = databaseObjectService.findByIdNoRelations(iterator.next().getStId());
            traversePathway(pathway);
        }

        log.info("number of pathways: " + wdPathways.size());
        log.info("number of reactions: " + wdReactions.size());
        log.info("number of physical entities: " + wdPhysicalEntities.size());
        log.info("number of modified proteins: " + wdModifiedProteins.size());
        log.info("number of child-parent links: " + wdParents.size());

        ObjectMapper objectMapper = new ObjectMapper();
        String outputDirectory = config.getString("outputdirectory");

        try {
            objectMapper.writeValue(new File(outputDirectory, pathwayFile), wdPathways);
            objectMapper.writeValue(new File(outputDirectory, reactionFile), wdReactions);
            objectMapper.writeValue(new File(outputDirectory, physicalEntityFile), wdPhysicalEntities);
            objectMapper.writeValue(new File(outputDirectory, modifiedProteinFile), wdModifiedProteins);
            objectMapper.writeValue(new File(outputDirectory, parentFile), wdParents);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    // traverse a pathway and its children
    private static void traversePathway(Pathway parent) {
        List<Event> loe = parent.getHasEvent();

        WDPathway wdPathway = new WDPathway(parent);
        List<WDLinks> parts = null;

        if (parent.getHasEvent() != null || parent.getHasEvent().size() > 0) {

            parts = new ArrayList<>();
            for (Event event : loe) {
                // adding and traversing the parts of a pathway
                parts.add(new WDLinks(event, null));
                addParentChildLink(event.getStId(), parent.getStId());
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

    // traverse a reaction and its children
    private static void traverseReaction(ReactionLikeEvent reaction) {

        WDReaction wdReaction = new WDReaction(reaction);

        List<PhysicalEntity> loe = reaction.getInput();

        // traverse inputs of a reaction
        List<WDLinks> input = null;
        if (loe != null && loe.size() > 0) {
            input = new ArrayList<>();
            for (StoichiometryObject so : reaction.fetchInput()) {
                WDLinks link = new WDLinks(so.getObject(), so.getStoichiometry());
                input.add(link);
                // if child is from Reactome, create child-parent link
                if (link.getIdType() == "REACTOME") {
                    addParentChildLink(link.getId(), reaction.getStId());
                }
                traverseEntity((PhysicalEntity) so.getObject());
            }
        }

        loe = reaction.getOutput();

        // traverse outputs of a reaction
        List<WDLinks> output = null;
        if (loe != null && loe.size() > 0) {
            for (PhysicalEntity pe : loe) {
                output = new ArrayList<>();
                for (StoichiometryObject so : reaction.fetchOutput()) {
                    WDLinks link = new WDLinks(so.getObject(), so.getStoichiometry());
                    output.add(link);
                    // if child is from Reactome, create child-parent link
                    if (link.getIdType() == "REACTOME") {
                        addParentChildLink(link.getId(), reaction.getStId());
                    }
                    traverseEntity((PhysicalEntity) so.getObject());
                }
                traverseEntity(pe);
            }
        }

        List<WDLinks> modifier = new ArrayList<>();
        // traverse the modifiers (catalyst activities and regulated by)
        // todo: should modifiers get added to child-parent link?
        // for example, https://reactome.org/content/detail/R-HSA-5673768 has modifiers, but the modifier doesnt
        // show the reactionin `Participant Of`
        if (reaction.getCatalystActivity() != null) {
            for (CatalystActivity catalystActivity : reaction.getCatalystActivity()) {
                //TODO figure out / ask about stoichiometry here. Should the stoichiometry be 1?
                WDLinks link = new WDLinks(catalystActivity.getPhysicalEntity(), 1);
                modifier.add(link);
                // if child is from Reactome, create child-parent link
                if (link.getIdType() == "REACTOME") {
                    addParentChildLink(link.getId(), reaction.getStId());
                }
                traverseEntity(catalystActivity.getPhysicalEntity());
            }
        }

        if (reaction.getRegulatedBy() != null) {
            for (Regulation reg : reaction.getRegulatedBy()) {
                DatabaseObject pe = reg.getRegulator();
                if (pe instanceof PhysicalEntity) {
                    //TODO figure out / ask about stoichiometry here. Should the stoichiometry be 1?
                    WDLinks link = new WDLinks(pe, 1);
                    modifier.add(link);
                    // if child is from Reactome, create child-parent link
                    if (link.getIdType() == "REACTOME") {
                        addParentChildLink(link.getId(), reaction.getStId());
                    }
                    traverseEntity((PhysicalEntity) (pe));
                }
            }
        }

        // setting input, output, modifiers and then adding the reaction to the list
        wdReaction.setInput(input);
        wdReaction.setOutput(output);
        wdReaction.setModifier(modifier);
        wdReactions.add(wdReaction);
    }

    // traverse a physical entity and its parts
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
                    // if child is from Reactome, create child-parent link
                    if (link.getIdType() == "REACTOME") {
                        addParentChildLink(link.getId(), physicalEntity.getStId());
                    }
                    traverseEntity(stoichiometryObject.getObject());
                }
            }

            wdComplex.setParts(parts);
            wdPhysicalEntities.add(wdComplex);
        } else if (physicalEntity instanceof EntitySet) {
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
                    } else {
                        parts.add(link);
                    }
                    if (link.getIdType() == "REACTOME") {
                        addParentChildLink(link.getId(), physicalEntity.getStId());
                    }
                    traverseEntity(member);
                }
            }


            wdEntitySet.setParts(parts);
            wdPhysicalEntities.add(wdEntitySet);
        } else if (isModifiedProtein(physicalEntity)) {
            WDModifiedProtein wdModifiedProtein = new WDModifiedProtein((EntityWithAccessionedSequence) (physicalEntity));
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

    private static void addParentChildLink(String key, String val) {
        if (wdParents.containsKey(key)) {
            Set set = wdParents.get(key);
            set.add(val);
        } else {
            Set set = new HashSet();
            set.add(val);
            wdParents.put(key, set);
        }
    }
}
