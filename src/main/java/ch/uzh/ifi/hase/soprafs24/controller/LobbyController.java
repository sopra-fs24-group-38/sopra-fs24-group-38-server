package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.Application;
import ch.uzh.ifi.hase.soprafs24.model.request.DefinitionPost;
import ch.uzh.ifi.hase.soprafs24.model.request.UserPost;
import ch.uzh.ifi.hase.soprafs24.model.request.VotePost;
import ch.uzh.ifi.hase.soprafs24.model.response.*;
import ch.uzh.ifi.hase.soprafs24.service.LobbyService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
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

    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LobbyGetId> createLobby(@RequestHeader(required = true, value = "Authorization") UUID token){

        //TODO real creation logic in lobbyService class
        LobbyGetId lobbyGetId = new LobbyGetId();
        lobbyGetId.setGamePin(1234L);
        System.out.println("created lobby");

        return ResponseEntity.status(HttpStatus.CREATED).body(lobbyGetId);
    }

    @PutMapping(value = "/users",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LobbyGetId> joinLobby(@RequestHeader(required = true, value = "Authorization") UUID token){

        //TODO real joining logic in lobbyService class
        LobbyGetId lobbyGetId = new LobbyGetId();
        lobbyGetId.setGamePin(1234L);
        System.out.println("user with token " + token.toString() + " joined lobby");

        return ResponseEntity.status(HttpStatus.OK).body(lobbyGetId);
    }

    @PutMapping(value = "/{gamePin}")
    @ResponseStatus(HttpStatus.OK)
    public void adjustLobbySettings(@RequestHeader(required = true, value = "Authorization") UUID token,
                                    @PathVariable Long gamePin){

        //TODO real adjusting settings logic in lobbyService class

        System.out.println("user with token " + token.toString() + " adjusted settings of lobby with pin "+ gamePin);

    }

    @GetMapping(value = "/{gamePin}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LobbyGet> getLobby(@RequestHeader(required = true, value = "Authorization") UUID token,
                                             @PathVariable Long gamePin){
        System.out.println("user with token " + token.toString() + " requested information of lobby with pin "+ gamePin);

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
    public void registerDefinition(@RequestHeader(required = true, value = "Authorization") UUID token,
                                   @Valid @RequestBody(required = true) DefinitionPost definitionToBeRegistered){

        //TODO real Definition registration Logic within lobbyService:
        System.out.println("user with token "+ token + " registered definition: " + definitionToBeRegistered.getDefinition());

    }

    @PutMapping(value = "/users/votes", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void registerVote(@RequestHeader(required = true, value = "Authorization") UUID token,
                             @Valid @RequestBody(required = true) VotePost voteToBeRegistered){
        //TODO real Vote registration Logic within lobbyService:
        System.out.println("user with token "+ token + " registered vote: " + voteToBeRegistered.getVote());
    }

}
