package com.example.leastscore.game;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GameState {
  private String roomCode;
  private long gameId;
  private List<PlayerState> players = new ArrayList<>();
  private List<PlayerState> eliminatedPlayers = new ArrayList<>();
  private int currentTurnIndex = 0;
  private List<Card> deck = new ArrayList<>();
  private Card openCard;
  private boolean ended = false;
  private Long declaredByUserId;
  private int roundNumber = 1;

  public String getRoomCode() {
    return roomCode;
  }

  public void setRoomCode(String roomCode) {
    this.roomCode = roomCode;
  }

  public long getGameId() {
    return gameId;
  }

  public void setGameId(long gameId) {
    this.gameId = gameId;
  }

  public List<PlayerState> getPlayers() {
    return players;
  }

  public void setPlayers(List<PlayerState> players) {
    this.players = players;
  }

  public List<PlayerState> getEliminatedPlayers() {
    return eliminatedPlayers;
  }

  public void setEliminatedPlayers(List<PlayerState> eliminatedPlayers) {
    this.eliminatedPlayers = eliminatedPlayers;
  }

  public int getCurrentTurnIndex() {
    return currentTurnIndex;
  }

  public void setCurrentTurnIndex(int currentTurnIndex) {
    this.currentTurnIndex = currentTurnIndex;
  }

  public List<Card> getDeck() {
    return deck;
  }

  public void setDeck(List<Card> deck) {
    this.deck = deck;
  }

  public Card getOpenCard() {
    return openCard;
  }

  public void setOpenCard(Card openCard) {
    this.openCard = openCard;
  }

  public boolean isEnded() {
    return ended;
  }

  public void setEnded(boolean ended) {
    this.ended = ended;
  }

  public Long getDeclaredByUserId() {
    return declaredByUserId;
  }

  public void setDeclaredByUserId(Long declaredByUserId) {
    this.declaredByUserId = declaredByUserId;
  }

  public int getRoundNumber() {
    return roundNumber;
  }

  public void setRoundNumber(int roundNumber) {
    this.roundNumber = roundNumber;
  }
}

