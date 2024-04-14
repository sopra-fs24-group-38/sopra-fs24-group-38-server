package ch.uzh.ifi.hase.soprafs24.model.database;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;
import ch.uzh.ifi.hase.soprafs24.constant.LobbyState;
import ch.uzh.ifi.hase.soprafs24.model.response.Challenge;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
    private int numberRounds = 10;

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    //Definition is the default mode :
    private Set<LobbyModes> lobbyModes = new HashSet<>(Arrays.asList(LobbyModes.DEFINITIONS));

    //FIXME map properly to the lobby? set a lobby in the user db model?
    @OneToMany
    private List<User> users = new ArrayList<>();

    @Column
    private Long gameMaster;

    @Column
    private Enum<LobbyState> state;

    @Column
    private boolean gameOver;


    @ElementCollection(fetch = FetchType.LAZY)
    @Column
    private List<Challenge> challenges = new ArrayList<>();

    @Column
    private Long roundNumber;

    public Set<LobbyModes> getLobbyModes() {
        return lobbyModes;
    }

    public void setLobbyModes(Set<LobbyModes> lobbyModes) {
        this.lobbyModes = lobbyModes;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public int getNumberRounds() {return numberRounds;}
    public void setNumberRounds(int numberRounds) {this.numberRounds = numberRounds;}
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public void setGameMaster(Long gameMaster) {
        this.gameMaster = gameMaster;
    }

    public Enum<LobbyState> getState() {
        return state;
    }

    public void setState(Enum<LobbyState> state) {
        this.state = state;
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
            return challenges.get((roundNumber.intValue())).getChallenge();
        }
        else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RoundNumber out of bounds");
    }

    public String getCurrentSolution() {
        if (roundNumber >= 0 && roundNumber < challenges.size()) {
            return challenges.get((roundNumber.intValue())).getSolution();
        }
        else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RoundNumber out of bounds");
    }

}
