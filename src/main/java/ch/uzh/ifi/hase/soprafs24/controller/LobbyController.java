package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.model.request.DefinitionPost;
import ch.uzh.ifi.hase.soprafs24.model.request.LobbyPut;
import ch.uzh.ifi.hase.soprafs24.model.request.VotePost;
import ch.uzh.ifi.hase.soprafs24.model.response.GameDetails;
import ch.uzh.ifi.hase.soprafs24.model.response.LobbyGet;
import ch.uzh.ifi.hase.soprafs24.model.response.LobbyGetId;
import ch.uzh.ifi.hase.soprafs24.model.response.Player;
import ch.uzh.ifi.hase.soprafs24.service.LobbyService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.websockets.SocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.UUID;

@RequestMapping("/lobbies")
@RestController
public class LobbyController {

    @Autowired
    SocketHandler socketHandler;

    @Autowired
    LobbyService lobbyService;

    @Autowired
    UserService userService;

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
        userService.setLobbyIdForGameMaster(userId, gamePin);
        lobbyGetId.setGamePin(gamePin);

        return ResponseEntity.status(HttpStatus.CREATED).body(lobbyGetId);
    }

    @PutMapping(value = "/users/{gamePin}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LobbyGetId> joinLobby(@RequestHeader(value = "Authorization") String token, @PathVariable Long gamePin){

        Long userId = userService.getUserIdByTokenAndAuthenticate(token);
        socketHandler.sendMessageToLobby(gamePin, "user_joined");
        lobbyService.addPlayerToLobby(userId, gamePin);

        LobbyGetId lobbyGetId = new LobbyGetId();
        lobbyGetId.setGamePin(gamePin);

        return ResponseEntity.status(HttpStatus.OK).body(lobbyGetId);
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
        System.out.println("user with token " + token + " requested information of lobby with pin "+ gamePin);

        //TODO real Get Logic with lobbyService:

        LobbyGet lobbyGet = new LobbyGet();
        lobbyGet.setGamePin(1234L);

        GameDetails gameDetails = new GameDetails();
        gameDetails.setGameOver(false);
        gameDetails.setChallenge("Who or what is flibbertigibbet ?");
        gameDetails.setGameState("LOBBY");
        gameDetails.setSolution("A chattering person");

        Player player1 = new Player();
        player1.setName("Harris");
        player1.setDefinition("To Help someone");
        player1.setToken(UUID.randomUUID().toString());
        player1.setScore(12);
        player1.setVotedFor(1);
        Player player2 = new Player();
        player2.setName("Markus");
        player2.setDefinition("a rare bird");
        player2.setToken(UUID.randomUUID().toString());
        player2.setScore(35);
        player2.setVotedFor(3);
        ArrayList<Player> player = new ArrayList<>();
        player.add(player1);
        player.add(player2);

        gameDetails.setPlayers(player);
        lobbyGet.setGameDetails(gameDetails);
        return ResponseEntity.status(HttpStatus.OK).body(lobbyGet);

    }

    @PutMapping(value = "/users/definitions",  consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void registerDefinition(@RequestHeader(value = "Authorization") String token,
                                   @Valid @RequestBody() DefinitionPost definitionToBeRegistered){

        //TODO real Definition registration Logic within lobbyService:
        System.out.println("user with token "+ token + " registered definition: " + definitionToBeRegistered.getDefinition());

    }

    @PutMapping(value = "/users/votes", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void registerVote(@RequestHeader(value = "Authorization") String token,
                             @Valid @RequestBody() VotePost voteToBeRegistered){
        //TODO real Vote registration Logic within lobbyService:
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

        lobbyService.startGame(userId);

        return ResponseEntity.ok().build();

    }

}
