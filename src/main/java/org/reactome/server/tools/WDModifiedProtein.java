package org.reactome.server.tools;

import org.reactome.server.graph.domain.model.*;

import java.util.ArrayList;
import java.util.List;

public class WDModifiedProtein extends WDReactome {

    private String uniprotID;
    private List<WDLinks> modifiedResidues;

    public WDModifiedProtein(EntityWithAccessionedSequence ewas) {
        super(ewas.getStId(), ewas.getDisplayName(), ewas.getSpeciesName());
        //TODO shift to constants file
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

    public String populateUniprotID(EntityWithAccessionedSequence ewas) {
        ReferenceSequence ref = ewas.getReferenceEntity();
        if (ref == null)  return null;
        return ref.getIdentifier();

    }

    public List<WDLinks> populateModifiedResidues(EntityWithAccessionedSequence ewas) {

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
                }
                //TODO take care of other types of modifications
            }
        }
        return residues;
    }
}
