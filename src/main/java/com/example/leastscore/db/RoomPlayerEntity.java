package com.example.leastscore.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "room_players")
@IdClass(RoomPlayerKey.class)
public class RoomPlayerEntity {
  @Id
  @Column(name = "room_id", nullable = false)
  private Long roomId;

  @Id
  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "seat_index", nullable = false)
  private int seatIndex;

  @Column(name = "joined_at", nullable = false)
  private Instant joinedAt = Instant.now();

  @Column(name = "disconnected_at")
  private Instant disconnectedAt;

  public Long getRoomId() {
    return roomId;
  }

  public void setRoomId(Long roomId) {
    this.roomId = roomId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public int getSeatIndex() {
    return seatIndex;
  }

  public void setSeatIndex(int seatIndex) {
    this.seatIndex = seatIndex;
  }

  public Instant getJoinedAt() {
    return joinedAt;
  }

  public Instant getDisconnectedAt() {
    return disconnectedAt;
  }

  public void setDisconnectedAt(Instant disconnectedAt) {
    this.disconnectedAt = disconnectedAt;
  }
}
