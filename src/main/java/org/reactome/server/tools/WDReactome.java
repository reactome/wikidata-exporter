package org.reactome.server.tools;


import java.io.Serializable;
import java.util.Objects;

public class WDReactome implements Serializable {

    private String id;
    private String name;
    private String species;
    private String type;
    private String description;

    public WDReactome() {}

    public WDReactome(String id, String name, String description, String species, String type) {
        this(id, name, species);
        this.description = description;
        this.type = type;
    }

    public WDReactome(String id, String name, String species) {
        this.id = id;
        this.name = name;
        this.species = species;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public void setSpecies(String species) { this.species = species; }

    public String getSpecies() { return species; }

    public void setType(String type) { this.type = type; }

    public String getType() { return type; }

    public void setDescription(String description) { this.description = description; }

    public String getDescription() { return description; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

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
