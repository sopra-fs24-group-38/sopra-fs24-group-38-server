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

Sever: 
- [Spring Boot](https://spring.io/projects/spring-boot) together with [JPA](https://spring.io/projects/spring-data-jpa)
- [Websockets](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-websocket/3.2.4)
- [H2 DB](https://www.h2database.com/html/main.html)
- [openAPI](https://mvnrepository.com/artifact/org.springdoc/springdoc-openapi-ui/1.7.0) for [swagger UI](https://sopra-fs24-group-38-server.oa.r.appspot.com/swagger-ui/index.html)
- [chatGPT](https://help.openai.com/en/articles/7039783-how-can-i-access-the-chatgpt-api) for external api calls

Client: 
- [React](https://react.dev/) - JS library
- [react-use-websocket](https://www.npmjs.com/package/react-use-websocket) - for websockets
- [Tailwind CSS](https://tailwindcss.com/) - for styling
- [react-toastify](https://fkhadra.github.io/react-toastify/introduction) - for neat feedback toasts

## High-level components

### Backend Main Components

These are the main files resources to get a good grasp of the application from a backend/logic related perspective:
1. [LobbyController](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/controller/LobbyController.java),
[UserController](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/controller/UserController.java)
this classes together with the [deployed swagger UI](https://sopra-fs24-group-38-server.oa.r.appspot.com/swagger-ui/index.html)
form a good introduction into the game logic and corresponding entry points into the backend code. 
2. To dive deeper into the main logic behind the most important endpoints [UserService](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/service/UserService.java) [LobbyService](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/service/LobbyService.java)
shall be looked at. 

### [Frontend](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client) Main Components

These are the main files necessary to get a good grasp of the application from a frontend perspective:

1. [LobbyWaiting](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/tree/main/src/components/pages/LobbyWaiting.jsx) is the main hub for player management.
2. [Game](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/tree/main/src/components/pages/Game.jsx) holds the full logic for the game cycle.
3. [AppRouter](https://github.com/sopra-fs24-group-38/sopra-fs24-group-38-client/tree/main/src/components/router/AppRouter.jsx) displays the layout of the application.


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

## External Dependencies 
No further external dependencies needed as gradle and node take care.

## Releases 
For further releases we refer to [this tutorial](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository)




## Authors and acknowledgment 

### Authors

Continuous teamwork and BackEnd authors:
* **Harris A** - *Main contributor* - [so-ri](https://github.com/so-ri)
* **Elia Aeberhard** - *Main contributor* - [Elyisha](https://github.com/Elyisha)
* **Samuel Frank** - *Main contributor* - [samuelfrnk](https://github.com/samuelfrnk)

For the FrontEnd part:
* **Cédric Styner** - *Main contributor* - [glt-cs](https://github.com/glt-cs)
* **Markus Senn** - *Main contributor* - [iKusii](https://github.com/iKusii)

**Stefan Schuler** - *Responsible TA* - [Steesch](https://github.com/Steesch)

### Acknowledgments

* Thanks to all the people that helped, on- and offline

## License
This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details.


