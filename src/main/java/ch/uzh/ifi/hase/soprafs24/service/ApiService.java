package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;
import ch.uzh.ifi.hase.soprafs24.model.response.Challenge;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


@Service
public class ApiService {

    public Set<Challenge> generateChallenges(Set<LobbyModes> lobbyModes, int numberRounds) {

        Set<Challenge> challenges = new HashSet<>();
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
