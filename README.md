# Nobody is perfect backend

## Sopra  - Group 38 

We want to build a fun and engaging web application in which you can creatively fool your
friends and have a good time. In combination with competitive aspects we want to solve the
problem of finding a cool game to play with your friends. Thus we want to create a multiplayer
game derived from the concept of the board game “Nobody’s Perfect”. Each round, the game
presents an unknown, difficult, technical or “slang” word. Each player then types in a wrong, but
possible explanation for this word. Then, the game shows all the player’s wrong and one correct
explanation and lets the players vote for what they think is correct. Successfully tricking others
into choosing your wrong explanation earns you points. As a multiplayer web application game,
that is easily extendable and repeatable with additional modes or languages (see optional user
stories), thus making it highly suitable for this course.



### Building with Gradle
You can use the local Gradle Wrapper to build the application.
-   macOS: `./gradlew`
-   Linux: `./gradlew`
-   Windows: `./gradlew.bat`


### Build

```bash
./gradlew build
```

### Run

```bash
./gradlew bootRun
```

### Environment variables

-   TOKEN_API: The token for the authorization header in the chatGPT api request.  

### Development Mode
You can start the backend in development mode, this will automatically trigger a new build and reload the application
once the content of a file has been changed.

Start two terminal windows and run:

`./gradlew build --continuous`

and in the other one:

`./gradlew bootRun`

If you want to avoid running all tests with every change, use the following command instead:

`./gradlew build --continuous -xtest`

### Contributors 

- Harris A (https://github.com/so-ri) 
- Elia Aeberhard (https://github.com/Elyisha)
- Samuel Frank (https://github.com/samuelfrnk)
- Markus Senn (https://github.com/iKusii)
- Cédric Yves Styner (https://github.com/glt-cs)


