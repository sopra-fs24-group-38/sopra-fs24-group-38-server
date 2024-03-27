package ch.uzh.ifi.hase.soprafs24.controller;


import ch.uzh.ifi.hase.soprafs24.model.database.User;
import ch.uzh.ifi.hase.soprafs24.model.request.UserPost;
import ch.uzh.ifi.hase.soprafs24.model.response.UserResponse;
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
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody(required = true) UserPost userToBeCreated) {

        UserResponse userResponse = userService.createUser(userToBeCreated);

        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> login(@Valid @RequestBody(required = true) UserPost userToBeLoggedIn) {

        UserResponse userResponse = userService.loginUser(userToBeLoggedIn);

        return ResponseEntity.status(HttpStatus.OK).body(userResponse);
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
