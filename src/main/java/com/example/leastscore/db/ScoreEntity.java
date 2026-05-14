package com.example.leastscore.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "scores")
@IdClass(ScoreKey.class)
public class ScoreEntity {
  @Id
  @Column(name = "game_id", nullable = false)
  private Long gameId;

  @Id
  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "total_score", nullable = false)
  private int totalScore;

  public Long getGameId() {
    return gameId;
  }

  public void setGameId(Long gameId) {
    this.gameId = gameId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public int getTotalScore() {
    return totalScore;
  }

  public void setTotalScore(int totalScore) {
    this.totalScore = totalScore;
  }
}
