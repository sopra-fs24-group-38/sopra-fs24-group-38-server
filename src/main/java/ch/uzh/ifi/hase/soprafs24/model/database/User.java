package ch.uzh.ifi.hase.soprafs24.model.database;

import ch.uzh.ifi.hase.soprafs24.model.response.Challenge;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


@Entity
@Table(name = "USER")
public class User implements Serializable {


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
    private Boolean isConnected;

    @Column
    private String definition;

    @Column
    private Long avatarId;

    @Column
    private Long votedForUserId = null;

    @Column
    private Long score = 0L;

    @Column
    private Boolean wantsNextRound = false;

    @Column
    private Boolean isAiPlayer = false;

    @ElementCollection(fetch = FetchType.LAZY)
    @Column
    private List<String> aiDefinitions = new ArrayList<>();
    @Column
    private Long permanentScore = 0L;
    @Column
    private Long permanentFools = 0L;


    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

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

    public Long getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(Long avatarId) {
        this.avatarId = avatarId;
    }

    public Long getVotedForUserId() {
        return votedForUserId;
    }

    public void setVotedForUserId(Long votedForUserId) {
        this.votedForUserId = votedForUserId;
    }

    public Boolean getConnected() {
        return isConnected;
    }

    public void setConnected(Boolean connected) {
        isConnected = connected;
    }

    public Boolean getWantsNextRound() {
        return wantsNextRound;
    }

    public void setWantsNextRound(Boolean wantsNextRound) {
        this.wantsNextRound = wantsNextRound;
    }


    public Boolean getAiPlayer() {return isAiPlayer;}

    public void setAiPlayer(Boolean aiPlayer) {isAiPlayer = aiPlayer;}

    public String dequeueAiDefinition() {
        if (!aiDefinitions.isEmpty()) {
            return aiDefinitions.remove(0);
        }
        return null;
    }
    public void setAiDefinitions(List<String> aiDefinitions) {this.aiDefinitions = aiDefinitions;}

    public Long getPermanentFools() {
        return permanentFools;
    }

    public void addPermanentFools(Long fools) {
        this.permanentFools += fools;
    }

    public Long getPermanentScore() {
        return permanentScore;
    }

    public void addPermanentScore(Long score) {
        this.permanentScore += score;
    }

}
