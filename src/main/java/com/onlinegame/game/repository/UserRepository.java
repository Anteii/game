package com.onlinegame.game.repository;

import com.onlinegame.game.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query(value = "SELECT MAX(position) FROM user", nativeQuery = true)
    Integer maxPosition();

    @Query(value = "SELECT MAX(next_val) FROM hibernate_sequence", nativeQuery = true)
    Integer getNextId();

    @Query( value =
            "SELECT * FROM user WHERE user_id=ANY(" +
                    "SELECT user_twoid FROM friendship WHERE user_oneid=?1)",
            nativeQuery = true
    )
    List<User> getFriendsListById(Long id);
    Optional<User> findByEmail(String email);

}
