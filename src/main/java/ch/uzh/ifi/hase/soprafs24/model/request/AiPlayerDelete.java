package ch.uzh.ifi.hase.soprafs24.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AiPlayerDelete {
    @JsonProperty
    Long avatarId;


    public Long getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(Long avatarId) {
        this.avatarId = avatarId;
    }

}
