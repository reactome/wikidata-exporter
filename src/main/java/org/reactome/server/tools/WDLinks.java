package org.reactome.server.tools;

import org.apache.log4j.Logger;
import org.reactome.server.graph.domain.model.*;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author Yusra Haider (yhaider@ebi.ac.uk)
 **/

// this class represents the children / parts for the different entities in Reactome
public class WDLinks implements Serializable {
    static Logger log = Logger.getLogger(WDLinks.class);

    private String id; // the id of the child / part
    private String idType; // does the child / part belong to Reactome or is it from an external database?
    private Integer qty; // the quantity of the child, for that parent

    public WDLinks(DatabaseObject databaseObject, Integer qty) {
        this.qty = qty;

        if (databaseObject instanceof Event || databaseObject instanceof CandidateSet || databaseObject instanceof DefinedSet || databaseObject instanceof Complex) {
            this.id = databaseObject.getStId();
            this.idType = "REACTOME";

        } else if (databaseObject instanceof SimpleEntity) {
            ReferenceMolecule ref = ((SimpleEntity) databaseObject).getReferenceEntity();
            if (ref != null) {
                this.id = ref.getIdentifier();
                this.idType = ref.getDatabaseName();
            }
        } else if (databaseObject instanceof EntityWithAccessionedSequence) {
            List<AbstractModifiedResidue> mods = ((EntityWithAccessionedSequence) databaseObject).getHasModifiedResidue();
            if (mods != null && mods.size() > 0) {
                this.idType = "REACTOME";
                this.id = databaseObject.getStId();
            } else {
                ReferenceSequence ref = ((EntityWithAccessionedSequence) databaseObject).getReferenceEntity();
                if (ref != null) {
                    this.idType = ref.getDatabaseName();
                    this.id = ref.getIdentifier();
                }
            }
        }

        // default return..
        else {
            this.id = databaseObject.getStId();
            this.idType = "UNKNOWN";
            log.warn(this.id + " being exported as UNKNOWN type link");
        }

    }

    // to deal with residues for modified proteins
    public WDLinks(String id, String idType, Integer qty) {
        this.id = id;
        this.idType = idType;
        this.qty = qty;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;

        if (other.getClass() != this.getClass()) return false;

        WDLinks that = (WDLinks) other;
        return this.id.equals(that.id);
    }

}
