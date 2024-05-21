package ch.uzh.ifi.hase.soprafs24.model.response;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class GameDetails {
    @JsonProperty("game_state")
    private String gameState;

    @JsonProperty("game_over")
    private boolean gameOver;

    private String challenge;

    private String solution;
    @JsonProperty("game_mode")
    private String gameMode;

    @JsonProperty("game_master_id")
    private Long gameMasterId;

    @JsonProperty("game_master_username")
    private String gameMasterUsername;

    private List<Player> players;

    @JsonProperty("round_number")
    private Long roundNumber;

    @JsonProperty("max_round_numbers")
    private int maxRoundNumbers;

    @JsonProperty("hide_mode")
    private Boolean hideMode;

    @JsonProperty("lobbymodes")
    private Set<LobbyModes> lobbyModes = new HashSet<>();

    @JsonProperty("static_stats")
    private List<GameStatsPlayer> statsPlayers;

    public List<GameStatsPlayer> getStatsPlayers() {
        return statsPlayers;
    }

    public void setStatsPlayers(List<GameStatsPlayer> statsPlayers) {
        this.statsPlayers = statsPlayers;
    }

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Long getGameMasterId() {
        return gameMasterId;
    }

    public void setGameMasterId(Long gameMasterId) {
        this.gameMasterId = gameMasterId;
    }


    public String getGameMasterUsername() {
        return gameMasterUsername;
    }

    public void setGameMasterUsername(String gameMasterUsername) {
        this.gameMasterUsername = gameMasterUsername;
    }

    public String getGameMode() {return gameMode;}

    public void setGameMode(String gameMode) {this.gameMode = gameMode;}

    public Long getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Long roundNumber) {
        this.roundNumber = roundNumber;
    }

    public int getMaxRoundNumbers() {
        return maxRoundNumbers;
    }

    public void setMaxRoundNumbers(int maxRoundNumbers) {
        this.maxRoundNumbers = maxRoundNumbers;
    }

    public Boolean getHideMode() {
        return hideMode;
    }

    public void setHideMode(Boolean hideMode) {
        this.hideMode = hideMode;
    }

    public Set<LobbyModes> getLobbyModes() {
        return lobbyModes;
    }

    public void setLobbyModes(Set<LobbyModes> lobbyModes) {
        this.lobbyModes = lobbyModes;
    }
}
