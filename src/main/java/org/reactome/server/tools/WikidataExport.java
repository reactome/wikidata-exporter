package org.reactome.server.tools;

import com.martiansoftware.jsap.*;
import org.apache.log4j.Logger;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.domain.result.SimpleDatabaseObject;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.graph.service.SchemaService;
import org.reactome.server.graph.service.SpeciesService;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.config.GraphNeo4jConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author Sarah Keating <skeating@ebi.ac.uk>
 */
public class WikidataExport {

    static Logger log = Logger.getLogger(WikidataExport.class);
//  OPTIONS REMOVED AS USEFUL FOR TESTING BUT NOT FOR AUTOMATED USE
    // arguments to determine what to output
    private static long singleId = 0;
    private static long[] multipleIds;
    private static String standardId = "";

    // HARDCODED to HomoSapiens
    private static long speciesId = 48887;
    private static String speciesCode = "hsa";

    private enum Status {
        SINGLE_PATH, ALL_PATWAYS, ALL_PATHWAYS_SPECIES, MULTIPLE_PATHS
    }

    private static Status outputStatus = Status.ALL_PATHWAYS_SPECIES;

    private static int dbVersion = 0;
    private static int count = 0;

    private static final int width = 70;
    private static int total;

    private static String outputdir = ".";
    private static String outputFilename = "pathway_data.csv";
    private static String reactionFilename = "reaction_data.csv";
    private static String entityFilename = "entity_data.csv";
    private static String modprotFilename = "modprot_data.csv";
    private static FileWriter fout;
    private static BufferedWriter out;
    private static FileWriter frout;
    private static BufferedWriter rout;
    private static FileWriter feout;
    private static BufferedWriter eout;
    private static FileWriter fmpout;
    private static BufferedWriter mpout;

    private static List<String> entriesMade = new ArrayList<String>();
    private static List<String> rnEntriesMade = new ArrayList<String>();
    private static List<String> entityEntriesMade = new ArrayList<String>();
    private static List<String> modprotEntriesMade = new ArrayList<String>();
    private static List<String> modprotNamesUsed = new ArrayList<String>();
    private static List<String> entriesNamesUsed = new ArrayList<String>();
    private static List<String> rnNamesUsed = new ArrayList<String>();
    private static List<String> entityNamesUsed = new ArrayList<String>();

    public static void main(String[] args) throws JSAPException {

        SimpleJSAP jsap = new SimpleJSAP(WikidataExport.class.getName(), "A tool to create a csv file to read data into Wikidata",
                new Parameter[]{
                        new FlaggedOption("host", JSAP.STRING_PARSER, "localhost", JSAP.REQUIRED, 'h', "host", "The neo4j host"),
                        new FlaggedOption("port", JSAP.STRING_PARSER, "7474", JSAP.NOT_REQUIRED, 'b', "port", "The neo4j port"),
                        new FlaggedOption("user", JSAP.STRING_PARSER, "neo4j", JSAP.REQUIRED, 'u', "user", "The neo4j user"),
                        new FlaggedOption("password", JSAP.STRING_PARSER, "reactome", JSAP.REQUIRED, 'p', "password", "The neo4j password"),
                        new FlaggedOption("outdir", JSAP.STRING_PARSER, ".", JSAP.REQUIRED, 'o', "outdir", "The output directory"),
//  OPTIONS REMOVED AS USEFUL FOR TESTING BUT NOT FOR AUTOMATED USE
//                        new FlaggedOption("outfilename", JSAP.STRING_PARSER, ".", JSAP.REQUIRED, 'o', "output", "The output filename"),
//                        new FlaggedOption("toplevelpath", JSAP.LONG_PARSER, "0", JSAP.NOT_REQUIRED, 't', "toplevelpath", "A single id of a pathway"),
//                        new FlaggedOption("species", JSAP.LONG_PARSER, "0", JSAP.NOT_REQUIRED, 's', "species", "The id of a species"),
                }
        );
        //  OPTIONS REMOVED AS USEFUL FOR TESTING BUT NOT FOR AUTOMATED USE
//        FlaggedOption m = new FlaggedOption("multiple", JSAP.LONG_PARSER, null, JSAP.NOT_REQUIRED, 'm', "multiple", "A list of ids of Pathways");
//        m.setList(true);
//        m.setListSeparator(',');
//        jsap.registerParameter(m);
//
//        FlaggedOption stdId = new FlaggedOption("stId", JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, 'i', "stId", "The standard id of a Pathway");
//        stdId.setList(true);
//        stdId.setListSeparator(',');
//        jsap.registerParameter(stdId);

        JSAPResult config = jsap.parse(args);
        if (jsap.messagePrinted()) System.exit(1);

        //Initialising ReactomeCore Neo4j configuration
        ReactomeGraphCore.initialise(config.getString("host"), config.getString("port"), config.getString("user"), config.getString("password"), GraphNeo4jConfig.class);

        DatabaseObjectService databaseObjectService = ReactomeGraphCore.getService(DatabaseObjectService.class);
        GeneralService genericService = ReactomeGraphCore.getService(GeneralService.class);
        SpeciesService speciesService = ReactomeGraphCore.getService(SpeciesService.class);
        SchemaService schemaService = ReactomeGraphCore.getService(SchemaService.class);

        System.out.println("Database name: " + genericService.getDBName());
        System.out.println("Database version: " + genericService.getDBVersion());

        // HARD CODED to output Homo Sapien data only
        outputStatus = Status.ALL_PATHWAYS_SPECIES;
        parseAdditionalArguments(config);
        count = 0;

        if (singleArgumentSupplied()) {
            dbVersion = genericService.getDBVersion();

            switch (outputStatus) {
                case SINGLE_PATH:
                    Pathway pathway = null;
                    ReactionLikeEvent reaction = null;
                    total = 1;
                    if (singleId != 0) {
                        try {
                            pathway = databaseObjectService.findByIdNoRelations(singleId);
                        } catch (Exception e) {
                            System.err.println(singleId + " is not the identifier of a valid Pathway object");
                        }
                    }
                    else if (standardId.length() > 0) {
                        try {
                            pathway = databaseObjectService.findByIdNoRelations(standardId);
                        } catch (Exception e) {
                            try {
                                reaction = databaseObjectService.findByIdNoRelations(standardId);
                            }
                            catch (Exception e1) {
                                System.err.println(standardId + " is not the identifier of a valid Pathway/Reaction object");
                            }
                        }
                    }
                    else {
                        System.err.println("Expected the identifier of a valid Pathway object");
                    }
                    if (pathway != null) {
                        outputPath(pathway);
                        updateProgressBar(1);
                    }
                    break;
                case ALL_PATWAYS:
                    for (Species s : speciesService.getSpecies()) {
                        outputPathsForSpecies(s, schemaService, databaseObjectService);
                        genericService.clearCache();
                    }
                    break;
                case ALL_PATHWAYS_SPECIES:
                    Species species = null;
                    try {
                        species = databaseObjectService.findByIdNoRelations(speciesId);
                    } catch (Exception e) {
                        System.err.println(speciesId + " is not the identifier of a valid Species object");
                    }
                    if (species != null) {
                        outputPathsForSpecies(species, schemaService, databaseObjectService);
                        genericService.clearCache();
                    }
                    break;
                case MULTIPLE_PATHS:
                    total = multipleIds.length;
                    Pathway pathway1;
                    int done = 0;
                    for (long id : multipleIds) {
                        pathway1 = null;
                        try {
                            pathway1 = databaseObjectService.findByIdNoRelations(id);
                        } catch (Exception e) {
                            System.err.println(id + " is not the identifier of a valid Pathway object");
                        }
                        if (pathway1 != null) {
                            outputPath(pathway1);
                            done++;
                            updateProgressBar(done);
                        }
                    }
                    break;
            }
        } else {
            System.err.println("Too many arguments detected.");
        }

        try {
            if (out != null) {
                out.close();
            }
            if (rout != null) {
                rout.close();
            }
            if (eout != null) {
                eout.close();
            }
            if (mpout != null) {
                mpout.close();
            }
        }
        catch (IOException e) {
            log.error("Caught IOException: " + e.getMessage());
        }
    }

    /**
     * function to get the command line arguments and determine the requested output
     *
     * @param config JSAPResult result of first parse
     */
    private static void parseAdditionalArguments(JSAPResult config) {
//  OPTIONS REMOVED AS USEFUL FOR TESTING BUT NOT FOR AUTOMATED USE
//        outputfilename = config.getString("outfilename");
//        if (outputfilename.length() == 0 || outputfilename.equals(".")) {
//            outputfilename = defaultFilename;
//        }
        outputdir = config.getString("outdir");
        try {
            fout = new FileWriter(new File(outputdir,speciesCode + "_" + outputFilename));
            out = new BufferedWriter(fout);
            frout = new FileWriter(new File(outputdir,speciesCode + "_" + reactionFilename));
            rout = new BufferedWriter(frout);
            feout = new FileWriter(new File(outputdir,speciesCode + "_" + entityFilename));
            eout = new BufferedWriter(feout);
            fmpout = new FileWriter(new File(outputdir,speciesCode + "_" + modprotFilename));
            mpout = new BufferedWriter(fmpout);
        }
        catch (IOException e) {
            log.error("Caught IOException: " + e.getMessage());
        }

//  OPTIONS REMOVED AS USEFUL FOR TESTING BUT NOT FOR AUTOMATED USE
//        standardId = config.getString("stId");
//        singleId = config.getLong("toplevelpath");
//        speciesId = config.getLong("species");
//        multipleIds = config.getLongArray("multiple");
//
//        if (singleId == 0 && standardId == null) {
//            if (speciesId == 0) {
//                if (multipleIds.length > 0) {
//                    outputStatus = Status.MULTIPLE_PATHS;
//                } else {
//                    outputStatus = Status.ALL_PATWAYS;
//                }
//            } else {
//                outputStatus = Status.ALL_PATHWAYS_SPECIES;
//            }
//        }
    }

    /**
     * function to check that only one argument relating to the pathway has been given
     *
     * @return true if only one argument, false if more than one
     */
    private static boolean singleArgumentSupplied() {
//  OPTIONS REMOVED AS USEFUL FOR TESTING BUT NOT FOR AUTOMATED USE
//        if (singleId != 0) {
//            // have -t shouldnt have anything else
//            if (standardId != null || speciesId != 0 || multipleIds.length > 0) {
//                return false;
//            }
//        }
//        else if (standardId != null && standardId.length() > 0) {
//            // have -i shouldnt have anything else
//            if (singleId != 0 ||speciesId != 0 || multipleIds.length > 0) {
//                return false;
//            }
//        }
//        else if (speciesId != 0) {
//            // have -s shouldnt have anything else
//            if (multipleIds.length > 0) {
//                return false;
//            }
//        }
        return true;
    }

    /**
     * Output all Pathways for the given Species
     *
     * @param species       ReactomeDB Species
     * @param schemaService database service to use
     */
    private static void outputPathsForSpecies(Species species, SchemaService schemaService, DatabaseObjectService databaseObjectService) {
        total = schemaService.getByClass(TopLevelPathway.class, species).size();
        int done = 0;
        System.out.println("\nOutputting pathways for " + species.getDisplayName());
        Collection<SimpleDatabaseObject> pathways = schemaService.getSimpleDatabaseObjectByClass(TopLevelPathway.class, species);
        Iterator<SimpleDatabaseObject> iterator = pathways.iterator();
        while (iterator.hasNext()) {
            Pathway path = databaseObjectService.findByIdNoRelations(iterator.next().getStId());
            // useful for testing
//            if (!is_appropriate(path)) {
//                continue;
//            }
            outputPath(path);
            done++;
            updateProgressBar(done);
            path = null;
        }
    }

    /**
     * Write the line relating to the pathway to the output file
     * and call function to deal with children
     *
     * @param path ReactomeDB Pathway to output
     *
     * This function makes entries in pathway_data.csv
     */
    private static void outputPath(Pathway path) {
        WikiDataPathwayExtractor wdExtract = new WikiDataPathwayExtractor(path, dbVersion);
        wdExtract.createWikidataEntry();
        String name = wdExtract.getEntryName();
        if (entriesNamesUsed.contains(name)) {
            // do something
            System.err.println("Repeated pathway name in outputPath" + name);
        }
        else {
            entriesNamesUsed.add(name);
        }
        try {
            writeLine(wdExtract.getWikidataEntry(), wdExtract.getStableID(), "P");
        }
        catch (IOException e) {
            log.error("Caught IOException: " + e.getMessage());
        }
        writeChildren(path);
    }


    /**
     * Function to write the direct children of a Pathway
     * Note this rescurses through any children of children
     *
     * @param path Pathway frrom ReactomeDB
     *
     *  This function makes entries in pathway_data.csv and reaction_data.csv
     */
    private static void writeChildren(Pathway path) {
        List<Event> loe = path.getHasEvent();
        if (loe == null || loe.size() == 0)
            return;
        for (Event event: loe) {
            if (event instanceof Pathway) {
                Pathway child = (Pathway) (event);
                if (!entriesMade.contains(child.getStId())) {
                    WikiDataPathwayExtractor wdExtract = new WikiDataPathwayExtractor(child, dbVersion, path.getStId());
                    wdExtract.createWikidataEntry();
                    writePathwayEntry(child, wdExtract, path);
//                    String name = wdExtract.getEntryName();
//                    if (entriesNamesUsed.contains(name)) {
//                        // do something
//                        System.err.println("Repeated pathway name " + name);
//                    }
//                    else {
//                        entriesNamesUsed.add(name);
//                    }
//                    try {
//                        writeLine(wdExtract.getWikidataEntry(), wdExtract.getStableID(), "P");
//                    } catch (IOException e) {
//                        log.error("Caught IOException: " + e.getMessage());
//                    }
                }
                writeChildren(child);
            }
            else if (event instanceof ReactionLikeEvent) {
                ReactionLikeEvent child = (ReactionLikeEvent) (event);
                if (!rnEntriesMade.contains(child.getStId())) {
                    WikiDataReactionExtractor wdExtract = new WikiDataReactionExtractor(child, dbVersion, path.getStId());
                    wdExtract.createWikidataEntry();
                    writeReactionEntry(child, wdExtract, path);
                }
                writeParticipants(child);
            }

        }

    }


    /**
     * Function to write line to pathway.csv - fixing duplicate labels where possible
     *
     * @param path       Pathway from ReactomeDB
     * @param wdExtract  instance of the Extractor class being used
     * @param parent       parent Pathway from ReactomeDB
     */
    private static void writePathwayEntry(Pathway path, ExtractorBase wdExtract, Pathway parent) {
        boolean writeEntry = true;
        String name = wdExtract.getEntryName();
        if (entriesNamesUsed.contains(name)) {
            String replacedname = adjustName(name, parent);
            if (!replacedname.equals("")) {
                wdExtract.replaceNameUsedInEntry(name, replacedname);
                entriesNamesUsed.add(replacedname);
            }
            else {
                writeEntry = false;
                System.err.println("Repeated pathway name " + name);
                log.warn("No unique label established for pathway " + path.getStId());
            }
        }
        else {
            entriesNamesUsed.add(name);
        }
        if (writeEntry) {
            try {
                writeLine(wdExtract.getWikidataEntry(), wdExtract.getStableID(), "P");
            } catch (IOException e) {
                System.err.println("Caught IOException: " + e.getMessage());
            }
        }
    }

    /**
     * Function to write line to reaction.csv - fixing duplicate labels where possible
     *
     * @param rle         ReactionLikeEvent from ReactomeDB
     * @param wdExtract  instance of the Extractor class being used
     * @param path       parent Pathway from ReactomeDB
     */
    private static void writeReactionEntry(ReactionLikeEvent rle, ExtractorBase wdExtract, Pathway path) {
        boolean writeEntry = true;
        String name = wdExtract.getEntryName();
        if (rnNamesUsed.contains(name)) {
            String replacedname = adjustName(name, path);
            if (!replacedname.equals("")) {
                wdExtract.replaceNameUsedInEntry(name, replacedname);
                rnNamesUsed.add(replacedname);
            }
            else {
                writeEntry = false;
                System.err.println("Repeated reaction name " + name);
                log.warn("No unique label established for reaction " + rle.getStId());
            }
        }
        else {
            rnNamesUsed.add(name);
        }
        if (writeEntry) {
            try {
                writeLine(wdExtract.getWikidataEntry(), wdExtract.getStableID(), "R");
            } catch (IOException e) {
                System.err.println("Caught IOException: " + e.getMessage());
            }
        }
    }

    /**
     * function to look for an alternative name for a PhysicalEntity
     *
     * @param name   existing name that has already been used
     * @param path   Pathway  from ReactomeDB
     *
     * @return string representing a new name or "" if none can be found
     */
    private static String adjustName(String name, Pathway path) {
        String replacedname = "";
        if (!rnNamesUsed.contains(name + "_" + path.getStId())) {
            replacedname = name + "_" + path.getStId();
        }
        return replacedname;
    }


    /**
     * Function to identify the particpant physical entities of a reaction
     * and pass them to the writeEntity function
     *
     * @param reaction ReactionLikeEvent from ReactomeDB
     */
    private static void writeParticipants(ReactionLikeEvent reaction) {
        List<PhysicalEntity> loe = reaction.getInput();
        if (loe != null && loe.size() > 0) {
            for (PhysicalEntity pe: loe) {
                writeEntity(pe);
            }
        }
        loe = reaction.getOutput();
        if (loe != null && loe.size() > 0) {
            for (PhysicalEntity pe: loe) {
                writeEntity(pe);
            }
        }
        if (reaction.getCatalystActivity() != null){
            for (CatalystActivity component: reaction.getCatalystActivity() ){
                writeEntity(component.getPhysicalEntity());
            }
        }
        if (reaction.getRegulatedBy() != null) {
            for (Regulation reg : reaction.getRegulatedBy()) {
                DatabaseObject pe = reg.getRegulator();
                if (pe instanceof PhysicalEntity) {
                    writeEntity((PhysicalEntity)(pe));
                }
            }
        }

    }

    /**
     * Function to write the data about a physical entity that requires its own
     * wikidata entry i.e. Complex/ EntitySet and recurse through children of the entity
     *
     * @param pe PhysicalEntity from ReactomDB
     *
     * This function writes to entity_data.csv
     */
    private static void writeEntity(PhysicalEntity pe) {
        if (pe instanceof Complex) {
            if (!entityEntriesMade.contains(pe.getStId())) {
                WikiDataComplexExtractor wdExtract = new WikiDataComplexExtractor((Complex) (pe), dbVersion);
                wdExtract.createWikidataEntry();
                writeEntityEntry(pe, wdExtract);
            }
        }
        else if (pe instanceof EntitySet) {
            if (!entityEntriesMade.contains(pe.getStId())) {
                WikiDataSetExtractor wdExtract = new WikiDataSetExtractor((EntitySet) (pe), dbVersion);
                wdExtract.createWikidataEntry();
                writeEntityEntry(pe, wdExtract);
            }
        }
        else if (isModifiedProtein(pe)) {
            if (!modprotEntriesMade.contains(pe.getStId())) {
                WikiDataModProteinExtractor wdExtract = new WikiDataModProteinExtractor((EntityWithAccessionedSequence) (pe), dbVersion);
                wdExtract.createWikidataEntry();
                String label = wdExtract.getLabelUsed();
                Integer count = 0;
                while (count < 3 && modprotNamesUsed.contains(label)) {
                    wdExtract.modifyLabel();
                    label = wdExtract.getLabelUsed();
                    count++;
                }
                if (count == 3 && modprotNamesUsed.contains(label)) {
                    log.warn("No unique label established for modified protein " + pe.getStId());
                }
                else {
                    modprotNamesUsed.add(label);

                    try {
                        writeLine(wdExtract.getWikidataEntry(), wdExtract.getStableID(), "MP");
                    } catch (IOException e) {
                        log.error("Caught IOException: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Function to write line to entity.csv - fixing duplicate labels where possible
     *
     * @param pe         PhysicalEntity from ReactomeDB
     * @param wdExtract  instance of the Extractor class being used
     */
    private static void writeEntityEntry(PhysicalEntity pe, ExtractorBase wdExtract) {
        boolean writeEntry = true;
        String name = wdExtract.getEntryName();
        if (entityNamesUsed.contains(name)) {
            String replacedname = adjustName(name, pe);
            if (!replacedname.equals("")) {
                wdExtract.replaceNameUsedInEntry(name, replacedname);
                entityNamesUsed.add(replacedname);
                name = replacedname;
            }
            else {
                writeEntry = false;
//                System.err.println("Repeated entity name " + name);
                log.warn("No unique label established for complex/set " + pe.getStId());
            }
        }
        else {
            entityNamesUsed.add(name);
        }
        if (writeEntry) {
            try {
                writeLine(wdExtract.getWikidataEntry(), wdExtract.getStableID(), "E");
            } catch (IOException e) {
                System.err.println("Caught IOException: " + e.getMessage());
            }
        }
        writeChildEntity(wdExtract.getChildEntities());

    }

    /**
     * function to look for an alternative name for a PhysicalEntity
     *
     * @param name   existing name that has already been used
     * @param pe     PhysicalEntity from ReactomeDB
     *
     * @return string representing a new name or "" if none can be found
     */
    private static String adjustName(String name, PhysicalEntity pe) {
        String replacedname = "";
        boolean done = false;
        if (pe.getName() != null) {
            Integer n = 0;
            while (!done && n < pe.getName().size()) {
                if (!entityNamesUsed.contains(pe.getName().get(n))) {
                    done = true;
                    replacedname = pe.getName().get(n);
                }
                n++;
            }
        }
        return replacedname;
    }

    /**
     * Function to determine if a PhysicalEntity is a ModifiedProtein
     *
     * @param pe PhysicalEntity from ReactomeDB
     *
     * @return true if pe is EWAS and hasModifiedResidue/ false otherwise
     */
    private static boolean isModifiedProtein(PhysicalEntity pe) {
        if (pe instanceof EntityWithAccessionedSequence) {
            List<AbstractModifiedResidue> mods = ((EntityWithAccessionedSequence) pe).getHasModifiedResidue();
            if (mods != null && mods.size() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper function to loop through a list of PhysicalEntities and call writeEntity
     *
     * @param lope ArrayList<PhysicalEntity></PhysicalEntity>
     */
    private static void writeChildEntity(ArrayList<PhysicalEntity> lope) {
        for (PhysicalEntity pe :lope) {
            writeEntity(pe);
        }
    }


//    private static void writeEntity(PhysicalEntity pe, String parent) {
//        if (pe instanceof Complex) {
//            if (!entityEntriesMade.contains(pe.getStId())) {
//                WikiDataComplexExtractor wdExtract = new WikiDataComplexExtractor((Complex) (pe), dbVersion);
//                wdExtract.createWikidataEntry();
//                String name = wdExtract.getEntryName();
//                String replacedname = name + "_" + parent;
//                if (entityNamesUsed.contains(name)) {
//                    replacedname = name + "_" + parent;
//                    if (entriesNamesUsed.contains(replacedname)) {
//                        System.err.println("Repeated entity name " + replacedname);
//                        log.warn("No entry made for Physical Entity " + pe.getStId() + " as no unique name could be derived.");
//                    }
//                    else {
//                        wdExtract.replaceNameUsedInEntry(name, replacedname);
//                        entityNamesUsed.add(replacedname);
//                    }
//                }
//                else {
//                    entityNamesUsed.add(name);
//                }
//                try {
//                    writeLine(wdExtract.getWikidataEntry(), wdExtract.getStableID(), "E");
//                } catch (IOException e) {
//                    System.err.println("Caught IOException: " + e.getMessage());
//                }
//                writeChildEntity(wdExtract.getChildEntities());
//            }
//        }
//        else if (pe instanceof EntitySet) {
//            if (!entityEntriesMade.contains(pe.getStId())) {
//                WikiDataSetExtractor wdExtract = new WikiDataSetExtractor((EntitySet) (pe), dbVersion);
//                wdExtract.createWikidataEntry();
//                String name = wdExtract.getEntryName();
//                String replacedname = name + "_" + parent;
//                if (entityNamesUsed.contains(name)) {
//                    replacedname = name + "_" + parent;
//                    if (entriesNamesUsed.contains(replacedname)) {
//                        System.err.println("Repeated entity name " + replacedname);
//                        log.warn("No entry made for Physical Entity " + pe.getStId() + " as no unique name could be derived.");
//                    }
//                    else {
//                        wdExtract.replaceNameUsedInEntry(name, replacedname);
//                        entityNamesUsed.add(replacedname);
//                    }
//                }
//                else {
//                    entityNamesUsed.add(name);
//                }
//                try {
//                    writeLine(wdExtract.getWikidataEntry(), wdExtract.getStableID(), "E");
//                } catch (IOException e) {
//                    System.err.println("Caught IOException: " + e.getMessage());
//                }
//                writeChildEntity(wdExtract.getChildEntities(), replacedname);
//            }
//        }
//        else if (isModifiedProtein(pe)) {
//            if (!modprotEntriesMade.contains(pe.getStId())) {
//                WikiDataModProteinExtractor wdExtract = new WikiDataModProteinExtractor((EntityWithAccessionedSequence) (pe), dbVersion);
//                wdExtract.createWikidataEntry();
//                String label = wdExtract.getLabelUsed();
//                Integer count = 0;
//                while (count < 3 && modprotNamesUsed.contains(label)) {
//                    wdExtract.modifyLabel();
//                    label = wdExtract.getLabelUsed();
//                    count++;
//                }
//                if (count == 3 && modprotNamesUsed.contains(label)) {
//                    log.warn("No unique label established for modified protein " + pe.getStId());
//                }
//                else {
//                    modprotNamesUsed.add(label);
//
//                    try {
//                        writeLine(wdExtract.getWikidataEntry(), wdExtract.getStableID(), "MP");
//                    } catch (IOException e) {
//                        log.error("Caught IOException: " + e.getMessage());
//                    }
//                }
//            }
//        }
//    }

    /**
     * Function to apply content filter to the pathways being added to the export file
     *
     * @param path  ReactomeDB Pathway to check
     *
     * @return true if path meets the criteria, false otherwise
     */
    private static boolean is_appropriate(Pathway path) {
        boolean isOK = true;
        String hs = new String("Homo sapiens");
        if (!path.getSpeciesName().equals(hs)) {
            System.err.println("Only the Homo sapien species is supported as yet");
            isOK = false;
        }
        if (!(path instanceof TopLevelPathway)){
            isOK = false;
        }
//        if (count > 10) {
//            isOK = false;
//        }

        if (isOK) count++;

        return isOK;
    }

    /**
     * Function to write a line to the appropriate data file
     *
     * @param entry String the wikidata export information
     * @param id String stableId of the object related to the entry
     * @param typeToWrite String indicating which data file to write to
     *
     * @note typeToWrite values are
     *        "P" pathway
     *        "R" reaction
     *        "E" entity
     *        "MP" modified protein
     * @throws IOException
     */
    private static void writeLine(String entry, String id, String typeToWrite) throws IOException {
        if (typeToWrite.equals("P")) {
            if (entriesMade.contains(id)){
                return;
            }
            entriesMade.add(id);
            out.write(entry);
            out.newLine();
        }
        else if (typeToWrite.equals("R")) {
            if (rnEntriesMade.contains(id)){
                return;
            }
            rnEntriesMade.add(id);
            rout.write(entry);
            rout.newLine();
        }
        else if (typeToWrite.equals("E")){
            if (entityEntriesMade.contains(id)){
                return;
            }
            entityEntriesMade.add(id);
            eout.write(entry);
            eout.newLine();
        }
        else if (typeToWrite.equals("MP")){
            if (modprotEntriesMade.contains(id)){
                return;
            }
            modprotEntriesMade.add(id);
            mpout.write(entry);
            mpout.newLine();
        }
        else {
            log.error("Unexpected type " + typeToWrite + " encountered");
        }
    }

    /**
     * Simple method that prints a progress bar to command line
     *
     * @param done Number of entries added to the graph
     */
    private static void updateProgressBar(int done) {
        String format = "\r%3d%% %s %c";
        char[] rotators = {'|', '/', '-', '\\'};
        double percent = (double) done / total;
        StringBuilder progress = new StringBuilder(width);
        progress.append('|');
        int i = 0;
        for (; i < (int) (percent * width); i++) progress.append("=");
        for (; i < width; i++) progress.append(" ");
        progress.append('|');
        System.out.printf(format, (int) (percent * 100), progress, rotators[((done - 1) % (rotators.length * 100)) / 100]);
    }


}

