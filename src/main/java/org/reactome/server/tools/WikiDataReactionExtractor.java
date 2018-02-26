package org.reactome.server.tools;

import org.apache.commons.lang3.ObjectUtils;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.domain.model.Event;


import java.util.ArrayList;
import java.util.List;

/**
 * @author Sarah Keating <skeating@ebi.ac.uk>
 */
class WikiDataReactionExtractor extends EventExtractorBase {

    /**
     * Construct an instance of the WikiDataReactionExtractor
     */
    public WikiDataReactionExtractor(){
        super ();
    }

    /**
     * Construct an instance of the WikiDataReactionExtractor for the specified
     * Reaction.
     *
     * @param reaction  ReactionLikeEvent from ReactomeDB
     */
    public WikiDataReactionExtractor(ReactionLikeEvent reaction){
        super((Event)(reaction));
    }

    /**
     * Construct an instance of the WikiDataReactionExtractor for the specified
     * Reaction.
     *
     * @param reaction ReactionLikeEvent from ReactomeDB
     * @param version Integer - version number of the database
     */
    public WikiDataReactionExtractor(ReactionLikeEvent reaction, Integer version){
        super((Event)(reaction), version);
    }

    /**
     * Construct an instance of the WikiDataReactionExtractor for the specified
     * Reaction.
     *
     * @param reaction ReactionLikeEvent from ReactomeDB
     * @param version Integer - version number of the database
     * @param parentId - the string representation of the id of the parent reaction
     */
    public WikiDataReactionExtractor(ReactionLikeEvent reaction, Integer version, String parentId){
        super((Event)(reaction), version);
        setParent(parentId);
    }

   /**
     * Create the wikidata entry using the Reactome Reaction specified in the constructor.
     */
    public void createWikidataEntry(){
        // currently ReactomeBot expects an entry for a Reaction of
        // species_code,stableId,eventType,Name,Description,[publication;publication;..],goterm,
        // [haspart_input;haspart_input], [haspart_output;], [haspart_mod;..],[partof],None
        String format = "%s,%s,%s,%s,%s,[%s],%s,[%s],[%s],[%s],[%s],None";

        String species = "HSA";
        String eventType = "R";
        if (thisObject == null || !(thisObject instanceof ReactionLikeEvent)) {
            wdEntry = "invalid reaction";
        }
        else {
            String stId = getStableID();
            String name = getEntryName();
            String description = composeDescription(name);
            String publications = getPublicationList();
            String goterm = getGoTerm();
            String input = getInputParts();
            String output = getOutputParts();
            String modifier = getModifierParts();
            String partof = getParent();

            wdEntry = String.format(format, species, stId, eventType, name, description, publications, goterm, input, output, modifier, partof);
        }
    }

     //////////////////////////////////////////////////////////////////////////////////

    // Private functions

    private String composeDescription(String name) {
        return "An instance of the biological reaction " + name + " in Homo sapiens";
    }

    private String getInputParts() {
        count.clear();
        String parts = "";
        StringBuilder sb;
        if (thisObject != null) {
            if (((ReactionLikeEvent)thisObject).getInput() != null){
                for (PhysicalEntity component: ((ReactionLikeEvent)thisObject).getInput() ){
                    addComponentId(component);
                }
            }
            parts = extractStructure();
        }
        return parts;
    }
    private String getOutputParts() {
        count.clear();
        String parts = "";
        StringBuilder sb;
        if (thisObject != null) {
            if (((ReactionLikeEvent)thisObject).getOutput() != null){
                for (PhysicalEntity component: ((ReactionLikeEvent)thisObject).getOutput() ){
                    addComponentId(component);
                }
            }
            parts = extractStructure();
        }
        return parts;
    }
    private String getModifierParts() {
        count.clear();
        String parts = "";
        StringBuilder sb;
        if (thisObject != null) {
            if (((ReactionLikeEvent)thisObject).getCatalystActivity() != null){
                for (CatalystActivity component: ((ReactionLikeEvent)thisObject).getCatalystActivity() ){
                    addComponentId(component.getPhysicalEntity());
                }
            }
            if (((ReactionLikeEvent)thisObject).getRegulatedBy() != null) {
                for (Regulation reg : ((ReactionLikeEvent)thisObject).getRegulatedBy()) {
                   DatabaseObject pe = reg.getRegulator();
                    if (pe instanceof PhysicalEntity) {
                        addComponentId((PhysicalEntity)(pe));
                    }
                 }
            }

            parts = extractStructure();
        }
        return parts;
    }
}
