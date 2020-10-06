package org.reactome.server.tools;

import org.reactome.server.graph.domain.model.CandidateSet;
import org.reactome.server.graph.domain.model.Complex;
import org.reactome.server.graph.domain.model.DefinedSet;
import org.reactome.server.graph.domain.model.PhysicalEntity;

import java.util.List;

public class WDPhysicalEntity extends WDReactome {

    private List<WDLinks> parts;

    public WDPhysicalEntity(PhysicalEntity physicalEntity) {
        super(physicalEntity.getStId(), physicalEntity.getDisplayName(), physicalEntity.getSpeciesName());

        if (physicalEntity instanceof Complex) {
            super.setType("COMP");
            super.setDescription("An instance of macromolecular complex in " + super.getSpecies() + " with Reactome ID (" + super.getId() + ")");
        }
        else if (physicalEntity instanceof DefinedSet) {
            super.setType("DS");
            super.setDescription("An instance of defined set in " + super.getSpecies() + " with Reactome ID (" + super.getId() + ")");
        }
        else if(physicalEntity instanceof CandidateSet) {
            super.setType("CS");
            super.setDescription("An instance of candidate set in " + super.getSpecies() + " with Reactome ID (" + super.getId() + ")");
        }
    }

    public void setParts(List<WDLinks> parts) { this.parts = parts; }

    public List<WDLinks> getParts() { return parts; }
}
