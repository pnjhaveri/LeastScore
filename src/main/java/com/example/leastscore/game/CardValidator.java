package com.example.leastscore.game;

import java.util.*;
import java.util.stream.Collectors;

public class CardValidator {

  public static class ValidationResult {
    private final boolean valid;
    private final String error;
    private final List<Card> validCards;

    private ValidationResult(boolean valid, String error, List<Card> validCards) {
      this.valid = valid;
      this.error = error;
      this.validCards = validCards;
    }

    public static ValidationResult ok(List<Card> cards) {
      return new ValidationResult(true, null, cards);
    }

    public static ValidationResult invalid(String error) {
      return new ValidationResult(false, error, List.of());
    }

    public boolean isValid() {
      return valid;
    }

    public String getError() {
      return error;
    }

    public List<Card> getValidCards() {
      return validCards;
    }
  }

  public static ValidationResult validateDiscard(List<Card> cards) {
    if (cards == null || cards.isEmpty()) {
      return ValidationResult.invalid("Must discard at least one card");
    }

    int count = cards.size();

    if (count == 1) {
      return ValidationResult.ok(cards);
    } else if (count == 2) {
      return validatePair(cards);
    } else if (count == 3) {
      return validateSequence(cards, 3);
    } else if (count == 4) {
      return validateFourCardSet(cards);
    } else if (count == 5) {
      return validateFiveCardSet(cards);
    } else {
      return ValidationResult.invalid("Can discard up to 5 cards at once");
    }
  }

  private static ValidationResult validatePair(List<Card> cards) {
    if (cards.get(0).rank() == cards.get(1).rank()) {
      return ValidationResult.ok(cards);
    }
    return ValidationResult.invalid("Two cards must be a pair (same rank)");
  }

  private static ValidationResult validateSequence(List<Card> cards, int expectedCount) {
    if (!allSameSuit(cards)) {
      return ValidationResult.invalid(expectedCount + " cards must be consecutive cards of the same suit");
    }
    if (!isConsecutive(cards)) {
      return ValidationResult.invalid(expectedCount + " cards must be consecutive ranks of the same suit");
    }
    return ValidationResult.ok(cards);
  }

  private static ValidationResult validateFourCardSet(List<Card> cards) {
    Map<Integer, Long> rankCounts = cards.stream()
        .collect(Collectors.groupingBy(Card::rank, Collectors.counting()));

    if (rankCounts.size() == 1 && rankCounts.values().iterator().next() == 4) {
      return ValidationResult.ok(cards);
    }

    long pairs = rankCounts.values().stream().filter(c -> c == 2).count();
    if (pairs == 2) {
      return ValidationResult.ok(cards);
    }

    return ValidationResult.invalid("Four cards must be two pairs or four of a kind (same rank)");
  }

  private static ValidationResult validateFiveCardSet(List<Card> cards) {
    if (allSameSuit(cards)) {
      if (isConsecutive(cards)) {
        return ValidationResult.ok(cards);
      }
      return ValidationResult.ok(cards);
    }

    return ValidationResult.invalid("Five cards must be consecutive cards of the same suit, or a flush (5 same suit)");
  }

  private static boolean allSameSuit(List<Card> cards) {
    if (cards.isEmpty()) return false;
    Card.Suit suit = cards.get(0).suit();
    return cards.stream().allMatch(c -> c.suit() == suit);
  }

  private static boolean isConsecutive(List<Card> cards) {
    if (cards.size() < 2) return false;
    List<Integer> ranks = cards.stream().map(Card::rank).sorted().toList();
    for (int i = 0; i < ranks.size() - 1; i++) {
      if (ranks.get(i + 1) != ranks.get(i) + 1) {
        return false;
      }
    }
    return true;
  }
}
