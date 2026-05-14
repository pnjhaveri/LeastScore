package com.example.leastscore.game;

import java.util.List;

public record RoundScoreEntry(int roundNumber, List<PlayerScore> scores) {
  public record PlayerScore(long userId, String username, int cumulativeScore, int handScore, boolean eliminated) {}
}