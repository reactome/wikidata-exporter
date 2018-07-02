package org.reactome.server.tools;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Logger;
import org.reactome.server.graph.domain.model.*;


import java.util.ArrayList;
import java.util.List;

/**
 * @author Sarah Keating <skeating@ebi.ac.uk>
 */
class WikiDataSetExtractor extends ExtractorBase{

    static Logger log = Logger.getLogger(WikiDataSetExtractor.class);

    /**
     * Construct an instance of the WikiDataSetExtractor
     */
    public WikiDataSetExtractor(){
        super ();
    }

    /**
     * Construct an instance of the WikiDataSetExtractor for the specified
     * EntitySet.
     *
     * @param c  EntitySet from ReactomeDB
     */
    public WikiDataSetExtractor(EntitySet c){
        super((PhysicalEntity) (c));
    }

    /**
     * Construct an instance of the WikiDataSetExtractor for the specified
     * EntitySet.
     *
     * @param c EntitySet from ReactomeDB
     * @param version Integer - version number of the database
     */
    public WikiDataSetExtractor(EntitySet c, Integer version){
        super((PhysicalEntity)(c), version);
    }

    /**
     * Create the wikidata entry using the Reactome EntitySet specified in the constructor.
     */
    public void createWikidataEntry(){
        // currently ReactomeBot expects an entry
        // species_code,entity_code,stableId,name,[part;part],None
       String format = "%s,%s,%s,%s,[%s],None";

        String species = "HSA";
        // only sets
        if (thisObject == null || !(thisObject instanceof EntitySet))
        {
            log.error("Invalid Set: " + this.getStableID());
            wdEntry = "invalid entity";
        }
        else {
            String stId = getStableID();
            String parts = getParts();
            String setType = getSetType();
            String name = getEntryName();
            wdEntry = String.format(format, species, setType, stId, name, parts);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////

    // functions to output resulting string
    // Private functions

    /**
     * Function to create a semicolon separated list of information for each member of set
     *
     * @note This function uses addComponentId to create a List of TypeCounter objects
     * and then uses extractStructure to create the string from the List
     *
     * @return a string representing the members of the set
     */
    private String getParts() {
        String parts = "";
        if (thisObject != null) {
            if (((EntitySet)thisObject).getHasMember() != null){
                for (PhysicalEntity component: ((EntitySet)thisObject).getHasMember() ){
                    addComponentId(component);
                }
            }
            parts = extractStructure();
        }
        return parts;
    }

    /**
     * Function to return a string respresenting the type of set
     *
     * @note: "DS" respresents a DefinedSet
     *        "CS" represents a CandidateSet
     *        "OS" represents an OpenSet
     *
     * @return string representing type of set
     */
    private String getSetType() {
        String setType = "DS";
        if (thisObject instanceof DefinedSet) {
            setType = "DS";
        }
        else if (thisObject instanceof CandidateSet) {
            setType = "CS";
        }
        else if (thisObject instanceof OpenSet) {
            setType = "OS";
        }
        else {
            log.warn("Unexpected type of set encountered " + thisObject.getClassName() + ": " + this.getStableID());
        }
        return setType;
    }
}

