package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;
import ch.uzh.ifi.hase.soprafs24.constant.LobbyState;
import ch.uzh.ifi.hase.soprafs24.model.database.Lobby;
import ch.uzh.ifi.hase.soprafs24.model.database.User;
import ch.uzh.ifi.hase.soprafs24.model.request.LobbyPut;
import ch.uzh.ifi.hase.soprafs24.model.request.UserPost;
import ch.uzh.ifi.hase.soprafs24.model.response.LobbyGet;
import ch.uzh.ifi.hase.soprafs24.model.response.UserResponse;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.websockets.SocketHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class LobbyServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    LobbyService lobbyService;

    @Autowired
    LobbyRepository lobbyRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SocketHandler socketHandler;



    /**
     * Creates a user, creates a lobby
     * checks if Lobby was created with initial values and user is part of the lobby
     */
    @DisplayName("LobbyService Test: createLobby")
    @Test
    public void createLobbyTest() {

        // create a User
        UserPost userPost = new UserPost();
        userPost.setUsername("lobbyTest");
        userPost.setPassword("pw");
        UserResponse userResponse = userService.createUser(userPost);

        // create a Lobby and retrieve it
        Long lobbyPin = lobbyService.createLobby(userResponse.getId());
        Lobby lobby = lobbyService.getLobbyAndExistenceCheck(lobbyPin);
        lobbyService.connectTestHomies(userResponse.getId());
        // assert that the lobby was created
        assertTrue(lobby.getLobbyPin() >= 1000 && lobby.getLobbyPin() <= 9999, "Lobby ID should be a four-digit number");
        assertEquals(10, lobby.getMaxRoundNumbers(), "Lobby should have 10 rounds by default");

        //get lobby content, retrieve user from the lobby and assert that he is in the lobby
        LobbyGet lobbyGet = lobbyService.getLobbyInfo(lobbyPin);
        assertEquals(lobbyGet.getGameDetails().getPlayers().get(0).getId(), userResponse.getId(), "Lobby should have user 1");
    }

    @DisplayName("LobbyService Test: testAddAiPlayer")
    @Test
    public void testAddAiPlayer() {
        // create a User
        UserPost userPost = new UserPost();
        userPost.setUsername("lobbyTest1");
        userPost.setPassword("pw");
        UserResponse userResponse = userService.createUser(userPost);

        // create a Lobby and retrieve it
        Long lobbyPin = lobbyService.createLobby(userResponse.getId());

        //add ai player
        lobbyService.addAiPlayerToLobby(lobbyPin);

        //assert that ai player has been added
        Lobby lobby = lobbyService.getLobbyAndExistenceCheck(lobbyPin);
        assertEquals(2 , lobby.getUsers().size());
    }

    @DisplayName("LobbyService Test: testRemoveWrongPlayer")
    @Test
    public void testRemoveWrongPlayer() {
        // create 2 User
        UserPost userPost = new UserPost();
        userPost.setUsername("lobbyTest2");
        userPost.setPassword("pw");
        UserResponse userResponse = userService.createUser(userPost);

        UserPost userPost1 = new UserPost();
        userPost1.setUsername("lobbyTest3");
        userPost1.setPassword("pw");
        UserResponse userResponse1 = userService.createUser(userPost1);

        // create a Lobby
        Long lobbyPin = lobbyService.createLobby(userResponse.getId());

        //add ai player
        lobbyService.addAiPlayerToLobby(lobbyPin);
        Lobby lobby = lobbyService.getLobbyAndExistenceCheck(lobbyPin);

        //join player 2
        lobbyService.addPlayerToLobby(userResponse1.getId(), lobbyPin);

        long userIdAI = lobby.getUsers().get(1).getId();

        //assert that removing ai player using removePlayerFromLobby method throws exception
        assertThrows(ResponseStatusException.class, () -> lobbyService.removePlayerFromLobby(userIdAI, lobbyPin));

        //assert that removing player not in lobby using removePlayerFromLobby method throws exception
        assertThrows(ResponseStatusException.class, () -> lobbyService.removePlayerFromLobby((long) -1, lobbyPin));

        //assert after removing gm a new gm is chosen
        lobbyService.removePlayerFromLobby(userResponse.getId(), lobbyPin);
        lobby = lobbyService.getLobbyAndExistenceCheck(lobbyPin);
        assertEquals(userResponse1.getId(), lobby.getGameMaster());
    }

    @DisplayName("LobbyService Test: testRegisterNextRound")
    @Test
    public void testRegisterNextRound() {
        // create a User
        UserPost userPost = new UserPost();
        userPost.setUsername("lobbyTest4");
        userPost.setPassword("pw");
        UserResponse userResponse = userService.createUser(userPost);

        // create a Lobby and retrieve it
        Long lobbyPin = lobbyService.createLobby(userResponse.getId());
        lobbyService.connectTestHomies(userResponse.getId());

        //add ai player
        lobbyService.addAiPlayerToLobby(lobbyPin);

        //start game
        lobbyService.startGame(userResponse.getId());

        //register next round
        lobbyService.registerNextRound(userResponse.getId());

        //assert that next round has been set (by making sure that lobby state has been reset to definition)
        Lobby lobby = lobbyService.getLobbyAndExistenceCheck(lobbyPin);
        assertEquals(LobbyState.DEFINITION, lobby.getLobbyState());
    }

    @DisplayName("LobbyService Test: testRemoveAiPlayer")
    @Test
    public void testRemoveAiPlayer() {
        // create a User
        UserPost userPost = new UserPost();
        userPost.setUsername("lobbyTest5");
        userPost.setPassword("pw");
        UserResponse userResponse = userService.createUser(userPost);

        // create a Lobby and retrieve it
        Long lobbyPin = lobbyService.createLobby(userResponse.getId());
        lobbyService.connectTestHomies(userResponse.getId());

        //add ai player
        lobbyService.addAiPlayerToLobby(lobbyPin);

        //get lobby
        Lobby lobby = lobbyService.getLobbyAndExistenceCheck(lobbyPin);
        Long avatarId = lobby.getUsers().get(1).getAvatarId();

        //remove ai player
        lobbyService.removeAiPlayer(lobbyPin,avatarId);

        //assert that ai player has been removed
        lobby = lobbyService.getLobbyAndExistenceCheck(lobbyPin);
        assertEquals(1, lobby.getUsers().size());
    }

    @DisplayName("LobbyService Test: testNewGameReset")
    @Test
    public void testNewGameReset() {
        // create a User
        UserPost userPost = new UserPost();
        userPost.setUsername("lobbyTest6");
        userPost.setPassword("pw");
        UserResponse userResponse = userService.createUser(userPost);

        // create a Lobby and retrieve it
        Long lobbyPin = lobbyService.createLobby(userResponse.getId());
        lobbyService.connectTestHomies(userResponse.getId());

        //add ai player
        lobbyService.addAiPlayerToLobby(lobbyPin);

        //start game
        lobbyService.startGame(userResponse.getId());

        //assert lobby state is definition
        Lobby lobby = lobbyService.getLobbyAndExistenceCheck(lobbyPin);
        assertEquals(LobbyState.DEFINITION, lobby.getLobbyState());

        //reset lobby
        lobbyService.newGameReset(userResponse.getId());

        //assert lobby state is waiting
        lobby = lobbyService.getLobbyAndExistenceCheck(lobbyPin);
        assertEquals(LobbyState.WAITING, lobby.getLobbyState());
    }
    @DisplayName("LobbyService Test: testRegisterNextRound")
    @Test
    public void testRegisterNextRoundLogic() {
        // Create a User
        UserPost userPost = new UserPost();
        userPost.setUsername("lobbyTest4");
        userPost.setPassword("pw");
        UserResponse userResponse = userService.createUser(userPost);

        Long lobbyPin = lobbyService.createLobby(userResponse.getId());
        lobbyService.connectTestHomies(userResponse.getId());

        UserPost userPost2 = new UserPost();
        userPost2.setUsername("lobbyTest5");
        userPost2.setPassword("pw");
        UserResponse userResponse2 = userService.createUser(userPost2);
        lobbyService.addPlayerToLobby(userResponse2.getId(), lobbyPin);

        lobbyService.registerNextRound(userResponse.getId());

        Lobby lobby = lobbyService.getLobbyAndExistenceCheck(lobbyPin);
        assertEquals(LobbyState.DEFINITION, lobby.getLobbyState());
    }

    @DisplayName("LobbyService Test: testRegisterNextRoundGetIsConnected")
    @Test
    public void testRegisterNextRoundGetIsConnected() {
        UserPost userPost = new UserPost();
        userPost.setUsername("lobbyTest4");
        userPost.setPassword("pw");
        UserResponse userResponse = userService.createUser(userPost);

        Long lobbyPin = lobbyService.createLobby(userResponse.getId());
        lobbyService.connectTestHomies(userResponse.getId());

        UserPost userPost2 = new UserPost();
        userPost2.setUsername("lobbyTest5");
        userPost2.setPassword("pw");
        UserResponse userResponse2 = userService.createUser(userPost2);
        lobbyService.addPlayerToLobby(userResponse2.getId(), lobbyPin);

        Lobby lobby = lobbyService.getLobbyAndExistenceCheck(lobbyPin);
        for (User user : lobby.getUsers()) {
            user.setWantsNextRound(true);
            user.setIsConnected(true);
        }

        lobbyService.registerNextRound(userResponse.getId());
        //indirectly tests logic whether disconnect and waiting does not hinder gameflow
        for (User user : lobby.getUsers()) {
            assertTrue(user.getWantsNextRound(), "User's wantsNextRound should be true");
        }
    }

    @DisplayName("LobbyService Test: testNewGameResetLogic")
    @Test
    public void testNewGameResetLogic() {
        UserPost userPost = new UserPost();
        userPost.setUsername("lobbyTest7");
        userPost.setPassword("pw");
        UserResponse userResponse = userService.createUser(userPost);
        Long lobbyPin = lobbyService.createLobby(userResponse.getId());
        lobbyService.connectTestHomies(userResponse.getId());
        UserPost userPost2 = new UserPost();
        userPost2.setUsername("lobbyTest8");
        userPost2.setPassword("pw");
        UserResponse userResponse2 = userService.createUser(userPost2);
        lobbyService.addPlayerToLobby(userResponse2.getId(), lobbyPin);
        lobbyService.newGameReset(userResponse.getId());
        Lobby lobby = lobbyService.getLobbyAndExistenceCheck(lobbyPin);
        assertEquals(LobbyState.WAITING, lobby.getLobbyState(), "Lobby state should be WAITING");
        assertTrue(lobby.getLobbyModes().contains(LobbyModes.BIZARRE), "Lobby modes should contain BIZARRE");
        assertEquals(10L, lobby.getRoundNumber(), "Round number should be 10");
    }

    @DisplayName("LobbyService Test: adjustSettings")
    @Test
    public void testAdjustSettings() {
        // Create a User
        UserPost userPost = new UserPost();
        userPost.setUsername("testAdjustSettings");
        userPost.setPassword("pw");
        UserResponse userResponse = userService.createUser(userPost);

        // Create a Lobby and retrieve it
        Long lobbyPin = lobbyService.createLobby(userResponse.getId());
        lobbyService.connectTestHomies(userResponse.getId());

        // Prepare settings to be registered
        LobbyPut settingsToBeRegistered = new LobbyPut();
        settingsToBeRegistered.setRounds(5);
        settingsToBeRegistered.setGameModes(Arrays.asList("BIZARRE", "PROGRAMMING"));
        settingsToBeRegistered.setHideMode(true);

        // Call adjustSettings
        lobbyService.adjustSettings(settingsToBeRegistered, lobbyPin);

        // Retrieve the lobby and verify settings
        Lobby lobby = lobbyService.getLobbyAndExistenceCheck(lobbyPin);
        assertEquals(5, lobby.getMaxRoundNumbers(), "Rounds should be updated to 5");
        assertTrue(lobby.getLobbyModes().containsAll(Arrays.asList(LobbyModes.BIZARRE, LobbyModes.PROGRAMMING)), "Lobby modes should contain BIZARRE and CLASSIC");
        assertTrue(lobby.getHideMode(), "Hide mode should be set to true");
    }
}