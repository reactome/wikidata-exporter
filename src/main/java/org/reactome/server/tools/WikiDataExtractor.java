package org.reactome.server.tools;

import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.domain.model.Event;
import org.sbml.jsbml.*;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;


import java.util.ArrayList;
import java.util.List;

/**
 * @author Sarah Keating <skeating@ebi.ac.uk>
 */
class WikiDataExtractor {

    private final Pathway thisPathway;

    private static Integer dbVersion = 0;

    private static String wdEntry;

    /**
     * Construct an instance of the WikiDataExtractor
     */
    public WikiDataExtractor(){
        thisPathway = null;
    }

    /**
     * Construct an instance of the WikiDataExtractor for the specified
     * Pathway.
     *
     * @param pathway  Pathway from ReactomeDB
     */
    public WikiDataExtractor(Pathway pathway){
        thisPathway = pathway;
    }

    /**
     * Construct an instance of the WikiDataExtractor for the specified
     * Pathway.
     *
     * @param pathway Pathway from ReactomeDB
     * @param version Integer - version number of the database
     */
    public WikiDataExtractor(Pathway pathway, Integer version){
        thisPathway = pathway;
        dbVersion = version;
    }

    /**
     * Create the wikidata entry using the Reactome Pathway specified in the constructor.
     */
    public void createWikidataEntry(){
        // currently ReactomeBot expects an entry
        // species_code,stableId,Name,Description,[publication;publication;..],goterm,None
        String format = "%s,%s,%s,%s,[%s],%s,None";

        String species = "HSA";
        String stId = thisPathway.getStId();
        String name = getName();
        String description = composeDescription(name);
        String publications = getPublicationList();
        String goterm = getGoTerm();

        wdEntry = String.format(format, species, stId, name, description, publications, goterm);
    }

    /**
     * Set the database version number.
     *
     * @param version  Integer the ReactomeDB version number being used.
     */
    public void setDBVersion(Integer version) {
        dbVersion = version;
    }


    ///////////////////////////////////////////////////////////////////////////////////

    // functions to output resulting string

    /**
     * Write the SBMLDocument to std output.
     */
    public void toStdOut()    {
        System.out.println(wdEntry);
    }

    //////////////////////////////////////////////////////////////////////////////////

    // functions to facilitate testing

    /**
     * Retrieve the Wikidata entry string.
     *
     * @return String representing the wikidata entry
     */
    String getWikidataEntry(){
        return wdEntry;
    }

    //////////////////////////////////////////////////////////////////////////////////

    // Private functions

    private String getName() {
        String name = thisPathway.getDisplayName();
        String name_nocommas = name.replaceAll(",", ";");
        return name_nocommas;
    }

    private String composeDescription(String name) {
        return "An instance of " + name + " in Homo sapiens";
    }

    private String getPublicationList() {
        String pubs = "";
        List<Publication> publications = thisPathway.getLiteratureReference();
        if (publications == null || publications.size() == 0) {
            return pubs;
        }
        for (Publication pub : publications) {
            if (pub instanceof LiteratureReference) {
                Integer pubmed = ((LiteratureReference) pub).getPubMedIdentifier();
                if (pubmed != null && pubmed != 0) {
                    if (pubs.length() > 0)
                        pubs = pubs + ";";
                    pubs = pubs + "http://identifiers.org/pubmed/" + pubmed.toString();
                }
            }
        }
        return pubs;
    }

    private String getGoTerm() {
        String goterm = "";
        GO_BiologicalProcess go = thisPathway.getGoBiologicalProcess();
        if (go == null) {
            return goterm;
        }
        goterm = "GO:" + go.getAccession();
        return goterm;
    }
}
