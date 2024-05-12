package ch.uzh.ifi.hase.soprafs24.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetUserStatusResponse {

    @JsonProperty("id")
    Long id;

    @JsonProperty("is_in_lobby")
    Boolean isInLobby;

    @JsonProperty("lobby_pin")
    Long lobbyPin;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getIsInLobby() {return isInLobby;}

    public void setIsInLobby(Boolean isInLobby) {this.isInLobby = isInLobby;}

    public Long getLobbyPin() {return lobbyPin;}

    public void setLobbyPin(Long lobbyPin) {this.lobbyPin = lobbyPin;}
}
