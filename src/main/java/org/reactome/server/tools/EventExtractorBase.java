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
            return "";
        }
        return mParentId;
    }

    //////////////////////////////////////////////////////////////////////////////////

    // Private functions

//    private String composeDescription(String name) {
//        return "An instance of the biological event " + name + " in Homo sapiens";
//    }
//
//    private String getParts() {
//        String parts = "";
//        StringBuilder sb;
//        if (thisObject != null) {
//            List<Event> events = ((Event) thisObject).getHasEvent();
//            if (events == null || events.size() == 0) {
//                return parts;
//            }
//            for (Event e : events) {
//                if (parts.length() > 0)
//                    parts = parts + ";";
//                parts = parts + e.getStId();
//            }
//        }
//        return parts;
//    }

    public String getGoTerm() {
        String goterm = "";
        GO_BiologicalProcess go = ((Event)thisObject).getGoBiologicalProcess();
        if (go == null) {
            return goterm;
        }
        goterm = "GO:" + go.getAccession();
        return goterm;
    }

    public String getPublicationList() {
        String pubs = "";
        List<Publication> publications = ((Event) thisObject).getLiteratureReference();
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
