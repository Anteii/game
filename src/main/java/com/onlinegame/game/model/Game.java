package com.onlinegame.game.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;

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
    private java.sql.Timestamp date;
    @Column
    private Integer teamScore;
    @Column
    private Boolean wined;

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
}
