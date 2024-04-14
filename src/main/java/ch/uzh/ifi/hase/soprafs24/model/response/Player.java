package ch.uzh.ifi.hase.soprafs24.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Player {
    private String username;

    private int score;

    private String definition;

    @JsonProperty("voted_for")
    private int votedFor = -1;

    private Long avatarId;

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Long getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(Long avatarId) {
        this.avatarId = avatarId;
    }
}
