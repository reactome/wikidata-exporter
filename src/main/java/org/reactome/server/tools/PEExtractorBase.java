package org.reactome.server.tools;

import org.apache.commons.lang3.ObjectUtils;
import org.reactome.server.graph.domain.model.*;


import java.util.ArrayList;
import java.util.List;

/**
 * @author Sarah Keating <skeating@ebi.ac.uk>
 */
class PEExtractorBase extends ExtractorBase{

    public ArrayList<TypeCounter> count;// = new ArrayList<TypeCounter>();

    /**
     * Construct an instance of the PEExtractorBase
     */
    public PEExtractorBase(){
        super ();
        count = new ArrayList<TypeCounter>();
    }

    /**
     * Construct an instance of the PEExtractorBase for the specified
     * PhysicalEntity.
     *
     * @param c  PhysicalEntity from ReactomeDB
     */
    public PEExtractorBase(PhysicalEntity c){
        super((DatabaseObject)(c));
        count = new ArrayList<TypeCounter>();
    }

    /**
     * Construct an instance of the PEExtractorBase for the specified
     * PhysicalEntity.
     *
     * @param c PhysicalEntity from ReactomeDB
     * @param version Integer - version number of the database
     */
    public PEExtractorBase(PhysicalEntity c, Integer version){
        super((DatabaseObject)(c), version);
        count = new ArrayList<TypeCounter>();
    }

    ///////////////////////////////////////////////////////////////////////////////////

    // functions to output resulting string

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
            type = "EWAS";
            ReferenceSequence ref = ((EntityWithAccessionedSequence)pe).getReferenceEntity();
            if (ref != null) {
                refid = ref.getIdentifier();
            }
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
        else if (pe instanceof Complex){
            type = "COMP";
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

