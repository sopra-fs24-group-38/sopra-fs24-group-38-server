package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;
import ch.uzh.ifi.hase.soprafs24.model.response.Challenge;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.websockets.SocketHandler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ApiServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    LobbyService lobbyService;

    @Autowired
    LobbyRepository lobbyRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ApiService apiService;
    @MockBean
    private RestTemplate restTemplate;

    @Test
    public void testGenerateChallenges() {
        int numberRounds = 5;
        Set<LobbyModes> lobbyModes = new HashSet<>();
        lobbyModes.add(LobbyModes.PROGRAMMING);
        lobbyModes.add(LobbyModes.RAREFOODS);

        Long lobbyId = 123L;
        String jsonOutput = "{"
                + "\"id\": \"chatcmpl-9RcAPPYfgEyuA4W0Jemd6pidyIHTQ\","
                + "\"object\": \"chat.completion\","
                + "\"created\": 1716368597,"
                + "\"model\": \"gpt-4-turbo-2024-04-09\","
                + "\"choices\": ["
                + "    {"
                + "        \"index\": 0,"
                + "        \"message\": {"
                + "            \"role\": \"assistant\","
                + "            \"content\": \"[\\n{\\\"value\\\": \\\"Snollygoster\\\", \\\"definition\\\": \\\"Clever dishonest person\\\"},\\n{\\\"value\\\": \\\"Bumfuzzle\\\", \\\"definition\\\": \\\"Confuse greatly\\\"},...{\\\"value\\\": \\\"Agelast\\\", \\\"definition\\\": \\\"Never laughs\\\"}\\n]\""
                + "        },"
                + "        \"logprobs\": null,"
                + "        \"finish_reason\": \"stop\""
                + "    }"
                + "],"
                + "\"system_fingerprint\": \"fp_e9446dc58f\""
                + "}";
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class))
        ).thenReturn(ResponseEntity.ok(jsonOutput));
        List<Challenge> challenges = apiService.generateChallenges(numberRounds, lobbyModes, lobbyId);
        assertEquals(6, challenges.size(), "Should return 6 challenges based on the mode settings");

    }
    @Test
    public void testGenerateChallengesForModes() {
        // Arrange: Define game modes
        Set<LobbyModes> lobbyModes = new HashSet<>();
        lobbyModes.add(LobbyModes.PROGRAMMING);
        lobbyModes.add(LobbyModes.DUTCH);

        Long lobbyId = 123L;
        int numberRounds = 3;

        List<Challenge> challenges = apiService.generateChallenges(numberRounds, lobbyModes, lobbyId);
        int numberWithFallBackChallenge = numberRounds + 1;
        assertEquals(numberWithFallBackChallenge, challenges.size(), "Should generate exactly the number of rounds requested");
    }

}
