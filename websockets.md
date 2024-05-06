## Websocket Specifications

| Event Listener On | Description                                                                           | message                             |
|-------------------|---------------------------------------------------------------------------------------|-------------------------------------|
| SERVER            | inital message                                                                        | { "action": "init", "userId": "1" } |
| CLIENT            | indicates to rerender the players in the lobby                                        | user_joined                         |
| CLIENT            | from 2-10 players in lobby the master has a possibility to start the game             | game_start                          |
| CLIENT            | user left from lobby                                                                  | {"user_left": "User123"}            |
| CLIENT            | Client has to make get request to see new gamehost                                    | {"gamehost_left": "User123"}        |
| CLIENT            | indicates that the game start endpoint has been called and the lobby will be prepared | game_preparing                      |
| CLIENT            | all contributions to the current round received                                       | definitions_finished                |
| CLIENT            | all users votes received                                                              | votes_finished                      |
| CLIENT            | all nextRound calls received                                                          | next_round                          |
| CLIENT            | game is over go to end screen                                                         | game_over                           |     
| CLIENT            | chatGPT API unavailable                                                               | api_unavailable                     |     