package com.example.leastscore.db;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreRepository extends JpaRepository<ScoreEntity, ScoreKey> {
  List<ScoreEntity> findByGameId(Long gameId);
}

