# 28.03 - 11.04
## Harris A
-   Added Registration functionality:
    -   ([#28](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/28))
    -   ([#31](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/31))
- Call with Elia to align on fundamental architectural decisions
- Call with Samuel to pair-program on Websocket config
- Added CreateLobby and JoinLobby Functionality:
    -   ([#39](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/39))
    -   ([#47](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/47))
    -   ([#40](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/40))
    -   ([#54](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/54))
-   Backend provides a functioning websockets connection (also deployed gc) in pair programming together with Elia and Samuel:
    - [#52](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/52)
## Elia Aeberhard
-   Added login functionality:
    -   ([#37](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/37))
    -   ([#38](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/38))
-   Added check for players who want to join a lobby whether they are already in a lobby
    - ([#53](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/53))
-   Backend provides a functioning websockets connection (also deployed gc) in pair programming together with Harris and Samuel:
    - [#52](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/52)
    -   ([#53](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/53))
-   Added functionality (endpoint) to remove a player from a lobby
    -   ([#56](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/56))
## Markus Senn
-   Revamped client project structure
-   Login password obfuscation + User token:
    -   ([#18](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/18))
    -   ([#17](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/17))
-   Error handling capabilities added:
    -   ([#19](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/19))
-   Added Logout functionality:
    -   ([#28](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/28))
-   Added dynamic game PIN in Lobby:
    -   ([#23](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/23))
## Cédric Styner
-   Login & Registration:
    -   ([#16](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/16))
    -   ([#15](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/15))
    -   ([#20](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/20))
-   Challange and forwarding to Voting:
    -   ([#32](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/32))
    -   ([#34](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/34))
    -   ([#35](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/35))
## Samuel Frank
-   Cleaned up the template:
    - Adjusted service, controller and tests ([#145](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/145))
    - Introduced contributions.md file ([#145](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/145))
-   Allow for modification of lobby entity with modes PUT /lobbies/{lobbyId} :
    - [#45](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/45)
    - [#44](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/44)
-   Provide Information over GET /lobbies for rendering names of player in joining lobby :
    - [#172](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/172)
-   Backend provides a functioning websockets connection (also deployed gc) in pair programming together with Elia and Harris:
    - [#52](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/52)

# 11.04 - 18.04
## Harris A
- started with startGame functionality:
    -  [#177](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/177)
    -  [#61](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/61)
- pair-programming session regarding gameStart, definitions, api call
    -  [#66](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/66)

## Elia Aeberhard
- Added endpoint for removal and api call functionality
    -  [#58](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/58)
    -  [#70](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/70)
## Markus Senn
Basically finished Lobby component
- added settings page
    -  [#21](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/21)
    -  [#30](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/30)
- added websocket connection
    -  [#27](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/27)
- added functionality to join existing lobby with PIN
    -  [#26](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/26)
- added functionality to see players join and leave
    -  [#24](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/24)
- added functionality to start a game session and redirect players to next section
    -  [#29](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/29)
    -  [#31](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/31)
- added route protection for existing lobbies
    -  [#25](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/25)

- minor visual fixes

## Cédric Styner

- Gameflow programmed. Different issues closed in the field of definition, voting and score
    -  [#36](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/36)
    -  [#49](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/49)
    -  [#51](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/51)
    -  [#52](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/52)


## Samuel Frank

- pair-programming session regarding gameStart, definitions, api call
    -  [#66](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/66)
- Lobby full check: 
    -  [#62](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/62)
- Gamelogic evaluation with points after votes:
    - [#81](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/81)

# 19.04 - 25.04
## Harris A
- Pair-programming session extending api call
    - [#70](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/70)
    - [#69](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/69)
- Store player scores
    - [#92](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/92)
- Notify frontend to advance to voting
    - [#72](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/72)
## Elia Aeberhard
- Pair-programming extended API call functionality
    - [#70](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/70)
- Receive votes functionality
    - [#80](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/80)
- Added game over notification to the frontend
    - [#91](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/91)
- Closed issue 100 since not implemented (unnecessary)
    - [#100](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/100)
## Markus Senn
- Created a game end view with a scoreboard
    - [#48](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/48)
- Added rules button content + functionality
    - [#54](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/54)
- Fixes for design and minor bugs
    - [#55](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/55)
    - [#64](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/64)

## Cédric Styner

- Gameflow finished.
    -  [#41](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/41)
    -  [#42](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/42)
    -  [#43](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/43)
    -  [#44](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/44)
    -  [#45](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/45)
    -  [#46](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/46)

## Samuel Frank
- Pair-programming session extending api call
    - [#71](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/71)
- Count player votes and notify frontend
    - [#82](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/82)
- Provide player score via API
    - [#87](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/87)
    - [#101](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/101)

# 26.04 - 02.05
## Elia Aeberhard
- Adjusted getLobby endpoint to exclude not-connected players
    - [#217](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/217)
- Added check for repeated words by chatGPT api
    - [#210](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/210)

## Markus Senn
- Added support for multiple modes selection
    - [#89](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/89)
- Updated Rules and Settings content + design
    - [#59](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/59)
    - [#91](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/91)
- Bugfix for feedback system
    - [#98](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/98)

## Samuel Frank
- Additional Gamemodes 
    - [#207](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/207)
    - [#108](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/108)
    - [#208](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/208)
- Started with AI player and finished lobby join logic for AI player 
    - [#129](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/129)

## Harris A
- started with additional Gamemode (exclusive) - discussion / different implementation ongoing:
    -  [#103](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/103)
    -  [#105](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/105)
    -  [#108](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/108)

## Cédric Styner

- worked on reliability.
    -  [#96](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/96)
    -  [#97](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/97)

# 03.05 - 09.05
## Elia Aeberhard
- Pair Programming on reliability regarding starting a new game
    - [#233](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/233)
- Pair Programming on overall stats and leaderboard
    - [#124](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/124)
    - [#123](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/123)
- Pair Programming on api fallback data and api availability
    - [#112](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/112)
    - [#111](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/111)
    - [#110](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/110)
- Added userService unit tests

## Harris A
- Pair Programming on reliability regarding starting a new game
    - [#233](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/233)
- Pair Programming on overall stats and leaderboard
    - [#124](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/124)
    - [#123](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/123)
- The backend always returns lowerCase definitions
    - [#231](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/231)
- Pair Programming on api fallback data and api availability
    - [#112](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/112)
    - [#111](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/111)
    - [#110](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/110)

## Samuel Frank
- Implemented AI Player logic (definition and vote) including new requests to chatGPT API
    - [#130](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/130)
    - [#131](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/131)
 - Improe the challenge fetching process 
    - [#236](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/236) Even distribution
    - [#237](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/237) Challenge shuffling

- Introduced Subcategories for ChatGpt request in order to avoid reappearing words 
    - [#210](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/210)

- Lower cased AI definitions: 
    - [#238](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/238)

## Markus Senn
- Introduced logic for lobby AI player addition/removal
    - [#62](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/62)
    - [#63](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/63)
    - [#104](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/104)
    - [#105](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/105)
- Made settings data persistent between mounts
    - [#102](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/102)
- Added leaderboard
    - [#60](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/60)
    - [#106](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/106)

## Cédric Styner

- worked on reliability.
    -  [#110](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/110)
- Add Roundinformations
    -  [#87](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/87)
    -  [#88](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/88)

    
# 10.05 - 17.05
## Harris A
- Added additional status info in the get Lobby info Endpoint
    - [#257](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/257)
- add UserStatus Endpoint for better error path handling
    - [#256](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/256)
- reviewed the new ChatGPT GPT 4o model locally, decided against implementation with Elia and Samuel

## Elia Aeberhard
- Added rejection functionality in case the submitted definition is equal to the solution
    - [#140](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/140)
- Added user service test for the reject-correct-definition issue
    - [#140](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/140)
- Changed test names to meaningful names
    - [#254](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/254)
- Added lobbyService unit tests
    - [#258](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/258)

## Samuel Frank
- Introduced more robustness for parsing AI players definition responsibly from external API (fallback json data):
    - [#248](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/248)
- Improved scalability issue regarding AI players username and the uniqueness constraint of usernames
    - [#252](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/issues/252)
- Introduced new avatars with both front- and backend logic
    - [#121](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/121)
- 200 commits itemized with an estimated effort of at least 2 whole working days


## Cédric Styner

- leave Game.
    -  [#90](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/90)
- Routing
    -  [#114](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/114)
- Inform about current states
    -  [#109](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/109)
- Bugfixes for corrupt states
    -  [#120](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/120)
 
## Markus Senn

- Added copying PIN to clipboard
   - [#112](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/112)
- Limited number of AI players to 2
    - [#113](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/113)
- Highlighting user in leaderboard
    - [#118](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/issues/118)

# 18.05 - 25.05
## Harris A
- use of joker
## Markus Senn
- use of joker
## Cédric Styner
- use of joker
## Samuel Frank
- use of joker
## Elia Aeberhard
- use of joker