package com.example.leastscore.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "games")
public class GameEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "room_id", nullable = false)
  private Long roomId;

  @Column(name = "state_json", nullable = false, columnDefinition = "text")
  private String stateJson;

  @Column(name = "current_turn_user_id")
  private Long currentTurnUserId;

  @Column(name = "started_at", nullable = false)
  private Instant startedAt = Instant.now();

  @Column(name = "ended_at")
  private Instant endedAt;

  public Long getId() {
    return id;
  }

  public Long getRoomId() {
    return roomId;
  }

  public void setRoomId(Long roomId) {
    this.roomId = roomId;
  }

  public String getStateJson() {
    return stateJson;
  }

  public void setStateJson(String stateJson) {
    this.stateJson = stateJson;
  }

  public Long getCurrentTurnUserId() {
    return currentTurnUserId;
  }

  public void setCurrentTurnUserId(Long currentTurnUserId) {
    this.currentTurnUserId = currentTurnUserId;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public Instant getEndedAt() {
    return endedAt;
  }

  public void setEndedAt(Instant endedAt) {
    this.endedAt = endedAt;
  }
}
