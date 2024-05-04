package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;
import ch.uzh.ifi.hase.soprafs24.model.response.Challenge;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;


import java.io.IOException;
import java.util.*;

@Service
public class ApiService {

    private RestTemplate restTemplate = new RestTemplate();
    private final Logger log = LoggerFactory.getLogger(ApiService.class);

    @Value("${api.token}")
    private String tokenEnv;

    public List<Challenge> generateChallenges(int numberRounds, Set<LobbyModes> lobbyModes) {
        Map<LobbyModes, Integer> numberQuestionPerMode = distributeNumModes(numberRounds + 1, lobbyModes);
        List<Challenge> challenges = new ArrayList<>();

        for (Map.Entry<LobbyModes, Integer> entry : numberQuestionPerMode.entrySet()) {
            fetchChallenges(challenges, entry.getKey(), entry.getValue());
        }
        return challenges;
    }

    private void fetchChallenges(List<Challenge> challenges, LobbyModes lobbyMode, int numberOfRoundsOfMode) {
        List<String> values = new ArrayList<>();
        List<String> definitions = new ArrayList<>();
        boolean shouldContinue = true;

        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getSecret());
        headers.set("Content-Type", "application/json");

        String jsonBody = getPromptBody(lobbyMode, numberOfRoundsOfMode);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        while (shouldContinue) {

            String response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();

            JSONObject jsonResponse = new JSONObject(response);
            String content = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
            JSONArray jsonArray = new JSONArray(content);

            shouldContinue = false;

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String word = jsonObject.getString("value");
                String definition = jsonObject.getString("definition");

                if (values.contains(word)) {
                    shouldContinue = true;
                    values.clear();
                    definitions.clear();
                    break;
                }

                values.add(word);
                definitions.add(definition.toLowerCase());
            }
        }

        for(int i = 0; i < values.size(); i++) {
            challenges.add(new Challenge(values.get(i), definitions.get(i), lobbyMode));
        }
    }
    private String getPromptBody(LobbyModes lobbyModes, int numberRounds){
        return "{"
                + "\"model\": \"gpt-4-turbo\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"You're an AI agent and can only answer in a valid JSON array like this: "
                + "[{\\\"value\\\": \\\"Value1\\\",\\\"definition\\\": \\\"definition 1\\\"}],{\\\"value\\\": \\\"Value2\\\",\\\"definition\\\": \\\"definition 2\\\"}] "
                + getModeDescription(lobbyModes)
                + "The definition should be very short, easy to understand and potentially written by a not-first-language-english human, and consist of maximum 4 words. "
                + "Now give us "
                + numberRounds
                + " of these words with their definitions. Do not repeat definitions, only use words that actually exist.\"}],"
                + "\"temperature\": 0.7"
                + "}";
    }

    private String getModeDescription(LobbyModes lobbyModes) {
        return switch (lobbyModes) {
            case BIZARRE -> "The value should return a bizarre and unknown word.";
            case PROGRAMMING -> "The value should return a rather unknown word related to Programming";
            case DUTCH -> "The value should return a funny dutch word.";
            case RAREFOODS -> "The value should return the name of an unknown food.";
        };
    }

    private Map<LobbyModes, Integer> distributeNumModes(int numberRounds, Set<LobbyModes> lobbyModes) {
        Map<LobbyModes, Integer> distribution = new HashMap<>();
        Random random = new Random();
        for (LobbyModes mode : lobbyModes) {
            distribution.put(mode, 0);
        }
        for (int i = 0; i < numberRounds; i++) {
            int randomIndex = random.nextInt(lobbyModes.size());
            LobbyModes selectedMode = (LobbyModes) lobbyModes.toArray()[randomIndex];
            distribution.put(selectedMode, distribution.get(selectedMode) + 1);
        }
        for (Map.Entry<LobbyModes, Integer> entry : distribution.entrySet()) {
            log.warn(entry.getKey() + " shall be played " + entry.getValue() + " times");
        }
        return distribution;
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
