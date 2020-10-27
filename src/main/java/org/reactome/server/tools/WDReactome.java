package org.reactome.server.tools;


import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Yusra Haider (yhaider@ebi.ac.uk)
 **/

public class WDReactome implements Serializable {

    private String id;
    private String name;
    private String species;
    private String type;
    private String description;

    public WDReactome(String id, String name, String species) {
        this.id = id;
        // StringUtils.abbreviate was added because there is a limit of 250 characters
        // for wikidata label
        this.name = StringUtils.abbreviate(name, 240);
        this.species = species;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getSpecies() {
        return species;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;

        if (other.getClass() != this.getClass()) return false;

        WDReactome that = (WDReactome) other;
        return this.id.equals(that.id);
    }

}
