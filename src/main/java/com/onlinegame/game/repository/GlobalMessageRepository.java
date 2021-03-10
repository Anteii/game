package com.onlinegame.game.repository;

import com.onlinegame.game.model.GlobalMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GlobalMessageRepository extends JpaRepository<GlobalMessage, Long> {
}
