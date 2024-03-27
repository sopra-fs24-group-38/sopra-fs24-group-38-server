package ch.uzh.ifi.hase.soprafs24.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This JSON is returned upon creation of a lobby or after a User joins  a lobby
 * via the endpoints POST /lobbies and PUT /lobbies/users
 */
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
