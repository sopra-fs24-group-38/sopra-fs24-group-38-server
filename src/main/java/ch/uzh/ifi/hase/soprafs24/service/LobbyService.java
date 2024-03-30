package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.model.database.Lobby;
import ch.uzh.ifi.hase.soprafs24.model.database.User;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.Set;

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
    public LobbyService(@Qualifier("lobbyRepository") LobbyRepository lobbyRepository) {
        this.lobbyRepository = lobbyRepository;
    }

    public void addPlayerToLobby(Long userId, Long lobbyId) {
        Lobby lobby = lobbyRepository.findLobbyByLobbyPin(lobbyId);
        lobby.addPlayer(userService.getUserById(userId));
        log.debug("user with id " + userId + " joined lobby " + lobbyId);
    }

    public Set<User> getPlayerSet(Long lobbyId) {
        Lobby lobby = lobbyRepository.findLobbyByLobbyPin(lobbyId);
        return lobby.getPlayers();
    }

    public Long createLobby(Long userId) {

        Lobby lobby = new Lobby();
        Long pin;

        //ensure the pin has not been set yet
        do {
            pin = (long) (random.nextInt(9000) + 1000);
        } while (lobbyRepository.findLobbyByLobbyPin(pin) != null);

        lobby.setLobbyPin(pin);
        lobby.addPlayer(userService.getUserById(userId));
        lobbyRepository.save(lobby);
        lobbyRepository.flush();

        log.debug("created lobby with pin " + pin);

        return pin;
    }
}
