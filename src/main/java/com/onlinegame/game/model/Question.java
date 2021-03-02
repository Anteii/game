package com.onlinegame.game.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "question")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private Long questionId;
    @Column
    private String text;
    @Column
    private String answer;
    @Column
    private Integer complexity;
    @ManyToOne
    @JoinColumn(
            name = "themeId",
            referencedColumnName = "themeId"
    )
    private Theme theme;
    @ManyToMany(mappedBy = "questions")
    private Collection<Game> games;

}
