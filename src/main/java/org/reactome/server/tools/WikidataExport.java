package org.reactome.server.tools;

import com.martiansoftware.jsap.*;
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

    private static String outputfilename = "";
    private static String defaultFilename = "reactome_data.csv";

    // arguments to determine what to output
    private static long singleId = 0;
    private static long speciesId = 0;
    private static long[] multipleIds;
    private static String standardId = "";

    private enum Status {
        SINGLE_PATH, ALL_PATWAYS, ALL_PATHWAYS_SPECIES, MULTIPLE_PATHS
    }

    private static Status outputStatus = Status.SINGLE_PATH;

    private static int dbVersion = 0;
    private static int count = 0;

    private static final int width = 70;
    private static int total;

    private static FileWriter fout;
    private static BufferedWriter out;

    private static List<String> entriesMade = new ArrayList<String>();

    public static void main(String[] args) throws JSAPException {

        SimpleJSAP jsap = new SimpleJSAP(WikidataExport.class.getName(), "A tool to create a csv file to read data into Wikidata",
                new Parameter[]{
                        new FlaggedOption("host", JSAP.STRING_PARSER, "localhost", JSAP.REQUIRED, 'h', "host", "The neo4j host"),
                        new FlaggedOption("port", JSAP.STRING_PARSER, "7474", JSAP.NOT_REQUIRED, 'b', "port", "The neo4j port"),
                        new FlaggedOption("user", JSAP.STRING_PARSER, "neo4j", JSAP.REQUIRED, 'u', "user", "The neo4j user"),
                        new FlaggedOption("password", JSAP.STRING_PARSER, "reactome", JSAP.REQUIRED, 'p', "password", "The neo4j password"),
                        new FlaggedOption("outfilename", JSAP.STRING_PARSER, ".", JSAP.REQUIRED, 'o', "output", "The output filename"),
                        new FlaggedOption("toplevelpath", JSAP.LONG_PARSER, "0", JSAP.NOT_REQUIRED, 't', "toplevelpath", "A single id of a pathway"),
                        new FlaggedOption("species", JSAP.LONG_PARSER, "0", JSAP.NOT_REQUIRED, 's', "species", "The id of a species"),
                }
        );
        FlaggedOption m = new FlaggedOption("multiple", JSAP.LONG_PARSER, null, JSAP.NOT_REQUIRED, 'm', "multiple", "A list of ids of Pathways");
        m.setList(true);
        m.setListSeparator(',');
        jsap.registerParameter(m);

        FlaggedOption stdId = new FlaggedOption("stId", JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, 'i', "stId", "The standard id of a Pathway");
        stdId.setList(true);
        stdId.setListSeparator(',');
        jsap.registerParameter(stdId);

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

        outputStatus = Status.SINGLE_PATH;
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
            System.err.println("Too many arguments detected. Expected either no pathway arguments or one of -t, -s, -m, -l.");
        }

        try {
            if (out != null) {
                out.close();
            }
        }
        catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
    }

    /**
     * function to get the command line arguments and determine the requested output
     *
     * @param config JSAPResult result of first parse
     */
    private static void parseAdditionalArguments(JSAPResult config) {
        outputfilename = config.getString("outfilename");
        if (outputfilename.length() == 0 || outputfilename.equals(".")) {
            outputfilename = defaultFilename;
        }
        try {
            fout = new FileWriter(outputfilename);
            out = new BufferedWriter(fout);
        }
        catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }

        standardId = config.getString("stId");
        singleId = config.getLong("toplevelpath");
        speciesId = config.getLong("species");
        multipleIds = config.getLongArray("multiple");

        if (singleId == 0 && standardId == null) {
            if (speciesId == 0) {
                if (multipleIds.length > 0) {
                    outputStatus = Status.MULTIPLE_PATHS;
                } else {
                    outputStatus = Status.ALL_PATWAYS;
                }
            } else {
                outputStatus = Status.ALL_PATHWAYS_SPECIES;
            }
        }
    }

    /**
     * function to check that only one argument relating to the pathway has been given
     *
     * @return true if only one argument, false if more than one
     */
    private static boolean singleArgumentSupplied() {
        if (singleId != 0) {
            // have -t shouldnt have anything else
            if (standardId != null || speciesId != 0 || multipleIds.length > 0) {
                return false;
            }
        }
        else if (standardId != null && standardId.length() > 0) {
            // have -i shouldnt have anything else
            if (singleId != 0 ||speciesId != 0 || multipleIds.length > 0) {
                return false;
            }
        }
        else if (speciesId != 0) {
            // have -s shouldnt have anything else
            if (multipleIds.length > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Output all Pathways for the given Species
     *
     * @param species       ReactomeDB Species
     * @param schemaService database service to use
     */
    private static void outputPathsForSpecies(Species species, SchemaService schemaService, DatabaseObjectService databaseObjectService) {
        total = schemaService.getByClass(Pathway.class, species).size();
        int done = 0;
        System.out.println("\nOutputting pathways for " + species.getDisplayName());
        Collection<SimpleDatabaseObject> pathways = schemaService.getSimpleDatabaseObjectByClass(TopLevelPathway.class, species);
        for (SimpleDatabaseObject pathway : pathways) {
            Pathway path = databaseObjectService.findByIdNoRelations(pathway.getStId());
            if (!is_appropriate(path)) {
                continue;
            }
            outputPath(path);
            done++;
            updateProgressBar(done);
            path = null;
        }
    }

    /**
     * Write the line relating to the pathway to the output file
     *
     * @param path ReactomeDB Pathway to output
     */
    private static void outputPath(Pathway path) {
        WikiDataPathwayExtractor wdExtract = new WikiDataPathwayExtractor(path, dbVersion);
        wdExtract.createWikidataEntry();
        try {
            writeLine(wdExtract.getWikidataEntry(), wdExtract.getStableID());
        }
        catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
        writeChildren(path);
    }


    private static void writeChildren(Pathway path) {
        List<Event> loe = path.getHasEvent();
        if (loe == null || loe.size() == 0)
            return;
        for (Event event: loe) {
            if (event instanceof Pathway) {
                Pathway child = (Pathway) (event);
                WikiDataPathwayExtractor wdExtract = new WikiDataPathwayExtractor(child, dbVersion, path.getStId());
                wdExtract.createWikidataEntry();
                try {
                    writeLine(wdExtract.getWikidataEntry(), wdExtract.getStableID());
                } catch (IOException e) {
                    System.err.println("Caught IOException: " + e.getMessage());
                }
                writeChildren(child);
            }
//            else if (event instanceof ReactionLikeEvent) {
//                ReactionLikeEvent child = (ReactionLikeEvent) (event);
//                WikiDataPathwayExtractor wdExtract = new WikiDataPathwayExtractor(child, dbVersion, path.getStId());
//                wdExtract.createWikidataEntry();
//                try {
//                    writeLine(wdExtract.getWikidataEntry(), wdExtract.getStableID());
//                } catch (IOException e) {
//                    System.err.println("Caught IOException: " + e.getMessage());
//                }
//
//            }

        }

    }

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

    private static void writeLine(String entry, String id) throws IOException {
        if (entriesMade.contains(id)){
            return;
        }
        entriesMade.add(id);
        out.write(entry);
        out.newLine();
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

