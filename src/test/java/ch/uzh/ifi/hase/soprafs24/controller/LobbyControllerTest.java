package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LobbyControllerTest {
    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @DisplayName("LobbyControllerTest: testMultipleLobbies")
    @Test
    public void testMultipleLobbies() {
        /**
         * The backend can handle multiple lobbies at the same time
         * #47
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user1", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());

        ResponseEntity<String> response2 = createUserWithSuccessAssertion("user2", "password");
        String tokenGameMaster2 = extractTokenFromResponse(response2.getBody());

        createLobbyWithSuccessCheck(tokenGameMaster1);
        ResponseEntity<String> createLobbyResponse2 = createLobbyWithSuccessCheck(tokenGameMaster2);

        assertEquals(HttpStatus.CREATED, createLobbyResponse2.getStatusCode());

    }

    @DisplayName("LobbyControllerTest: testLobbyModifications")
    @Test
    public void testLobbyModifications() {
        /**
         * The backend manages the lobby entities to allow for modifications of the lobby
         * #45
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user3", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());
        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());

        //Prepare Modes:
        ArrayList<LobbyModes> modes = new ArrayList<>();
        modes.add(LobbyModes.BIZARRE);
        modes.add(LobbyModes.DUTCH);

        //Perform adjustments
        ResponseEntity<String> responseLobbyAdjustments = adjustRoundLengthAndModes(lobbyId, 5L, modes, tokenGameMaster1);

        //and expect 200:
        assertEquals(HttpStatus.OK, responseLobbyAdjustments.getStatusCode());
    }

    @DisplayName("LobbyControllerTest: testCreateLobby")
    @Test
    public void testCreateLobby() {
        /**
         * The backend provides dedicated endpoints for creating and adjusting lobbies
         * #44
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user4", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());

        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());

        //Prepare Modes:
        ArrayList<LobbyModes> modes = new ArrayList<>();
        modes.add(LobbyModes.DUTCH);

        //Perform adjustments modes / rounds
        ResponseEntity<String> responseLobbyAdjustments = adjustRoundLengthAndModes(lobbyId, 5L, modes, tokenGameMaster1);
        //and expect 200:
        assertEquals(HttpStatus.OK, responseLobbyAdjustments.getStatusCode());

        //Add player to lobby :
        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user5", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
        ResponseEntity<String> responsePlayerJoin = joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId);

        //and expect 200:
        assertEquals(HttpStatus.OK, responsePlayerJoin.getStatusCode());
    }



    @DisplayName("LobbyControllerTest: testGamePin")
    @Test
    public void testGamePin() {
        /**
         * The backend generates a game pin when the user creates a lobby
         * #40
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user6", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());

        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());
        assertNotNull(lobbyId);
    }

    @DisplayName("LobbyControllerTest: testPlayerInformation")
    @Test
    public void testPlayerInformation() {
        /**
         * The backend provides information about the player in the lobby in order to render their details in the lobby.
         * #172
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user7", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());

        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user8", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
        joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId);

        connectAllPlayer(tokenGameMaster1);

        checkIfLobbyJoinWorked(lobbyId, tokenGameMaster1, "user7", "user8");
    }

    @DisplayName("LobbyControllerTest: testJoinPlayer")
    @Test
    public void testJoinPlayer() {
        /**
         The backend adds the user to the lobby entity he wants to join
         #54
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user9", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());

        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user10", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
        joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId);
        connectAllPlayer(tokenGameMaster1);

        checkIfLobbyJoinWorked(lobbyId, tokenGameMaster1, "user9", "user10");
    }

    @DisplayName("LobbyControllerTest: testBlockJoinLobby")
    @Test
    public void testBlockJoinLobby() {
        /**
         The backend blocks the user from joining a lobby if he is in a lobby already
         #53
         */

        //Create two lobbies
        ResponseEntity<String> responseGM1 = createUserWithSuccessAssertion("user11", "password");
        String tokenGameMaster1 = extractTokenFromResponse(responseGM1.getBody());

        ResponseEntity<String> responseGM2 = createUserWithSuccessAssertion("user12", "password");
        String tokenGameMaster2 = extractTokenFromResponse(responseGM2.getBody());


        ResponseEntity<String> responseLobbyCreation1 = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId1 = extractLobbyId(responseLobbyCreation1.getBody());

        ResponseEntity<String> responseLobbyCreation2 = createLobbyWithSuccessCheck(tokenGameMaster2);
        Long lobbyId2 = extractLobbyId(responseLobbyCreation2.getBody());

        //Join Player in lobby1

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user13", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
        joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId1);

        //Join same Player in lobby2 and expect error
        ResponseEntity<String> responseSecondJoin = joinPlayerToLobby(tokenJoinPlayer, lobbyId2);

        assertEquals(HttpStatus.BAD_REQUEST, responseSecondJoin.getStatusCode());

    }

    @DisplayName("LobbyControllerTest: testMinAndMaxPlayers")
    @Test
    public void testMinAndMaxPlayers() {
        /**
         The backend recognizes when the lobby is full and prohibits further player from joining the lobby (2-6)
         #62
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user14", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());

        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());
        assertNotNull(lobbyId);

        for(int i = 15; i< 20; i++) {
            ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user" + i, "password");
            String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
            joinPlayerToLobby(tokenJoinPlayer, lobbyId);
        }

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user" + 20, "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
        ResponseEntity<String> responseJoin = joinPlayerToLobby(tokenJoinPlayer, lobbyId);
        assertEquals(HttpStatus.BAD_REQUEST, responseJoin.getStatusCode());
        assertTrue(responseJoin.toString().contains("Lobby full"));
    }

    @DisplayName("LobbyControllerTest: testEvaluatePoints")
    @Test
    public void testEvaluatePoints() {
        /**
         * The backend evaluates how many points each player scored with their vote
         * #81
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user21", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());

        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());
        assertNotNull(lobbyId);

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user22", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
        joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId);

        connectAllPlayer(tokenGameMaster1);
        startLobby(tokenGameMaster1);

        registerDefinition(tokenGameMaster1, "placeboDefintion1");
        registerDefinition(tokenJoinPlayer, "placeboDefinition2");


    }

    @DisplayName("LobbyControllerTest: testRemoveUser")
    @Test
    public void testRemoveUser() {
        /**
         The backend provides an endpoint which removes the user from lobby and removes the current token from the user entity
         #58
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user23", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());

        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());
        assertNotNull(lobbyId);

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user24", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
        joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId);

        ResponseEntity<String> responseLobbyLeave = removePlayer(tokenJoinPlayer, lobbyId);
        assertEquals(HttpStatus.OK, responseLobbyLeave.getStatusCode());

        ResponseEntity<String> logoutPlayer = logoutPlayer(tokenJoinPlayer);
        assertEquals(HttpStatus.OK, logoutPlayer.getStatusCode());

    }


    @DisplayName("LobbyControllerTest: testStartGame")
    @Test
    public void testStartGame() {
        /**
         The backend provides a way to start the game.
         #177
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user25", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());

        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());
        assertNotNull(lobbyId);

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user26", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
        joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId);

        ResponseEntity<String> responseLobbyStart = startLobby(tokenGameMaster1);
        assertEquals(HttpStatus.OK, responseLobbyStart.getStatusCode());
    }
    @DisplayName("LobbyControllerTest: testReceiveChallengesApi")
    @Test
    public void testReceiveChallengesApi() {
        /**
         The backend provides a way to start the game.
         #71 The backend receives the answers to the current challange via an api endpoint and stores them in the lobby entity
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user27", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());

        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());
        assertNotNull(lobbyId);

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user28", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
        joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId);

        startLobby(tokenGameMaster1);

        ResponseEntity<String> response1 = registerDefinition(tokenGameMaster1, "DummyAnswer");
        assertEquals(HttpStatus.OK, response1.getStatusCode());

    }

    @DisplayName("LobbyControllerTest: testSendChallenge")
    @Test
    public void testSendChallenge() {
        /**
         The backend provides the current challange to the frontend via an api endpoint #70
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user2433", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());

        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());
        assertNotNull(lobbyId);

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user3450", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
        joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId);
        connectAllPlayer(tokenGameMaster1);
        startLobby(tokenGameMaster1);

        ResponseEntity<String> response1 = getLobby(tokenGameMaster1, lobbyId);
        JsonNode responseBody = responseToJsonNode(response1);

        //assert that the challenge field is not null and also that the API request worked
        assertNotNull(responseBody.get("game_details").get("challenge").toString());
    }

    @DisplayName("LobbyControllerTest: testReceiveVotes")
    @Test
    public void testReceiveVotes() {
        /**
         The backend is able to receive the votes which the players submitted #80
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user3861", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());

        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());
        assertNotNull(lobbyId);

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user3286", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
        joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId);

        startLobby(tokenGameMaster1);
        connectAllPlayer(tokenGameMaster1);

        registerDefinition(tokenGameMaster1, "placeboDefintion1");
        registerDefinition(tokenJoinPlayer, "placeboDefintion2");

        JsonNode responseBodyCreationPlayer1 = responseToJsonNode(response);
        Long userId = responseBodyCreationPlayer1.get("id").asLong();

        ResponseEntity<String> castedVoteResponse = castVote(userId, tokenGameMaster1);

        assertEquals(HttpStatus.OK, castedVoteResponse.getStatusCode());

    }

    @DisplayName("LobbyControllerTest: testStoreNumbers")
    @Test
    public void testStoreNumbers() {
        /**
         The backend stores the number of points each player scored
         #92
         */

        ResponseEntity<String> response = createUserWithSuccessAssertion("user7533", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());

        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());
        Long gameMasterUserId = responseToJsonNode(response).get("id").asLong();

        assertNotNull(lobbyId);

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user6475", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());

        joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId);
        connectAllPlayer(tokenGameMaster1);

        startLobby(tokenGameMaster1);

        registerDefinition(tokenGameMaster1, "dummyDefinitionGM");
        registerDefinition(tokenJoinPlayer, "dummyDefinitionPlayer2");

        //Game master casts the real definition
        //the second player (associated with token 'tokenjoinPlayer') gets fooled by GM
        castVote(0L, tokenGameMaster1);
        castVote(gameMasterUserId, tokenJoinPlayer);

        //which should result in the game master receiving 3 points and the second player zero:
        JsonNode results = responseToJsonNode(getLobby(tokenGameMaster1, lobbyId));

        JsonNode players = results.path("game_details").path("players");
        assertTrue(players.isArray(), "Players should be an array");

        for (JsonNode player : players) {
            String username = player.path("username").asText();
            int score = player.path("score").asInt();

            if (username.equals("user33")) {
                assertEquals(3, score, "User32 should have a score of 3");
            } else if (username.equals("user64")) {
                assertEquals(0, score, "User33 should have a score of 0");
            }
        }

    }

    @DisplayName("LobbyControllerTest: testBackendStates")
    @Test
    public void testBackendStates() {
        /**
         The backend tells frontend when its voting time / all votings cast / and whether or not game over
         Issues #72, #82 & #91
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user3864", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());
        Long gameMasterUserId = responseToJsonNode(response).get("id").asLong();


        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());
        assertNotNull(lobbyId);

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user4635", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
        joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId);
        connectAllPlayer(tokenGameMaster1);

        startLobby(tokenGameMaster1);

        registerDefinition(tokenGameMaster1, "dummyDefinitionGM");
        registerDefinition(tokenJoinPlayer, "dummyDefinitionPlayer2");


        //Test that vote indication works issue #72
        ResponseEntity<String> responseLobbyGet = getLobby(tokenGameMaster1, lobbyId);
        JsonNode lobbyInfo = responseToJsonNode(responseLobbyGet);
        String gameState = lobbyInfo.get("game_details").get("game_state").asText();
        assertEquals("VOTE", gameState);

        //perform all votes
        castVote(0L, tokenGameMaster1);
        castVote(gameMasterUserId, tokenJoinPlayer);

        //Test that all votes cast indication works issue #82
        responseLobbyGet = getLobby(tokenGameMaster1, lobbyId);
        JsonNode lobbyInfoEval = responseToJsonNode(responseLobbyGet);
        String gameStateEval = lobbyInfoEval.get("game_details").get("game_state").asText();
        assertEquals("EVALUATION", gameStateEval);

        //Test that game not over yet issue #91
        String gameOver = lobbyInfoEval.get("game_details").get("game_over").asText();
        assertEquals("false", gameOver);
    }

    @DisplayName("LobbyControllerTest: testJoinAiLobby")
    @Test
    public void testJoinAiLobby() {
        /**
         The backend is able to add ai players to the lobby
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user318611", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());

        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());
        assertNotNull(lobbyId);

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user3872", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
        joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId);

        String uri = "http://localhost:" + port + "/lobbies/users/" + lobbyId + "/ai";
        HttpHeaders headers = prepareHeader(tokenGameMaster1);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> responseJoinAi = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);
        assertEquals(HttpStatus.OK, responseJoinAi.getStatusCode());
    }

    @DisplayName("LobbyControllerTest: testDeleteAiLobby")
    @Test
    public void testDeleteAiLobby() {
        /**
         The backend is able to delete ai players from the lobby
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user31181g", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());

        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());
        assertNotNull(lobbyId);

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user238732", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
        joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId);
        String uri = "http://localhost:" + port + "/lobbies/users/" + lobbyId + "/ai";
        HttpHeaders headers = prepareHeader(tokenGameMaster1);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(headers);
        restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);

        ResponseEntity<String> getLobby = getLobby(tokenGameMaster1, lobbyId);


        JSONObject lobbyObject = new JSONObject(getLobby.getBody());
        JSONArray array = lobbyObject.getJSONObject("game_details").getJSONArray("players");
        Long avatarId = array.getJSONObject(0).getLong("avatarId");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("avatarId", avatarId);
        requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> responseDeleteAi = restTemplate.exchange(uri, HttpMethod.DELETE, requestEntity, String.class);
        assertEquals(HttpStatus.OK, responseDeleteAi.getStatusCode());
    }

    @DisplayName("LobbyControllerTest: testMultipleAiPlayers")
    @Test
    public void testMultipleAiPlayers() {
        /**
         The backend is able to handle many ai players
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user318612312311", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());

        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());
        assertNotNull(lobbyId);

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user381231231372", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
        joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId);

        String uri = "http://localhost:" + port + "/lobbies/users/" + lobbyId + "/ai";
        HttpHeaders headers = prepareHeader(tokenGameMaster1);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> responseJoinAi = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);
        ResponseEntity<String> responseJoinAi1 = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);
        ResponseEntity<String> responseJoinAi2 = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);
        assertEquals(HttpStatus.OK, responseJoinAi.getStatusCode());
        assertEquals(HttpStatus.OK, responseJoinAi1.getStatusCode());
        assertEquals(HttpStatus.OK, responseJoinAi2.getStatusCode());
    }

    @DisplayName("LobbyControllerTest: testNextRound")
    @Test
    public void testNextRound() {
        /**
         The backend is able to start the next round
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user312271", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());
        Long gameMasterUserId = responseToJsonNode(response).get("id").asLong();

        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());
        assertNotNull(lobbyId);

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user322", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
        joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId);

        startLobby(tokenGameMaster1);
        connectAllPlayer(tokenGameMaster1);

        registerDefinition(tokenGameMaster1, "dummyDefinitionGM");
        registerDefinition(tokenJoinPlayer, "dummyDefinitionPlayer2");

        castVote(0L, tokenGameMaster1);
        castVote(gameMasterUserId, tokenJoinPlayer);

        String uri = "http://localhost:" + port + "/lobbies/rounds/start";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", tokenGameMaster1);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> responseGM = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
        assertEquals(HttpStatus.OK, responseGM.getStatusCode());

        headers.set("Authorization", tokenJoinPlayer);
        requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> responsePlayer = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
        assertEquals(HttpStatus.OK, responsePlayer.getStatusCode());

    }

    @DisplayName("LobbyControllerTest: testNewGame")
    @Test
    public void testNewGame() {
        /**
         The backend is able to start a new game
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user13111", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());
        Long gameMasterUserId = responseToJsonNode(response).get("id").asLong();

        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());
        assertNotNull(lobbyId);

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user132", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
        joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId);

        startLobby(tokenGameMaster1);
        connectAllPlayer(tokenGameMaster1);

        registerDefinition(tokenGameMaster1, "dummyDefinitionGM");
        registerDefinition(tokenJoinPlayer, "dummyDefinitionPlayer2");

        castVote(0L, tokenGameMaster1);
        castVote(gameMasterUserId, tokenJoinPlayer);

        String uri = "http://localhost:" + port + "/lobbies/newround";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", tokenGameMaster1);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> responseNewGame = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);
        assertEquals(HttpStatus.OK, responseNewGame.getStatusCode());
    }

    private ResponseEntity<String> castVote(Long userId, String tokenGameMaster1) {
        String uri = "http://localhost:" + port + "/lobbies/users/votes";
        HttpHeaders headers = prepareHeader(tokenGameMaster1);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("vote", userId);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        return restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);
    }


    public JsonNode responseToJsonNode(ResponseEntity<String> response) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = null;

        String jsonBody = response.getBody();
        try {
            rootNode = mapper.readTree(jsonBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return rootNode;
    }
    private ResponseEntity<String> getLobby(String token, Long lobbyId) {
        String uri = "http://localhost:" + port + "/lobbies/"+lobbyId;
        HttpHeaders headers = prepareHeader(token);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(headers);
        return restTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);
    }
    private ResponseEntity<String> removePlayer(String token, Long lobbyId){
        String uri = "http://localhost:" + port + "/lobbies/users/"+lobbyId;
        HttpHeaders headers = prepareHeader(token);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(headers);
        return restTemplate.exchange(uri, HttpMethod.DELETE, requestEntity, String.class);
    }

    private ResponseEntity<String> logoutPlayer(String token){
        String uri = "http://localhost:" + port + "/users/logout";
        HttpHeaders headers = prepareHeader(token);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(headers);
        return restTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);
    }


    private ResponseEntity<String> registerDefinition(String token, String defintion) {
        String uri = "http://localhost:" + port + "/lobbies/users/definitions";
        HttpHeaders header = prepareHeader(token);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("definition", defintion);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, header);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response;
    }

    private ResponseEntity<String> startLobby(String tokenGameMaster1) {
        String uri = "http://localhost:" + port + "/lobbies/start";
        HttpHeaders header = prepareHeader(tokenGameMaster1);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(header);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response;
    }


    private void checkIfLobbyJoinWorked(Long lobbyId, String tokenGameMaster1, String username1, String username2) {
        String uri = "http://localhost:" + port + "/lobbies/" + lobbyId.toString();
        HttpHeaders header = prepareHeader(tokenGameMaster1);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(header);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);
        assertTrue(response.getBody().contains(username1));
        assertTrue(response.getBody().contains(username2));
    }

    private HttpHeaders prepareHeader(String tokenGameMaster1) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", tokenGameMaster1);
        return headers;
    }

    private ResponseEntity<String> joinPlayerToLobbyAndSuccessCheck(String tokenJoinPlayer, Long lobbyId) {
        String uri = "http://localhost:" + port + "/lobbies/users/" + lobbyId.toString();
        HttpHeaders headers = prepareHeader(tokenJoinPlayer);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response;
    }

    private ResponseEntity<String> joinPlayerToLobby(String tokenJoinPlayer, Long lobbyId) {
        String uri = "http://localhost:" + port + "/lobbies/users/" + lobbyId.toString();
        HttpHeaders headers = prepareHeader(tokenJoinPlayer);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);
        return response;
    }

    private ResponseEntity<String> adjustRoundLengthAndModes(Long gamePin, Long length, ArrayList<LobbyModes> modes, String token) {
        String uri = "http://localhost:" + port + "/lobbies/" + gamePin.toString();

        Map<String, Object> requestBody = new HashMap<>();
        List<String> modeNames = new ArrayList<>();
        for (LobbyModes mode : modes) {
            modeNames.add(mode.toString());
        }
        requestBody.put("game_modes", modeNames);
        requestBody.put("rounds", length.toString());

        HttpHeaders headers = prepareHeader(token);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        return restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);
    }

    private String extractTokenFromResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            return jsonObject.getString("token");
        } catch (JSONException e) {
            fail("Failed to parse JSON response");
            return null;
        }
    }

    private Long extractLobbyId(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            return jsonObject.getLong("game_pin");
        } catch (JSONException e) {
            fail("Failed to parse JSON response");
            return null;
        }
    }
    private ResponseEntity<String> createUserWithSuccessAssertion(String username, String password) {
        String uri = "http://localhost:" + port + "/users";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        return response;
    }
    private ResponseEntity<String> createLobbyWithSuccessCheck(String token) {
        String uri = "http://localhost:" + port + "/lobbies";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        return response;
    }

    private void connectAllPlayer(String token) {
        String uri = "http://localhost:" + port + "/lobbies/connect";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
