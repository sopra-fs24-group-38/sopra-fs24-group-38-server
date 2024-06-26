package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;
import ch.uzh.ifi.hase.soprafs24.model.database.Lobby;
import ch.uzh.ifi.hase.soprafs24.model.database.User;
import ch.uzh.ifi.hase.soprafs24.model.response.Challenge;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.Test;
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
public class ApiServiceUnitTest {

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
    public void testRandomDistributionForAllModes() {
        // Arrange: Define game modes
        Set<LobbyModes> lobbyModes = new HashSet<>();
        lobbyModes.add(LobbyModes.PROGRAMMING);
        lobbyModes.add(LobbyModes.DUTCH);
        lobbyModes.add(LobbyModes.BIZARRE);
        lobbyModes.add(LobbyModes.RAREFOODS);

        Long lobbyId = 123L;
        int numberRounds = 7;

        List<Challenge> challenges = apiService.generateChallenges(numberRounds, lobbyModes, lobbyId);
        int numberProgrammingChallenges = 0;
        int numberDutchChallenges = 0;
        int numberBizarreChallenges = 0;
        int numberRareFoods = 0;
        for(Challenge challenge: challenges){
            if(challenge.getLobbyMode() == LobbyModes.DUTCH){
                numberDutchChallenges+= 1;
            }
            if(challenge.getLobbyMode() == LobbyModes.PROGRAMMING){
                numberProgrammingChallenges += 1;
            }
            if(challenge.getLobbyMode() == LobbyModes.BIZARRE){
                numberBizarreChallenges += 1;
            }
            if(challenge.getLobbyMode() == LobbyModes.RAREFOODS){
                numberRareFoods += 1;
            }
        }
        assertEquals(2, numberDutchChallenges);
        assertEquals(2, numberProgrammingChallenges);
        assertEquals(2, numberBizarreChallenges);
        assertEquals(2, numberRareFoods);
    }
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

    @Test
    public void testGenerateModeDistribution() {
        // Arrange: Define game modes
        Set<LobbyModes> lobbyModes = new HashSet<>();
        lobbyModes.add(LobbyModes.PROGRAMMING);
        lobbyModes.add(LobbyModes.DUTCH);

        Long lobbyId = 123L;
        int numberRounds = 3;

        List<Challenge> challenges = apiService.generateChallenges(numberRounds, lobbyModes, lobbyId);
        int numberProgrammingChallenges = 0;
        int numberDutchChallenges = 0;
        for(Challenge challenge: challenges){
            if(challenge.getLobbyMode() == LobbyModes.DUTCH){
                numberDutchChallenges+= 1;
            }
            if(challenge.getLobbyMode() == LobbyModes.PROGRAMMING){
                numberProgrammingChallenges += 1;
            }
        }
        assertEquals(2, numberDutchChallenges);
        assertEquals(2, numberProgrammingChallenges);
    }

    @Test
    public void aiPlayersDefinitionLowerCase(){
        Lobby lobby = new Lobby();
        User aiUser = new User();
        aiUser.setAiPlayer(true);
        lobby.setUsers(Collections.singletonList(aiUser));
        Challenge challenge = new Challenge();
        challenge.setChallenge("dummyChallenge");
        challenge.setSolution("dummySolution");
        challenge.setLobbyMode(LobbyModes.BIZARRE);
        lobby.setChallenges(Collections.singletonList(challenge));

        // Mock the faulty JSON response in order to test fallback logic
        String responseBodyContainingNotLowerCaseDefinition = "{"
                + "\"id\": \"chatcmpl-9RcAPPYfgEyuA4W0Jemd6pidyIHTQ\","
                + "\"object\": \"chat.completion\","
                + "\"created\": 1716368597,"
                + "\"model\": \"gpt-4-turbo-2024-04-09\","
                + "\"choices\": ["
                + "    {"
                + "        \"index\": 0,"
                + "        \"message\": {"
                + "            \"role\": \"assistant\","
                + "            \"content\": \"[\\n{\\\"definition\\\": \\\"Clever Dishonest Person\\\"},{\\\"definition\\\": \\\"Clever Dishonest Person\\\"},{\\\"definition\\\": \\\"Clever Dishonest Person\\\"}\\n]\""
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
        ).thenReturn(ResponseEntity.ok(responseBodyContainingNotLowerCaseDefinition));

        // Act
        apiService.generateAiPlayersDefinitions(lobby);
        boolean isLowercase = true;
        for (char c : aiUser.getDefinition().toCharArray()) {
            if (!Character.isLowerCase(c) && c != ' ') {  // Check for lowercase and space
                isLowercase = false;
                break;
            }
        }
        //assert whether the generateAIPlayersDefinition method turns the mocked gpt definition response into lowercase
        assertTrue(isLowercase, "The Ai generated Definition was uppercase");
    }
    @Test
    public void testGenerateUniqueChallenges() {
        int numberRounds = 5;
        Set<LobbyModes> lobbyModes = new HashSet<>();
        lobbyModes.add(LobbyModes.PROGRAMMING);
        lobbyModes.add(LobbyModes.RAREFOODS);

        Long lobbyId = 123L;
        List<Challenge> challenges = apiService.generateChallenges(numberRounds, lobbyModes, lobbyId);

        Set<Challenge> uniqueChallenges = new HashSet<>(challenges);
        assertEquals(challenges.size(), uniqueChallenges.size(), "Challenges should be unique");
    }


    @Test
    public void testGenerateAiPlayersDefinitionsWithFallback() {
        Lobby lobby = new Lobby();
        User aiUser = new User();
        aiUser.setAiPlayer(true);
        lobby.setUsers(Collections.singletonList(aiUser));
        Challenge challenge = new Challenge();
        challenge.setChallenge("dummyChallenge");
        challenge.setSolution("dummySolution");
        challenge.setLobbyMode(LobbyModes.BIZARRE);
        lobby.setChallenges(Collections.singletonList(challenge));

        // Act
        apiService.generateAiPlayersDefinitions(lobby);

        // Assert
        // Check if fallback logic applied by verifying AI player has definitions assigned
        assertNotNull(aiUser.getDefinition(), "AI user should have definitions assigned");
    }
}
