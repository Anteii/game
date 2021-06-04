package com.onlinegame.game.service;

import com.onlinegame.game.dto.ActiveGame;
import com.onlinegame.game.model.Game;
import com.onlinegame.game.model.User;
import com.onlinegame.game.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameService {
    private final List<Game> activeGames;
    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
        activeGames = new ArrayList<>();
    }

    public boolean isActive(String gameName){
        return activeGames.stream().anyMatch(x -> x.getGameName().equals(gameName));
    }

    public ActiveGame createGame(User user, String gameName){
        Game game = new Game();
        game.setUsers(new HashSet<>());
        game.setGameId(gameRepository.getNextId());
        game.setQuestions(new ArrayList<>(14));
        game.setGameName(gameName);
        game.setHost(user);
        activeGames.add(game);
        return new ActiveGame(game.getGameId(), game.getGameName(), game.getHost().getUsername());
    }
    public void deleteActiveGame(ActiveGame activeGame){
        activeGames.removeIf(x -> x.getHost().getUsername().equals(activeGame.getHostUsername()));
    }
    public List<ActiveGame> getActiveGames(){
        return activeGames
                .stream()
                .map(game -> new ActiveGame(game.getGameId(), game.getGameName(), game.getHost().getUsername()))
                .sorted(Comparator.comparing(ActiveGame::getGameName))
                .collect(Collectors.toList());
    }
    public void connectToTheGame(Long gameId, User user){
        Game game = activeGames.stream().filter(x -> x.getGameId().equals(gameId)).findAny().orElseThrow();
        game.getUsers().add(user);
    }

    public Game getGameById(Long id) {
        return activeGames.stream().filter(x -> x.getGameId().equals(id)).findAny().orElseThrow();
    }

}
