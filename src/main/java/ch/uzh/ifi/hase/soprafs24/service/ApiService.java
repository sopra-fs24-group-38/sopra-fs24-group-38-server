package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;
import ch.uzh.ifi.hase.soprafs24.model.response.Challenge;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class ApiService {

    public Set<Challenge> generateChallenges(Set<LobbyModes> lobbyModes, int numberRounds) {
        Set<Challenge> challenges = new HashSet<>();
        challenges.add(new Challenge("testchallenge", "testSolution"));
        return challenges;
    }
}