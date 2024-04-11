package ch.uzh.ifi.hase.soprafs24.model.response;

import java.util.ArrayList;
import java.util.List;

public class ChallengeList {
    private List<Challenge> challenges;

    public ChallengeList() {
        this.challenges = new ArrayList<>();
    }

    public void addChallenge(String challenge, String solution) {
        challenges.add(new Challenge(challenge, solution));
    }

    public Challenge getChallenge(int index) {
        if (index < 0 || index >= challenges.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + challenges.size());
        }
        return challenges.get(index);
    }
}

