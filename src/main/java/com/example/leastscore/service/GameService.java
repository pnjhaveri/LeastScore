package com.example.leastscore.service;

import com.example.leastscore.db.*;
import com.example.leastscore.game.Card;
import com.example.leastscore.game.CardValidator;
import com.example.leastscore.game.GameState;
import com.example.leastscore.game.MoveType;
import com.example.leastscore.game.PlayerState;
import com.example.leastscore.util.RoomCodes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class GameService {
  private static final int MIN_PLAYERS = 2;
  private static final int MAX_PLAYERS = 6;
  private static final int HAND_SIZE = 5;
  private static final int MOVE_HISTORY_LIMIT = 20;
  private static final int BAD_SHOW_PENALTY = 40;
  private static final int ELIMINATION_THRESHOLD = 100;

  private final ObjectMapper objectMapper;
  private final UserRepository userRepository;
  private final RoomRepository roomRepository;
  private final RoomPlayerRepository roomPlayerRepository;
  private final GameRepository gameRepository;
  private final MoveRepository moveRepository;
  private final ScoreRepository scoreRepository;

  public GameService(
      ObjectMapper objectMapper,
      UserRepository userRepository,
      RoomRepository roomRepository,
      RoomPlayerRepository roomPlayerRepository,
      GameRepository gameRepository,
      MoveRepository moveRepository,
      ScoreRepository scoreRepository) {
    this.objectMapper = objectMapper;
    this.userRepository = userRepository;
    this.roomRepository = roomRepository;
    this.roomPlayerRepository = roomPlayerRepository;
    this.gameRepository = gameRepository;
    this.moveRepository = moveRepository;
    this.scoreRepository = scoreRepository;
  }

  @Transactional
  public RoomEntity createRoom() {
    String code;
    do {
      code = RoomCodes.newCode(6);
    } while (roomRepository.findByRoomCode(code).isPresent());

    var room = new RoomEntity();
    room.setRoomCode(code);
    room.setStatus("LOBBY");
    return roomRepository.save(room);
  }

  @Transactional
  public RoomEntity joinRoom(String roomCode, long userId) {
    RoomEntity room =
        roomRepository
            .findByRoomCode(roomCode)
            .orElseThrow(() -> new IllegalArgumentException("room not found"));

    List<RoomPlayerEntity> players = roomPlayerRepository.findByRoomIdOrderBySeatIndexAsc(room.getId());
    boolean alreadyInRoom = players.stream().anyMatch(p -> p.getUserId() == userId);
    if (alreadyInRoom) {
      return room;
    }

    if (!"LOBBY".equals(room.getStatus())) {
      throw new IllegalStateException("cannot join - game already started");
    }

    if (players.size() >= MAX_PLAYERS) {
      throw new IllegalStateException("room is full");
    }
    int nextSeat = players.isEmpty() ? 0 : players.get(players.size() - 1).getSeatIndex() + 1;
    var rp = new RoomPlayerEntity();
    rp.setRoomId(room.getId());
    rp.setUserId(userId);
    rp.setSeatIndex(nextSeat);
    roomPlayerRepository.save(rp);
    return room;
  }

  @Transactional(readOnly = true)
  public RoomInfo getRoomInfo(String roomCode) {
    RoomEntity room =
        roomRepository
            .findByRoomCode(roomCode)
            .orElseThrow(() -> new IllegalArgumentException("room not found"));

    List<RoomPlayerEntity> players = roomPlayerRepository.findByRoomIdOrderBySeatIndexAsc(room.getId());
    List<RoomPlayerInfo> playerInfos = new ArrayList<>();
    for (RoomPlayerEntity rp : players) {
      UserEntity u = userRepository.findById(rp.getUserId()).orElseThrow();
      playerInfos.add(new RoomPlayerInfo(u.getId(), u.getUsername(), rp.getSeatIndex()));
    }

    return new RoomInfo(room.getRoomCode(), room.getStatus(), playerInfos);
  }

  public record RoomInfo(String roomCode, String status, List<RoomPlayerInfo> players) {}
  public record RoomPlayerInfo(long userId, String username, int seatIndex) {}

  @Transactional
  public GameEntity startGame(String roomCode, long userId) {
    RoomEntity room =
        roomRepository
            .findByRoomCode(roomCode)
            .orElseThrow(() -> new IllegalArgumentException("room not found"));

    List<RoomPlayerEntity> players = roomPlayerRepository.findByRoomIdOrderBySeatIndexAsc(room.getId());
    if (players.stream().noneMatch(p -> p.getUserId() == userId)) {
      throw new IllegalStateException("must join room first");
    }
    if (players.size() < MIN_PLAYERS) {
      throw new IllegalStateException("need at least " + MIN_PLAYERS + " players to start");
    }
    if (!"LOBBY".equals(room.getStatus())) {
      throw new IllegalStateException("room is not in lobby");
    }

    GameState state = new GameState();
    state.setRoomCode(roomCode);

    List<Card> deck = createDeck();
    Collections.shuffle(deck);
    state.getDeck().addAll(deck);

    for (RoomPlayerEntity rp : players) {
      UserEntity u = userRepository.findById(rp.getUserId()).orElseThrow();
      var ps = new PlayerState(u.getId(), u.getUsername());
      for (int i = 0; i < HAND_SIZE; i++) {
        ps.getHand().add(draw(state));
      }
      ps.setTotal(calculateScore(ps.getHand()));
      state.getPlayers().add(ps);
    }

    state.setOpenCard(draw(state));
    state.setCurrentTurnIndex(0);
    state.setTurnsInRound(0);

    var game = new GameEntity();
    game.setRoomId(room.getId());
    game.setStateJson(writeState(state));
    game.setCurrentTurnUserId(state.getPlayers().get(0).getUserId());
    game = gameRepository.save(game);

    state.setGameId(game.getId());
    game.setStateJson(writeState(state));
    gameRepository.save(game);

    room.setStatus("IN_GAME");
    roomRepository.save(room);

    recordMove(game.getId(), userId, MoveType.START, "{}");
    upsertScores(game.getId(), state);
    return game;
  }

  private List<Card> createDeck() {
    List<Card> deck = new ArrayList<>();
    for (Card.Suit suit : Card.Suit.values()) {
      for (int rank = 1; rank <= 13; rank++) {
        deck.add(new Card(suit, rank));
      }
    }
    return deck;
  }

  @Transactional
  public GameState getState(String roomCode) {
    RoomEntity room = roomRepository.findByRoomCode(roomCode).orElseThrow(() -> new IllegalArgumentException("room not found"));
    Optional<GameEntity> gameOpt = gameRepository.findFirstByRoomIdOrderByStartedAtDesc(room.getId());
    if (gameOpt.isEmpty()) {
      GameState s = new GameState();
      s.setRoomCode(roomCode);
      return s;
    }
    try {
      return readState(gameOpt.get().getStateJson());
    } catch (Exception e) {
      GameState s = new GameState();
      s.setRoomCode(roomCode);
      return s;
    }
  }

  @Transactional
  public GameState takeTurn(String roomCode, long userId, TurnRequest req) {
    if (req == null || req.action() == null) {
      throw new IllegalArgumentException("action is required");
    }

    RoomEntity room = roomRepository.findByRoomCode(roomCode).orElseThrow(() -> new IllegalArgumentException("room not found"));
    GameEntity game = gameRepository.findFirstByRoomIdOrderByStartedAtDesc(room.getId()).orElseThrow(() -> new IllegalStateException("game not started"));

    GameState state = readState(game.getStateJson());
    if (state.isEnded()) {
      throw new IllegalStateException("game ended");
    }

    int playerIndex = indexOfPlayer(state, userId);
    if (playerIndex < 0) {
      throw new IllegalStateException("not in game");
    }
    if (playerIndex != state.getCurrentTurnIndex()) {
      throw new IllegalStateException("not your turn");
    }

    PlayerState player = state.getPlayers().get(playerIndex);

    List<Integer> indices = req.discardIndices();
    if (indices == null || indices.isEmpty()) {
      throw new IllegalArgumentException("discardIndices required");
    }

    Card picked;
    boolean isCombo = false;

    if (req.action() == TurnAction.DRAW_FROM_DECK) {
      picked = draw(state);
      recordMove(game.getId(), userId, MoveType.DRAW_FROM_DECK, "{\"card\":\"" + picked.getDisplayName() + "\"}");
    } else if (req.action() == TurnAction.TAKE_OPEN_CARD || req.action() == TurnAction.DISCARD_COMBO) {
      picked = state.getOpenCard();
      if (picked == null) {
        throw new IllegalArgumentException("no open card to take");
      }
      isCombo = req.action() == TurnAction.DISCARD_COMBO;
      recordMove(game.getId(), userId, MoveType.TAKE_OPEN_CARD, "{\"card\":\"" + picked.getDisplayName() + "\"}");
    } else {
      throw new IllegalArgumentException("Invalid action");
    }

    List<Card> cardsToDiscard = new ArrayList<>();
    for (Integer idx : indices) {
      if (idx < 0 || idx >= player.getHand().size()) {
        throw new IllegalArgumentException("invalid discard index: " + idx);
      }
      cardsToDiscard.add(player.getHand().get(idx));
    }

    if (cardsToDiscard.size() > 1 || isCombo) {
      CardValidator.ValidationResult validation = CardValidator.validateDiscard(cardsToDiscard);
      if (!validation.isValid()) {
        throw new IllegalArgumentException(validation.getError());
      }
    }

    for (int i = indices.size() - 1; i >= 0; i--) {
      player.getHand().remove((int) indices.get(i));
    }

    player.getHand().add(picked);
    state.setOpenCard(cardsToDiscard.get(0));
    player.setTotal(calculateScore(player.getHand()));

    String discardedNames = cardsToDiscard.stream()
        .map(c -> "\"" + c.getDisplayName() + "\"")
        .reduce((a, b) -> a + "," + b).orElse("");
    recordMove(game.getId(), userId, MoveType.DISCARD,
        "{\"discarded\":[" + discardedNames + "],\"kept\":\"" + picked.getDisplayName() + "\"}");

    state.setTurnsInRound(state.getTurnsInRound() + 1);
    int next = (state.getCurrentTurnIndex() + 1) % state.getPlayers().size();
    state.setCurrentTurnIndex(next);
    game.setCurrentTurnUserId(state.getPlayers().get(next).getUserId());

    game.setStateJson(writeState(state));
    gameRepository.save(game);
    upsertScores(game.getId(), state);
    return state;
  }

  @Transactional
  public GameState declare(String roomCode, long userId) {
    RoomEntity room = roomRepository.findByRoomCode(roomCode).orElseThrow(() -> new IllegalArgumentException("room not found"));
    GameEntity game = gameRepository.findFirstByRoomIdOrderByStartedAtDesc(room.getId()).orElseThrow(() -> new IllegalStateException("game not started"));

    GameState state = readState(game.getStateJson());
    if (state.isEnded()) return state;

    int idx = indexOfPlayer(state, userId);
    if (idx < 0) throw new IllegalStateException("not in game");

    state.setDeclaredByUserId(userId);
    state.setEnded(true);
    recordMove(game.getId(), userId, MoveType.DECLARE, "{}");

    applyEndGamePenaltiesAndEliminate(state);
    recordMove(game.getId(), userId, MoveType.END_GAME, "{}");

    int activePlayers = (int) state.getPlayers().stream().filter(p -> !p.isEliminated()).count();
    if (activePlayers <= 1) {
      room.setStatus("GAME_OVER");
    } else {
      room.setStatus("ROUND_ENDED");
    }
    roomRepository.save(room);

    game.setEndedAt(Instant.now());
    game.setStateJson(writeState(state));
    gameRepository.save(game);

    return state;
  }

  private void applyEndGamePenaltiesAndEliminate(GameState state) {
    Long declaredId = state.getDeclaredByUserId();
    if (declaredId == null) return;

    PlayerState declaredPlayer = state.getPlayers().stream()
        .filter(p -> p.getUserId() == declaredId)
        .findFirst().orElse(null);
    if (declaredPlayer == null) return;

    int declaredScore = declaredPlayer.getTotal();
    int lowestScore = state.getPlayers().stream()
        .mapToInt(PlayerState::getTotal)
        .min()
        .orElse(declaredScore);

    boolean isBadShow = state.getPlayers().stream()
        .anyMatch(p -> p.getUserId() != declaredId && p.getTotal() <= declaredScore);

    for (PlayerState p : state.getPlayers()) {
      int points;
      if (isBadShow) {
        if (p.getUserId() == declaredId) {
          points = BAD_SHOW_PENALTY;
        } else if (p.getTotal() == lowestScore) {
          points = 0;
        } else {
          points = p.getTotal();
        }
      } else {
        points = p.getUserId() == declaredId ? 0 : p.getTotal();
      }

      int newCumulativeScore = p.getCumulativeScore() + points;
      p.setCumulativeScore(newCumulativeScore);

      if (newCumulativeScore >= ELIMINATION_THRESHOLD) {
        p.setEliminated(true);
        state.getEliminatedPlayers().add(p);
      }
    }

    for (PlayerState p : state.getEliminatedPlayers()) {
      state.getPlayers().remove(p);
    }
  }

  @Transactional
  public GameState startNextRound(String roomCode, long userId) {
    RoomEntity room = roomRepository.findByRoomCode(roomCode).orElseThrow(() -> new IllegalArgumentException("room not found"));
    GameEntity oldGame = gameRepository.findFirstByRoomIdOrderByStartedAtDesc(room.getId()).orElse(null);

    GameState oldState = oldGame != null ? readState(oldGame.getStateJson()) : null;

    List<PlayerState> activePlayers;
    List<PlayerState> eliminatedPlayers;

    if (oldState != null) {
      activePlayers = oldState.getPlayers().stream()
          .filter(p -> !p.isEliminated())
          .toList();
      eliminatedPlayers = oldState.getEliminatedPlayers();
    } else {
      List<RoomPlayerEntity> roomPlayers = roomPlayerRepository.findByRoomIdOrderBySeatIndexAsc(room.getId());
      activePlayers = new ArrayList<>();
      eliminatedPlayers = new ArrayList<>();
      for (RoomPlayerEntity rp : roomPlayers) {
        UserEntity u = userRepository.findById(rp.getUserId()).orElseThrow();
        activePlayers.add(new PlayerState(u.getId(), u.getUsername()));
      }
    }

    if (activePlayers.size() < 2) {
      throw new IllegalStateException("Need at least 2 players to continue");
    }

    GameState state = new GameState();
    state.setRoomCode(roomCode);
    state.setRoundNumber(oldState != null ? oldState.getRoundNumber() + 1 : 1);
    state.setEliminatedPlayers(eliminatedPlayers);

    List<Card> deck = createDeck();
    Collections.shuffle(deck);
    state.getDeck().addAll(deck);

    for (PlayerState ps : activePlayers) {
      ps.getHand().clear();
      ps.setTotal(0);
      for (int i = 0; i < HAND_SIZE; i++) {
        ps.getHand().add(draw(state));
      }
      ps.setTotal(calculateScore(ps.getHand()));
      state.getPlayers().add(ps);
    }

    state.setOpenCard(draw(state));
    state.setCurrentTurnIndex(0);
    state.setTurnsInRound(0);

    var game = new GameEntity();
    game.setRoomId(room.getId());
    game.setStateJson(writeState(state));
    game.setCurrentTurnUserId(state.getPlayers().get(0).getUserId());
    game = gameRepository.save(game);

    state.setGameId(game.getId());
    game.setStateJson(writeState(state));
    gameRepository.save(game);

    room.setStatus("IN_GAME");
    roomRepository.save(room);

    recordMove(game.getId(), userId, MoveType.START, "{\"round\":" + state.getRoundNumber() + "}");
    upsertScores(game.getId(), state);
    return state;
  }

  @Transactional(readOnly = true)
  public List<MoveEntity> getRecentMoves(String roomCode) {
    RoomEntity room = roomRepository.findByRoomCode(roomCode).orElseThrow(() -> new IllegalArgumentException("room not found"));
    Optional<GameEntity> gameOpt = gameRepository.findFirstByRoomIdOrderByStartedAtDesc(room.getId());
    if (gameOpt.isEmpty()) {
      return List.of();
    }
    return moveRepository.findRecentByGameId(gameOpt.get().getId(), PageRequest.of(0, MOVE_HISTORY_LIMIT));
  }

  private void upsertScores(long gameId, GameState state) {
    for (PlayerState p : state.getPlayers()) {
      var s = new ScoreEntity();
      s.setGameId(gameId);
      s.setUserId(p.getUserId());
      s.setTotalScore(p.getTotal());
      scoreRepository.save(s);
    }
  }

  private int indexOfPlayer(GameState state, long userId) {
    for (int i = 0; i < state.getPlayers().size(); i++) {
      if (state.getPlayers().get(i).getUserId() == userId) return i;
    }
    return -1;
  }

  private Card draw(GameState state) {
    List<Card> deck = state.getDeck();
    if (deck.isEmpty()) throw new IllegalStateException("deck is empty");
    return deck.remove(0);
  }

  private int calculateScore(List<Card> hand) {
    return hand.stream().mapToInt(Card::getGameValue).sum();
  }

  private void recordMove(long gameId, long userId, MoveType type, String payloadJson) {
    var m = new MoveEntity();
    m.setGameId(gameId);
    m.setUserId(userId);
    m.setMoveType(type.name());
    m.setPayloadJson(StringUtils.hasText(payloadJson) ? payloadJson : "{}");
    moveRepository.save(m);
  }

  private String writeState(GameState state) {
    try {
      return objectMapper.writeValueAsString(state);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("failed to write game state", e);
    }
  }

  private GameState readState(String json) {
    try {
      return objectMapper.readValue(json, GameState.class);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("failed to read game state", e);
    }
  }

  public GameState sanitizeForPublic(GameState state) {
    return sanitizeForPublic(state, -1);
  }

  public GameState sanitizeForPublic(GameState state, long viewerUserId) {
    GameState pub = new GameState();
    pub.setRoomCode(state.getRoomCode());
    pub.setGameId(state.getGameId());
    pub.setCurrentTurnIndex(state.getCurrentTurnIndex());
    pub.setOpenCard(state.getOpenCard());
    pub.setEnded(state.isEnded());
    pub.setDeclaredByUserId(state.getDeclaredByUserId());
    pub.setRoundNumber(state.getRoundNumber());
    pub.setDeckSize(state.getDeck().size());
    pub.setTurnsInRound(state.getTurnsInRound());

    for (PlayerState p : state.getPlayers()) {
      PlayerState sp = new PlayerState(p.getUserId(), p.getUsername());
      sp.setTotal(p.getTotal());
      sp.setCumulativeScore(p.getCumulativeScore());
      sp.setEliminated(p.isEliminated());
      sp.setHandSize(p.getHand().size());
      if (p.getUserId() == viewerUserId) {
        sp.setHand(new ArrayList<>(p.getHand()));
      }
      pub.getPlayers().add(sp);
    }
    for (PlayerState p : state.getEliminatedPlayers()) {
      PlayerState sp = new PlayerState(p.getUserId(), p.getUsername());
      sp.setTotal(p.getTotal());
      sp.setCumulativeScore(p.getCumulativeScore());
      sp.setEliminated(p.isEliminated());
      sp.setHandSize(p.getHand().size());
      pub.getEliminatedPlayers().add(sp);
    }
    return pub;
  }

  @Transactional(readOnly = true)
  public List<Card> getPlayerHand(String roomCode, long userId) {
    RoomEntity room = roomRepository.findByRoomCode(roomCode)
        .orElseThrow(() -> new IllegalArgumentException("room not found"));
    var gameOpt = gameRepository.findFirstByRoomIdOrderByStartedAtDesc(room.getId());
    if (gameOpt.isEmpty()) return List.of();
    GameState state = readState(gameOpt.get().getStateJson());
    return state.getPlayers().stream()
        .filter(p -> p.getUserId() == userId)
        .findFirst()
        .map(PlayerState::getHand)
        .orElse(List.of());
  }

  public enum TurnAction {
    TAKE_OPEN_CARD,
    DRAW_FROM_DECK,
    DISCARD_COMBO
  }

  public record TurnRequest(TurnAction action, List<Integer> discardIndices) {}
}
