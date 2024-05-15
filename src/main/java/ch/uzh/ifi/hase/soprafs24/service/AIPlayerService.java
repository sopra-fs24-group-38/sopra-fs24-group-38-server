package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.model.database.Lobby;
import ch.uzh.ifi.hase.soprafs24.model.database.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class AIPlayerService {

    @Autowired
    LobbyService lobbyService;
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final Random random = new Random();

    ObjectMapper objectMapper = new ObjectMapper();
    private final List<String> names = new ArrayList<>();
    private final ResourceLoader resourceLoader;

    @Value("${avatar.ai.number}")
    private int numAvasAi;


    @Autowired
    public AIPlayerService(@Qualifier("userRepository") UserRepository userRepository, ResourceLoader resourceLoader) {
        this.userRepository = userRepository;
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        Resource resource = resourceLoader.getResource("classpath:ai_names.txt");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                names.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User createAiUser(Long gamePin) {
        User aiUser = new User();
        aiUser.setAiPlayer(true);
        String name = getRandomUniqueName();
        aiUser.setUsername(name);
        aiUser.setLobbyId(gamePin);
        aiUser.setAvatarId(getUnUsedAvaIdForAIPlayer(gamePin));
        aiUser.setIsConnected(true);
        aiUser.setWantsNextRound(true);
        //Needed because attributes not nullable in DB :
        aiUser.setToken(UUID.randomUUID().toString());
        aiUser.setPassword("AI");

        userRepository.save(aiUser);
        userRepository.flush();
        log.warn("AI player with name "+name+ " created");
        return aiUser;
    }
    private Long getUnUsedAvaIdForAIPlayer(Long lobbyId) {
        Lobby lobby = lobbyService.getLobbyAndExistenceCheck(lobbyId);
        List<User> users = lobby.getUsers();
        Set<Long> existingIds = users.stream()
                .map(User::getAvatarId)
                .collect(Collectors.toSet());
        Long potentialId;
        do {
            Random random = new Random();
            potentialId = 100L + (long) random.nextInt(numAvasAi);
        } while (existingIds.contains(potentialId));
        return potentialId;
    }

    private String getRandomUniqueName() {
        Boolean anyConstrainViolations = false;
        for(String name : names){
            if(name.length() > 12){
                System.out.println(name +  " too long");
                anyConstrainViolations = true;
            }
        }
        Set<String> set = new HashSet<>();
        for (String name : names) {
            if (!set.add(name)) {
                anyConstrainViolations = true;
                System.out.println(name + " not unique");
            }
        }
        System.out.println("There are constrain violations: {} " + anyConstrainViolations);

        boolean nameUnique = false;
        boolean fetchingWorked = false;
        String name = "";
        int tries = 0;
        int maxTries = 50;
        while (!nameUnique && tries <= maxTries) {
            name = names.get(random.nextInt(names.size()));
            User user = userRepository.findByUsername(name);
            if (user==null) {
                nameUnique = true;
                fetchingWorked = true;
            }
            tries+=1;
        }
        if(!fetchingWorked){
            int fiveDigitNumber = random.nextInt(90000) + 10000;
            name = "Robo" + fiveDigitNumber;
        }
        return name;
    }

}
