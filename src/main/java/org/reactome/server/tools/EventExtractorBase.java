package org.reactome.server.tools;

import org.apache.commons.lang3.ObjectUtils;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.domain.model.Event;


import java.util.ArrayList;
import java.util.List;

/**
 * @author Sarah Keating <skeating@ebi.ac.uk>
 */
class EventExtractorBase extends ExtractorBase{

    private String mParentId = null;
    /**
     * Construct an instance of the EventExtractorBase
     */
    public EventExtractorBase(){
        super ();
    }

    /**
     * Construct an instance of the EventExtractorBase for the specified
     * Event.
     *
     * @param event  Event from ReactomeDB
     */
    public EventExtractorBase(Event event){
        super((DatabaseObject)(event));
    }

    /**
     * Construct an instance of the EventExtractorBase for the specified
     * Event.
     *
     * @param event Event from ReactomeDB
     * @param version Integer - version number of the database
     */
    public EventExtractorBase(Event event, Integer version){
        super((DatabaseObject)(event), version);
    }

    /**
     * Construct an instance of the EventExtractorBase for the specified
     * Event.
     *
     * @param event Event from ReactomeDB
     * @param version Integer - version number of the database
     * @param parentId - the string representation of the id of the parent event
     */
    public EventExtractorBase(Event event, Integer version, String parentId){
        super((DatabaseObject)(event), version);
        mParentId = parentId;
    }

    /**
     * Set the event id of the parent; only used when looping thru data
     *
     * @param event the stable id of the parent event
     */
    public void setParent(String event) {
        mParentId = event;
    }

    /**
     * Get the id of the parent of this event
     *
     * @return stableId of parent of this Reactome Event
     */
    public String getParent() {
        if (mParentId == null){
            log.warn("Parent event not set" + this.getStableID());
            return "";
        }
        return mParentId;
    }

    //////////////////////////////////////////////////////////////////////////////////

    // Private functions

    /**
     * Return the Go term for the Reactome DB Event as a string
     *
     * @return a string representing the GO:NNN biological process accession number
     * or an empty string if there is no GO term associated with the Event
     */
    protected String getGoTerm() {
        String goterm = "";
        GO_BiologicalProcess go;
        try {
            go = ((Event)thisObject).getGoBiologicalProcess();
        }
        catch (NullPointerException e) {
            log.error("No database object set.");
            return "";
        }

        if (go == null) {
            return "";
        }

        goterm = "GO:" + go.getAccession();
        return goterm;
    }

    /**
     * Return a semi-colon separated string list of all publications referenced;
     * adding https://identifiers.org/pubmed/ to each entry
     *
     * @return string list of publications
     * or empty string if there are none
     */
    protected String getPublicationList() {
        String pubs = "";
        List<Publication> publications;
        try {
            publications = ((Event) thisObject).getLiteratureReference();
        }
        catch (NullPointerException e) {
            log.error("No database object set.");
            return "";
        }

        if (publications == null || publications.size() == 0) {
            return "";
        }
        for (Publication pub : publications) {
            if (pub instanceof LiteratureReference) {
                Integer pubmed = ((LiteratureReference) pub).getPubMedIdentifier();
                if (pubmed != null && pubmed != 0) {
                    if (pubs.length() > 0)
                        pubs = pubs + ";";
                    pubs = pubs + "https://identifiers.org/pubmed/" + pubmed.toString();
                }
            }
        }
        return pubs;
    }

}
