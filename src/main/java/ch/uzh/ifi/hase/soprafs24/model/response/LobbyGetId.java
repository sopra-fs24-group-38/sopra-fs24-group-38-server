package ch.uzh.ifi.hase.soprafs24.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LobbyGetId {
    @JsonProperty("game_pin")
    Long gamePin;

    public Long getGamePin() {
        return gamePin;
    }

    public void setGamePin(Long gamePin) {
        this.gamePin = gamePin;
    }
}
