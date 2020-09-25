package org.reactome.server.tools;

import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.service.helper.StoichiometryObject;

import java.util.ArrayList;
import java.util.List;

public class WDReaction extends WDEvent {

    private List<WDLinks> input;
    private List<WDLinks> output;
    private List<WDLinks> modifier;

    public WDReaction(ReactionLikeEvent rle) {
        super(rle);
    }


    public void setInput(List<WDLinks> input) { this.input = input; }

    public void setOutput(List<WDLinks> output) { this.output = output; }

    public void setModifier(List<WDLinks> modifier) { this.modifier = modifier; }

    public List<WDLinks> getInput() { return input; }

    public List<WDLinks> getOutput() { return output; }

    public List<WDLinks> getModifier() { return modifier; }

}
