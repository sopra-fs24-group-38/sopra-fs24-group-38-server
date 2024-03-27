package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.Application;
import ch.uzh.ifi.hase.soprafs24.model.request.UserPost;
import ch.uzh.ifi.hase.soprafs24.model.response.LobbyGet;
import ch.uzh.ifi.hase.soprafs24.model.response.LobbyGetId;
import ch.uzh.ifi.hase.soprafs24.model.response.UserGet;
import ch.uzh.ifi.hase.soprafs24.service.LobbyService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
        LobbyGetId lobbyGetId = new LobbyGetId();
        lobbyGetId.setGamePin(1234L);
        System.out.println("user with token " + token.toString() + " adjusted settings of lobby with pin "+ gamePin);

    }

    @GetMapping(value = "/{gamePin}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LobbyGet> getLobby(@RequestHeader(required = true, value = "Authorization") UUID token) {

    }



}
