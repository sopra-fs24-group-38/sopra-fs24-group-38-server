package ch.uzh.ifi.hase.soprafs24.model.database;

import javax.persistence.*;
import java.io.Serializable;


@Entity
@Table(name = "USER")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String token;

    @Column
    private Long lobbyId;

    @Column
    private String sessionId;

    @Column
    private Boolean isConnected = false;

    @Column
    private String definition;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {return password;}

    public void setPassword(String password) {this.password = password;}
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(Long gamePin) {
        this.lobbyId = gamePin;
    }

    public void setIsConnected (Boolean isConnected) {
        this.isConnected = isConnected;
    }

    public Boolean getIsConnected () {
        return this.isConnected;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }
}
