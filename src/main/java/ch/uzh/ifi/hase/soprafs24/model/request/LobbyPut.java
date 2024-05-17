package ch.uzh.ifi.hase.soprafs24.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
public class LobbyPut {
    @JsonProperty("game_modes")
    private List<String> gameModes;
    @JsonProperty("rounds")
    int rounds;
    @JsonProperty("hide_mode")
    boolean hideMode;

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public List<String> getGameModes() {
        return gameModes;
    }

    public void setGameModes(List<String> gameModes) {
        this.gameModes = gameModes;
    }

    public Boolean getHideMode() {return hideMode;}

    public void setHideMode(Boolean hideMode) {this.hideMode = hideMode;}

}
