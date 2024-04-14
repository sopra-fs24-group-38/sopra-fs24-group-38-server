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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


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
    public void badCredentialsRaises401() {
        // Register ...
        createUser("User1", "password");

        // Try Login with bad credentials (pwd) ...
        String uri = "http://localhost:" + port + "/users/login";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", "User1");
        requestBody.put("password", "passwordFalse");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        assertThrows(ResourceAccessException.class, () -> restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class));
    }


    @DisplayName("UserController Test: Issue #31 Token Generation")
    @Test
    public void generatesToken() {
        String uri = "http://localhost:" + port + "/users";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", "testUsername");
        requestBody.put("password", "testPassword");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);

        assertEquals(201, response.getStatusCodeValue());

        String responseBody = response.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("\"token\""));
        String token = extractTokenFromResponse(responseBody);
        assertNotNull(token);
        assertValidUUID(token);
    }


    private void createUser(String username, String password) {
        String uri = "http://localhost:" + port + "/users";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
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
