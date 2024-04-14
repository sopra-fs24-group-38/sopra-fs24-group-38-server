package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.model.response.Challenge;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
public class ApiService {

    private RestTemplate restTemplate = new RestTemplate();

    public List<Challenge> generateChallenges(int numberRounds) {

        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getSecret());
        headers.set("Content-Type", "application/json");

        String jsonBody = "{"
                + "\"model\": \"gpt-4\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"You're an AI agent and can only answer in a valid JSON array like this: "
                + "[{\\\"value\\\": \\\"Value1\\\",\\\"definition\\\": \\\"definition 1\\\"}],{\\\"value\\\": \\\"Value2\\\",\\\"definition\\\": \\\"definition 2\\\"}] "
                + "The value should return a bizzare and unknown word. The definition should be very short, easy to understand and potentially written by a not-first-language-english human, and consist of maximum 4 words. "
                + "Now give us "
                + numberRounds
                + " of these words with their definitions. Do not repeat definitions, only use words that actually exist.\"}],"
                + "\"temperature\": 1"
                + "}";

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        System.out.println("Response: " + response.getBody());


        List<Challenge> challenges = new ArrayList<>();
        challenges.add(new Challenge("testchallenge", getSecret()));
        return challenges;
    }

    public String getSecret() {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            SecretVersionName secretVersionName = SecretVersionName.of("sopra-fs24-group-38-server", "TOKEN_API", "latest");
            return client.accessSecretVersion(secretVersionName).getPayload().getData().toStringUtf8();
        } catch (IOException e) {
            throw new RuntimeException("Error retrieving secret from Google Secret Manager", e);
        }
    }
}
