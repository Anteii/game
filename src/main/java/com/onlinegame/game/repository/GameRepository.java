package com.onlinegame.game.repository;

import com.onlinegame.game.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GameRepository extends JpaRepository<Game, Long> {
    @Query(value = "SELECT MAX(next_val) FROM hibernate_sequence", nativeQuery = true)
    Long getNextId();
}
