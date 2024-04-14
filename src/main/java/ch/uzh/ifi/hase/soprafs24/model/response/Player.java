package ch.uzh.ifi.hase.soprafs24.model.response;

public class Player {
    private String username;

    private int score;

    private String definition;

    private Long votedForUserId;

    private Long avatarId;

    private Long id;

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public Long getVotedForUserId() {
        return votedForUserId;
    }

    public void setVotedForUserId(Long votedForUserId) {
        this.votedForUserId = votedForUserId;
    }

    public Long getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(Long avatarId) {
        this.avatarId = avatarId;
    }
}
