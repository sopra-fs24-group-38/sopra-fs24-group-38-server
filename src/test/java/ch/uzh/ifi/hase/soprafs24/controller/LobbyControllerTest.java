package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;
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

    private void checkIfLobbyJoinWorked(Long lobbyId, String tokenGameMaster1, String username1, String username2) {
        String uri = "http://localhost:" + port + "/lobbies/" + lobbyId.toString();
        HttpHeaders header = prepareHeader(tokenGameMaster1);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(header);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);

        System.out.println("**");
        System.out.println(response);

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
