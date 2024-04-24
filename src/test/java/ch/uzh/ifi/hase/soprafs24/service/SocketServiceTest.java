package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.controller.LobbyController;
import ch.uzh.ifi.hase.soprafs24.model.database.User;
import ch.uzh.ifi.hase.soprafs24.websockets.SocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.mockito.MockitoAnnotations;


import java.util.ArrayList;
import java.util.List;

@WebMvcTest(LobbyController.class)
public class SocketServiceTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private LobbyService lobbyService;

    @MockBean
    private SocketHandler socketHandler;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

    }

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
}
