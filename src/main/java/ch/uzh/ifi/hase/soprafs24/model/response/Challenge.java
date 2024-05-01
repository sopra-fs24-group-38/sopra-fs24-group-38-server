package ch.uzh.ifi.hase.soprafs24.model.response;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;

import javax.persistence.Embeddable;

@Embeddable
public class Challenge {
    private String challenge;
    private String solution;
    private LobbyModes lobbyMode;

    public Challenge(String challenge, String solution, LobbyModes lobbyMode) {
        this.challenge = challenge;
        this.solution = solution;
        this.lobbyMode = lobbyMode;
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

    public LobbyModes getLobbyMode() {return lobbyMode;}
    public void setLobbyMode(LobbyModes lobbyMode) {this.lobbyMode = lobbyMode;}
}