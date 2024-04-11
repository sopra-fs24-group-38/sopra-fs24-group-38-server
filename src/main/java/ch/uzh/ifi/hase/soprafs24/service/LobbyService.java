package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;
import ch.uzh.ifi.hase.soprafs24.constant.LobbyState;
import ch.uzh.ifi.hase.soprafs24.model.database.Lobby;
import ch.uzh.ifi.hase.soprafs24.model.database.User;
import ch.uzh.ifi.hase.soprafs24.model.request.LobbyPut;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.websockets.SocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@Transactional
public class LobbyService {

    private final Logger log = LoggerFactory.getLogger(LobbyService.class);

    private final Random random = new Random();

    @Autowired
    LobbyRepository lobbyRepository;

    @Autowired
    UserService userService;
    @Autowired
    private SocketHandler socketHandler;

    @Autowired
    public LobbyService(@Qualifier("lobbyRepository") LobbyRepository lobbyRepository) {
        this.lobbyRepository = lobbyRepository;
    }

    public void addPlayerToLobby(Long userId, Long lobbyId) {
        Lobby lobby = getLobbyAndExistenceCheck(lobbyId);
        User user = userService.getUserById(userId);
        checkWhetherPlayerInLobby(userId);
        lobby.addPlayer(userService.getUserById(userId));
        user.setLobbyId(lobbyId);
        log.warn("user with id " + userId + " joined lobby " + lobbyId);
    }

    public void removePlayerFromLobby(Long userId, Long lobbyId) {
        Lobby lobby = getLobbyAndExistenceCheck(lobbyId);
        User user = userService.getUserById(userId);
        if (!lobby.getPlayers().contains(user))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not in the specified lobby");
        lobby.removePlayer(user);
        user.setLobbyId(null);
        log.warn("user with id " + userId + " removed from lobby " + lobbyId);
    }

    public Set<User> getPlayerSet(Long lobbyId) {
        Lobby lobby = lobbyRepository.findLobbyByLobbyPin(lobbyId);
        return lobby.getPlayers();
    }

    public Long createLobby(Long userId) {

        Lobby lobby = new Lobby();
        Long pin;

        //ensure the pin has not been set yet
        do {
            pin = (long) (random.nextInt(9000) + 1000);
        } while (lobbyRepository.findLobbyByLobbyPin(pin) != null);

        lobby.setLobbyPin(pin);
        lobby.setGameMaster(userId);
        lobby.addPlayer(userService.getUserById(userId));
        lobbyRepository.save(lobby);
        lobbyRepository.flush();

        log.warn("created lobby with pin " + pin);

        return pin;
    }

    public void adjustSettings(LobbyPut settingsToBeRegistered, Long gamePin) {
        int roundUpdate = settingsToBeRegistered.getRounds();
        List<String> gameModes = settingsToBeRegistered.getGameModes();
        Lobby lobby = getLobbyAndExistenceCheck(gamePin);
        if (gameModes != null) {
            setGameModes(gameModes, lobby);
        }
        if (gameModes != null) {
            setRounds(roundUpdate, lobby);
        }
        lobbyRepository.save(lobby);
        lobbyRepository.flush();
    }

    private void setRounds(int roundUpdate, Lobby lobby) {
        if (roundUpdate < 5 || roundUpdate > 15) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allowed Number of Rounds are 5 - 15");
        }
        lobby.setNumberRounds(roundUpdate);
    }

    private void setGameModes(List<String> gameModes, Lobby lobby) {
        Set<LobbyModes> lobbyModes = new HashSet<>();
        for (String gameModeNotValidated : gameModes) {
            try {
                LobbyModes lobbyMode = LobbyModes.valueOf(gameModeNotValidated);
                lobbyModes.add(lobbyMode);
            }
            catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gamemode " + gameModeNotValidated + " is not valid");
            }
        }
        lobby.setLobbyModes(lobbyModes);
    }

    public Lobby getLobbyAndExistenceCheck(Long gamePin) {
        Lobby lobbyToReturn = lobbyRepository.findLobbyByLobbyPin(gamePin);
        if (lobbyToReturn == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The referenced Lobby does not exist");
        }
        return lobbyToReturn;
    }

    private void checkWhetherPlayerInLobby(Long userId) {
        List<Lobby> allLobbies = lobbyRepository.findAll();

        for (Lobby lobby : allLobbies) {
            Set<User> players = lobby.getPlayers();
            if (players.stream().anyMatch(user -> user.getId().equals(userId))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already in a lobby");
            }
        }
    }

    public void startGame(Long userId) {

        User user = userService.getUserById(userId);
        Long lobbyId = user.getLobbyId();
        Lobby lobby = getLobbyAndExistenceCheck(lobbyId);

        if (!Objects.equals(userId, lobby.getGameMaster())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user is not gameMaster");
        }

        lobby.setState(LobbyState.DEFINITION);
        lobby.setGameOver(false);
        //lobby.setChallenges(apiHandler.generateChallenges(gameMode, gameRounds));
        lobby.setRoundNumber(1L);
        lobbyRepository.save(lobby);
        lobbyRepository.flush();

        socketHandler.sendMessageToLobby(lobbyId, "game_start");
    }
}
