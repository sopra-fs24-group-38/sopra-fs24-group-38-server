package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.model.database.Lobby;
import ch.uzh.ifi.hase.soprafs24.model.database.User;
import ch.uzh.ifi.hase.soprafs24.model.request.DefinitionPost;
import ch.uzh.ifi.hase.soprafs24.model.request.UserPost;
import ch.uzh.ifi.hase.soprafs24.model.response.LobbyGet;
import ch.uzh.ifi.hase.soprafs24.model.response.UserResponse;
import ch.uzh.ifi.hase.soprafs24.model.response.allUsersScores;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.hibernate.Hibernate;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.*;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserServiceTest {

    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    UserService userService;

    @Autowired
    LobbyService lobbyService;
    @Autowired
    UserRepository userRepository;

    @DisplayName("UserService Test: addSessionToUser")
    @Test
    public void testAddSessionToUser() {

        // create a User
        UserPost userPost = new UserPost();
        userPost.setUsername("testAddSession");
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
        assertFalse(user1.getIsConnected());

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
        for (allUsersScores allUser : allUsers) {
            if (Objects.equals(allUser.getUsername(), userPost.getUsername())) {
                foundUser = true;
                break;
            }
        }

        //make sure user was found
        assertTrue(foundUser);
    }

    @DisplayName("UserService Test: Definition equal to solution")
    @Test
    public void testCorrectDefinition() {

        //create user 1 and get the token
        String uri = "http://localhost:" + port + "/users";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", "test7");
        requestBody.put("password", "pw");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
        JSONObject jsonObject = new JSONObject(response.getBody());
        String gameMasterToken = jsonObject.getString("token");
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        //create user 2 and get the token
        requestBody.put("username", "test8");
        requestBody.put("password", "pw");
        requestEntity = new HttpEntity<>(requestBody, headers);
        response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
        jsonObject = new JSONObject(response.getBody());
        String playerToken = jsonObject.getString("token");
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        //create lobby and save pin
        uri = "http://localhost:" + port + "/lobbies";
        headers.set("Authorization", gameMasterToken);
        response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
        jsonObject = new JSONObject(response.getBody());
        long lobbyId = jsonObject.getLong("game_pin");
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        //join player to lobby
        uri = "http://localhost:" + port + "/lobbies/users/" + String.valueOf(lobbyId);
        headers.set("Authorization", playerToken);
        response = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        //start lobby
        uri = "http://localhost:" + port + "/lobbies/start";
        headers.set("Authorization", gameMasterToken);
        response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        //get lobby
        uri = "http://localhost:" + port + "/lobbies/"+lobbyId;
        response = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);
        jsonObject = new JSONObject(response.getBody());
        String solution = jsonObject.getJSONObject("game_details").getString("solution");

        //try to submit definition that is solution and assert that it is rejected
        uri = "http://localhost:" + port + "/lobbies/users/definitions";
        headers.set("Authorization", playerToken);
        requestBody = new HashMap<>();
        requestBody.put("definition", solution);
        requestEntity = new HttpEntity<>(requestBody, headers);
        response = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }
    @DisplayName("UserService Test: registerDefinitions lowercase check")
    @Test
    public void testRegisterDefinitionsLowercase() {
        UserPost userPost = new UserPost();
        userPost.setUsername("testLowercase");
        userPost.setPassword("pw");
        UserResponse userResponse = userService.createUser(userPost);

        Long lobbyPin = lobbyService.createLobby(userResponse.getId());
        lobbyService.connectTestHomies(userResponse.getId());

        DefinitionPost definitionPost = new DefinitionPost();
        definitionPost.setDefinition("TestDefinition");

        userService.registerDefinitions(definitionPost, userResponse.getId());

        User user = userService.getUserById(userResponse.getId());
        assertEquals("testdefinition", user.getDefinition(), "Definition should be converted to lowercase");
    }

    @Test
    public void userStatsPersistet(){
        //Create user
        UserPost userPost = new UserPost();
        userPost.setUsername("testName");
        userPost.setPassword("pw");
        UserResponse userResponse = userService.createUser(userPost);
        User userEntityDb = userRepository.findByUsername("testName");
        userEntityDb.addPermanentScore(2L);
        userEntityDb.addPermanentFools(2L);
        userRepository.save(userEntityDb);
        userRepository.flush();
        User userEntityAfterStatUpdate = userRepository.findByUsername("testName");
        assertEquals(2, userEntityAfterStatUpdate.getPermanentScore(), "Score not persisted");
        assertEquals(2, userEntityAfterStatUpdate.getPermanentFools(), "Fools not persisted");

    }

}