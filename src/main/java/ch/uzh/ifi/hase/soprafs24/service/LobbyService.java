package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;
import ch.uzh.ifi.hase.soprafs24.constant.LobbyState;
import ch.uzh.ifi.hase.soprafs24.model.database.Lobby;
import ch.uzh.ifi.hase.soprafs24.model.database.User;
import ch.uzh.ifi.hase.soprafs24.model.request.LobbyPut;
import ch.uzh.ifi.hase.soprafs24.model.response.GameDetails;
import ch.uzh.ifi.hase.soprafs24.model.response.LobbyGet;
import ch.uzh.ifi.hase.soprafs24.model.response.Player;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
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
    public LobbyService(@Qualifier("lobbyRepository") LobbyRepository lobbyRepository) {
        this.lobbyRepository = lobbyRepository;
    }

    public void addPlayerToLobby(Long userId, Long lobbyId) {
        Lobby lobby = getLobbyAndExistenceCheck(lobbyId);
        checkWhetherPlayerInLobby(userId);
        lobby.addPlayer(userService.getUserById(userId));
        log.debug("user with id " + userId + " joined lobby " + lobbyId);
    }

    public LobbyGet fetchLobbyInfo(Long gamePin) {
        Lobby lobbyInternal = getLobbyAndExistenceCheck(gamePin);
        LobbyGet infoLobbyJson = new LobbyGet();
        infoLobbyJson.setGamePin(gamePin);
        //Set the GameDetails field of LobbyGet class ...
        setGameState(lobbyInternal, infoLobbyJson);
        setGameOver(lobbyInternal, infoLobbyJson);
        setChallengeAndSolution(lobbyInternal, infoLobbyJson);
        setPlayer(lobbyInternal, infoLobbyJson);
        // ... Set the GameDetails field of LobbyGet class
        return infoLobbyJson;
    }

    public void removePlayerFromLobby(Long userId, Long lobbyId) {
        Lobby lobby = getLobbyAndExistenceCheck(lobbyId);
        User user = userService.getUserById(userId);
        if(!lobby.getPlayers().contains(user)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not in the specified lobby");
        lobby.removePlayer(user);
        log.debug("user with id " + userId + " removed from lobby " + lobbyId);
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
        lobby.addPlayer(userService.getUserById(userId));
        lobbyRepository.save(lobby);
        lobbyRepository.flush();

        log.debug("created lobby with pin " + pin);

        return pin;
    }

    public void adjustSettings(LobbyPut settingsToBeRegistered, Long gamePin) {
        int roundUpdate = settingsToBeRegistered.getRounds();
        List<String> gameModes = settingsToBeRegistered.getGameModes();
        Lobby lobby = getLobbyAndExistenceCheck(gamePin);
        if(gameModes != null){
            setGameModes(gameModes, lobby);
        }
        if(gameModes != null){
            setRounds(roundUpdate, lobby);
        }
        lobbyRepository.save(lobby);
        lobbyRepository.flush();
    }

    private void setRounds(int roundUpdate, Lobby lobby) {
        if(roundUpdate < 5 || roundUpdate > 15){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allowed Number of Rounds are 5 - 15");
        }
        lobby.setNumberRounds(roundUpdate);
    }

    private void setGameModes(List<String> gameModes, Lobby lobby) {
        Set<LobbyModes> lobbyModes = new HashSet<>();
        for(String gameModeNotValidated : gameModes){
            try {
                LobbyModes lobbyMode = LobbyModes.valueOf(gameModeNotValidated);
                lobbyModes.add(lobbyMode);
            } catch(IllegalArgumentException e){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gamemode " + gameModeNotValidated + " is not valid");
            }
        }
        lobby.setLobbyModes(lobbyModes);
    }

    private Lobby getLobbyAndExistenceCheck(Long gamePin) {
        Lobby lobbyToReturn = lobbyRepository.findLobbyByLobbyPin(gamePin);
        if(lobbyToReturn == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The referenced Lobby does not exist");
        }
        return lobbyToReturn;
    }

    private void checkWhetherPlayerInLobby(Long userId) {
        List<Lobby> allLobbies = lobbyRepository.findAll();

        for(Lobby lobby : allLobbies) {
            Set<User> players = lobby.getPlayers();
            if(players.stream().anyMatch(user -> user.getId().equals(userId))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already in a lobby");
            }
        }
    }

    private void setGameState(Lobby lobbyInternal, LobbyGet infoLobbyJson) {
        //TODO REAL LOGIC OF current game state probably with new entity GameRound
        GameDetails gameDetails = new GameDetails();
        gameDetails.setGameState(LobbyState.WAITING.toString());
        infoLobbyJson.setGameDetails(gameDetails);
    }

    private void setGameOver(Lobby lobbyInternal, LobbyGet infoLobbyJson) {
        //TODO set real game over logic
        infoLobbyJson.getGameDetails().setGameOver(false);
    }
    private void setChallengeAndSolution(Lobby lobbyInternal, LobbyGet infoLobbyJson) {
        //TODO real set challenge probably with new entity GameRound with a UUID as a field in Lobby entity
        //containing all information about the round which can be accessed here to
        //provide the gamelogic info in GetLobby object
        //Dummyvalues :
        GameDetails gameDetails = infoLobbyJson.getGameDetails();
        gameDetails.setGameOver(false);
        gameDetails.setChallenge("Who or what is flibbertigibbet ?");
        gameDetails.setGameState("LOBBY");
        gameDetails.setSolution("A chattering person");
        infoLobbyJson.setGameDetails(gameDetails);

    }
    private void setPlayer(Lobby lobbyInternal, LobbyGet infoLobbyJson) {
        GameDetails gameDetails = infoLobbyJson.getGameDetails();
        List<Player> players = new ArrayList<>();
        Set<User> users = lobbyInternal.getPlayers();
        for (User user : users) {
            Player player = new Player();
            player.setName(user.getUsername());
            player.setToken(user.getToken());
            players.add(player);
        }
        gameDetails.setPlayers(players);
    }







}
