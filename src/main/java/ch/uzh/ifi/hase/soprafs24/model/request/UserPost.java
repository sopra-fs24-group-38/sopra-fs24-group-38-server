package ch.uzh.ifi.hase.soprafs24.model.request;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserPost {
    @JsonProperty("username")
    String username;

    @JsonProperty("password")
    Long password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getPassword() {
        return password;
    }

    public void setPassword(Long password) {
        this.password = password;
    }

}
