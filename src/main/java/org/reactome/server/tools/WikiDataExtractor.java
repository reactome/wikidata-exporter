package org.reactome.server.tools;

import org.apache.commons.lang3.ObjectUtils;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.domain.model.Event;


import java.util.ArrayList;
import java.util.List;

/**
 * @author Sarah Keating <skeating@ebi.ac.uk>
 */
class WikiDataExtractor {

    private final Pathway thisPathway;
    private final ReactionLikeEvent thisReaction;

    private static Integer dbVersion = 0;

    private static String wdEntry = "";

    private static String parentPathway = "";

    /**
     * Construct an instance of the WikiDataExtractor
     */
    public WikiDataExtractor(){
        thisPathway = null;
        thisReaction = null;
        parentPathway = "";
        wdEntry  = "";
    }

    /**
     * Construct an instance of the WikiDataExtractor for the specified
     * Pathway.
     *
     * @param pathway  Pathway from ReactomeDB
     */
    public WikiDataExtractor(Pathway pathway){
        thisPathway = pathway;
        thisReaction = null;
        parentPathway = "";
        wdEntry  = "";
    }

    /**
     * Construct an instance of the WikiDataExtractor for the specified
     * Pathway.
     *
     * @param pathway Pathway from ReactomeDB
     * @param version Integer - version number of the database
     */
    public WikiDataExtractor(Pathway pathway, Integer version){
        thisPathway = pathway;
        thisReaction = null;
        dbVersion = version;
        parentPathway = "";
        wdEntry  = "";
    }

    /**
     * Construct an instance of the WikiDataExtractor for the specified
     * Pathway.
     *
     * @param pathway Pathway from ReactomeDB
     * @param version Integer - version number of the database
     * @param parentId - the string representation of the id of the parent pathway
     */
    public WikiDataExtractor(Pathway pathway, Integer version, String parentId){
        thisPathway = pathway;
        thisReaction = null;
        dbVersion = version;
        parentPathway = parentId;
        wdEntry  = "";
    }

    /**
     * Construct an instance of the WikiDataExtractor for the specified
     * Reaction.
     *
     * @param reaction  ReactionLikeEvent from ReactomeDB
     */
    public WikiDataExtractor(ReactionLikeEvent reaction){
        thisPathway = null;
        thisReaction = reaction;
        parentPathway = "";
        wdEntry  = "";
    }

    /**
     * Construct an instance of the WikiDataExtractor for the specified
     * Pathway.
     *
     * @param reaction  ReactionLikeEvent from ReactomeDB
     * @param version Integer - version number of the database
     */
    public WikiDataExtractor(ReactionLikeEvent reaction, Integer version){
        thisPathway = null;
        thisReaction = reaction;
        dbVersion = version;
        parentPathway = "";
        wdEntry  = "";
    }

    /**
     * Construct an instance of the WikiDataExtractor for the specified
     * Pathway.
     *
     * @param reaction  ReactionLikeEvent from ReactomeDB
     * @param version Integer - version number of the database
     * @param parentId - the string representation of the id of the parent pathway
     */
    public WikiDataExtractor(ReactionLikeEvent reaction, Integer version, String parentId){
        thisPathway = null;
        thisReaction = reaction;
        dbVersion = version;
        parentPathway = parentId;
        wdEntry  = "";
    }

    /**
     * Create the wikidata entry using the Reactome Pathway specified in the constructor.
     */
    public void createWikidataEntry(){
        // currently ReactomeBot expects an entry
        // species_code,stableId,Name,Description,[publication;publication;..],goterm,[part;part],[partof;partof],None
        String format = "%s,%s,%s,%s,%s,[%s],%s,[%s],[%s],None";

        // species_code,stableId,Name,Description,[publication;publication;..],goterm,[part;part],[partof;partof],[protein;protein]None
//       String format = "%s,%s,%s,%s,%s,[%s],%s,[%s],[%s],[%s],None";

        String species = "HSA";
        String eventType = ((thisPathway != null) ? "P" : "R");
        if (thisPathway != null || thisReaction != null) {
            String stId = getIdentifier();
            String name = getName();
            String description = composeDescription(name);
            String publications = getPublicationList();
            String goterm = getGoTerm();
            String parts = getParts();
            String partof = getParents();

            wdEntry = String.format(format, species, stId, eventType, name, description, publications, goterm, parts, partof);
//            String proteins = getProteins();
//            wdEntry = String.format(format, species, stId, eventType, name, description, publications, goterm, parts, partof,proteins);
        }
        else {
            wdEntry = "invalid pathway";
        }
    }

    /**
     * Set the database version number.
     *
     * @param version  Integer the ReactomeDB version number being used.
     */
    public void setDBVersion(Integer version) {
        dbVersion = version;
    }

    /**
     * Set the pathway id of the parent; only used when looping thru data
     *
     * @param pathway the stable id of the parent pathway
     */
    public void setParentPathway(String pathway) {parentPathway = pathway; }

    public String getStableID() {
        return getIdentifier();
    }
    ///////////////////////////////////////////////////////////////////////////////////

    // functions to output resulting string

    /**
     * Write the SBMLDocument to std output.
     */
    public void toStdOut()    {
        System.out.println(wdEntry);
    }

    //////////////////////////////////////////////////////////////////////////////////

    // functions to facilitate testing

    /**
     * Retrieve the Wikidata entry string.
     *
     * @return String representing the wikidata entry
     */
    String getWikidataEntry(){
        return wdEntry;
    }

    //////////////////////////////////////////////////////////////////////////////////

    // Private functions

    private String getIdentifier() {
        String id = "";
        if (thisPathway != null || thisReaction != null) {
            id = ((thisPathway != null) ? thisPathway.getStId() : thisReaction.getStId());
        }
        return id;
    }

    private String getName() {
        String name;
        try {
            name = ((thisPathway != null) ? thisPathway.getDisplayName() : thisReaction.getDisplayName());
        }
        catch (NullPointerException e) {
            return "";
        }
        String name_nocommas = name.replaceAll(",", ";");
        return name_nocommas;
    }

    private String composeDescription(String name) {
        String eventType = ((thisPathway != null) ? "pathway" : "reaction");
        return "An instance of the biological " + eventType + " " + name + " in Homo sapiens";
    }

    private String getParts() {
        String parts = "";
        StringBuilder sb;
        if (thisPathway != null) {
            List<Event> events = thisPathway.getHasEvent();
            if (events == null || events.size() == 0) {
                return parts;
            }
            for (Event e : events) {
                if (parts.length() > 0)
                    parts = parts + ";";
                parts = parts + e.getStId();
            }
        }
        return parts;
    }

    private String getProteins() {
        String parts = "";
        if (thisReaction != null) {
            List<PhysicalEntity> events = thisReaction.getInput();
            List<PhysicalEntity> outputs = thisReaction.getOutput();
            if ((events == null && outputs == null) || (events.size() == 0 && outputs.size() == 0)) {
                return parts;
            }
            for (PhysicalEntity e : events) {
                String ref = getPhysicalEntityReference(e);
                if (!ref.equals("")) {
                    if (parts.length() > 0)
                        parts = parts + ";";
                    parts = parts + ref;
                }
            }
        }
        return parts;
    }

    private String getPhysicalEntityReference(PhysicalEntity pe) {
        String strref = "";
//        if (pe instanceof SimpleEntity){
//        }
        if (pe instanceof EntityWithAccessionedSequence){
            ReferenceEntity ref = ((EntityWithAccessionedSequence)(pe)).getReferenceEntity();
            if (ref != null) {
                String db = ref.getDatabaseName();
                String uni = "UniProt";
                if (db.equals(uni)) {
                    strref = ref.getIdentifier();
                }
            }
            ref = null;
        }
//        else if (pe instanceof Complex){
//            List<PhysicalEntity> components = ((Complex)(pe)).getHasComponent();
//            if (components != null) {
//                for (PhysicalEntity component : components) {
//                    if (strref.length() > 0)
//                        strref = strref + ";";
//                    strref = strref + getPhysicalEntityReference(component);
//                }
//            }
//            components = null;
//        }
//        else if (pe instanceof EntitySet){
//        }
//        else if (pe instanceof Polymer){
//        }
//        else {
//        }
        return strref;
    }

    private String getParents() {
        return parentPathway;
    }

    private String getGoTerm() {
        String goterm = "";
//        GO_BiologicalProcess go = thisReaction.getGoBiologicalProcess();
        GO_BiologicalProcess go = ((thisPathway != null) ? thisPathway.getGoBiologicalProcess() : thisReaction.getGoBiologicalProcess());
        if (go == null) {
            return goterm;
        }
        goterm = "GO:" + go.getAccession();
        return goterm;
    }

    private String getPublicationList() {
        String pubs = "";
        List<Publication> publications = ((thisPathway != null) ? thisPathway.getLiteratureReference() : thisReaction.getLiteratureReference());
        if (publications == null || publications.size() == 0) {
            return pubs;
        }
        for (Publication pub : publications) {
            if (pub instanceof LiteratureReference) {
                Integer pubmed = ((LiteratureReference) pub).getPubMedIdentifier();
                if (pubmed != null && pubmed != 0) {
                    if (pubs.length() > 0)
                        pubs = pubs + ";";
                    pubs = pubs + "http://identifiers.org/pubmed/" + pubmed.toString();
                }
            }
        }
        return pubs;
    }

}
