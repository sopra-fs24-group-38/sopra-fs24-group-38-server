package ch.uzh.ifi.hase.soprafs24.model.response;

import javax.persistence.Embeddable;

@Embeddable
public class Challenge {
    private String challenge;
    private String solution;

    public Challenge(String challenge, String solution) {
        this.challenge = challenge;
        this.solution = solution;
    }

    public Challenge() {

    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getChallenge() {
        return challenge;
    }

    public String getSolution() {
        return solution;
    }
}