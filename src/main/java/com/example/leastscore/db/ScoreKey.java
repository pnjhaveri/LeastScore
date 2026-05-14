package com.example.leastscore.db;

import java.io.Serializable;
import java.util.Objects;

public class ScoreKey implements Serializable {
  private Long gameId;
  private Long userId;

  public ScoreKey() {}

  public ScoreKey(Long gameId, Long userId) {
    this.gameId = gameId;
    this.userId = userId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ScoreKey scoreKey = (ScoreKey) o;
    return Objects.equals(gameId, scoreKey.gameId) && Objects.equals(userId, scoreKey.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gameId, userId);
  }
}
