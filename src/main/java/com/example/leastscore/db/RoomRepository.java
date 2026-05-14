package com.example.leastscore.db;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<RoomEntity, Long> {
  Optional<RoomEntity> findByRoomCode(String roomCode);
}

