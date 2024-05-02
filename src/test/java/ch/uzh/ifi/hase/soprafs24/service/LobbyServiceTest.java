package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.model.database.Lobby;
import ch.uzh.ifi.hase.soprafs24.model.request.UserPost;
import ch.uzh.ifi.hase.soprafs24.model.response.LobbyGet;
import ch.uzh.ifi.hase.soprafs24.model.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class LobbyServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    LobbyService lobbyService;


    /**
     * Creates a user, creates a lobby
     * checks if Lobby was created with initial values and user is part of the lobby
     */
    @DisplayName("Unit Test: Lobby")
    @Test
    public void createLobbyTest() {

        // create a User
        UserPost userPost = new UserPost();
        userPost.setUsername("test");
        userPost.setPassword("pw");
        UserResponse userResponse = userService.createUser(userPost);

        // create a Lobby and retrieve it
        Long lobbyPin = lobbyService.createLobby(userResponse.getId());
        Lobby lobby = lobbyService.getLobbyAndExistenceCheck(lobbyPin);
        lobbyService.connecTestHomies(userResponse.getId());
        // assert that the lobby was created
        assertTrue(lobby.getLobbyPin() >= 1000 && lobby.getLobbyPin() <= 9999, "Lobby ID should be a four-digit number");
        assertEquals(10, lobby.getMaxRoundNumbers(), "Lobby should have 10 rounds by default");

        //get lobby content, retrieve user from the lobby and assert that he is in the lobby
        LobbyGet lobbyGet = lobbyService.getLobbyInfo(lobbyPin);
        assertEquals(lobbyGet.getGameDetails().getPlayers().get(0).getId(), userResponse.getId(), "Lobby should have user 1");
    }

}