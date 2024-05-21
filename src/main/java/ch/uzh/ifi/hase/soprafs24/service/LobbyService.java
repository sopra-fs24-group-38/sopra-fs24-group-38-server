package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyModes;
import ch.uzh.ifi.hase.soprafs24.constant.LobbyState;
import ch.uzh.ifi.hase.soprafs24.model.database.Lobby;
import ch.uzh.ifi.hase.soprafs24.model.database.User;
import ch.uzh.ifi.hase.soprafs24.model.request.LobbyPut;
import ch.uzh.ifi.hase.soprafs24.model.response.*;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.websockets.SocketHandler;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${avatar.ai.number}")
    private int numAvasAi;
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
        if(!checkIfEnoughPlayer(lobby)){
            log.warn("player {} couldnt leave lobby {} because of too less players availabl", user.getUsername(), lobbyId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot leave: there are not enough users left.");
        }
        resetUser(userId);
        lobby.removePlayer(user);

        //check if all users in lobby are AI players if yes delete
        if(isLobbyFullWithBots(lobby)){
            deleteBotLobby(lobby);
            return;
        }


        if(Objects.equals(user.getId(), lobby.getGameMaster()) && !lobby.getUsers().isEmpty()) {
            boolean newGameMasterIsBot = true;
            while(newGameMasterIsBot){
                for(User user1 : lobby.getUsers()){
                    if(!user1.getAiPlayer()){
                        lobby.setGameMasterId(user1.getId());
                        newGameMasterIsBot = false;
                        log.warn(user1.getUsername() + " is new gamemaster");
                    }
                }
            }
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
        if (settingsToBeRegistered.getHideMode() != null) {
            lobby.setHideMode(settingsToBeRegistered.getHideMode());
        }
        lobbyRepository.save(lobby);
        lobbyRepository.flush();
    }


    public void connectTestHomies(Long userId) {
        User user = userService.getUserById(userId);
        Lobby lobby = getLobbyAndExistenceCheck(user.getLobbyId());
        List<User> users = lobby.getUsers();
        for(User u : users){
            log.warn("Homie "+ u.getUsername() + "connected");
            u.setIsConnected(true);
        }
        lobbyRepository.save(lobby);
        lobbyRepository.flush();
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

        if (!Objects.equals(userId, lobby.getGameMaster())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user is not gameMaster");
        }

        socketHandler.sendMessageToLobby(lobby.getLobbyPin(), "game_preparing");
        List<Challenge> challenges = apiService.generateChallenges(lobby.getMaxRoundNumbers(), lobby.getLobbyModes(), lobby.getLobbyPin());
        Collections.shuffle(challenges);
        lobby.setChallenges(challenges);
        apiService.generateAiPlayersDefinitions(lobby);
        lobby.setLobbyState(LobbyState.DEFINITION);
        lobbyRepository.save(lobby);
        lobbyRepository.flush();
        return lobby.getLobbyPin();
    }

    public void resetLobby(Long userId) {
        User user1 = userService.getUserById(userId);
        Lobby lobby = lobbyRepository.findLobbyByLobbyPin(user1.getLobbyId());
        lobby.setLobbyState(LobbyState.WAITING);
        lobby.setGameOver(false);
        lobby.setChallenges(new ArrayList<>());
        lobby.setRoundNumber(1L);

        for (User user : lobby.getUsers()) {
            if(!user.getAiPlayer()) {
                user.setDefinition(null);
                user.setVotedForUserId(null);
                user.setScore(0L);
                user.setWantsNextRound(false);
            }
        }
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

        gameDetails.setRoundNumber(lobby.getRoundNumber());
        gameDetails.setMaxRoundNumbers(lobby.getMaxRoundNumbers());
        gameDetails.setLobbyModes(lobby.getLobbyModes());
        gameDetails.setHideMode(lobby.getHideMode());
        gameDetails.setStatsPlayers(lobby.getStats());

        List<Player> players = new ArrayList<>();
        for (User user : lobby.getUsers()) {
            if(user.getIsConnected()) {
                Player player = objectMapper.convertValue(user, Player.class);
                players.add(player);
            }
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
            persistGameStats(lobby);
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
            if(!user.getIsConnected() && user.getDefinition() == null){
                return;
            }
            if(user.getIsConnected()) {
                if (user.getDefinition() == null && !user.getAiPlayer()) {
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


        for(User aiUser : users) {
            if (aiUser.getAiPlayer() && aiUser.getVotedForUserId() == null) {
                List<Long> userIds = new ArrayList<>();
                userIds.add(0L);
                for (User userx : users)
                    if (!userx.getId().equals(aiUser.getId()))
                        userIds.add(userx.getId());

                if (!userIds.isEmpty()) {
                    int randomIndex = random.nextInt(userIds.size());
                    aiUser.setVotedForUserId(userIds.get(randomIndex));
                    //test
                }
            }
        }

        for(User user : users) {
            if(!user.getIsConnected() && user.getVotedForUserId() == null){
                return;
            }
            if(user.getIsConnected()) {
                if (user.getVotedForUserId() == null) {
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

    public void removeAiPlayer(Long gamePin, Long avatarId) {
        Lobby lobby = getLobbyAndExistenceCheck(gamePin);
        checkIfAvatarIdValidAIPlayer(avatarId, lobby);
        List<User> users = lobby.getUsers();
        User userToBeRemoved = null;
        for(User user : users) {
            if (user.getAvatarId().equals(avatarId)) {
                userToBeRemoved = user;
            }
        }
        if(userToBeRemoved != null){
            users.remove(userToBeRemoved);
            userService.deleteUser(userToBeRemoved.getId());
            socketHandler.sendMessageToLobby(gamePin, "{\"ai_removed\": \"" + userToBeRemoved.getAvatarId() + "\"}");
        }
    }

    public void newGameReset(Long userId) {
        User user = userService.getUserById(userId);
        Lobby lobby = getLobbyAndExistenceCheck(user.getLobbyId());
        lobby.setLobbyState(LobbyState.WAITING);
        Set<LobbyModes> defaultModeSetting = new HashSet<>();
        defaultModeSetting.add(LobbyModes.BIZARRE);
        lobby.setLobbyModes(defaultModeSetting);
        lobby.setRoundNumber(10L);
        lobbyRepository.flush();
    }

    private void checkIfAvatarIdValidAIPlayer(Long avatarId, Lobby lobby) {
        if(avatarId < 100 || avatarId > 100 + numAvasAi - 1){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a valid Avatar ID");
        }

        for(User user : lobby.getUsers()){
            if(Objects.equals(user.getAvatarId(), avatarId)) return;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no AI player with that AvaID in the lobby");

    }

    private void resetLobbyAndNextRoundBool(Lobby lobby, List<User> users) {
        //next round bool
        for(User user: users){
            if(!user.getAiPlayer()) {
                user.setWantsNextRound(false);
                user.setVotedForUserId(null);
                user.setDefinition(null);
            }
            else{
                user.setDefinition(user.dequeueAiDefinition());
                user.setVotedForUserId(null);
            }
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
                        user.addPermanentScore(2L);
                        user.addPermanentFools(1L);
                    }
                }
            }
            if(user.getVotedForUserId() != null) {
                if (user.getVotedForUserId().equals(0L)) {
                    user.setScore(user.getScore() + 1L);
                    user.addPermanentScore(1L);
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
        lobby.setUsers(null);
        for(User user : users){
            userService.deleteUser(user.getId());
        }
        lobbyRepository.delete(lobby);
        lobbyRepository.flush();
        log.warn("Deleted lobby with ID {} because it was full with bots", lobby.getLobbyPin());
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

    private boolean checkIfEnoughPlayer(Lobby lobby) {
        if(lobby.getLobbyState() == LobbyState.WAITING || lobby.getLobbyState() == LobbyState.GAMEOVER){
            return true;
        }
        int humanPlayer = 0;
        for(User user: lobby.getUsers()){
            if(!user.getAiPlayer()){
                humanPlayer+= 1;
            }
        }
        if(lobby.getUsers().size() == 2 && humanPlayer == 2){
            return false;
        }
        return true;
    }

    private void persistGameStats(Lobby lobby) {
        ArrayList<GameStatsPlayer> stats = new ArrayList<>();
        for(User user : lobby.getUsers()){
            GameStatsPlayer statsPlayer = new GameStatsPlayer();
            statsPlayer.setAvatarId(user.getAvatarId());
            statsPlayer.setScore(user.getScore());
            statsPlayer.setUserName(user.getUsername());
            stats.add(statsPlayer);
        }
        lobby.setStats(stats);
        lobbyRepository.save(lobby);
        lobbyRepository.flush();
    }


}
