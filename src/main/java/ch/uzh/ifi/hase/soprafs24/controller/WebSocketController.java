package ch.uzh.ifi.hase.soprafs24.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    /**
    @MessageMapping("/lobbies/{lobbyId}/rejoin")
    @SendToUser("/queue/join")
    public PlayerDTO rejoinLobby(@DestinationVariable long lobbyId, PlayerRejoinDTO playerDTO, SimpMessageHeaderAccessor headerAccessor){
        Player player = lobbyManager.rejoinPlayer(playerDTO.getId(), headerAccessor.getSessionId());
        PlayerDTO playerDTOReturned = DTOMapperWebsocket.INSTANCE.convertEntityToPlayerDTO(player);
        playerDTOReturned.setAvatar(playerDTO.getAvatar());
        playerDTOReturned.setLobbyId(player.getLobby().getId());

        messagingTemplate.convertAndSend(String.format("/queue/lobbies/%d", player.getLobby().getId()), playerDTOReturned);
        return playerDTOReturned;
    }
    **/
}
