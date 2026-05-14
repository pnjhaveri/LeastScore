package com.example.leastscore.db;

import java.io.Serializable;
import java.util.Objects;

public class RoomPlayerKey implements Serializable {
  private Long roomId;
  private Long userId;

  public RoomPlayerKey() {}

  public RoomPlayerKey(Long roomId, Long userId) {
    this.roomId = roomId;
    this.userId = userId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RoomPlayerKey that = (RoomPlayerKey) o;
    return Objects.equals(roomId, that.roomId) && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roomId, userId);
  }
}
