package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.model.response.Challenge;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

@Service
public class ApiService {

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${api.token}")
    private String tokenEnv;

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
        String response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();

        JSONObject jsonResponse = new JSONObject(response);
        String content = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        JSONArray jsonArray = new JSONArray(content);
        List<Challenge> challenges = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String Word = jsonObject.getString("value");
            String definition = jsonObject.getString("definition");
            challenges.add(new Challenge(Word, definition));
        }

        return challenges;
    }

    public String getSecret() {
        if(tokenEnv.equals("NO_ENV_SET")){
            try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
                SecretVersionName secretVersionName = SecretVersionName.of("sopra-fs24-group-38-server", "TOKEN_API", "latest");
                return client.accessSecretVersion(secretVersionName).getPayload().getData().toStringUtf8();
            } catch (IOException e) {
                throw new RuntimeException("Error retrieving secret from Google Secret Manager", e);
            }
        }
        else{
            return tokenEnv;
        }
    }
}
