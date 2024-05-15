package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;
import ch.uzh.ifi.hase.soprafs24.constant.SubCategories.BroadSubCategories;
import ch.uzh.ifi.hase.soprafs24.constant.SubCategories.SubCategoriesFood;
import ch.uzh.ifi.hase.soprafs24.constant.SubCategories.SubCategoriesProgramming;
import ch.uzh.ifi.hase.soprafs24.model.database.Lobby;
import ch.uzh.ifi.hase.soprafs24.model.database.User;
import ch.uzh.ifi.hase.soprafs24.model.response.Challenge;
import ch.uzh.ifi.hase.soprafs24.websockets.SocketHandler;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiService {
    private final SocketHandler socketHandler;
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

    private final Logger log = LoggerFactory.getLogger(ApiService.class);

    private final RestTemplate restTemplate;

    @Value("${api.token}")
    private String tokenEnv;

    public ApiService(SocketHandler socketHandler) {
        this.socketHandler = socketHandler;
        factory.setConnectTimeout(20000);
        factory.setReadTimeout(20000);
        restTemplate = new RestTemplate(factory);
    }

    public List<Challenge> generateChallenges(int numberRounds, Set<LobbyModes> lobbyModes, Long lobbyId) {
        Map<LobbyModes, Integer> numberQuestionPerMode = distributeNumModes(numberRounds + 1, lobbyModes);
        List<Challenge> challenges = new ArrayList<>();

        for (Map.Entry<LobbyModes, Integer> entry : numberQuestionPerMode.entrySet()) {
            fetchChallenges(challenges, entry.getKey(), entry.getValue(), lobbyId);
        }
        return challenges;
    }


    private void fetchChallenges(List<Challenge> challenges, LobbyModes lobbyMode, int numberOfRoundsOfMode, Long lobbyId) {
        List<String> values = new ArrayList<>();
        List<String> definitions = new ArrayList<>();

        boolean shouldContinue = true;

        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getSecret());
        headers.set("Content-Type", "application/json");

        String jsonBody = getPromptBodyChallenges(lobbyMode, numberOfRoundsOfMode);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        try {
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
        } catch(Exception e) {
            log.warn("api unavailable");
            socketHandler.sendMessageToLobby(lobbyId, "api_unavailable");
            values.clear();
            definitions.clear();
            String jsonStr = "";

            try {
                jsonStr = new String(Files.readAllBytes(Paths.get("src/main/resources/static/fallback-words.json")));
            } catch(Exception e2) {
                log.warn("Backend has lost");
            }

            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONArray dataArray = jsonObject.getJSONArray("data");
            Random random = new Random();
            for(int i = 0; i < numberOfRoundsOfMode; i++) {
                int randomIndex = random.nextInt(dataArray.length());
                JSONObject wordObject = dataArray.getJSONObject(randomIndex).getJSONObject("word");
                values.add(wordObject.getString("value"));
                definitions.add(wordObject.getString("definition"));
            }
        }

        for(int i = 0; i < values.size(); i++) {
            challenges.add(new Challenge(values.get(i), definitions.get(i), lobbyMode));
        }
    }

    private String getPromptBodyChallenges(LobbyModes lobbyModes, int numberRounds){
        String prompt = "{"
                + "\"model\": \"gpt-4-turbo\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"You're an AI agent and can only answer in a valid JSON array like this: "
                + "[{\\\"value\\\": \\\"Value1\\\",\\\"definition\\\": \\\"definition 1\\\"}],{\\\"value\\\": \\\"Value2\\\",\\\"definition\\\": \\\"definition 2\\\"}] "
                + getModeDescription(lobbyModes)
                + "The definition should be very short, easy to understand and potentially written by a not-first-language-english human, and consist of maximum 4 words. Dont include special characters. "
                + randomSubcategories(lobbyModes)
                + "Now give us "
                + numberRounds
                + " of these words with their definitions. Do not repeat definitions, only use words that actually exist.\"}],"
                + "\"temperature\": 0.7"
                + "}";
        log.warn("PROMPT FOR WORDS AND DEFINITION: {} ", prompt);
        return prompt;
    }
    private String getModeDescription(LobbyModes lobbyModes) {
        return switch (lobbyModes) {
            case BIZARRE -> "The value should return a bizarre and unknown word. It is important that the words are not common. ";
            case PROGRAMMING -> "The value should return a rather unknown word related to Programming. It is important that the words are not common. ";
            case DUTCH -> "The value should return a funny dutch word. It is important that the words are not common.";
            case RAREFOODS -> "The value should return a unknown word related to food and cooking. It is important that the words are not common.";
        };
    }

    private String randomSubcategories(LobbyModes lobbyMode) {
        List<String> categories = switch (lobbyMode) {
            case BIZARRE -> Arrays.stream(BroadSubCategories.values()).map(Enum::toString).collect(Collectors.toList());
            case DUTCH -> Arrays.stream(BroadSubCategories.values()).map(Enum::toString).collect(Collectors.toList());
            case PROGRAMMING -> Arrays.stream(SubCategoriesProgramming.values()).map(Enum::toString).collect(Collectors.toList());
            case RAREFOODS -> Arrays.stream(SubCategoriesFood.values()).map(Enum::toString).collect(Collectors.toList());
        };

        Collections.shuffle(categories);
        String result = categories.stream().limit(3).collect(Collectors.joining(", "));
        int lastComma = result.lastIndexOf(",");
        if (lastComma != -1) {
            result = result.substring(0, lastComma) + " or" + result.substring(lastComma + 1);
        }
        String concatenatedResult = "The words should also either be related to " + result + ". ";
        log.warn("Subcategory prompt sentences: {}", concatenatedResult);
        return concatenatedResult;
    }
    public void generateAiPlayersDefinitions(Lobby lobby) {
        List<User> users = lobby.getUsers();
        List<Challenge> challenges = lobby.getChallenges();
        for(User user : users){
            if(user.getAiPlayer()){
                List<String> definitions = fetchAiDefinitions(challenges);
                user.setAiDefinitions(definitions);
                user.setDefinition(user.dequeueAiDefinition());
                for(int i = 0; i < lobby.getChallenges().size() ; i++){
                    //log.warn("ITERATION CHECK (Mode: {}, Round: {} AI User with username {} generated definition {} for word {}",challenges.get(i).getLobbyMode(),i, user.getUsername(), user.dequeueAiDefinition(), challenges.get(i).getChallenge());
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

        log.warn("REQUEST CHECK 1 : response body (content variable) {} ", jsonResponse);

        JSONArray jsonArray = new JSONArray(content);


        List<String> definitions =  new LinkedList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String aiPlayersDefinition = jsonObject.getString("definition");
            definitions.add(aiPlayersDefinition.toLowerCase());
        }

        //might become usefull for prompt improvements:

        log.warn("FETCH CHECK1: length challange array : {} ", challenges.size());
        log.warn("FETCH CHECK2: length ai definition array : {} ", definitions.size());

        return definitions;
    }

    private String getPromptBodyAIDefinitions(List<Challenge> challenges){
        StringBuilder promptBuilder = new StringBuilder();
        for (int i = 0; i < challenges.size(); i++) {
            Challenge challenge = challenges.get(i);
            promptBuilder.append(challenge.getChallenge() + " (Category: "+challenge.getLobbyMode() + ")");
            if (i < challenges.size() - 1) {
                promptBuilder.append(", ");
            }
        }

        String requestBody= "{"
                + "\"model\": \"gpt-4-turbo\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"You're an AI agent and can only answer in a valid JSON array like this (always use `definitions` as key and the actual definition as value) : "
                + "[{\\\"definitions\\\": \\\"definition1\\\",\\\"definitions\\\": \\\"definition2\\\"},{\\\"definitions\\\": \\\"definition3\\\",\\\"definitions\\\": \\\"definition4\\\"}] "
                + "Those are the words for which i need a wrong definition: "
                +  promptBuilder
                + "Give a plausible but false definition which tricks human into thinking it is correct"
                + "The wrong definition should be plausible and be related to the same category"
                + "The wrong definition should be less than 4 words. Remember to answer  to \"}],"
                + "\"temperature\": 0.7"
                + "}";
        log.warn("PROMPT FOR AI DEFINITION: {} ", requestBody);
        return requestBody;
    }



    private Map<LobbyModes, Integer> distributeNumModes(int numberRounds, Set<LobbyModes> lobbyModes) {
        Map<LobbyModes, Integer> distribution = new HashMap<>();

        int modesCount = lobbyModes.size();
        int baseRoundsPerMode = numberRounds / modesCount;
        int remainderRounds = numberRounds % modesCount;

        for (LobbyModes mode : lobbyModes) {
            distribution.put(mode, baseRoundsPerMode);
        }
        for (LobbyModes mode : lobbyModes) {
            if (remainderRounds > 0) {
                distribution.put(mode, distribution.get(mode) + 1);
                remainderRounds--;
            }
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
