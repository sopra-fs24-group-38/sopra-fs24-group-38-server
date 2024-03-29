package ch.uzh.ifi.hase.soprafs24.websockets;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {

    /**
    * This class is necessary because it is not possible per default to retrieve a session while only having a sessionId.
     * At the same time it's not possible to save a whole Session Object in our DB. Thus we save a sessionID and make it accessible here
     * at runtime.
    * */

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void registerSession(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session.getId());
    }

    public WebSocketSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

}

