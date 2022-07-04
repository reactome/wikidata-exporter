package org.reactome.server.tools;

import org.apache.log4j.Logger;
import org.reactome.server.graph.domain.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yusra Haider (yhaider@ebi.ac.uk)
 **/

public class WDModifiedProtein extends WDReactome {

    static Logger log = Logger.getLogger(WDModifiedProtein.class);

    private String uniprotID;
    private List<WDLinks> modifiedResidues;

    public WDModifiedProtein(EntityWithAccessionedSequence ewas) {
        super(ewas.getStId(), ewas.getDisplayName(), ewas.getSpeciesName());
        super.setType("EWASMOD");
        this.uniprotID = populateUniprotID(ewas);
        this.modifiedResidues = populateModifiedResidues(ewas);
        super.setDescription("An instance of entity with accessioned sequence in " + super.getSpecies() + " with Reactome ID (" + super.getId() + ")");
    }

    public String getUniprotID() {
        return uniprotID;
    }

    public void setUniprotID(String uniprotID) {
        this.uniprotID = uniprotID;
    }

    public List<WDLinks> getModifiedResidues() {
        return modifiedResidues;
    }

    public void setModifiedResidues(List<WDLinks> modifiedResidues) {
        this.modifiedResidues = modifiedResidues;
    }

    private String populateUniprotID(EntityWithAccessionedSequence ewas) {
        ReferenceSequence ref = ewas.getReferenceEntity();
        if (ref == null) return null;
        return ref.getIdentifier();

    }

    private List<WDLinks> populateModifiedResidues(EntityWithAccessionedSequence ewas) {

        List<WDLinks> residues = null;
        List<AbstractModifiedResidue> mods = ewas.getHasModifiedResidue();

        if (mods != null && mods.size() > 0) {
            residues = new ArrayList<>();

            for (AbstractModifiedResidue m : mods) {
                if (m instanceof TranslationalModification) {
                    Integer coordinate = ((TranslationalModification) (m)).getCoordinate();
                    PsiMod psiMod = ((TranslationalModification) (m)).getPsiMod();
                    residues.add(new WDLinks(psiMod.getDatabaseName() + ":" + psiMod.getIdentifier(), psiMod.getDatabaseName(), coordinate));

                } else if (m instanceof ReplacedResidue) {
                    Integer coordinate = ((ReplacedResidue) (m)).getCoordinate();
                    List<PsiMod> psiMods = ((ReplacedResidue) (m)).getPsiMod();
                    for (PsiMod psiMod : psiMods) {
                        residues.add(new WDLinks(psiMod.getDatabaseName() + ":" + psiMod.getIdentifier(), psiMod.getDatabaseName(), coordinate));
                    }
                } else {
                    log.warn("modified residue not being exported: " + m.getDisplayName());
                }
            }
        }
        return residues;
    }
}
