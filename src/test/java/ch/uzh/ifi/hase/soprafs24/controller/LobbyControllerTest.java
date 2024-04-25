package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import javax.validation.constraints.AssertTrue;
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

    @DisplayName("LobbyControllerTest: Issue #47")
    @Test
    public void issue47() {
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

    @DisplayName("LobbyControllerTest: Issue #45")
    @Test
    public void issue45() {
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
        modes.add(LobbyModes.DEFINITIONS);
        modes.add(LobbyModes.DUTCH);

        //Perform adjustments
        ResponseEntity<String> responseLobbyAdjustments = adjustRoundLengthAndModes(lobbyId, 5L, modes, tokenGameMaster1);

        //and expect 200:
        assertEquals(HttpStatus.OK, responseLobbyAdjustments.getStatusCode());
    }

    @DisplayName("LobbyControllerTest: Issue #44")
    @Test
    public void issue44() {
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



    @DisplayName("LobbyControllerTest: Issue #40")
    @Test
    public void issue40() {
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

    @DisplayName("LobbyControllerTest: Issue #172")
    @Test
    public void issue172() {
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

        checkIfLobbyJoinWorked(lobbyId, tokenGameMaster1, "user7", "user8");
    }

    @DisplayName("LobbyControllerTest: Issue #54")
    @Test
    public void issue54() {
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

        checkIfLobbyJoinWorked(lobbyId, tokenGameMaster1, "user9", "user10");
    }

    @DisplayName("LobbyControllerTest: Issue #53")
    @Test
    public void issue53() {
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

    @DisplayName("LobbyControllerTest: Issue #62")
    @Test
    public void issue62() {
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

    @DisplayName("LobbyControllerTest: Issue #81")
    @Test
    public void issue81() {
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

        startLobby(tokenGameMaster1);

        registerDefinition(tokenGameMaster1, "placeboDefintion1");
        registerDefinition(tokenJoinPlayer, "placeboDefinition2");


    }

    @DisplayName("LobbyControllerTest: Issue #58")
    @Test
    public void issue58() {
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

    }


    @DisplayName("LobbyControllerTest: Issue #177")
    @Test
    public void issue177() {
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
    @DisplayName("LobbyControllerTest: Issue #71")
    @Test
    public void issue71() {
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

    @DisplayName("LobbyControllerTest: Issue #70")
    @Test
    public void issue70() {
        /**
         The backend provides the current challange to the frontend via an api endpoint #70
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user29", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());

        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());
        assertNotNull(lobbyId);

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user30", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
        joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId);

        startLobby(tokenGameMaster1);

        ResponseEntity<String> response1 = getLobby(tokenGameMaster1, lobbyId);
        JsonNode responseBody = responseToJsonNode(response1);

        //assert that the challenge field is not null and also that the API request worked
        assertNotNull(responseBody.get("game_details").get("challenge").toString());
    }

    @DisplayName("LobbyControllerTest: Issue #80")
    @Test
    public void issue80() {
        /**
         The backend is able to receive the votes which the players submitted #80
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user31", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());

        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());
        assertNotNull(lobbyId);

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user32", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
        joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId);

        startLobby(tokenGameMaster1);

        registerDefinition(tokenGameMaster1, "placeboDefintion1");
        registerDefinition(tokenJoinPlayer, "placeboDefintion2");

        JsonNode responseBodyCreationPlayer1 = responseToJsonNode(response);
        Long userId = responseBodyCreationPlayer1.get("id").asLong();

        ResponseEntity<String> castedVoteResponse = castVote(userId, tokenGameMaster1);

        assertEquals(HttpStatus.OK, castedVoteResponse.getStatusCode());

    }

    @DisplayName("LobbyControllerTest: Issue #92")
    @Test
    public void issue92() {
        /**
         The backend stores the number of points each player scored
         #92
         */

        ResponseEntity<String> response = createUserWithSuccessAssertion("user33", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());

        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());
        Long gameMasterUserId = responseToJsonNode(response).get("id").asLong();

        assertNotNull(lobbyId);

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user34", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());

        joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId);

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
            } else if (username.equals("user34")) {
                assertEquals(0, score, "User33 should have a score of 0");
            }
        }

    }

    @DisplayName("LobbyControllerTest: Issue #72 #82 & #91")
    @Test
    public void issue72_82_91() {
        /**
         The backend tells frontend when its voting time / all votings cast / and whether or not game over
         Issues #72, #82 & #91
         */
        ResponseEntity<String> response = createUserWithSuccessAssertion("user34", "password");
        String tokenGameMaster1 = extractTokenFromResponse(response.getBody());
        Long gameMasterUserId = responseToJsonNode(response).get("id").asLong();


        ResponseEntity<String> responseLobbyCreation = createLobbyWithSuccessCheck(tokenGameMaster1);
        Long lobbyId = extractLobbyId(responseLobbyCreation.getBody());
        assertNotNull(lobbyId);

        ResponseEntity<String> responseUserJoin = createUserWithSuccessAssertion("user35", "password");
        String tokenJoinPlayer = extractTokenFromResponse(responseUserJoin.getBody());
        joinPlayerToLobbyAndSuccessCheck(tokenJoinPlayer, lobbyId);

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
}
