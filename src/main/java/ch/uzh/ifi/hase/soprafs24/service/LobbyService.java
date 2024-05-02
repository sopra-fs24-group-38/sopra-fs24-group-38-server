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
import ch.uzh.ifi.hase.soprafs24.websockets.SocketHandler;
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
    private ApiService apiService;

    @Autowired
    private AIPlayerService aiPlayerService;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public LobbyService(@Qualifier("lobbyRepository") LobbyRepository lobbyRepository) {
        this.lobbyRepository = lobbyRepository;
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void addPlayerToLobby(Long userId, Long lobbyId) {
        Lobby lobby = getLobbyAndExistenceCheck(lobbyId);
        performPlayerNumberCheck(lobby);
        User user = userService.getUserById(userId);
        checkIfPlayerInLobby(userId);
        lobby.addPlayer(userService.getUserById(userId));
        userService.setLobbyId(userId, lobbyId);
        userService.setAvatarPin(userId);
        log.warn("user with id " + userId + " joined lobby " + lobbyId);
    }

    public void addAiPlayerToLobby(Long gamePin) {
        Lobby lobby = getLobbyAndExistenceCheck(gamePin);
        performPlayerNumberCheck(lobby);
        User aiUser = aiPlayerService.createAiUser(gamePin);
        lobby.addPlayer(aiUser);
    }


    public void removePlayerFromLobby(Long userId, Long lobbyId) {
        Lobby lobby = getLobbyAndExistenceCheck(lobbyId);
        User user = userService.getUserById(userId);

        //check if user in lobby or user AI player
        if (!lobby.getUsers().contains(user))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not in the specified lobby");
        if (user.getAiPlayer())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong endpoint for removing AI player");

        resetUser(userId);
        lobby.removePlayer(user);

        //check if all users in lobby are AI players if yes delete
        if(isLobbyFullWithBots(lobby)){
            deleteBotLobby(lobby);
            return;
        }
        
        if(Objects.equals(user.getId(), lobby.getGameMaster()) && !lobby.getUsers().isEmpty()) {
            lobby.setGameMasterId(lobby.getUsers().get(0).getId());
            lobbyRepository.save(lobby);
            lobbyRepository.flush();
            socketHandler.sendMessageToLobby(lobbyId, "{\"gamehost_left\": \"" + user.getUsername() + "\"}");
        }
        else socketHandler.sendMessageToLobby(lobbyId, "{\"user_left\": \"" + user.getUsername() + "\"}");
        log.warn("user with id " + userId + " removed from lobby " + lobbyId);
    }

    public List<User> getUsers(Long lobbyId) {
        Lobby lobby = lobbyRepository.findLobbyByLobbyPin(lobbyId);
        return lobby.getUsers();
    }

    public Long createLobby(Long userId) {

        Lobby lobby = new Lobby();
        Long pin;

        //ensure the pin has not been set yet
        do {
            pin = (long) (random.nextInt(9000) + 1000);
        } while (lobbyRepository.findLobbyByLobbyPin(pin) != null);

        lobby.setLobbyPin(pin);
        lobby.setGameMasterId(userId);
        lobby.addPlayer(userService.getUserById(userId));
        lobby.setLobbyState(LobbyState.WAITING);
        lobby.setGameOver(false);
        lobby.setRoundNumber(1L);
        lobbyRepository.save(lobby);
        lobbyRepository.flush();

        userService.setLobbyId(userId, pin);
        userService.setAvatarPin(userId);
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
        if (roundUpdate != 0) {
            setRounds(roundUpdate, lobby);
        }
        lobbyRepository.save(lobby);
        lobbyRepository.flush();
    }

    private void setRounds(int roundUpdate, Lobby lobby) {
        if (roundUpdate < 3 || roundUpdate > 15) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allowed Number of Rounds are 5 - 15");
        }
        lobby.setMaxRoundNumbers(roundUpdate);
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The referenced Lobby does not exist lobbyPin: " +gamePin);
        }
        return lobbyToReturn;
    }

    public Long startGame(Long userId) {

        User user = userService.getUserById(userId);
        Lobby lobby = getLobbyAndExistenceCheck(user.getLobbyId());
        socketHandler.sendMessageToLobby(lobby.getLobbyPin(), "game_preparing");

        if (!Objects.equals(userId, lobby.getGameMaster())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user is not gameMaster");
        }

        lobby.setChallenges(apiService.generateChallenges(lobby.getMaxRoundNumbers(), lobby.getLobbyModes()));
        lobby.setLobbyState(LobbyState.DEFINITION);
        lobbyRepository.save(lobby);
        lobbyRepository.flush();
        return lobby.getLobbyPin();
    }



    public LobbyGet getLobbyInfo(Long gamePin) {
        Lobby lobby = getLobbyAndExistenceCheck(gamePin);

        // construct GameDetails object
        GameDetails gameDetails = new GameDetails();

        gameDetails.setChallenge(lobby.getCurrentChallenge());
        gameDetails.setSolution(lobby.getCurrentSolution());
        gameDetails.setGameMode(lobby.getCurrentMode());

        gameDetails.setGameState(lobby.getLobbyState().toString());
        gameDetails.setGameOver(lobby.isGameOver());
        gameDetails.setGameMasterId(lobby.getGameMaster());
        gameDetails.setGameMasterUsername(userService.getUserById(lobby.getGameMaster()).getUsername());

        List<Player> players = new ArrayList<>();
        for (User user : lobby.getUsers()) {
            Player player = objectMapper.convertValue(user, Player.class);
            players.add(player);
        }
        gameDetails.setPlayers(players);

        // construct LobbyGet object
        LobbyGet infoLobbyJson = new LobbyGet();

        infoLobbyJson.setGameDetails(gameDetails);
        infoLobbyJson.setGamePin(gamePin);
        return infoLobbyJson;
    }
    public void registerNextRound(Long userId) {
        User user = userService.getUserById(userId);
        user.setWantsNextRound(true);
        log.warn("next_round vote received from user" + user.getUsername());
        Lobby lobby = getLobbyAndExistenceCheck(user.getLobbyId());

        //check if already nextRound
        List<User> users = lobby.getUsers();
        for(User userReady : users) {
            if (userReady.getIsConnected() && !userReady.getWantsNextRound()) {
                log.warn("not all users in the lobby have submitted next round wish");
                return;
            }
        }

        log.warn("Lobby with id "+ lobby.getLobbyPin() + " reset..");
        resetLobbyAndNextRoundBool(lobby, users);

        if(lobby.getRoundNumber() - 1 >= lobby.getMaxRoundNumbers()) {
            lobby.setLobbyState(LobbyState.GAMEOVER);
            socketHandler.sendMessageToLobby(lobby.getLobbyPin(), "game_over");
        }
        else{
            socketHandler.sendMessageToLobby(lobby.getLobbyPin(), "next_round");
        }

        lobbyRepository.save(lobby);
        lobbyRepository.flush();
    }
    public void checkState(Long userId, LobbyState requiredLobbyState) {
        User user = userService.getUserById(userId);
        Lobby lobby = lobbyRepository.findLobbyByLobbyPin(user.getLobbyId());
        if(lobby.getLobbyState() != requiredLobbyState){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lobby not in state: " + requiredLobbyState.toString());
        }
    }

    public void checkIfAllDefinitionsReceived(Long lobbyId) {
        Lobby lobby = getLobbyAndExistenceCheck(lobbyId);
        List<User> users = lobby.getUsers();
        for(User user : users) {
            if(user.getIsConnected() == null && user.getDefinition() == null){
                return;
            }
            if(user.getIsConnected() != null) {
                if (user.getIsConnected() && user.getDefinition() == null) {
                    log.warn("not all users in the lobby have submitted their definition");
                    return;
                }
            }
        }
        lobby.setLobbyState(LobbyState.VOTE);
        socketHandler.sendMessageToLobby(lobbyId, "definitions_finished");
    }

    public void checkIfAllVotesReceived(Long lobbyId) {
        Lobby lobby = getLobbyAndExistenceCheck(lobbyId);
        List<User> users = lobby.getUsers();
        for(User user : users) {
            if(user.getIsConnected() == null && user.getVotedForUserId() == null){
                return;
            }
            if(user.getIsConnected() != null) {
                if (user.getIsConnected() && user.getVotedForUserId() == null) {
                    log.warn("not all users in the lobby have submitted their votes");
                    return;
                }
            }
        }
        evaluateVotes(users);
        lobbyRepository.save(lobby);
        lobbyRepository.flush();

        lobby.setLobbyState(LobbyState.EVALUATION);
        socketHandler.sendMessageToLobby(lobbyId, "votes_finished");
    }

    private void resetLobbyAndNextRoundBool(Lobby lobby, List<User> users) {
        //next round bool
        for(User user: users){
            user.setWantsNextRound(false);
            user.setVotedForUserId(null);
            user.setDefinition(null);
        }
        lobby.setRoundNumber(lobby.getRoundNumber()+ 1L);
        lobby.setLobbyState(LobbyState.DEFINITION);
    }

    private void checkIfPlayerInLobby(Long userId) {
        List<Lobby> allLobbies = lobbyRepository.findAll();

        for (Lobby lobby : allLobbies) {
            List<User> users = lobby.getUsers();
            for(User user : users) {
                if(Objects.equals(user.getId(), userId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already in a lobby");
            }
        }
    }

    private void performPlayerNumberCheck(Lobby lobby) {
        int numPlayers = lobby.getUsers().size();
        if(numPlayers >= 5) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lobby full");
    }

    private void evaluateVotes( List<User> users) {
        for(User user : users) {
            for(User userz : users) {
                if(userz.getVotedForUserId() != null) {
                    if (!Objects.equals(user.getToken(), userz.getToken()) && Objects.equals(userz.getVotedForUserId(), user.getId())) {
                        user.setScore(user.getScore() + 2L);
                    }
                }
            }
            if(user.getVotedForUserId() != null) {
                if (user.getVotedForUserId().equals(0L)) {
                    user.setScore(user.getScore() + 1L);
                }
            }
        }
    }
    private void resetUser(Long userId) {
        User user = userService.getUserById(userId);
        user.setDefinition(null);
        user.setVotedForUserId(null);
        user.setScore(0L);
        user.setLobbyId(null);
    }

    private boolean isLobbyFullWithBots(Lobby lobby) {
        List<User> users = lobby.getUsers();
        for(User user : users){
            if(!user.getAiPlayer()){
                return false;
            }
        }
        return true;
    }

    private void deleteBotLobby(Lobby lobby) {
        List<User> users = lobby.getUsers();
        for(User user : users){
            userService.deleteUser(user.getId());
        }
        lobbyRepository.delete(lobby);
        lobbyRepository.flush();
        log.warn("Deleted lobby with ID {} because it was full with bots", lobby.getLobbyPin());
    }


}
