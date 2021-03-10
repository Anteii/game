package com.onlinegame.game.repository;

import com.onlinegame.game.model.Friendship;
import com.onlinegame.game.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    Optional<Friendship> findByUserOneAndUserTwo(User u1, User u2);
}
