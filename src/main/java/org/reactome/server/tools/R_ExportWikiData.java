package org.reactome.server.tools;

import com.martiansoftware.jsap.*;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.Species;
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
public class R_ExportWikiData {

    private static String outputfilename = "";
    private static String defaultFilename = "reactome_data.csv";

    // arguments to determine what to output
    private static long singleId = 0;
    private static long speciesId = 0;
    private static long[] multipleIds;

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

    public static void main(String[] args) throws JSAPException {

        SimpleJSAP jsap = new SimpleJSAP(R_ExportWikiData.class.getName(), "A tool to create a csv file to read data into Wikidata",
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
                    total = 1;
                    try {
                        pathway = (Pathway) databaseObjectService.findByIdNoRelations(singleId);
                    } catch (Exception e) {
                        System.err.println(singleId + " is not the identifier of a valid Pathway object");
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
                        species = (Species) databaseObjectService.findByIdNoRelations(speciesId);
                    } catch (Exception e) {
                        System.err.println(speciesId + " is not the identifier of a valid Species object");
                    }
                    if (species != null) {
                        outputPathsForSpecies(species, schemaService, databaseObjectService);
                    }
                    break;
                case MULTIPLE_PATHS:
                    total = multipleIds.length;
                    Pathway pathway1 = null;
                    int done = 0;
                    for (long id : multipleIds) {
                        pathway1 = null;
                        try {
                            pathway1 = (Pathway) databaseObjectService.findByIdNoRelations(id);
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
        if (outputfilename.length() == 0 || outputfilename == ".") {
            outputfilename = defaultFilename;
        }
        try {
            fout = new FileWriter(outputfilename);
            out = new BufferedWriter(fout);
        }
        catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }

        singleId = config.getLong("toplevelpath");
        speciesId = config.getLong("species");
        multipleIds = config.getLongArray("multiple");

        if (singleId == 0) {
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
            if (speciesId != 0 || multipleIds.length > 0) {
                return false;
            }
        } else if (speciesId != 0) {
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
        Collection<SimpleDatabaseObject> pathways = schemaService.getSimpleDatabaseObjectByClass(Pathway.class, species);
        Iterator<SimpleDatabaseObject> iterator = pathways.iterator();
        while (iterator.hasNext()) {
            Pathway path = databaseObjectService.findByIdNoRelations(iterator.next().getStId());
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
        if (!is_appropriate(path)) {
            return;
        }
        WikiDataExtractor wdExtract = new WikiDataExtractor(path, dbVersion);
        wdExtract.createWikidataEntry();
        try {
            out.write(wdExtract.getWikidataEntry());
            out.newLine();
 //           wdExtract.toStdOut();
        }
        catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
    }


    /**
     * Function to apply content filter to the pathways being added to the export file
     *
     * @param path  ReactomeDB Pathway to check
     *
     * @return true if path meets teh criteria, false otherwise
     */
    private static boolean is_appropriate(Pathway path) {
        boolean isOK = true;
        String hs = new String("Homo sapiens");
        if (!path.getSpeciesName().equals(hs)) {
            System.err.println("Only the Homo sapien species is supported as yet");
            isOK = false;
        }
        if (count > 10) {
            isOK = false;
        }

        if (isOK) count++;

        return isOK;
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

