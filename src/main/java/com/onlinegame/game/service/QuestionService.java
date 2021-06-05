package com.onlinegame.game.service;

import com.onlinegame.game.model.Question;
import com.onlinegame.game.repository.QuestionRepository;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public Question getRandomQuestion(){
        return questionRepository.getRandomQuestion();
    }
}
