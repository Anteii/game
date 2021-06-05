package com.onlinegame.game.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class GameInProcess {
    private Game game;
    private List<GameRound> rounds;
    private Set<Integer> sectors;
    private int pts;
    public String getScoreString(){
        int expertsPoints = 0;
        for(GameRound round : rounds){
            if (round.getIsAnswerCorrect() != null && round.getIsAnswerCorrect()){
                expertsPoints++;
            }
        }
        return (rounds.size()-expertsPoints) + ":" + expertsPoints;
    }
    public Integer getExpertsScore(){
        int points = 0;
        for(GameRound round : rounds){
            if (round.getIsAnswerCorrect() != null && round.getIsAnswerCorrect()){
                points++;
            }
        }
        return points;
    }
    public Integer getViewersScore(){
        int points = 0;
        for(GameRound round : rounds){
            if (round.getIsAnswerCorrect() != null && !round.getIsAnswerCorrect()){
                points++;
            }
        }
        return points;
    }
}

