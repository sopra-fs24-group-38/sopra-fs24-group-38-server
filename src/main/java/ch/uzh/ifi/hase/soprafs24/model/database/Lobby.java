package ch.uzh.ifi.hase.soprafs24.model.database;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;
import ch.uzh.ifi.hase.soprafs24.constant.LobbyState;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "LOBBY")
public class Lobby {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    private Long lobbyPin;

    @Column
    private String challenge;

    @Column
    private LobbyState lobbyState;

    @Column
    private int numberRounds;

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    private Set<LobbyModes> lobbyModes = new HashSet<>();

    //FIXME map properly to the lobby? set a lobby in the user db model?
    @OneToMany
    private Set<User> players = new HashSet<>();


    public int getNumberRounds() {return numberRounds;}
    public void setNumberRounds(int numberRounds) {this.numberRounds = numberRounds;}
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public LobbyState getLobbyState() {
        return lobbyState;
    }

    public void setLobbyState(LobbyState lobbyState) {
        this.lobbyState = lobbyState;
    }

    public void addPlayer(User player) {
        players.add(player);
    }

    public Set<User> getPlayers() {
        return players;
    }

    public void removePlayer(User player) {
        players.remove(player);
    }

    public Long getLobbyPin() {
        return lobbyPin;
    }

    public void setLobbyPin(Long lobbyPin) {
        this.lobbyPin = lobbyPin;
    }
}
