package org.reactome.server.tools;

import org.apache.commons.lang3.ObjectUtils;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.domain.model.Event;


import java.util.ArrayList;
import java.util.List;

/**
 * @author Sarah Keating <skeating@ebi.ac.uk>
 */
class WikiDataPathwayExtractor extends ExtractorBase{

    private String mParentId = null;
    /**
     * Construct an instance of the WikiDataPathwayExtractor
     */
    public WikiDataPathwayExtractor(){
        super ();
    }

    /**
     * Construct an instance of the WikiDataPathwayExtractor for the specified
     * Pathway.
     *
     * @param pathway  Pathway from ReactomeDB
     */
    public WikiDataPathwayExtractor(Pathway pathway){
        super((DatabaseObject)(pathway));
    }

    /**
     * Construct an instance of the WikiDataPathwayExtractor for the specified
     * Pathway.
     *
     * @param pathway Pathway from ReactomeDB
     * @param version Integer - version number of the database
     */
    public WikiDataPathwayExtractor(Pathway pathway, Integer version){
        super((DatabaseObject)(pathway), version);
    }

    /**
     * Construct an instance of the WikiDataPathwayExtractor for the specified
     * Pathway.
     *
     * @param pathway Pathway from ReactomeDB
     * @param version Integer - version number of the database
     * @param parentId - the string representation of the id of the parent pathway
     */
    public WikiDataPathwayExtractor(Pathway pathway, Integer version, String parentId){
        super((DatabaseObject)(pathway), version);
        mParentId = parentId;
    }

   /**
     * Create the wikidata entry using the Reactome Pathway specified in the constructor.
     */
    public void createWikidataEntry(){
        // currently ReactomeBot expects an entry for a Pathway of
        // species_code,stableId,Name,Description,[publication;publication;..],goterm,[part;part],[partof;partof],None
        String format = "%s,%s,%s,%s,%s,[%s],%s,[%s],[%s],None";

        String species = "HSA";
        String eventType = "P";
        if (thisObject == null || !(thisObject instanceof Pathway)) {
            wdEntry = "invalid pathway";
        }
        else {
            String stId = getIdentifier();
            String name = getName();
            String description = composeDescription(name);
            String publications = getPublicationList();
            String goterm = getGoTerm();
            String parts = getParts();
            String partof = getParents();

            wdEntry = String.format(format, species, stId, eventType, name, description, publications, goterm, parts, partof);
        }
    }

    /**
     * Set the pathway id of the parent; only used when looping thru data
     *
     * @param pathway the stable id of the parent pathway
     */
    public void setParentPathway(String pathway) {mParentId = pathway; }

    //////////////////////////////////////////////////////////////////////////////////

    // Private functions

    private String composeDescription(String name) {
        return "An instance of the biological pathway " + name + " in Homo sapiens";
    }

    private String getParts() {
        String parts = "";
        StringBuilder sb;
        if (thisObject != null) {
            List<Event> events = ((Pathway) thisObject).getHasEvent();
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
        if (mParentId == null){
            return "";
        }
        return mParentId;
    }

    private String getGoTerm() {
        String goterm = "";
//        GO_BiologicalProcess go = thisReaction.getGoBiologicalProcess();
        GO_BiologicalProcess go = ((Pathway)thisObject).getGoBiologicalProcess();
        if (go == null) {
            return goterm;
        }
        goterm = "GO:" + go.getAccession();
        return goterm;
    }

    private String getPublicationList() {
        String pubs = "";
        List<Publication> publications = ((Pathway) thisObject).getLiteratureReference();
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
