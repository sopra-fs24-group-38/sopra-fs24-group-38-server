package ch.uzh.ifi.hase.soprafs24.model.response;

public class Challenge {
    private String challenge;
    private String solution;

    public Challenge(String challenge, String solution) {
        this.challenge = challenge;
        this.solution = solution;
    }

    public String getChallenge() {
        return challenge;
    }

    public String getSolution() {
        return solution;
    }
}