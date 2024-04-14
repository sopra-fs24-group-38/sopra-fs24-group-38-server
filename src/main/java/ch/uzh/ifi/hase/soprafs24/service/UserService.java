package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.model.database.Lobby;
import ch.uzh.ifi.hase.soprafs24.model.database.User;
import ch.uzh.ifi.hase.soprafs24.model.request.DefinitionPost;
import ch.uzh.ifi.hase.soprafs24.model.request.UserPost;
import ch.uzh.ifi.hase.soprafs24.model.response.UserResponse;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


@Service
@Transactional
public class UserService {

    @Autowired
    LobbyService lobbyService;
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public User getUserById(Long userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This user does not exist");
        }
        return user;
    }

    public UserResponse createUser(UserPost userPost) {

        if (userRepository.findByUsername(userPost.getUsername()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "username already taken");
        }

        User newUser = objectMapper.convertValue(userPost, User.class);

        newUser.setToken(UUID.randomUUID().toString());

        newUser = userRepository.save(newUser);
        userRepository.flush();

        log.warn("Created User: {}", newUser);

        return objectMapper.convertValue(newUser,UserResponse.class);
    }

    public UserResponse loginUser(UserPost userPost) {
        User user = userRepository.findByUsername(userPost.getUsername());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This username does not exist");
        }
        if(!Objects.equals(user.getPassword(), userPost.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }

        return objectMapper.convertValue(user, UserResponse.class);
    }

    public void addSessionToPlayer(String sessionId, Long userId) {
        User user = getUserById(userId);
        user.setSessionId(sessionId);
        userRepository.save(user);
        userRepository.flush();

    }

    public void registerDefinitions(DefinitionPost definitionPost, Long userId) {
        User user = getUserById(userId);
        user.setDefinition(definitionPost.getDefinition());
        userRepository.save(user);
        userRepository.flush();
        lobbyService.checkIfAllDefinitionsReceived(user.getLobbyId());
    }

    public Long getUserIdByTokenAndAuthenticate(String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated (bad token)");
        }
        else {
            return user.getId();
        }
    }

    public void setLobbyIdForGameMaster(Long userId, Long gamePin) {
        User userToBeMappedToLobby = userRepository.findUserById(userId);
        userToBeMappedToLobby.setLobbyId(gamePin);
        userRepository.save(userToBeMappedToLobby);
        userRepository.flush();
    }

    public Long getUserIdBySessionId(String id) {
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            if(Objects.equals(user.getSessionId(), id)) {
                return user.getId();
            }
        }
        return null;
    }

    public void setIsConnected(Long userId, Boolean connected) {
        User user = getUserById(userId);
        user.setIsConnected(connected);
        userRepository.save(user);
        userRepository.flush();

    }
}
