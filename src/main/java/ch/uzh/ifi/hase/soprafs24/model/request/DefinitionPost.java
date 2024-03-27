package ch.uzh.ifi.hase.soprafs24.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DefinitionPost {
    @JsonProperty
    String definition;

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }
}
