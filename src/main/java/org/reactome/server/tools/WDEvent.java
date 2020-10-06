package org.reactome.server.tools;
import org.reactome.server.graph.domain.model.*;

import java.util.ArrayList;
import java.util.List;


public class WDEvent extends WDReactome {

    private List<String> publication;
    private String goterm;

    public WDEvent(Event event) {
        super(event.getStId(), event.getDisplayName(), event.getSpeciesName());

        if (event instanceof Pathway) {
            super.setDescription("An instance of the biological pathway in " + super.getSpecies() + " with Reactome ID (" + super.getId() + ")");
            super.setType("PATHWAY");
        }
        else if (event instanceof ReactionLikeEvent){
            super.setDescription("An instance of the biological reaction in " + super.getSpecies() + " with Reactome ID (" + super.getId() + ")");
            super.setType("REACTION");
        }
        this.goterm = populateGoTerm(event.getGoBiologicalProcess());
        this.publication = populatePublication(event.getLiteratureReference());
    }

    public List<String> getPublication() { return publication; }

    public void setPublication(List<String> publication) { this.publication = publication; }

    public String getGoterm() { return goterm; }

    public void setGoterm(String goterm) { this.goterm = goterm; }


    private String populateGoTerm(GO_BiologicalProcess go) {
        if(go == null)
            return null;
        return "GO:" + go.getAccession();
    }

    //TODO: maybe redo the publication to be like wdlinks so that we can cater for stuff that isnt pubmed..?
    // is that even a valid use case? ask
    private List<String> populatePublication(List<Publication> pubs) {
        List<String> publication = new ArrayList<>();
        if (pubs == null || pubs.size() == 0) {
            return null;
        }
        for (Publication pub : pubs) {
            if (pub instanceof LiteratureReference) {
                Integer pubmed = ((LiteratureReference) pub).getPubMedIdentifier();
                if (pubmed != null && pubmed != 0) {
                    publication.add(pubmed.toString());
                }
            }
        }
        return publication;
    }
}

