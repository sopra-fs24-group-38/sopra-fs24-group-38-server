package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.model.database.Lobby;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("lobbyRepository")
public interface LobbyRepository extends JpaRepository<Lobby, Long> {

    Lobby findLobbyByLobbyPin(Long id);
}
