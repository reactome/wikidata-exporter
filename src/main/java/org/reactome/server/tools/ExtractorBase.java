package org.reactome.server.tools;

import org.apache.commons.lang3.ObjectUtils;
import org.reactome.server.graph.domain.model.*;


import java.util.ArrayList;
import java.util.List;

/**
 * @author Sarah Keating <skeating@ebi.ac.uk>
 */

/**
 * Base class for all Extractor classes
 */
class ExtractorBase {

    public static Integer dbVersion = 0;

    public static String wdEntry = "";

    public static DatabaseObject thisObject;

    public ArrayList<TypeCounter> count;// = new ArrayList<TypeCounter>();

    public ArrayList<PhysicalEntity> childEntities;// = new ArrayList<TypeCounter>();

    /**
     * Construct an instance of the ExtractorBase
     */
    public ExtractorBase(){
        wdEntry  = "";
        thisObject = null;
        count = new ArrayList<TypeCounter>();
        childEntities = new ArrayList<PhysicalEntity>();
    }

    /**
     * Construct an instance of ExtractorBase with a databaseObject
     *
     * @param object DatabaseObject form Reactome
     */
    public ExtractorBase(DatabaseObject object) {
        wdEntry  = "";
        thisObject = object;
        count = new ArrayList<TypeCounter>();
        childEntities = new ArrayList<PhysicalEntity>();
    }

    /**
     * Construct an instance of ExtractorBase with databaseObject and dbVersion
     *
     * @param object DatabaseObject from Reactome
     * @param version Integer version number of ReactomeDB
     */
    public ExtractorBase(DatabaseObject object, int version) {
        wdEntry  = "";
        thisObject = object;
        dbVersion = version;
        count = new ArrayList<TypeCounter>();
        childEntities = new ArrayList<PhysicalEntity>();
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
     * Get the stable identifier from the DatabaseObject
     *
     * @return String representing the stable identifier from ReactomeDB
     */
    public String getStableID() {
        if (thisObject != null)
            return thisObject.getStId();
        else
            return "";
    }

    /**
     * Get the name of the entry with any commas replaced
     *
     * @return String representing the name from ReactomeDB with commas replaced by semicolons
     */
    public String getEntryName() {
        return getName();
    }

    ///////////////////////////////////////////////////////////////////////////////////

    // functions to output resulting string

    /**
     * Write the wikidataEntry line to std output.
     */
    public void toStdOut()    {
        System.out.println(wdEntry);
    }

    //////////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieve the Wikidata entry string.
     *
     * @return String representing the wikidata entry
     */
    String getWikidataEntry(){
        return wdEntry;
    }

    //////////////////////////////////////////////////////////////////////////////////


    /**
     * Function to adjust display name as appropriate
     *
     * @return String representing the name to used in wikidata entry
     */
    private String getName() {
        String name;
        try {
            name = thisObject.getDisplayName();
        }
        catch (NullPointerException e) {
            return "";
        }
        String name_nocommas = name.replaceAll(",", ";");
        return name_nocommas;
    }

    ///////////////////////////////////////////////////////////////////////////////////

    public ArrayList<PhysicalEntity> getChildEntities() {
        return childEntities;
    }

    /**
     * create string describing the physical entity structure within Reactome
     *
     * @return  String representing the structure
     */
    public String extractStructure(){
        String structure = null;
        int num = count.size();
        if (num == 0) return null;
        int numAdded = 0;
        if (num > 0) {
            structure = "";
            for (TypeCounter tc : count){
                String struct = String.format("%s %s %d %s", tc.getType(), tc.getName(), tc.getCount(), tc.getStId());
                structure += struct;
                numAdded++;
                if (numAdded < num){
                    structure += ";";
                }
            }
        }
        return structure;
    }


    /**
     * Add the identifier of the referenced entity to the list
     *
     * @param pe  PhysicalEntity to process
     *
     * @return true if all components have been referenced, false otherwise
     */
    public boolean addComponentId(PhysicalEntity pe) {
        boolean complete = false;
        String id = pe.getStId();
        String refid = null;
        String type = null;
        if (pe instanceof SimpleEntity){
            type = "SE";
            ReferenceMolecule ref = ((SimpleEntity)pe).getReferenceEntity();
            if (ref != null) {
                refid = ref.getIdentifier();
            }
        }
        else if (pe instanceof EntityWithAccessionedSequence){
            List<AbstractModifiedResidue> mods = ((EntityWithAccessionedSequence)pe).getHasModifiedResidue();
            if (mods != null && mods.size() > 0 ) {
                type = "EWASMOD";
            }
            else {
                type = "EWAS";
                ReferenceSequence ref = ((EntityWithAccessionedSequence) pe).getReferenceEntity();
                if (ref != null) {
                    refid = ref.getIdentifier();
                }
            }
        }
        else if (pe instanceof GenomeEncodedEntity){
            type = "GEE";
        }
        else if (pe instanceof CandidateSet){
            type = "CS";
            childEntities.add(pe);
        }
        else if (pe instanceof DefinedSet){
            type = "DS";
            childEntities.add(pe);
        }
        else if (pe instanceof OpenSet){
            type = "OS";
            childEntities.add(pe);
        }
        else if (pe instanceof Complex){
            type = "COMP";
            childEntities.add(pe);
        }
        else if (pe instanceof OtherEntity){
            type = "OE";
            childEntities.add(pe);
        }
        else {
            type = "UNKNOWN";
        }

        if (id != null) {
            for (TypeCounter tc: count) {
                if (tc.getStId().equals(id)) {
                    tc.incrementCount();
                    complete = true;
                    break;
                }
            }
            if (!complete){
                TypeCounter tc1 = new TypeCounter(id, refid, type);
                tc1.incrementCount();
                count.add(tc1);
                complete = true;
            }

        }
        return complete;
    }

}

/**
 * Class to count the types of physical entities encountered in a complex/set
 */
class TypeCounter {
    private final String mName;
    private final String mStId;
    private Integer mCount;
    private final String mType;

    TypeCounter(String stId, String name, String type) {
        mName = name;
        mStId = stId;
        mCount = 0;
        mType = type;
    }

    String getName() { return mName; }

    String getType() { return mType; }

    String getStId() { return mStId; }

    Integer getCount() { return mCount; }

    void incrementCount() {
        mCount++;
    }
}


