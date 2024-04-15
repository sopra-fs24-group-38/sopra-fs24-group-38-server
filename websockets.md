## Websocket Specifications

| Event Listener On | Description                                                               | message                               |
|-------------------|---------------------------------------------------------------------------|---------------------------------------|
| SERVER            | inital message                                                            | `{ "action": "init", "userId": "1" }` |
| CLIENT            | indicates to rerender the players in the lobby                            | `user_joined`                         |
| CLIENT            | from 2-10 players in lobby the master has a possibility to start the game | `game_start`                          |
| CLIENT            | user left from lobby                                                      | `{"user_left": "User123"}`            |
| CLIENT            | Client has to make get request to see new gamehost                        | `{"gamehost_left": "User123"}`        |
| CLIENT            | all contributions to the current round received                           | `definitions_finished`                |
| CLIENT            | all users votes received                                                  | `votes_finished`                      |
