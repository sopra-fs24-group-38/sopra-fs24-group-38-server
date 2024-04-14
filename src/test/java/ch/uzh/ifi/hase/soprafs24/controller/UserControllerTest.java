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
import org.springframework.web.client.ResourceAccessException;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {
    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;


    @DisplayName("UserController Test: Issue #38 Bad credentials")
    @Test
    public void badCredentialsRaisesError() {
        // Register ...
        createUser("dummyUserName", "password");
        //Login with bad credential and expect error
        assertThrows(ResourceAccessException.class, () -> login("User1", "passwordFalse"));
    }


    @DisplayName("UserController Test: Issue #31 Token Generation")
    @Test
    public void generatesToken() {
        String response = createUser("username", "password");
        assertTrue(response.contains("\"token\""));
        String token = extractTokenFromResponse(response);
        assertNotNull(token);
        assertValidUUID(token);
    }
    @DisplayName("UserController Test: Issue #28 Create User and backend receives credentials")
    @Test
    public void backendReceivesCredentials() {
        //createUser Method already performs an assert for created response status
        String responseRegister = createUser("User1", "password1");

        // Try Login with good credentials (pwd) to check that credentials are being persisted

        String responseLogin = login("User1", "password1");

        // and tokens match
        String tokenFromRegister = extractTokenFromResponse(responseRegister);
        String tokenFromLogin = extractTokenFromResponse(responseLogin);
        assertEquals(tokenFromRegister, tokenFromLogin);
    }

    private String createUser(String username, String password) {
        String uri = "http://localhost:" + port + "/users";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        return response.getBody();
    }

    private String login(String username, String password){
        String uri = "http://localhost:" + port + "/users/login";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
        return response.getBody();
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

    private void assertValidUUID(String token) {
        try {
            UUID uuid = UUID.fromString(token);
            assertNotNull(uuid);
        } catch (IllegalArgumentException e) {
            fail("Token is not a valid UUID");
        }
    }
}
