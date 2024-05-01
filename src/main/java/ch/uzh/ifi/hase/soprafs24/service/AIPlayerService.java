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


    @Value("${avatar.number}")
    private int numAvas;

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
        String name = getRandomUniqueName(gamePin);
        aiUser.setUsername(name);
        aiUser.setPassword("AI");
        aiUser.setToken(UUID.randomUUID().toString());
        userRepository.save(aiUser);
        userRepository.flush();
        System.out.println("player with name "+name+ " created");
        return aiUser;
    }

    public void setAvatarPin(Long gamePin, User aiUser) {
        aiUser.setAvatarId(8L);
        userRepository.save(aiUser);
        userRepository.flush();
    }

    public void setLobbyId(Long userId, Long lobbyId) {
        User user = userRepository.findUserById(userId);
        user.setLobbyId(lobbyId);
        userRepository.save(user);
        userRepository.flush();
    }

    private Long getUnUsedAvaId(Long lobbyId) {
        Lobby lobby = lobbyService.getLobbyAndExistenceCheck(lobbyId);
        List<User> users = lobby.getUsers();
        Set<Long> existingIds = users.stream()
                .map(User::getAvatarId)
                .collect(Collectors.toSet());
        Long potentialId;
        do {
            Random random = new Random();
            potentialId = 1L + (long) random.nextInt(numAvas);
        } while (existingIds.contains(potentialId));
        return potentialId;
    }

    private String getRandomUniqueName(Long lobbyId) {
        List<User> users = lobbyService.getUsers(lobbyId);
        Set<String> existingNames = new HashSet<>();
        for (User user : users) {
            existingNames.add(user.getUsername());
        }
        boolean nameUnique = false;
        String name = "";
        while (!nameUnique) {
            name = names.get(random.nextInt(names.size()));
            if (!existingNames.contains(name)) {
                nameUnique = true;
            }
        }
        return name;
    }
}
