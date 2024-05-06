package ch.uzh.ifi.hase.soprafs24.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class allUsersScores {
    @JsonProperty("username")
    String username;

    @JsonProperty("permanentScore")
    Long permanentScore;

    @JsonProperty("permanentFools")
    Long permanentFools;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getPermanentScore() {
        return permanentScore;
    }

    public void setPermanentScore(Long permanentScore) {
        this.permanentScore = permanentScore;
    }

    public Long getPermanentFools() {
        return permanentFools;
    }

    public void setPermanentFools(Long permanentFools) {
        this.permanentFools = permanentFools;
    }
}
