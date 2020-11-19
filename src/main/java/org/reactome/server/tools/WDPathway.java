package org.reactome.server.tools;

import org.reactome.server.graph.domain.model.Pathway;

import java.util.List;

/**
 * @author Yusra Haider (yhaider@ebi.ac.uk)
 **/

public class WDPathway extends WDEvent {

    private List<WDLinks> parts;

    public WDPathway(Pathway pathway) {
        super(pathway);
    }

    public List<WDLinks> getParts() {
        return parts;
    }

    public void setParts(List<WDLinks> parts) {
        this.parts = parts;
    }

}