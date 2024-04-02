## Websocket Specifications

| Event Listener On | Event Name           | Description                                                                         | Param                                 |
|-------------------|----------------------|-------------------------------------------------------------------------------------|---------------------------------------|
| CLIENT            | user_joined          | indicates to rerender the players in the lobby                                      | `{ "action": "init", "userId": "1" }` |
| SERVER            | lobby_start          | indicates that from now on the “start button” shall be available for the gamemaster |                                       | 
| CLIENT            | game_start           | from 2-10 players in lobby the master has a possibility to start the game           |                                       |
| CLIENT            | definitions_finished | all contributions to the current round received                                     |                                       |
| CLIENT            | votes_finished       | all users votes received                                                            |                                       |
| SERVER            | player_next_round    | user clicks next round                                                              |                                       |
