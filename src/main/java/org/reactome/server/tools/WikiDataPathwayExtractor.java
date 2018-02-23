package org.reactome.server.tools;

import org.apache.commons.lang3.ObjectUtils;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.domain.model.Event;


import java.util.ArrayList;
import java.util.List;

/**
 * @author Sarah Keating <skeating@ebi.ac.uk>
 */
class WikiDataPathwayExtractor extends EventExtractorBase{

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
        super((Event)(pathway));
    }

    /**
     * Construct an instance of the WikiDataPathwayExtractor for the specified
     * Pathway.
     *
     * @param pathway Pathway from ReactomeDB
     * @param version Integer - version number of the database
     */
    public WikiDataPathwayExtractor(Pathway pathway, Integer version){
        super((Event)(pathway), version);
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
        super((Event)(pathway), version);
        setParent(parentId);
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
            String stId = getStableID();
            String name = getEntryName();
            String description = composeDescription(name);
            String publications = getPublicationList();
            String goterm = getGoTerm();
            String parts = getParts();
            String partof = getParent();

            wdEntry = String.format(format, species, stId, eventType, name, description, publications, goterm, parts, partof);
        }
    }

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
}