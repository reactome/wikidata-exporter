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
    private String modificationType = "";
    private boolean labelModified = false;


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
//        String name = wikiLabel;

        if (modificationType.equals("P")) {
            wikiLabel += " phosphorylated";
        }
        wdEntry = String.format(format, species, "EWASMOD", modificationType, stId, wikiLabel, uniprot, modres);
    }

    public String getLabelUsed() {
        return wikiLabel;
    }

    public void modifyLabel() {
        String oldwikiLabel = wikiLabel;
        if (!labelModified) {
            String compart = "";
            List<EntityCompartment> comp = ((PhysicalEntity) (thisObject)).getCompartment();
            if (comp != null && comp.size() > 0) {
                compart = comp.get(0).getName();
            }
            wikiLabel = oldwikiLabel + " [" + compart + "]";
        }
        else {
            wikiLabel = thisObject.getDisplayName();
        }
        wikiLabel = wikiLabel.replace(",", " ");
        wdEntry = wdEntry.replace(oldwikiLabel, wikiLabel);
        labelModified = true;

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
                        modificationType = "P";
                        Integer coord = ((TranslationalModification) (m)).getCoordinate();
                        PsiMod psi = ((TranslationalModification) (m)).getPsiMod();
                        String name = psi.getDisplayName().replace(",", " ");
                        String thismod;
                        if (coord != null) {
                            wikiLabel = wikiLabel + String.format(" ser-%d", coord);
                            thismod = String.format("%s %d", name, coord);
                        }
                        else {
                            wikiLabel = wikiLabel + String.format(" ser-unknown");
                            thismod = String.format("%s unknown", name);
                        }
                        if (single) {
                            mod = mod + thismod;
                        } else {
                            mod = mod + ";" + thismod;
                        }
                        single = false;
                    }
                    else if (m instanceof ReplacedResidue) {
                        modificationType = "R";
                        Integer coord = ((ReplacedResidue) (m)).getCoordinate();
                        wikiLabel = m.getDisplayName();
                        List<PsiMod> psimods = ((ReplacedResidue) (m)).getPsiMod();
                        for (PsiMod psi : psimods) {
                            String name = psi.getDisplayName().replace(",", " ");
                            String thismod;
                            if (coord != null) {
                                thismod = String.format("%s %d", name, coord);
                            }
                            else {
                                thismod = String.format("%s unknown", name);
                            }
                            if (single) {
                                mod = mod + thismod;
                            } else {
                                mod = mod + ";" + thismod;
                            }
                            single = false;
                        }
                    }
//                    else if (m instanceof FragmentInsertionModification) {
//                        modificationType = "FI";
//                        Integer coord = ((FragmentInsertionModification) (m)).getCoordinate();
//                        wikiLabel = m.getDisplayName();
//                        String thismod = String.format("%s %d", "none", coord);
//                        mod = mod + thismod;
//                    }
                }
                if (mod.equals("")) {
                    for (AbstractModifiedResidue m : mods) {
                        System.out.println("mod with id " + thisObject.getStId());
                    }

                }
            }
        }
        return mod;
    }
}

