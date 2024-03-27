package ch.uzh.ifi.hase.soprafs24.controller;


import ch.uzh.ifi.hase.soprafs24.model.request.UserPost;
import ch.uzh.ifi.hase.soprafs24.model.response.UserGet;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.UUID;

@RequestMapping("/users")
@RestController
public class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserGet> createUser(@Valid @RequestBody(required = true) UserPost userToBeCreated) {

        //TODO real creation logic in userService class
        UserGet user = new UserGet();
        user.setToken(UUID.randomUUID().toString());
        user.setId(23L);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserGet> login(@Valid @RequestBody(required = true) UserPost userToBeLoggedIn) {

        //TODO real login logic in userService class
        UserGet user = new UserGet();
        user.setToken(UUID.randomUUID().toString());
        user.setId(23L);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping(value = "/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(@RequestHeader(required = true, value = "Authorization") UUID token) {

        //TODO real logout logic in userService class
        System.out.println("logged out user");

    }

    @PutMapping(value = "/{userID}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@RequestHeader(required = true, value = "Authorization") UUID token,
                       @PathVariable int userID) {

        //TODO real PUT operation logic in userService class
        System.out.println("updated user " + userID);


    }

    @DeleteMapping(value = "/{userID}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@RequestHeader(required = true, value = "Authorization") UUID token,
                       @PathVariable int userID) {

        //TODO real PUT operation logic in userService class
        System.out.println("deleted user " + userID);


    }

}
