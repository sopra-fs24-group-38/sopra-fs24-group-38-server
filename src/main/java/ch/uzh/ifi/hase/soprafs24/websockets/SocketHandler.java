package ch.uzh.ifi.hase.soprafs24.websockets;

import ch.uzh.ifi.hase.soprafs24.model.database.User;
import ch.uzh.ifi.hase.soprafs24.service.LobbyService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;

@Component
public class SocketHandler extends TextWebSocketHandler {
    private final Logger log = LoggerFactory.getLogger(SocketHandler.class);
    ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    LobbyService lobbyService;
    @Autowired
    UserService userService;
    @Autowired
    WebSocketSessionManager webSocketSessionManager;
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        webSocketSessionManager.registerSession(session);

        log.warn(message.toString());
        log.warn(message.getPayload());
        Map messageMap = objectMapper.readValue(message.getPayload(), Map.class);
        Long userId = Long.parseLong((String) messageMap.get("userId"));
        userService.setIsConnected(userId, true);
        if ("init".equals(messageMap.get("action"))) {
            userService.addSessionToUser(session.getId(), userId);
            log.warn("received initial ws message for userId" + userId);
        }
        // Handle other textmessages: add else {}
    }

    public void sendMessageToLobby(Long lobbyId, String message) {

        List<User> users = lobbyService.getUsers(lobbyId);
        for (User user: users) {
            WebSocketSession session = null;
            try {
                session = webSocketSessionManager.getSession(user.getSessionId());
            } catch(NullPointerException e){
                String sessionInfo = "Couldn't fetch session (sessionID: " + user.getSessionId() + ")";
                String userInfo = "For user with userID: " + user.getId() + " and username: "  + user.getUsername();
                log.warn(sessionInfo + userInfo);
            }
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    System.err.println("Failed to send message to user " + user.getId() + ": " + e.getMessage());
                }
            }
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.warn(session.getId());
        userService.setIsConnected(userService.getUserIdBySessionId(session.getId()), false);
        super.afterConnectionClosed(session, status);
    }
}
