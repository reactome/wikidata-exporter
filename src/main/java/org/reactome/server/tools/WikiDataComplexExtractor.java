package org.reactome.server.tools;

import org.apache.commons.lang3.ObjectUtils;
import org.reactome.server.graph.domain.model.*;


import java.util.ArrayList;
import java.util.List;

/**
 * @author Sarah Keating <skeating@ebi.ac.uk>
 */
class WikiDataComplexExtractor extends ExtractorBase{

//    private final Complex thisComplex;
    private ArrayList<TypeCounter> count;// = new ArrayList<TypeCounter>();

    /**
     * Construct an instance of the WikiDataComplexExtractor
     */
    public WikiDataComplexExtractor(){
        super ();
        count = new ArrayList<TypeCounter>();
    }

    /**
     * Construct an instance of the WikiDataComplexExtractor for the specified
     * Complex.
     *
     * @param c  Complex from ReactomeDB
     */
    public WikiDataComplexExtractor(Complex c){
        super((DatabaseObject)(c));
        count = new ArrayList<TypeCounter>();
    }

    /**
     * Construct an instance of the WikiDataComplexExtractor for the specified
     * Complex.
     *
     * @param c Complex from ReactomeDB
     * @param version Integer - version number of the database
     */
    public WikiDataComplexExtractor(Complex c, Integer version){
        super((DatabaseObject)(c), version);
        count = new ArrayList<TypeCounter>();
    }

    /**
     * Create the wikidata entry using the Reactome Complex specified in the constructor.
     */
    public void createWikidataEntry(){
        // currently ReactomeBot expects an entry
        // species_code,stableId,[part;part],None
       String format = "%s,%s,[%s],None";

        String species = "HSA";
        // only complexes
        if (thisObject == null || !(thisObject instanceof Complex))
        {
            wdEntry = "invalid complex";
        }
        else {
            String stId = getIdentifier();
            String parts = getParts();
            wdEntry = String.format(format, species, stId, parts);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////

    // functions to output resulting string
    // Private functions

    private String getParts() {
        String parts = "";
        StringBuilder sb;
        if (thisObject != null) {
            parts = extractComplexStructure((Complex)(thisObject));
        }
        return parts;
    }
    /**
     * create string describing the complex structure within Reactome
     *
     * @param complex   Reactome Complex to describe
     *
     * @return          String representing the complex structure
     */
    private String extractComplexStructure(Complex complex){
        String structure = null;
        if (complex.getHasComponent() != null){
            for (PhysicalEntity component: complex.getHasComponent() ){
                addComponentId(component);
            }
        }
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
    private boolean addComponentId(PhysicalEntity pe) {
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
            type = "EWAS";
            ReferenceSequence ref = ((EntityWithAccessionedSequence)pe).getReferenceEntity();
            if (ref != null) {
                refid = ref.getIdentifier();
            }
        }
        else if (pe instanceof Complex) {
            type = "COMP";
        }
        else if (pe instanceof GenomeEncodedEntity){
            type = "GEE";
        }
        else if (pe instanceof CandidateSet){
            type = "CS";
        }
        else if (pe instanceof DefinedSet){
            type = "DS";
        }
        else if (pe instanceof OpenSet){
            type = "OS";
        }
//        else if (pe instanceof EntitySet){
//            type = "EWAS";
//            ReferenceSequence ref = ((EntityWithAccessionedSequence)pe).getReferenceEntity();
//            if (ref != null) {
//                refid = ref.getIdentifier();
//            }
//        }
//        else if (pe instanceof Polymer){
//                type = "EWAS";
//                ReferenceSequence ref = ((EntityWithAccessionedSequence)pe).getReferenceEntity();
//                if (ref != null) {
//                    refid = ref.getIdentifier();
//                }
//        }

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


