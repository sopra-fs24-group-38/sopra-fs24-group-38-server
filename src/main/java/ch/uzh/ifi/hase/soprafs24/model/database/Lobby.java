package ch.uzh.ifi.hase.soprafs24.model.database;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyState;

import javax.persistence.*;

@Entity
@Table(name = "LOBBY")
public class Lobby {
    private static final long serialVersionUID = 1L;

    //Also the gamepin:
    @Id
    @GeneratedValue
    private Long id;
    @Column
    private String challenge;

    @Column
    private LobbyState lobbyState;




}
