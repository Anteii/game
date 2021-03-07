package com.onlinegame.game.repository;

import com.onlinegame.game.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query(value = "SELECT MAX(position) FROM user", nativeQuery = true)
    Integer maxPosition();

    @Query(value = "SELECT MAX(next_val) FROM hibernate_sequence", nativeQuery = true)
    Integer getNextId();

    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);
}
