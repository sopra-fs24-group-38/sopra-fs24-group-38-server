package ch.uzh.ifi.hase.soprafs24.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents the JSON data which is returned in case of GET /lobbies call
 * It nests a GameDetail (also a class) JSON object which nests a (class) Player array
 */
public class LobbyGet {
    @JsonProperty("game_pin")
    private Long gamePin;

    @JsonProperty("game_details")
    private GameDetails gameDetails;


    public Long getGamePin() {
        return gamePin;
    }

    public void setGamePin(Long gamePin) {
        this.gamePin = gamePin;
    }

    public GameDetails getGameDetails() {
        return gameDetails;
    }

    public void setGameDetails(GameDetails gameDetails) {
        this.gameDetails = gameDetails;
    }
}
