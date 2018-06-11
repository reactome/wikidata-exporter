package org.reactome.server.tools;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Logger;
import org.reactome.server.graph.domain.model.*;


import java.util.ArrayList;
import java.util.List;

/**
 * @author Sarah Keating <skeating@ebi.ac.uk>
 */
class WikiDataComplexExtractor extends ExtractorBase{
    static Logger log = Logger.getLogger(WikiDataComplexExtractor.class);


    /**
     * Construct an instance of the WikiDataComplexExtractor
     */
    public WikiDataComplexExtractor(){
        super ();
    }

    /**
     * Construct an instance of the WikiDataComplexExtractor for the specified
     * Complex.
     *
     * @param c  Complex from ReactomeDB
     */
    public WikiDataComplexExtractor(Complex c){
        super((PhysicalEntity) (c));
    }

    /**
     * Construct an instance of the WikiDataComplexExtractor for the specified
     * Complex.
     *
     * @param c Complex from ReactomeDB
     * @param version Integer - version number of the database
     */
    public WikiDataComplexExtractor(Complex c, Integer version){
        super((PhysicalEntity)(c), version);
    }

    /**
     * Create the wikidata entry using the Reactome Complex specified in the constructor.
     */
    public void createWikidataEntry(){
        // currently ReactomeBot expects an entry
        // species_code,entity_code,name,stableId,[part;part],complexportalid,None
       String format = "%s,%s,%s,%s,[%s],%s,None";

        String species = "HSA";
        // only complexes
        if (thisObject == null || !(thisObject instanceof Complex))
        {
            log.error("Invalid Complex: " + this.getStableID());
            wdEntry = "invalid complex";
        }
        else {
            String stId = getStableID();
            String parts = getParts();
            String name = getEntryName();
            String cpRef = getComplexPortalRef();
            wdEntry = String.format(format, species, "COMP", stId, name, parts, cpRef);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Function to get the Complex Portal id
     *
     * @note: This will facilitate adding a cross reference once ComplexPortal is in Wikidata
     *
     * @return string representing ComplexPortal Id
     */
    private String getComplexPortalRef() {
        String cpId = "";
        List<DatabaseIdentifier> xrefs = ((Complex) thisObject).getCrossReference();
        if (xrefs != null) {
            for (DatabaseIdentifier db: xrefs) {
                if (db.getDatabaseName().equals("ComplexPortal")) {
                    cpId = db.getIdentifier();
                    break;
                }
            }
        }
        return cpId;
    }


    /**
     * Function to create a semicolon separated list of information for each component of complex
     *
     * @note This function uses addComponentId to create a List of TypeCounter objects
     * and then uses extractStructure to create the string from the List
     *
     * @return a string representing the members of the set
     */
    private String getParts() {
        String parts = "";
        if (thisObject != null) {
            if (((Complex)thisObject).getHasComponent() != null){
                for (PhysicalEntity component: ((Complex)thisObject).getHasComponent() ){
                    addComponentId(component);
                }
            }

            parts = extractStructure();
        }
        return parts;
    }
}

