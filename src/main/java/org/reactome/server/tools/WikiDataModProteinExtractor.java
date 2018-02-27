package org.reactome.server.tools;

import org.apache.commons.lang3.ObjectUtils;
import org.reactome.server.graph.domain.model.*;


import java.util.ArrayList;
import java.util.List;

/**
 * @author Sarah Keating <skeating@ebi.ac.uk>
 */
class WikiDataModProteinExtractor extends ExtractorBase{


    /**
     * Construct an instance of the WikiDataModProteinExtractor
     */
    public WikiDataModProteinExtractor(){
        super ();
    }

    /**
     * Construct an instance of the WikiDataModProteinExtractor for the specified
     * EntityWithAccessionedSequence.
     *
     * @param ewas  EntityWithAccessionedSequence from ReactomeDB
     */
    public WikiDataModProteinExtractor(EntityWithAccessionedSequence ewas){
        super((PhysicalEntity) (ewas));
    }

    /**
     * Construct an instance of the WikiDataModProteinExtractor for the specified
     * EntityWithAccessionedSequence.
     *
     * @param ewas EntityWithAccessionedSequence from ReactomeDB
     * @param version Integer - version number of the database
     */
    public WikiDataModProteinExtractor(EntityWithAccessionedSequence ewas, Integer version){
        super((PhysicalEntity)(ewas), version);
    }

    /**
     * Create the wikidata entry using the Reactome EntityWithAccessionedSequence specified in the constructor.
     */
    public void createWikidataEntry(){
        // currently ReactomeBot expects an entry
        // species_code,entity_code,name,stableId,[part;part],complexportalid,None
       String format = "%s,%s,%s,%s,None";

        String species = "HSA";
        // only ewas
        if (thisObject == null || !(thisObject instanceof EntityWithAccessionedSequence))
        {
            wdEntry = "invalid EWAS";
        }
        else {
            String stId = getStableID();
            String name = getEntryName();
            wdEntry = String.format(format, species, "EWASMOD", stId, name);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////

}

