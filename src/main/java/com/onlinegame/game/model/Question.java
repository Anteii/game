package com.onlinegame.game.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question = (Question) o;
        return Objects.equals(questionId, question.questionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId);
    }
}
