package org.reactome.server.tools;

import org.apache.commons.lang3.ObjectUtils;
import org.reactome.server.graph.domain.model.*;


import java.util.ArrayList;
import java.util.List;

/**
 * @author Sarah Keating <skeating@ebi.ac.uk>
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

    public ExtractorBase(DatabaseObject object) {
        wdEntry  = "";
        thisObject = object;

    }

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

    public String getStableID() {
        return getIdentifier();
    }

    public String getEntryName() {
        return getName();
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


    String getIdentifier() {
        return thisObject.getStId();
    }

    String getName() {
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


