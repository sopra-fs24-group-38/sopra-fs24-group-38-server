# Nobody is perfect

## Introduction 

This is a fun and engaging web application in which you can creatively fool your
friends and have a good time. In combination with competitive aspects we want to solve the
problem of finding a cool game to play with your friends. Thus we want to create a multiplayer
game derived from the concept of the board game “Nobody’s Perfect”. Each round, the game
presents an unknown, difficult word related to Dutch, Programming, Foods or Bizarre. Each player then types in a wrong, but
possible explanation for this word. Then, the game shows all the player’s wrong and one correct
explanation and lets the players vote for what they think is correct. Successfully tricking others
into choosing your wrong explanation earns you points. There is also the possibility to further customize the gaming experience 
using (a combination) of game modes and adding AI players to have an engaging experience with fewer friends available.  


## Technologies used

- Sever: Spring Boot, STOMP, JPA, H2, openAPI, chatGPT
- Client: React, SocketJS, Tailwind

## High-level components
### References (main class, file or function)
## Launch and deploy 
### Getting started 
1. Clone both this and the [corresponding frontend repository](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client) locally.
### Server
1. Acquire a valid chatGPT token which enables GPT 4 turbo requests and set it in the environment of the cloned backend repository (export TOKEN_API=sk-7ie5o... for macOS). 
2. Development : 
We recommend to use the build option without tests during local development and use the testsuite manually if needed locally.
And otherwise rely on the automated tests during deployment.

- Build without tests:
```bash
`./gradlew build --continuous -xtest`
```
- Build with tests: 
```bash
./gradlew build
```
- Run:
```bash
./gradlew bootRun
```
- Testsuite locally:
```bash
./gradlew test
```
### Client

Run the following line of code in the project root directory

```bash
npm install
```

Start a dev environment with

```bash
npm start
```
You can now see the application at http://localhost:3000 in your browser of choice.

### External Dependencies 
No further external dependencies needed as gradle and node take care.
### Releases 
For further releases we refer to [this tutorial](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository)

## Environment variables

- TOKEN_API: The token for the authorization header in the chatGPT api request also important for testsuite. 
- AVATAR_NUMBER: Number of Avatars for real player. (Can be left on default unless new JPG files are added in frontend)
- AVATAR_AI_NUMBER: Number of Avatars for AI player. (Can be left on default unless new JPG files are added in frontend)



## Authors and acknowledgment 

- Harris A (https://github.com/so-ri) 
- Elia Aeberhard (https://github.com/Elyisha)
- Samuel Frank (https://github.com/samuelfrnk)
- Markus Senn (https://github.com/iKusii)
- Cédric Yves Styner (https://github.com/glt-cs)

## License 
This project is licensed under the MIT License - see the [LICENSE](LICENSE) for details


