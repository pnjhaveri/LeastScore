package com.example.leastscore.realtime;

import com.example.leastscore.game.GameState;
import com.example.leastscore.service.GameService;
import com.example.leastscore.service.GameService.RoomInfo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class RoomBroadcaster {
  private final SimpMessagingTemplate messagingTemplate;
  private final GameService gameService;

  public RoomBroadcaster(SimpMessagingTemplate messagingTemplate, GameService gameService) {
    this.messagingTemplate = messagingTemplate;
    this.gameService = gameService;
  }

  public void broadcastState(String roomCode, GameState state) {
    messagingTemplate.convertAndSend("/topic/room." + roomCode + ".state", gameService.sanitizeForPublic(state));
  }

  public void broadcastRoomState(String roomCode, RoomInfo roomInfo) {
    messagingTemplate.convertAndSend("/topic/room." + roomCode + ".room", roomInfo);
  }
}

