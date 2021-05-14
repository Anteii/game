package com.onlinegame.game.restController;

import com.onlinegame.game.dto.ActiveGame;
import com.onlinegame.game.model.User;
import com.onlinegame.game.repository.UserRepository;
import com.onlinegame.game.service.GameService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class GameController {
    private final UserRepository userRepository;
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
            this.username = user.getUsername();
            this.nickname = user.getNickname();
            this.pictureName = user.getAvatarPic();
        }
    };
    private final GameService gameService;
    private final SimpMessagingTemplate template;

    public GameController(UserRepository userRepository, GameService gameService, SimpMessagingTemplate template) {
        this.userRepository = userRepository;
        this.gameService = gameService;
        this.template = template;
    }

    // GOVNO KAKOETO
    @MessageMapping("/game/{id}")
    public void greeting(@DestinationVariable Long gameId, Event event) throws Exception {
        this.template.convertAndSend("/topic/game/" + gameId, event);
    }
    @PostMapping("/games/create-game")
    public ResponseEntity<ActiveGame> createLobby(String gameName){
        ActiveGame game = gameService.createGame(getCurrentUser(), gameName);
        this.template.convertAndSend("/topic/active-games", game);
        return new ResponseEntity<>(game, HttpStatus.OK);
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

    @GetMapping("/games/load-active-games")
    public List<ActiveGame> getActiveGames(){
        return gameService.getActiveGames();
    }
    private User getCurrentUser(){
        UserDetails principle = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(principle.getUsername()).orElseThrow();
    }
}
