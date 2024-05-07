package ch.uzh.ifi.hase.soprafs24.controller;


import ch.uzh.ifi.hase.soprafs24.model.request.UserPost;
import ch.uzh.ifi.hase.soprafs24.model.response.UserResponse;
import ch.uzh.ifi.hase.soprafs24.model.response.allUsersScores;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequestMapping("/users")
@RestController
public class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserPost userToBeCreated) {

        UserResponse userResponse = userService.createUser(userToBeCreated);

        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> login(@Valid @RequestBody UserPost userToBeLoggedIn) {

        UserResponse userResponse = userService.loginUser(userToBeLoggedIn);

        return ResponseEntity.status(HttpStatus.OK).body(userResponse);
    }

    @GetMapping(value = "/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(@RequestHeader(value = "Authorization") String token) {

        userService.logout(token);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<allUsersScores>> getAllUsers(@RequestHeader(value = "Authorization") String token) {
        List<allUsersScores> users =  userService.getAllUsers(token);
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

}
