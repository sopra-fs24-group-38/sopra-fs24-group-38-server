package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;
import ch.uzh.ifi.hase.soprafs24.constant.LobbyState;
import ch.uzh.ifi.hase.soprafs24.controller.LobbyController;
import ch.uzh.ifi.hase.soprafs24.model.database.Lobby;
import ch.uzh.ifi.hase.soprafs24.model.database.User;
import ch.uzh.ifi.hase.soprafs24.model.response.Challenge;
import ch.uzh.ifi.hase.soprafs24.model.response.LobbyGet;
import ch.uzh.ifi.hase.soprafs24.websockets.SocketHandler;
import org.junit.jupiter.api.Test;


import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;


import java.util.ArrayList;
import java.util.List;

@WebMvcTest(LobbyController.class)
public class ApiServiceTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @MockBean
    private LobbyService lobbyService;
    @MockBean
    private SocketHandler socketHandler;
    @MockBean
    private ApiService apiService;
    @Mock
    private RestTemplate restTemplate;


    @Test
    public void testJoinLobby() throws Exception {
        Long gamePin = 12345L;
        Long userId = 1L;
        String token = "Bearer your_auth_token";

        User user1 = new User();
        User user2 = new User();

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        given(userService.getUserIdByTokenAndAuthenticate(token)).willReturn(userId);
        doNothing().when(lobbyService).addPlayerToLobby(userId, gamePin);
        given(lobbyService.getUsers(gamePin)).willReturn(users);

        mockMvc.perform(put("/lobbies/users/{gamePin}", gamePin)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //Check whether the method performs the socket call to lobby

        verify(userService).getUserIdByTokenAndAuthenticate(token);
        verify(lobbyService).addPlayerToLobby(userId, gamePin);
        verify(socketHandler).sendMessageToLobby(gamePin, "user_joined");
    }

    @Test
    public void testStartGameLogic() throws Exception {
        //Issue 61 test
        Long gamePin = 1234L;
        String token = "Bearer your_auth_token";

        User user1 = new User();
        Long userId1 = 1L;
        user1.setId(userId1);

        User user2 = new User();
        Long userId2 = 2L;
        user2.setId(userId2);

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        Lobby lobby = new Lobby();
        lobby.setUsers(users);
        lobby.setGameMasterId(userId1);
        lobby.setLobbyPin(gamePin);
        lobby.getLobbyModes().add(LobbyModes.BIZARRE);

        ArrayList<Challenge> challenges = new ArrayList<>();
        Challenge challenge = new Challenge();
        challenges.add(challenge);
        challenge.setChallenge("dummyChallenge");
        challenge.setSolution("dummySolution");

        given(userService.getUserIdByTokenAndAuthenticate(token)).willReturn(userId1);
        given(userService.getUserById(userId1)).willReturn(user1);
        given(lobbyService.getLobbyAndExistenceCheck(anyLong())).willReturn(lobby);
        given(apiService.generateChallenges(anyInt(), anySet(), anyLong())).willReturn(challenges);
        doNothing().when(lobbyService).checkState(gamePin, LobbyState.WAITING);

        mockMvc.perform(post("/lobbies/start")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userService).getUserIdByTokenAndAuthenticate(token);
        verify(lobbyService).checkState(userId1, LobbyState.WAITING);
        verify(userService).getUserIdByTokenAndAuthenticate(token);
        verify(socketHandler).sendMessageToLobby(anyLong(), eq("game_start"));
    }

    @Test
    public void testFallbackJson() throws Exception {
        //Issue 61 test
        Long gamePin = 1244L;
        String token = "Bearer your_auth_token";

        User user1 = new User();
        Long userId1 = 1L;
        user1.setId(userId1);

        User user2 = new User();
        Long userId2 = 2L;
        user2.setId(userId2);

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        Lobby lobby = new Lobby();
        lobby.setUsers(users);
        lobby.setGameMasterId(userId1);
        lobby.setLobbyPin(gamePin);
        lobby.getLobbyModes().add(LobbyModes.BIZARRE);
        lobby.getLobbyModes().add(LobbyModes.RAREFOODS);

        lobbyService.startGame(userId1);

        given(userService.getUserIdByTokenAndAuthenticate(token)).willReturn(userId1);
        given(userService.getUserById(userId1)).willReturn(user1);
        given(lobbyService.getLobbyAndExistenceCheck(anyLong())).willReturn(lobby);

        doThrow(new RuntimeException("API failure")).when(restTemplate)
                .exchange(eq("https://api.openai.com/v1/chat/completions"), eq(HttpMethod.POST),
                        any(HttpEntity.class), eq(String.class));

        doNothing().when(lobbyService).checkState(gamePin, LobbyState.WAITING);


        mockMvc.perform(post("/lobbies/start")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userService).getUserIdByTokenAndAuthenticate(token);
        verify(lobbyService).checkState(userId1, LobbyState.WAITING);
        verify(userService).getUserIdByTokenAndAuthenticate(token);
        verify(socketHandler).sendMessageToLobby(anyLong(), eq("game_start"));
    }

}
