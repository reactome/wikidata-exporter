package org.reactome.server.tools;

import org.apache.commons.lang3.ObjectUtils;
import org.reactome.server.graph.domain.model.*;


import java.util.ArrayList;
import java.util.List;

/**
 * @author Sarah Keating <skeating@ebi.ac.uk>
 */
class WikiDataModProteinExtractor extends ExtractorBase{

    private String wikiLabel = "";


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
        // species_code,entity_code,type,stableId,uniprotid;name,[mod, mod],None
       String format = "%s,%s,%s,%s,%s,%s,[%s],None";

        String species = "HSA";
        // only ewas
        if (thisObject == null || !(thisObject instanceof EntityWithAccessionedSequence))
        {
            wdEntry = "invalid EWAS";
            return;
        }
        String stId = getStableID();
        String uniprot = getProtein();
        String modres = getModifiedResidues();

        if (isReplacedResidue()) {
            String name = wikiLabel.replace(",", " ");
            wdEntry = String.format(format, species, "EWASMOD", "R", stId, name, uniprot, modres);
        }
        else {
            String name = wikiLabel + " phosphorylated";
            wdEntry = String.format(format, species, "EWASMOD", "P", stId, name, uniprot, modres);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////

    private boolean isReplacedResidue() {
        boolean replaced = false;
        if (thisObject != null) {
            List<AbstractModifiedResidue> mods = ((EntityWithAccessionedSequence) thisObject).getHasModifiedResidue();
            if (mods != null && mods.size() > 0) {
                for (AbstractModifiedResidue m : mods) {
                    if (m instanceof ReplacedResidue) {
                        replaced = true;
                    }
                }
            }
        }
        return replaced;
    }
    ///////////////////////////////////////////////////////////////////////////////////

    private String getProtein() {
        String protein = "";
        if (thisObject != null) {
            ReferenceSequence ref = ((EntityWithAccessionedSequence) thisObject).getReferenceEntity();
            if (ref != null) {
                protein = ref.getIdentifier();
                List<String> names = ref.getGeneName();
                if (names != null && names.size() > 0) {
                    wikiLabel = wikiLabel + names.get(0);
                }
            }
        }
        return String.format("%s", protein);
    }

    private String getModifiedResidues() {
        String mod = "";
        if (thisObject != null) {
            List<AbstractModifiedResidue> mods = ((EntityWithAccessionedSequence) thisObject).getHasModifiedResidue();
            boolean single = true;
            if (mods != null && mods.size() > 0) {
                for (AbstractModifiedResidue m : mods) {
                    if (m instanceof TranslationalModification) {
                        Integer coord = ((TranslationalModification) (m)).getCoordinate();
                        wikiLabel = wikiLabel + String.format(" ser-%d", coord);
                        PsiMod psi = ((TranslationalModification) (m)).getPsiMod();
                        ReferenceDatabase refdb = psi.getReferenceDatabase();
                        String name = psi.getDisplayName().replace(",", " ");
                        String thismod = String.format("%s %d", name, coord);
                        if (single) {
                            mod = mod + thismod;
                        } else {
                            mod = mod + ";" + thismod;
                        }
                        single = false;
                    }
                }
                if (mod.equals("")) {
                    for (AbstractModifiedResidue m : mods) {
                        if (m instanceof ReplacedResidue) {
                            Integer coord = ((ReplacedResidue) (m)).getCoordinate();
                            wikiLabel = m.getDisplayName();
                            List<PsiMod> psimods = ((ReplacedResidue) (m)).getPsiMod();
                            for (PsiMod psi : psimods) {
                                ReferenceDatabase refdb = psi.getReferenceDatabase();
                                String name = psi.getDisplayName().replace(",", " ");
                                String thismod = String.format("%s %d", name, coord);
                                if (single) {
                                    mod = mod + thismod;
                                } else {
                                    mod = mod + ";" + thismod;
                                }
                                single = false;
                            }
                        }
                    }

                }
            }
        }
        return mod;
    }
}

