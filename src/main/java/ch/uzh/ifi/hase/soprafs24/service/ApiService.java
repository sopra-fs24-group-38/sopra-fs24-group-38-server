package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;
import ch.uzh.ifi.hase.soprafs24.model.response.Challenge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;


@Service
public class ApiService {

    @Autowired
    private GCPService gcpService;

    //@Value("${token.api}")
    //private String token;

    public Set<Challenge> generateChallenges(Set<LobbyModes> lobbyModes, int numberRounds) {

        Set<Challenge> challenges = new HashSet<>();
        challenges.add(new Challenge("testchallenge", gcpService.getSecret()));
        return challenges;
    }
}
