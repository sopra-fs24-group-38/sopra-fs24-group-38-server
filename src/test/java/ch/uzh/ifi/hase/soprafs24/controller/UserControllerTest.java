package ch.uzh.ifi.hase.soprafs24.controller;

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


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {
    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @DisplayName("UserController Test: createUser")
    @Test
    public void createUserAndAssertSuccessStatus() {
        ResponseEntity<String> response = createUserAndAssertSuccessStatus("username77", "password");
        JSONObject jsonObject = new JSONObject(response.getBody());

        String token = jsonObject.getString("token");
        Long id = jsonObject.getLong("id");

        assertValidUUID(token);
        assertNotNull(id);
    }


    @DisplayName("UserController Test: Issue #31")
    @Test
    public void issue31() {
        /**
         * The backend generates a token to enable authenatication and re-login.
         * #31
         */
        String response = createUserAndAssertSuccessStatus("username", "password").getBody();
        assertTrue(response.contains("\"token\""));
        String token = extractTokenFromResponse(response);
        assertNotNull(token);
        assertValidUUID(token);
    }
    @DisplayName("UserController Test: Issue #28")
    @Test
    public void issue28() {
        /**
         * The backend is able to receive credentials and create a user entity accordingly
         * #28
         */
        //createUser Method already performs an assert for created response status
        String responseRegister = createUserAndAssertSuccessStatus("User99d", "password1").getBody();

        // Try Login with good credentials (pwd) to check that credentials are being persisted

        String responseLogin = loginWithSuccessAssertion("User99d", "password1");

        // and tokens match
        String tokenFromRegister = extractTokenFromResponse(responseRegister);
        String tokenFromLogin = extractTokenFromResponse(responseLogin);
        assertEquals(tokenFromRegister, tokenFromLogin);
    }
    @DisplayName("UserController Test: Issue #37")
    @Test
    public void issue37() {
        /**
         * The backend is able to check the user's credentials and return a token upon success
         * #37
         **/
        createUserAndAssertSuccessStatus("testUser22", "pw22");
        //login :
        String responseLogin = loginWithSuccessAssertion("testUser22", "pw22");

        String token = extractTokenFromResponse(responseLogin);
        assertValidUUID(token);

    }

    @DisplayName("UserController Test: Issue #38")
    @Test
    public void issue38() {
        /**
         * The backend returns a http error if the credentials do not match #38
         */
        // Register ...
        createUserAndAssertSuccessStatus("dummyUserName", "password");
        //Login with bad credential and expect error
        ResponseEntity<String> response = login("dummyUserName", "passwordFalse");
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
    @DisplayName("Complex Unit Test")
    @Test
    public void complexUnitTest() {
        /**
         * Creates a user first, then creates a lobby and then performs a GET request on the lobby to check
         * that the user is registered in the lobby
         */
        //createUser Method already performs an assert for created response status
        String responseRegister = createUserAndAssertSuccessStatus("User1", "password1").getBody();
        String tokenFromRegister = extractTokenFromResponse(responseRegister);

        //Create a lobby
        String uri = "http://localhost:" + port + "/lobbies";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", tokenFromRegister);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);

        //assert that lobby was created
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JSONObject jsonObject = new JSONObject(response.getBody());
        long gamePin = jsonObject.getLong("game_pin");
        assertTrue(gamePin >= 1000 && gamePin <= 9999, "Lobby ID should be a four-digit number");

        //extract the user from the lobby and assert that he is in the lobby
        uri = "http://localhost:" + port + "/lobbies/" + gamePin;
        ResponseEntity<String> lobbyResponse = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);
        assertEquals(HttpStatus.OK, lobbyResponse.getStatusCode());
        JSONObject lobbyObject = new JSONObject(lobbyResponse.getBody());
        JSONObject gameDetails = lobbyObject.getJSONObject("game_details");
        JSONArray players = gameDetails.getJSONArray("players");
        JSONObject firstPlayer = players.getJSONObject(0);
        String username = firstPlayer.getString("username");
        assertEquals("User1", username, "The username does not match the expected value");
    }


    //UTILITY METHODS :
    private ResponseEntity<String> createUserAndAssertSuccessStatus(String username, String password) {
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

    private ResponseEntity<String> login(String username, String password){
        String uri = "http://localhost:" + port + "/users/login";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
        return response;
    }

    private String loginWithSuccessAssertion(String username, String password){
        String uri = "http://localhost:" + port + "/users/login";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);

        assertEquals(HttpStatus.OK,response.getStatusCode());
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
