package com.example.leastscore.game;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerState {
  private long userId;
  private String username;
  private List<Card> hand = new ArrayList<>();
  private int total;
  private int cumulativeScore = 0;
  private boolean eliminated = false;
  private int handSize;

  public PlayerState() {}

  public PlayerState(long userId, String username) {
    this.userId = userId;
    this.username = username;
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public List<Card> getHand() {
    return hand;
  }

  public void setHand(List<Card> hand) {
    this.hand = hand;
  }

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public int getCumulativeScore() {
    return cumulativeScore;
  }

  public void setCumulativeScore(int cumulativeScore) {
    this.cumulativeScore = cumulativeScore;
  }

  public boolean isEliminated() {
    return eliminated;
  }

  public void setEliminated(boolean eliminated) {
    this.eliminated = eliminated;
  }

  public int getHandSize() {
    return handSize;
  }

  public void setHandSize(int handSize) {
    this.handSize = handSize;
  }
}

