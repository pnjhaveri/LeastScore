package com.example.leastscore.web;

import com.example.leastscore.game.Card;
import com.example.leastscore.db.MoveEntity;
import com.example.leastscore.db.RoomEntity;
import com.example.leastscore.realtime.RoomBroadcaster;
import com.example.leastscore.service.GameService;
import com.example.leastscore.service.GameService.TurnRequest;
import com.example.leastscore.service.IdentityService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
@Validated
public class RoomController {
  private final IdentityService identityService;
  private final GameService gameService;
  private final RoomBroadcaster broadcaster;

  public RoomController(IdentityService identityService, GameService gameService, RoomBroadcaster broadcaster) {
    this.identityService = identityService;
    this.gameService = gameService;
    this.broadcaster = broadcaster;
  }

  @PostMapping
  public ResponseEntity<?> createRoom(HttpSession session) {
    long userId = requireUser(session);
    RoomEntity room = gameService.createRoom();
    gameService.joinRoom(room.getRoomCode(), userId);
    GameService.RoomInfo roomInfo = gameService.getRoomInfo(room.getRoomCode());
    return ResponseEntity.ok(roomInfo);
  }

  @PostMapping("/{roomCode}/join")
  public ResponseEntity<?> join(@PathVariable @NotBlank String roomCode, HttpSession session) {
    long userId = requireUser(session);
    gameService.joinRoom(roomCode, userId);
    GameService.RoomInfo roomInfo = gameService.getRoomInfo(roomCode);
    broadcaster.broadcastRoomState(roomCode, roomInfo);
    return ResponseEntity.ok(roomInfo);
  }

  @GetMapping("/{roomCode}")
  public ResponseEntity<?> getRoom(@PathVariable @NotBlank String roomCode, HttpSession session) {
    requireUser(session);
    GameService.RoomInfo roomInfo = gameService.getRoomInfo(roomCode);
    return ResponseEntity.ok(roomInfo);
  }

  @PostMapping("/{roomCode}/start")
  public ResponseEntity<?> start(@PathVariable @NotBlank String roomCode, HttpSession session) {
    long userId = requireUser(session);
    var game = gameService.startGame(roomCode, userId);
    broadcaster.broadcastState(roomCode, gameService.getState(roomCode));
    broadcaster.broadcastRoomState(roomCode, gameService.getRoomInfo(roomCode));
    return ResponseEntity.ok(new StartResponse(game.getId(), roomCode));
  }

  @GetMapping("/{roomCode}/state")
  public ResponseEntity<?> state(@PathVariable @NotBlank String roomCode, HttpSession session) {
    long userId = requireUser(session);
    return ResponseEntity.ok(gameService.sanitizeForPublic(gameService.getState(roomCode), userId));
  }

  @PostMapping("/{roomCode}/turn")
  public ResponseEntity<?> turn(
      @PathVariable @NotBlank String roomCode, @RequestBody TurnRequest req, HttpSession session) {
    long userId = requireUser(session);
    var state = gameService.takeTurn(roomCode, userId, req);
    broadcaster.broadcastState(roomCode, state);
    return ResponseEntity.ok(gameService.sanitizeForPublic(state, userId));
  }

  @PostMapping("/{roomCode}/declare")
  public ResponseEntity<?> declare(@PathVariable @NotBlank String roomCode, HttpSession session) {
    long userId = requireUser(session);
    var state = gameService.declare(roomCode, userId);
    broadcaster.broadcastState(roomCode, state);
    broadcaster.broadcastRoomState(roomCode, gameService.getRoomInfo(roomCode));
    return ResponseEntity.ok(gameService.sanitizeForPublic(state, userId));
  }

  @PostMapping("/{roomCode}/new-game")
  public ResponseEntity<?> newGame(@PathVariable @NotBlank String roomCode, HttpSession session) {
    long userId = requireUser(session);
    var state = gameService.startNewGame(roomCode, userId);
    broadcaster.broadcastState(roomCode, state);
    broadcaster.broadcastRoomState(roomCode, gameService.getRoomInfo(roomCode));
    return ResponseEntity.ok(gameService.sanitizeForPublic(state, userId));
  }

  @PostMapping("/{roomCode}/next-round")
  public ResponseEntity<?> nextRound(@PathVariable @NotBlank String roomCode, HttpSession session) {
    long userId = requireUser(session);
    var state = gameService.startNextRound(roomCode, userId);
    broadcaster.broadcastState(roomCode, state);
    return ResponseEntity.ok(gameService.sanitizeForPublic(state, userId));
  }

  @GetMapping("/{roomCode}/hand")
  public ResponseEntity<?> hand(@PathVariable @NotBlank String roomCode, HttpSession session) {
    long userId = requireUser(session);
    List<Card> cards = gameService.getPlayerHand(roomCode, userId);
    return ResponseEntity.ok(Map.of("cards", cards));
  }

  @GetMapping("/{roomCode}/moves")
  public ResponseEntity<?> moves(@PathVariable @NotBlank String roomCode, HttpSession session) {
    requireUser(session);
    List<MoveEntity> moves = gameService.getRecentMoves(roomCode);
    return ResponseEntity.ok(moves);
  }

  private long requireUser(HttpSession session) {
    Long userId = identityService.getCurrentUserId(session);
    if (userId == null) throw new IllegalStateException("set username first via POST /api/session/username");
    return userId;
  }

  public record RoomResponse(String roomCode, String status) {}

  public record StartResponse(long gameId, String roomCode) {}
}

