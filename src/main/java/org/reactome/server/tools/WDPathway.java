//TODO: set jackson to ignore null fields

package org.reactome.server.tools;
import org.reactome.server.graph.domain.model.*;

import java.util.List;

public class WDPathway extends WDEvent {

    private List<WDLinks> parts;

    public WDPathway(Pathway pathway) {
        super(pathway);
    }

    public List<WDLinks> getParts() { return parts; }

    public void setParts(List<WDLinks> parts) { this.parts = parts; }

}