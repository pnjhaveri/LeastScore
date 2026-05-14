package com.example.leastscore.util;

import java.security.SecureRandom;

public final class RoomCodes {
  private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
  private static final SecureRandom RNG = new SecureRandom();

  private RoomCodes() {}

  public static String newCode(int length) {
    var sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(ALPHABET[RNG.nextInt(ALPHABET.length)]);
    }
    return sb.toString();
  }
}

