package com.example.leastscore.game;

public record Card(Suit suit, int rank) {
  public enum Suit {
    SPADES("♠"),
    HEARTS("♥"),
    DIAMONDS("♦"),
    CLUBS("♣");

    private final String symbol;

    Suit(String symbol) {
      this.symbol = symbol;
    }

    public String getSymbol() {
      return symbol;
    }
  }

  public int getGameValue() {
    if (rank == 1) return 1;
    if (rank >= 2 && rank <= 10) return rank;
    return 10;
  }

  public String getDisplayName() {
    String rankStr = switch (rank) {
      case 1 -> "A";
      case 11 -> "J";
      case 12 -> "Q";
      case 13 -> "K";
      default -> String.valueOf(rank);
    };
    return rankStr + suit.getSymbol();
  }
}
