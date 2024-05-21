package ch.uzh.ifi.hase.soprafs24.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Embeddable;

@Embeddable
public class GameStatsPlayer {
    @JsonProperty("avatarId")
    private Long avatarId;

    @JsonProperty("username")
    private String userName;
    @JsonProperty("score")
    private Long score;

    public Long getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(Long avatarId) {
        this.avatarId = avatarId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }
}
