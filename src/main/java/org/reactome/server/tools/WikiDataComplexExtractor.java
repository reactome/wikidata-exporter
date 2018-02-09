package org.reactome.server.tools;

import org.apache.commons.lang3.ObjectUtils;
import org.reactome.server.graph.domain.model.*;


import java.util.ArrayList;
import java.util.List;

/**
 * @author Sarah Keating <skeating@ebi.ac.uk>
 */
class WikiDataComplexExtractor extends PEExtractorBase{


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
        // species_code,stableId,[part;part],None
       String format = "%s,%s,%s,[%s],None";

        String species = "HSA";
        // only complexes
        if (thisObject == null || !(thisObject instanceof Complex))
        {
            wdEntry = "invalid complex";
        }
        else {
            String stId = getStableID();
            String parts = getParts();
            wdEntry = String.format(format, species, "COMP", stId, parts);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////

    // functions to output resulting string
    private String getParts() {
        String parts = "";
        StringBuilder sb;
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

