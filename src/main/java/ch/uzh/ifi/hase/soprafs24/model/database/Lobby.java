package ch.uzh.ifi.hase.soprafs24.model.database;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "LOBBY")
public class Lobby {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

}
