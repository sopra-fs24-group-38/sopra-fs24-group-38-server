package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyState;
import ch.uzh.ifi.hase.soprafs24.model.database.Lobby;
import ch.uzh.ifi.hase.soprafs24.model.database.User;
import ch.uzh.ifi.hase.soprafs24.model.request.AiPlayerDelete;
import ch.uzh.ifi.hase.soprafs24.model.request.DefinitionPost;
import ch.uzh.ifi.hase.soprafs24.model.request.LobbyPut;
import ch.uzh.ifi.hase.soprafs24.model.request.VotePost;
import ch.uzh.ifi.hase.soprafs24.model.response.LobbyGet;
import ch.uzh.ifi.hase.soprafs24.model.response.LobbyGetId;
import ch.uzh.ifi.hase.soprafs24.service.LobbyService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.websockets.SocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/lobbies")
@RestController
public class LobbyController {

    @Autowired
    SocketHandler socketHandler;

    @Autowired
    LobbyService lobbyService;

    @Autowired
    UserService userService;


    private final Logger log = LoggerFactory.getLogger(LobbyController.class);

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    /**
     * Endpoint solely for testing purposes
     * */
    @PutMapping(value = "/testws/{gamePin}")
    @ResponseStatus(HttpStatus.OK)
    public void testws(@PathVariable Long gamePin){
        socketHandler.sendMessageToLobby(gamePin, "HELLO");
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LobbyGetId> createLobby(@RequestHeader(value = "Authorization") String token){

        Long userId = userService.getUserIdByTokenAndAuthenticate(token);


        LobbyGetId lobbyGetId = new LobbyGetId();

        Long gamePin = lobbyService.createLobby(userId);

        lobbyGetId.setGamePin(gamePin);

        return ResponseEntity.status(HttpStatus.CREATED).body(lobbyGetId);
    }

    @PutMapping(value = "/users/{gamePin}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LobbyGetId> joinLobby(@RequestHeader(value = "Authorization") String token, @PathVariable Long gamePin){

        Long userId = userService.getUserIdByTokenAndAuthenticate(token);
        lobbyService.addPlayerToLobby(userId, gamePin);
        socketHandler.sendMessageToLobby(gamePin, "user_joined");
        LobbyGetId lobbyGetId = new LobbyGetId();
        lobbyGetId.setGamePin(gamePin);

        return ResponseEntity.status(HttpStatus.OK).body(lobbyGetId);
    }

    @PutMapping(value = "/users/{gamePin}/ai",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LobbyGetId> joinLAiLobby(@RequestHeader(value = "Authorization") String token, @PathVariable Long gamePin){
        Long userId = userService.getUserIdByTokenAndAuthenticate(token);
        lobbyService.checkState(userId, LobbyState.WAITING);
        lobbyService.addAiPlayerToLobby(gamePin);
        socketHandler.sendMessageToLobby(gamePin, "user_joined");
        LobbyGetId lobbyGetId = new LobbyGetId();
        lobbyGetId.setGamePin(gamePin);
        return ResponseEntity.status(HttpStatus.OK).body(lobbyGetId);
    }

    @DeleteMapping(value = "/users/{gamePin}/ai",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LobbyGetId> deleteLAiLobby(@RequestHeader(value = "Authorization") String token, @PathVariable Long gamePin,
    @Valid @RequestBody() AiPlayerDelete aiPlayerDelete){
        Long userId = userService.getUserIdByTokenAndAuthenticate(token);
        lobbyService.checkState(userId, LobbyState.WAITING);
        lobbyService.removeAiPlayer(gamePin, aiPlayerDelete.getAvatarId());
        socketHandler.sendMessageToLobby(gamePin, "user_joined");
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/{gamePin}")
    @ResponseStatus(HttpStatus.OK)
    public void adjustLobbySettings(@RequestHeader(value = "Authorization") String token, @PathVariable Long gamePin,
                                    @Valid @RequestBody LobbyPut settingsToBeRegistered){

        Long userId = userService.getUserIdByTokenAndAuthenticate(token);
        lobbyService.adjustSettings(settingsToBeRegistered, gamePin);

        System.out.println("user with token " + token + " adjusted settings of lobby with pin "+ gamePin);
    }

    @GetMapping(value = "/{gamePin}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LobbyGet> getLobby(@RequestHeader(value = "Authorization") String token, @PathVariable Long gamePin){
        log.warn("user with token " + token + " requested information of lobby with pin "+ gamePin);
        Long userId = userService.getUserIdByTokenAndAuthenticate(token);
        return ResponseEntity.status(HttpStatus.OK).body(lobbyService.getLobbyInfo(gamePin));
    }

    @PutMapping(value = "/users/definitions",  consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void registerDefinition(@RequestHeader(value = "Authorization") String token,
                                   @Valid @RequestBody() DefinitionPost definitionToBeRegistered){
        Long userId = userService.getUserIdByTokenAndAuthenticate(token);
        lobbyService.checkState(userId, LobbyState.DEFINITION);
        userService.registerDefinitions(definitionToBeRegistered, userId);
    }

    @PutMapping(value = "/users/votes", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void registerVote(@RequestHeader(value = "Authorization") String token,
                             @Valid @RequestBody() VotePost voteToBeRegistered){

        Long userId = userService.getUserIdByTokenAndAuthenticate(token);
        lobbyService.checkState(userId, LobbyState.VOTE);
        userService.registerVote(userId, voteToBeRegistered.getVote());
        System.out.println("user with token "+ token + " registered vote: " + voteToBeRegistered.getVote());
    }

    @DeleteMapping(value = "/users/{gamePin}",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<LobbyGetId> leaveLobby(@RequestHeader(value = "Authorization") String token, @PathVariable Long gamePin){

        Long userId = userService.getUserIdByTokenAndAuthenticate(token);

        lobbyService.removePlayerFromLobby(userId, gamePin);

        LobbyGetId lobbyGetId = new LobbyGetId();
        lobbyGetId.setGamePin(gamePin);

        return ResponseEntity.status(HttpStatus.OK).body(lobbyGetId);
    }

    @PostMapping(value = "/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> startGame(@RequestHeader(value = "Authorization") String token) {
        Long userId = userService.getUserIdByTokenAndAuthenticate(token);
        lobbyService.checkState(userId, LobbyState.WAITING);
        Long lobbyId = lobbyService.startGame(userId);
        socketHandler.sendMessageToLobby(lobbyId, "game_start");
        return ResponseEntity.ok().build();

    }
    @PostMapping(value = "/rounds/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> nextRound(@RequestHeader(value = "Authorization") String token) {

        Long userId = userService.getUserIdByTokenAndAuthenticate(token);
        lobbyService.checkState(userId, LobbyState.EVALUATION);
        lobbyService.registerNextRound(userId);

        return ResponseEntity.ok().build();
    }

}
