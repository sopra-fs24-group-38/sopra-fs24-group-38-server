package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.model.database.Lobby;
import ch.uzh.ifi.hase.soprafs24.model.database.User;
import ch.uzh.ifi.hase.soprafs24.model.request.UserPost;
import ch.uzh.ifi.hase.soprafs24.model.response.LobbyGet;
import ch.uzh.ifi.hase.soprafs24.model.response.UserResponse;
import ch.uzh.ifi.hase.soprafs24.model.response.allUsersScores;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    LobbyService lobbyService;

    @DisplayName("UserService Test: addSessionToUser")
    @Test
    public void testAddSessionToUser() {

        // create a User
        UserPost userPost = new UserPost();
        userPost.setUsername("test");
        userPost.setPassword("pw");
        UserResponse userResponse = userService.createUser(userPost);
        String session = "sessionId";

        userService.addSessionToUser(session, userResponse.getId());

        User updatedUser = userService.getUserById(userResponse.getId());

        assertEquals(updatedUser.getSessionId(), session);
    }

    @DisplayName("UserService Test: getUserBySessionId")
    @Test
    public void testGetUserBySessionId() {

        //create a User
        UserPost userPost = new UserPost();
        userPost.setUsername("test1");
        userPost.setPassword("pw");
        UserResponse userResponse = userService.createUser(userPost);
        String session = "customSessionId";

        //add session to user
        userService.addSessionToUser(session, userResponse.getId());

        //get user and assert session is the same
        Long userId = userService.getUserIdBySessionId(session);
        assertEquals(userId, userResponse.getId());
    }

    @DisplayName("UserService Test: setIsConnected")
    @Test
    public void testSetIsConnected() {

        //create a User
        UserPost userPost = new UserPost();
        userPost.setUsername("test2");
        userPost.setPassword("pw");
        UserResponse userResponse = userService.createUser(userPost);
        Boolean connected = true;

        //make sure connected is initially null
        User user1 = userService.getUserById(userResponse.getId());
        assertNull(user1.getIsConnected());

        //set connected to true
        userService.setIsConnected(userResponse.getId(), connected);

        //make sure connected is true
        User user = userService.getUserById(userResponse.getId());
        assertEquals(connected, user.getConnected());
    }

    @DisplayName("UserService Test: logout")
    @Test
    public void testLogout() {

        //create a User
        UserPost userPost = new UserPost();
        userPost.setUsername("test3");
        userPost.setPassword("pw");
        UserResponse userResponse = userService.createUser(userPost);

        //create a Lobby and retrieve it
        Long lobbyPin = lobbyService.createLobby(userResponse.getId());

        //logout user
        userService.logout(userResponse.getToken());

        //make sure lobbyId is null
        assertNull(userService.getUserById(userResponse.getId()).getLobbyId());
    }

    @DisplayName("UserService Test: logout")
    @Test
    public void testDelete() {

        //create a User
        UserPost userPost = new UserPost();
        userPost.setUsername("test4");
        userPost.setPassword("pw");
        UserResponse userResponse = userService.createUser(userPost);

        //delete user
        userService.deleteUser(userResponse.getId());

        //make sure user does not exist anymore
        assertThrows(ResponseStatusException.class, () -> userService.getUserById(userResponse.getId()));
    }

    @DisplayName("UserService Test: getAllUsers")
    @Test
    public void testGetAllUsers() {

        //create a User
        UserPost userPost = new UserPost();
        userPost.setUsername("test5");
        userPost.setPassword("pw");
        UserResponse userResponse = userService.createUser(userPost);

        //getAllUsers
        List<allUsersScores> allUsers = userService.getAllUsers(userResponse.getToken());

        //make sure user is in the response
        boolean foundUser = false;
        for(allUsersScores allUser : allUsers) {
            if (Objects.equals(allUser.getUsername(), userPost.getUsername())) {
                foundUser = true;
                break;
            }
        }

        //make sure user was found
        assertTrue(foundUser);
    }
}