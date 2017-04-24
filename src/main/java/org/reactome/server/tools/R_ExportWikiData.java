package org.reactome.server.tools;

import com.martiansoftware.jsap.*;
import com.thoughtworks.xstream.io.path.Path;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.graph.service.SchemaService;
import org.reactome.server.graph.service.SpeciesService;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.sarah.ebi.config.GraphQANeo4jConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Sarah Keating <skeating@ebi.ac.uk>
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class MyLookup {

    static int level = 0;
    static int minChildren = 0;

    public static void main(String[] args) throws JSAPException {

        SimpleJSAP jsap = new SimpleJSAP(MyLookup.class.getName(), "A tool for generating SBML files",
                new Parameter[]{
                        new FlaggedOption("host", JSAP.STRING_PARSER, "localhost", JSAP.REQUIRED, 'h', "host", "The neo4j host"),
                        new FlaggedOption("port", JSAP.STRING_PARSER, "7474", JSAP.NOT_REQUIRED, 'b', "port", "The neo4j port"),
                        new FlaggedOption("user", JSAP.STRING_PARSER, "neo4j", JSAP.REQUIRED, 'u', "user", "The neo4j user"),
                        new FlaggedOption("password", JSAP.STRING_PARSER, "reactome", JSAP.REQUIRED, 'p', "password", "The neo4j password"),
                }
        );
        JSAPResult config = jsap.parse(args);
        if (jsap.messagePrinted()) System.exit(1);

        //Initialising ReactomeCore Neo4j configuration
        ReactomeGraphCore.initialise(config.getString("host"), config.getString("port"), config.getString("user"), config.getString("password"), GraphQANeo4jConfig.class);

        GeneralService genericService = ReactomeGraphCore.getService(GeneralService.class);
        System.out.println("Database name: " + genericService.getDBName());
        System.out.println("Database version: " + genericService.getDBVersion());

        DatabaseObjectService databaseObjectService = ReactomeGraphCore.getService(DatabaseObjectService.class);

//        long dbid = 5663205L; // infectious disease
//        long dbid = 167168L;  // HIV transcription termination (pathway no events)
//        long dbid = 180627L; // reaction
//        long dbid = 168275L; // pathway with a single child reaction
//        long dbid = 168255L; // influenza life cycle - which is where my pathway 168275 comes from
//        long dbid = 2978092L; // pathway with a catalysis
//        long dbid = 5619071L; // failed reaction
//        long dbid = 69205L; // black box event
//        long dbid = 392023L; // reaction
//        long dbid = 5602410L; // species genome encoded entity
//        long dbid = 9609481L; // polymer entity
//        long dbid = 453279L;// path with black box
//        long dbid = 76009L; // path with reaction
//        long dbid = 2022090L; // polymerisation
//        long dbid = 162585L; //depoly
//        long dbid = 9719495L;
//        long dbid = 1280218; // toplevel pathway
        long dbid = 5619507L;
        List<Long> al = Arrays.asList(168276L, 5619084L, 1799339L, 5619507L, 9719495L, 844615L, 192869L, 392023L,
                168275L, 1640170L);
        String stdId = "R-HSA-71403";
        int option = 4;

        switch (option) {
            case 1:
                lookupPaths(databaseObjectService);
                break;
            case 2:
                lookupSpecies(databaseObjectService);
                break;
            case 3:
                lookupEvents(dbid, databaseObjectService);

                break;
            case 4:
                Pathway path = (Pathway) databaseObjectService.findById(stdId);
                printHierarchy(path, 0);
 //               System.out.println("stid" + path.getStId());
                break;
            case 5:
                AbstractModifiedResidue amr = (AbstractModifiedResidue) databaseObjectService.findById(dbid);
                System.out.println("stid" + amr.getStId());
                 break;
            case 6:
                Pathway path1 = (Pathway) databaseObjectService.findByIdNoRelations("R-ATH-1630316");
                printHierarchy(path1, 0);
                System.out.println("dbid" + path1.getDbId());
                break;
            case 7:
                for (Long l : al) {
                    Pathway path2 = (Pathway) databaseObjectService.findById(l);
                    //               printHierarchy(path, 0);
                    System.out.println("id " + l + " stid " + path2.getStId());

                }
                break;
            case 8:
                PhysicalEntity pe = (PhysicalEntity) databaseObjectService.findById(stdId);
                printDetails(pe, databaseObjectService);
                break;
            default:
                System.out.println("Invalid option");
                break;

        }
    }

    static void printDetails(PhysicalEntity pe, DatabaseObjectService databaseObjectService) {
        System.out.println("PE: " + pe.getDbId() + " " + getEWAS(pe, databaseObjectService));

    }

    static String getEWAS(PhysicalEntity pe, DatabaseObjectService databaseObjectService) {
        String data = "None";
        if (!(pe instanceof EntityWithAccessionedSequence)) {
            return data;
        }
        else {
            data = " RefType:" + ((EntityWithAccessionedSequence) pe).getReferenceEntity().getDatabaseName() + ":" + ((EntityWithAccessionedSequence) pe).getReferenceEntity().getIdentifier();
            writeCross(((EntityWithAccessionedSequence)pe), databaseObjectService);
            writeComp(((EntityWithAccessionedSequence)pe), databaseObjectService);
            return data;
        }
    }

    static void writeCross(EntityWithAccessionedSequence pe, DatabaseObjectService databaseObjectService) {
        if (pe.getCrossReference() == null || pe.getCrossReference().size() == 0) {
            return;
        }
        for (DatabaseIdentifier id : pe.getCrossReference()){
            if (databaseObjectService.findById(id) instanceof PhysicalEntity) {
                PhysicalEntity pe1 = ((PhysicalEntity) databaseObjectService.findById(id));
                printDetails(pe, databaseObjectService);
            }
        }
    }
    static void writeComp(EntityWithAccessionedSequence pe, DatabaseObjectService databaseObjectService) {
        if (pe.getComponentOf() == null || pe.getComponentOf().size() == 0) {
            return;
        }
        for (Complex id : pe.getComponentOf()){
                PhysicalEntity pe1 = ((PhysicalEntity) databaseObjectService.findById(id));
                printDetails(id, databaseObjectService);
        }
    }
    private static void lookupPaths(DatabaseObjectService databaseObjectService){
        long sp = 48887L; // homo sapiens
 //       long sp = 170905L;// arapidoosis

        Species homoSapiens = (Species) databaseObjectService.findByIdNoRelations(sp);
        SchemaService schemaService = ReactomeGraphCore.getService(SchemaService.class);
        int count = 0;
        int total = 0;
        for (TopLevelPathway path : schemaService.getByClass(TopLevelPathway.class)) {
            if (isMatch(path, databaseObjectService)) {
                count++;
                printMatch(path);
            }
            total++;
        }
        System.out.println("Found " + count + " of " + total);
    }

    private static boolean isMatch(Pathway path, DatabaseObjectService databaseObjectService) {
        boolean match = false;

        // is the not path top level
        if (path instanceof Pathway && (path instanceof TopLevelPathway)) {
            match = true;
        }

        if (!match)
            return match;

        match = hasGOTerm(path);

        return match;
    }

    static boolean hasGOTerm(Pathway path) {
        boolean hasgoterm = false;

        if (path.getGoBiologicalProcess() != null)
        {
            hasgoterm = true;
        }

        return hasgoterm;
    }

//
//        // pathway with no events
////        List<Event> events = path.getHasEvent();
////        if (path instanceof Pathway && (events == null || events.size() == 0)) {
////            match = true;
////        }
//
//
////        match = false;
////        // has events of particular kind
////        List<Event> events = path.getHasEvent();
////        if (events != null) {
////            for (Event e : events) {
////                if (e.getEventOf() != null && e.getEventOf().size()> 2){
////                    match = true;
////                }
////            }
////        }
//
//        // has particular physical entities
//        List<Event> events = path.getHasEvent();
//        if (events == null || events.size() == 0)  {
//            match = false;
//            return match;
//        }
//
//        int numChild = getNumChildren(events);
//        if (minChildren == 0) {
//            minChildren = numChild;
//        }
//        if (numChild < minChildren) {
//            match = true;
//            minChildren = numChild;
//        }
//        else {
//            match = false;
//        }
////            for (Event e : events) {
//
//
///* code for findng an catalysis event where the ec numbers come from pe rather than raction
////                if (!(e instanceof ReactionLikeEvent)) {
////                    continue;
////                }
////                ReactionLikeEvent rle = (ReactionLikeEvent)(e);
////                if (rle.getCatalystActivity() != null && rle.getCatalystActivity().size() > 0) {
////                    boolean hasECNums = false;
////                    for (CatalystActivity cat : rle.getCatalystActivity()) {
////                        String ecnum = cat.getActivity().getEcNumber();
////                        if (ecnum != null) {
////                            hasECNums = true;
////                        }
////                    }
////                    if (!hasECNums) {
////                        for (CatalystActivity cat : rle.getCatalystActivity()) {
////                            PhysicalEntity pe = cat.getPhysicalEntity();
////                            if (pe != null) {
////                                List<DatabaseIdentifier> cross = pe.getCrossReference();
////                                if (cross != null && cross.size()> 0) {
////                                    for (DatabaseIdentifier db: cross) {
////                                        DatabaseObject obj = databaseObjectService.findById(db);
////                                        if (obj instanceof ReferenceDatabase) {
////                                            if (obj.getDisplayName() == "EC"){
////                                                match = true;
////                                            }
////                                        }
////                                        if (match) break;
////                                    }
////                                }
////
////                            }
////                            if (match) break;
////                        }
////
////                    }
////                }
////                if (match) break;
//*/
////        }
//
//        return match;
//    }

    static int getNumChildren(List<Event> loe) {
        int num = loe.size();
        for (Event e : loe) {
            if (e instanceof Pathway) {
                List<Event> le = ((Pathway)e).getHasEvent();
                if (le != null) {
                    num = num + getNumChildren(le);
                }
            }
        }
        return num;
    }

    private static void printMatch(Pathway path) {
        System.out.println("Pathway " + path.getStId() + " has Go Term " + path.getGoBiologicalProcess().getAccession());
    }


    private static void lookupSpecies(DatabaseObjectService databaseObjectService) {
//        Species homoSapiens = (Species) databaseObjectService.findByIdNoRelations(48887L);
        SpeciesService schemaService = ReactomeGraphCore.getService(SpeciesService.class);
        for (Species s : schemaService.getSpecies()){
            System.out.println("Species: " + s.getName() + " has id " + s.getDbId());
        }
    }

    private static void lookupEvents(long dbid, DatabaseObjectService databaseObjectService){
        Pathway pathway = (Pathway) databaseObjectService.findById(dbid);
        List<Event> events = pathway.getHasEvent();
        if (events != null) {
            for (Event e : events) {
                System.out.println(getDescription(e));
                displayHierarchy(e);
            }
        }
    }

    private static void displayHierarchy(Event e) {
        if (e.getPrecedingEvent() != null) {
            System.out.println("Hierarchy of " + e.getDbId());
            for (Event ee : e.getPrecedingEvent()) {
                System.out.println(ee.getDbId());

            }
        }
    }


    private static void printHierarchy(Pathway path, int thislevel) {
        printHierarchy((Event)(path), thislevel);
        List<Event> le = path.getHasEvent();
        if (le != null) {
            for (Event e : le) {
                if (e instanceof Pathway){
                    printHierarchy((Pathway)(e), thislevel+1);
                }
                else {
                    printHierarchy(e, thislevel+1);
                }
            }
        }
    }

    private static void printHierarchy(Event event, int thislevel) {
        System.out.println(getDetails(thislevel, event.getStId(), getDescription(event)));
    }
    private static String getDetails(int level, String dbid, String type){
        String line = "Level " + level + ": " + dbid + " " + type;
        return line;
    }

    private static String getDescription(Event e){
        String type;
        if (e instanceof Reaction)
            type = "Reaction";
        else if (e instanceof Polymerisation)
            type = "Polymerisation";
        else if (e instanceof FailedReaction)
            type = "failedReaction";
        else if (e instanceof  Depolymerisation)
            type = "Depolymerisation";
        else if (e instanceof  BlackBoxEvent)
            type = "BlackBoxEvent";
        else if (e instanceof TopLevelPathway) {
            type = "Top level Pathway";
        }
        else if (e instanceof Pathway){
            type = "Pathway";
        }
        else
            type = "UNKNOWN";
        String go_term = "None";
        GO_BiologicalProcess goterm = e.getGoBiologicalProcess();
        if (goterm != null)
        {
            go_term = goterm.getDatabaseName() + ":" + goterm.getAccession();
        }
        return  "Event: " + e.getName() + " has id " + e.getDbId() + " and type " + type + " with reference: " + go_term;
    }


}

