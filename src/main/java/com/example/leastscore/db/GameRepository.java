package com.example.leastscore.db;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<GameEntity, Long> {
  Optional<GameEntity> findFirstByRoomIdOrderByStartedAtDesc(Long roomId);
}

