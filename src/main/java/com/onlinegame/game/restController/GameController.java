package com.onlinegame.game.restController;

import com.onlinegame.game.dto.ActiveGame;
import com.onlinegame.game.model.*;
import com.onlinegame.game.repository.UserRepository;
import com.onlinegame.game.service.GameService;
import com.onlinegame.game.service.QuestionService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
public class GameController {
    private final UserRepository userRepository;
    final Random random = new Random();
    @AllArgsConstructor private class CreateGameAnswer{
        Long gameId;
    };
    @Data @AllArgsConstructor @NoArgsConstructor private class Event{
        private String type;
        private String value;
    };
    @Data @AllArgsConstructor @NoArgsConstructor private class Player{
        String username;
        String nickname;
        String pictureName;
        Player(User user){
            if (user == null) return;
            this.username = user.getUsername();
            this.nickname = user.getNickname();
            this.pictureName = user.getAvatarPic();
        }
    };
    @Data @AllArgsConstructor @NoArgsConstructor private class GameUsersData{
        private Player Host;
        private Player Captain;
        private List<Player> Players;
        public GameUsersData(Game game){
            Host = new Player(game.getHost());
            Captain = new Player(game.getCaptain());
            Players = game.getUsers().stream().map(Player::new).collect(Collectors.toList());
        }
    };
    @Data @AllArgsConstructor @NoArgsConstructor private class NextQuestionDto{
        String text;
        String answer;
        Double angle;
        Integer sector;
    };
    private final GameService gameService;
    private final QuestionService questionService;
    private final SimpMessagingTemplate template;

    public GameController(UserRepository userRepository, GameService gameService, QuestionService questionService, SimpMessagingTemplate template) {
        this.userRepository = userRepository;
        this.gameService = gameService;
        this.questionService = questionService;
        this.template = template;
    }

    @MessageMapping("/game/{id}")
    public void greeting(@DestinationVariable Long gameId, Event event) throws Exception {
        this.template.convertAndSend("/topic/game/" + gameId, event);
    }
    @PostMapping("/games/create-game")
    public ResponseEntity<ActiveGame> createLobby(String gameName){
        if (gameService.isActive(gameName))
            return new ResponseEntity<>(new ActiveGame(), HttpStatus.BAD_REQUEST);
        Game game = gameService.createGame(getCurrentUser(), gameName);
        this.template.convertAndSend("/topic/active-games", new ActiveGame(game));
        return new ResponseEntity<>(new ActiveGame(game), HttpStatus.OK);
    }
    @GetMapping("/game/{id}/get-players")
    public List<Player> getPlayers(@PathVariable("id") Long gameId){
        return gameService.getGameById(gameId).getUsers().stream()
                .map(Player::new)
                .collect(Collectors.toList());
    }
    @GetMapping("/game/{id}/get-host")
    public Player getHost(@PathVariable("id") Long gameId){
        return new Player(gameService.getGameById(gameId).getHost());
    }
    @GetMapping("/game/{id}/request-next-question")
    ResponseEntity<GameUsersData> nextQuestion(@PathVariable("id") Long gameId){
        GameInProcess gameInProcess;
        try{
            gameInProcess = gameService.getOngoingGameById(gameId);
        } catch (NoSuchElementException ex){
            return new ResponseEntity<GameUsersData>(HttpStatus.FORBIDDEN);
        }
        // генерируем вопрос
        Question question = questionService.getRandomQuestion();
        Question finalQuestion = question;
        boolean flag = gameInProcess.getRounds().stream().anyMatch(x->x.getQuestion().equals(finalQuestion));
        while (flag){
            question = questionService.getRandomQuestion();
            Question finalQuestion1 = question;
            flag = gameInProcess.getRounds().stream().anyMatch(x->x.getQuestion().equals(finalQuestion1));
        }
        // Генерируем сектор
        Integer n = random.nextInt(14);
        while (gameInProcess.getSectors().contains(n))
            n = random.nextInt(14);
        // Генерируем угол на который нужно крутануть
        double angle = generateAngle(n);
        // Добавляем его
        gameInProcess.getRounds().add(new GameRound(question, null));
        gameInProcess.getSectors().add(n);

        this.template.convertAndSend(
                "/topic/game/" + gameId + "/next-question-for-player",
                new NextQuestionDto(question.getText(), null, angle, n)
        );
        this.template.convertAndSend(
                "/topic/game/" + gameId + "/next-question-for-host",
                new NextQuestionDto(question.getText(), question.getAnswer(), angle, n)
        );
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PatchMapping("/game/{id}/answer-question")
    ResponseEntity<GameUsersData> answerQuestion(@PathVariable("id") Long gameId, Boolean isCorrect){
        GameInProcess gameInProcess;
        try{
            gameInProcess = gameService.getOngoingGameById(gameId);
        } catch (NoSuchElementException ex){
            return new ResponseEntity<GameUsersData>(HttpStatus.FORBIDDEN);
        }
        int roundNum = gameInProcess.getRounds().size()-1;
        gameInProcess.getRounds().get(roundNum).setIsAnswerCorrect(isCorrect);
        gameInProcess.setPts(gameInProcess.getPts() + gameInProcess.getRounds().get(roundNum).getQuestion().getComplexity());
        changeState(gameId, "IDLE");
        getScore(gameId);
        if (gameService.gameIsEnd(gameInProcess)){
            gameService.endGame(gameInProcess);
            this.template.convertAndSend(
                    "/topic/game/" + gameId + "/change-state",
                    "END"
            );
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PatchMapping("/game/{id}/change-state")
    ResponseEntity<GameUsersData> changeState(@PathVariable("id") Long gameId, String state){
        GameInProcess gameInProcess;
        try{
            gameInProcess = gameService.getOngoingGameById(gameId);
        } catch (NoSuchElementException ex){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        this.template.convertAndSend(
                "/topic/game/" + gameId + "/change-state", state
        );
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @GetMapping("/game/{id}/get-score")
    ResponseEntity<String> getScore(@PathVariable("id") Long gameId){
        GameInProcess gameInProcess;
        try{
            gameInProcess = gameService.getOngoingGameById(gameId);
        } catch (NoSuchElementException ex){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        this.template.convertAndSend(
                "/topic/game/" + gameId + "/get-score", gameInProcess.getScoreString()
        );
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PatchMapping("/game/{id}/start-game")
    ResponseEntity<GameUsersData> startGame(@PathVariable("id") Long gameId){
        Game game;
        try{
            game = gameService.getActiveGameById(gameId);
        } catch (NoSuchElementException ex){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        gameService.startGame(game);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @GetMapping("/game/{id}/request-update-game-users-data")
    public ResponseEntity<GameUsersData> updateInGameUsersData(@PathVariable("id") Long gameId){
        Game game = null;
        try{
            game = gameService.getGameById(gameId);

        } catch (NoSuchElementException ex){
            return new ResponseEntity<GameUsersData>(HttpStatus.FORBIDDEN);
        }

        this.template.convertAndSend("/topic/game/" + gameId + "/update-game-users-data", new GameUsersData(game));
        return new ResponseEntity<>(new GameUsersData(game), HttpStatus.OK);
    }
    @GetMapping("/games/load-active-games")
    public List<ActiveGame> getActiveGames(){
        return gameService.getActiveGames().stream().map(ActiveGame::new).collect(Collectors.toList());
    }
    private User getCurrentUser(){
        UserDetails principle = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(principle.getUsername()).orElseThrow();
    }
    private double generateAngle(int n){
        double min = 2 * Math.PI / 14 * (n + 10);
        double max = 2 * Math.PI / 14 * (n + 11);
        return (Math.random() * (max - min) + min) % (2 * Math.PI);
    }
}
