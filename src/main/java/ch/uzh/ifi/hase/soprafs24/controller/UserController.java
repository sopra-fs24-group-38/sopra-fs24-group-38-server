package ch.uzh.ifi.hase.soprafs24.controller;



import ch.uzh.ifi.hase.soprafs24.model.request.UserPost;
import ch.uzh.ifi.hase.soprafs24.model.response.UserGet;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequestMapping("/users")
@RestController
public class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserGet> createUser(@RequestBody(required = true) UserPost userToBeCreated) {
        UserGet user = new UserGet();
        user.setToken(UUID.randomUUID().toString());
        return ResponseEntity.ok(user);
    }


}
