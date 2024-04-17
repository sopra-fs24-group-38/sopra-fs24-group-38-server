package ch.uzh.ifi.hase.soprafs24.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VotePost {
    @JsonProperty("vote")
    Long vote;

    public Long getVote() {
        return this.vote;
    }

    public void setVote(Long vote) {
        this.vote = vote;
    }
}
