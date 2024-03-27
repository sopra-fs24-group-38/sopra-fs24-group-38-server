package ch.uzh.ifi.hase.soprafs24.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Player {
    private String name;

    private String token;

    private int score;

    private String definition;

    @JsonProperty("voted_for")
    private int votedFor;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public int getVotedFor() {
        return votedFor;
    }

    public void setVotedFor(int votedFor) {
        this.votedFor = votedFor;
    }
}
