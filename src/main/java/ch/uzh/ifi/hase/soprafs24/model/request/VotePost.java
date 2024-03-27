package ch.uzh.ifi.hase.soprafs24.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VotePost {
    @JsonProperty("vote")
    int vote;

    public int getVote() {
        return this.vote;
    }

    public void setVote(int vote) {
        this.vote = vote;
    }
}
