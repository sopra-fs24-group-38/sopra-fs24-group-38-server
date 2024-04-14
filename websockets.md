## Websocket Specifications

| Event Listener On | Event Name           | Description                                                               | Param                                 |
|-------------------|----------------------|---------------------------------------------------------------------------|---------------------------------------|
| SERVER            |                      | inital message                                                            | `{ "action": "init", "userId": "1" }` |
| CLIENT            | user_joined          | indicates to rerender the players in the lobby                            |                                       |
| CLIENT            | game_start           | from 2-10 players in lobby the master has a possibility to start the game |                                       |
| CLIENT            | new_gamehost         | Client has to make get request to see new gamehost                        |                                       |
| CLIENT            | definitions_finished | all contributions to the current round received                           |                                       |
| CLIENT            | votes_finished       | all users votes received                                                  |                                       |
