package ch.uzh.ifi.hase.soprafs24.model.database;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;
import ch.uzh.ifi.hase.soprafs24.constant.LobbyState;
import ch.uzh.ifi.hase.soprafs24.model.response.Challenge;
import ch.uzh.ifi.hase.soprafs24.model.response.GameStatsPlayer;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "LOBBY")
public class Lobby {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    private Long lobbyPin;

    @Column
    private LobbyState lobbyState;

    @Column
    private int maxRoundNumbers = 10;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    //Definition is the default mode :
    private Set<LobbyModes> lobbyModes = new HashSet<>(Arrays.asList(LobbyModes.BIZARRE));


    @OneToMany(fetch = FetchType.EAGER)
    private List<User> users = new ArrayList<>();

    @Column
    private Long gameMaster;

    @Column
    private boolean gameOver = false;

    @ElementCollection(fetch = FetchType.LAZY)
    @Column
    private List<Challenge> challenges = new ArrayList<>();

    @Column
    private Long roundNumber;

    @Column
    private boolean hideMode = false;
    @ElementCollection(fetch = FetchType.LAZY)
    @Column
    private List<GameStatsPlayer> stats =  new ArrayList<>();

    public List<GameStatsPlayer> getStats() {
        return stats;
    }

    public void setStats(List<GameStatsPlayer> stats) {
        this.stats = stats;
    }

    public Set<LobbyModes> getLobbyModes() {
        return lobbyModes;
    }

    public void setLobbyModes(Set<LobbyModes> lobbyModes) {
        this.lobbyModes = lobbyModes;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public int getMaxRoundNumbers() {return maxRoundNumbers;}

    public void setMaxRoundNumbers(int numberRounds) {this.maxRoundNumbers = numberRounds;}

    public LobbyState getLobbyState() {
        return lobbyState;
    }

    public void setLobbyState(LobbyState lobbyState) {
        this.lobbyState = lobbyState;
    }

    public void addPlayer(User player) {
        users.add(player);
    }

    public List<User> getUsers() {
        return users;
    }

    public void removePlayer(User player) {
        users.remove(player);
    }

    public Long getLobbyPin() {
        return lobbyPin;
    }

    public void setLobbyPin(Long lobbyPin) {
        this.lobbyPin = lobbyPin;
    }

    public Long getGameMaster() {
        return gameMaster;
    }

    public void setGameMasterId(Long gameMasterId) {
        this.gameMaster = gameMasterId;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public Long getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Long roundNumber) {
        this.roundNumber = roundNumber;
    }

    public List<Challenge> getChallenges() {
        return challenges;
    }

    public void setChallenges(List<Challenge> challenges) {
        this.challenges = challenges;
    }

    public String getCurrentChallenge() {
        if (roundNumber >= 0 && roundNumber < challenges.size()) {
            return challenges.get(roundNumber.intValue()-1).getChallenge();
        }
        else return null;
    }

    public String getCurrentSolution() {
        if (roundNumber >= 0 && roundNumber < challenges.size()) {
            return challenges.get(roundNumber.intValue()-1).getSolution();
        }
        else return null;
    }

    public String getCurrentMode() {
        if (roundNumber >= 0 && roundNumber < challenges.size()) {
            return challenges.get(roundNumber.intValue()-1).getLobbyMode().toString();
        }
        else return null;
    }

    public boolean getHideMode() {
        return hideMode;
    }

    public void setHideMode(boolean hideMode) {
        this.hideMode = hideMode;
    }
}
