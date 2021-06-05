package com.onlinegame.game.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "game")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private Long gameId;
    @Column
    private String gameName;
    @Column
    private Instant date;
    @Column
    private Integer teamScore;
    @Column
    private Boolean wined;

    @ManyToOne(
            cascade = CascadeType.ALL
    )
    @JoinColumn(
            name = "hostId",
            referencedColumnName = "userId"
    )
    private User host;
    @ManyToOne(
            cascade = CascadeType.ALL
    )
    @JoinColumn(
            name = "captainId",
            referencedColumnName = "userId"
    )
    private User captain;
    @ManyToMany(
            cascade = CascadeType.ALL
    )
    @JoinTable(
            name="game_user",
            joinColumns = {@JoinColumn(name = "gameId")},
            inverseJoinColumns = {@JoinColumn(name="userId")}
    )
    private Collection<User> users;

    @ManyToMany(
            cascade = CascadeType.ALL
    )
    @JoinTable(
            name="game_question",
            joinColumns = {@JoinColumn(name = "gameId")},
            inverseJoinColumns = {@JoinColumn(name="questionId")}
    )
    private Collection<Question> questions;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return gameId.equals(game.gameId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameId);
    }
}
