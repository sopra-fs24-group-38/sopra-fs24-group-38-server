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

    private ResponseEntity<String> adjustRoundLengthAndModes(Long gamePin, Long length, ArrayList<LobbyModes> modes, String token) {
        String uri = "http://localhost:" + port + "/lobbies/" + gamePin.toString();

        Map<String, Object> requestBody = new HashMap<>();
        List<String> modeNames = new ArrayList<>();
        for (LobbyModes mode : modes) {
            modeNames.add(mode.toString());
        }
        requestBody.put("game_modes", modeNames);
        requestBody.put("rounds", length.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);
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
