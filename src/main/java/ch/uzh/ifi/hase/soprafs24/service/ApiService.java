package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;
import ch.uzh.ifi.hase.soprafs24.model.database.Lobby;
import ch.uzh.ifi.hase.soprafs24.model.database.User;
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

        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getSecret());
        headers.set("Content-Type", "application/json");

        String jsonBody = getPromptBodyChallenges(lobbyMode, numberOfRoundsOfMode);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        String response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();

        JSONObject jsonResponse = new JSONObject(response);
        String content = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        JSONArray jsonArray = new JSONArray(content);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String word = jsonObject.getString("value");
            String definition = jsonObject.getString("definition");
            challenges.add(new Challenge(word, definition, lobbyMode));
        }

    }

    private String getPromptBodyChallenges(LobbyModes lobbyModes, int numberRounds){
        return "{"
                + "\"model\": \"gpt-4\","
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

    public void generateAiPlayersDefinitions(Lobby lobby) {
        List<User> users = lobby.getUsers();
        List<Challenge> challenges = lobby.getChallenges();
        for(User user : users){
            if(user.getAiPlayer()){
                user.setAiDefinitions(fetchAiDefinitions(challenges));
                for(int i = 0; i < lobby.getChallenges().size() ; i++){
                    //log.warn("ITERATION CHECK {} AI User with username {} generated definition {} for word {}",challenges.get(i).getLobbyMode(), user.getUsername(), user.getAiDefinitions().get(i), challenges.get(i).getChallenge());
                }
            }

        }




    }
    private List<String> fetchAiDefinitions(List<Challenge> challenges) {
        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getSecret());
        headers.set("Content-Type", "application/json");

        String jsonBody = getPromptBodyAIDefinitions(challenges);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        String response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();

        JSONObject jsonResponse = new JSONObject(response);
        String content = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        JSONArray jsonArray = new JSONArray(content);

        //log.warn("REQUEST CHECK 1 : response body (jsonResponse) {} ", jsonResponse);

        List<String> definitions = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String aiPlayersDefinition = jsonObject.getString("definition");
            definitions.add(aiPlayersDefinition);
        }

        //might become usefull for prompt improvements:

        //log.warn("FETCH CHECK1: length challange array : {} ", challenges.size());
        //log.warn("FETCH CHECK2: length ai definition array : {} ", definitions.size());

        return definitions;
    }

    private String getPromptBodyAIDefinitions(List<Challenge> challenges){
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("[");
        for (int i = 0; i < challenges.size(); i++) {
            Challenge challenge = challenges.get(i);
            promptBuilder.append(challenge.getChallenge() + "(Category: "+challenge.getLobbyMode() + ")");
            if (i < challenges.size() - 1) {
                promptBuilder.append(", ");
            }
        }

        String requestBody= "{"
                + "\"model\": \"gpt-4\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"You're an AI agent and can only answer in a valid JSON array like this: "
                + "[{\\\"definition\\\": \\\"definition1\\\",\\\"definition\\\": \\\"definition2\\\"}],{\\\"definition\\\": \\\"definition3\\\",\\\"definition\\\": \\\"definition4\\\"}] "
                + "Those are the words for which i need a wrong definition: "
                +  promptBuilder
                + "Give a plausible but false definition which tricks human into thinking it is correct"
                + "The wrong definition should be plausible and be related to the same category"
                + "The wrong definition should be less than 4 words\"}],"
                + "\"temperature\": 0.7"
                + "}";

        return requestBody;
    }



    private Map<LobbyModes, Integer> distributeNumModes(int numberRounds, Set<LobbyModes> lobbyModes) {
        Map<LobbyModes, Integer> distribution = new HashMap<>();;
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
