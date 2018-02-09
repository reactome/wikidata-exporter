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


    /**
     * Construct an instance of the ExtractorBase
     */
    public ExtractorBase(){
        wdEntry  = "";
        thisObject = null;
    }

    /**
     * Construct an instance of ExtractorBase with a databaseObject
     *
     * @param object DatabaseObject form Reactome
     */
    public ExtractorBase(DatabaseObject object) {
        wdEntry  = "";
        thisObject = object;

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


