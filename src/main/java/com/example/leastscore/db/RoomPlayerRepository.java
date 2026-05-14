package com.example.leastscore.db;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomPlayerRepository extends JpaRepository<RoomPlayerEntity, RoomPlayerKey> {
  List<RoomPlayerEntity> findByRoomIdOrderBySeatIndexAsc(Long roomId);
}

