package ch.uzh.ifi.hase.soprafs24.controller;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
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

    private String extractTokenFromResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            return jsonObject.getString("token");
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
