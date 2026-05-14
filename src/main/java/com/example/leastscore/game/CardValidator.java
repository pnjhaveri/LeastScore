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

    if (count == 2) {
      return validatePair(cards);
    } else if (count == 3) {
      return validateThreeCardCombo(cards);
    } else if (count == 4) {
      return validateFourCardCombo(cards);
    } else if (count == 5) {
      return validateFiveCardCombo(cards);
    } else {
      return ValidationResult.invalid("Can only discard 2-5 cards");
    }
  }

  private static ValidationResult validatePair(List<Card> cards) {
    if (cards.size() != 2) {
      return ValidationResult.invalid("Pair requires exactly 2 cards");
    }
    if (cards.get(0).rank() == cards.get(1).rank()) {
      return ValidationResult.ok(cards);
    }
    return ValidationResult.invalid("Must be a pair (same rank)");
  }

  private static ValidationResult validateThreeCardCombo(List<Card> cards) {
    Map<Integer, Long> rankCounts = cards.stream()
        .collect(Collectors.groupingBy(Card::rank, Collectors.counting()));

    long pairs = rankCounts.values().stream().filter(c -> c == 2).count();

    if (pairs == 1) {
      return ValidationResult.ok(cards);
    }

    List<Card> sorted = new ArrayList<>(cards);
    sorted.sort(Comparator.comparingInt(Card::rank));
    if (isConsecutive(sorted)) {
      return ValidationResult.ok(cards);
    }

    return ValidationResult.invalid(
        "Three cards must be a pair or a sequence of 3 consecutive ranks. 3-of-a-kind is NOT allowed.");
  }

  private static ValidationResult validateFourCardCombo(List<Card> cards) {
    Map<Integer, Long> rankCounts = cards.stream()
        .collect(Collectors.groupingBy(Card::rank, Collectors.counting()));

    long pairs = rankCounts.values().stream().filter(c -> c == 2).count();

    if (pairs == 2) {
      return ValidationResult.ok(cards);
    }

    return ValidationResult.invalid("Four cards must be two pairs");
  }

  private static ValidationResult validateFiveCardCombo(List<Card> cards) {
    Map<Integer, Long> rankCounts = cards.stream()
        .collect(Collectors.groupingBy(Card::rank, Collectors.counting()));

    long pairs = rankCounts.values().stream().filter(c -> c == 2).count();

    if (pairs == 1) {
      return ValidationResult.ok(cards);
    }

    if (pairs == 2) {
      return ValidationResult.ok(cards);
    }

    List<Card> sorted = new ArrayList<>(cards);
    sorted.sort(Comparator.comparingInt(Card::rank));

    if (isConsecutive(sorted)) {
      return ValidationResult.ok(cards);
    }

    if (isFlush(cards)) {
      return ValidationResult.ok(cards);
    }

    return ValidationResult.invalid(
        "Five cards must be: a pair, two pairs, a sequence of 5, or a flush of 5");
  }

  private static boolean isConsecutive(List<Card> cards) {
    if (cards.size() < 3) return false;

    List<Integer> ranks = cards.stream().map(Card::rank).sorted().toList();

    for (int i = 0; i < ranks.size() - 1; i++) {
      if (ranks.get(i + 1) != ranks.get(i) + 1) {
        return false;
      }
    }
    return true;
  }

  private static boolean isFlush(List<Card> cards) {
    if (cards.size() != 5) return false;

    Card.Suit firstSuit = cards.get(0).suit();
    return cards.stream().allMatch(c -> c.suit() == firstSuit);
  }
}
