package com.example.leastscore.db;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MoveRepository extends JpaRepository<MoveEntity, Long> {
  @Query(
      value =
          """
          select m from MoveEntity m
          where m.gameId = :gameId
          order by m.createdAt desc
          """)
  List<MoveEntity> findRecentByGameId(@Param("gameId") Long gameId, Pageable pageable);
}

