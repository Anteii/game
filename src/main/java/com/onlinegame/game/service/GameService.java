package com.onlinegame.game.service;

import com.onlinegame.game.model.Game;
import com.onlinegame.game.model.GameInProcess;
import com.onlinegame.game.model.GameRound;
import com.onlinegame.game.model.User;
import com.onlinegame.game.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameService {
    private Long nextId;
    private final List<Game> activeGames;
    private final List<GameInProcess> ongoingGames;
    private final GameRepository gameRepository;
    private final UserService userService;
    final int MAX_PLAYERS_NUMBER = 2;
    final int MAX_ROUND_NUMBER = 3;

    public GameService(GameRepository gameRepository, UserService userService) {
        nextId = gameRepository.getNextId()-1;
        this.gameRepository = gameRepository;
        this.userService = userService;
        activeGames = new ArrayList<>();
        ongoingGames = new ArrayList<>();
    }

    public boolean isActive(String gameName){
        return activeGames.stream().anyMatch(x -> x.getGameName().equals(gameName));
    }

    public Game createGame(User user, String gameName){
        Game game = new Game();
        game.setGameId(++nextId);
        game.setUsers(new LinkedList<>());
        game.setQuestions(new ArrayList<>(14));
        game.setGameName(gameName);
        activeGames.add(game);
        System.out.println("GaME ID: " + game.getGameId());
        return game;
    }
    public void deleteActiveGame(Game game){
        activeGames.remove(game);
    }
    public List<Game> getActiveGames(){
        return activeGames
                .stream()
                .sorted(Comparator.comparing(Game::getGameName))
                .collect(Collectors.toList());
    }
    public List<GameInProcess> getOngoingGames(){
        return ongoingGames
                .stream()
                .sorted(Comparator.comparing(x->x.getGame().getGameName()))
                .collect(Collectors.toList());
    }
    public void connectToTheGame(Game game, User user){
        if (game.getHost() == null){
            game.setHost(user);
        }
        else{
            if (game.getCaptain() == null){
                game.setCaptain(user);
            }
            else{
                if (game.getUsers().size() < MAX_PLAYERS_NUMBER){
                    game.getUsers().add(user);
                }
            }
        }
    }
    public void startGame(Game game){
        deleteActiveGame(game);
        ongoingGames.add(new GameInProcess(game, new LinkedList<>(), new HashSet<>(), 0));
    }
    public Game getGameById(Long id) {
        return activeGames.stream().filter(x -> x.getGameId().equals(id)).findAny().orElseThrow();
    }
    public Game getActiveGameById(Long id) {
        return activeGames.stream().filter(x -> x.getGameId().equals(id)).findAny().orElseThrow();
    }
    public GameInProcess getOngoingGameById(Long id) {
        return ongoingGames.stream().filter(x -> x.getGame().getGameId().equals(id)).findAny().orElseThrow();
    }
    public boolean isActiveGameFull(Game activeGame){
        return (activeGame.getUsers().size() == MAX_PLAYERS_NUMBER &&
                activeGame.getCaptain() != null &&
                activeGame.getHost() != null);
    }
    public boolean isUserInThisGame(User user, Game activeGame){
        return (activeGame.getUsers().stream().anyMatch(x->x.equals(user)) ||
                user.equals(activeGame.getHost()) || user.equals(activeGame.getCaptain()));
    }
    public void disconnectFromGame(User user, Game game){
        if (user.equals(game.getCaptain())){
            game.setCaptain(null);
        }
        else if (user.equals(game.getHost())){
            game.setHost(null);
        }
        else{
            game.getUsers().remove(user);
        }
    }
    public boolean isGameEmpty(Game game){
        return (game.getUsers().size() == 0 &&
                game.getCaptain() == null &&
                game.getHost() == null);
    }

    public boolean gameIsEnd(GameInProcess gameInProcess) {
        int n1 = gameInProcess.getExpertsScore();
        int n2 = gameInProcess.getViewersScore();
        return (Math.max(n1, n2) >= MAX_ROUND_NUMBER && n1 != n2);
    }

    public void endGame(GameInProcess gameInProcess) {
        int expertsScore = gameInProcess.getExpertsScore();
        int viewersScore = gameInProcess.getViewersScore();

        User host = gameInProcess.getGame().getHost();
        User captain = gameInProcess.getGame().getCaptain();
        List<User> players = (List<User>) gameInProcess.getGame().getUsers();

        host.setScore(host.getScore()+1);
        host.setTotalGames(host.getTotalGames() + 1);

        captain.setScore((int) (captain.getScore() + gameInProcess.getPts() * 1.5));
        captain.setTotalGames(host.getTotalGames() + 1);
        for (User player : players){
            player.setScore(player.getScore() + gameInProcess.getPts());
            player.setTotalGames(host.getTotalGames() + 1);
        }

        if (expertsScore > viewersScore){
            host.setWinedGames(host.getWinedGames() + 1);
            captain.setWinedGames(captain.getWinedGames() + 1);
            for (User player : players){
                player.setWinedGames(player.getWinedGames() + 1);
            }
        }

        Game game = gameInProcess.getGame();
        game.setQuestions(gameInProcess.getRounds().stream().map(GameRound::getQuestion).collect(Collectors.toList()));
        game.setDate(Instant.now());
        game.setWined(expertsScore > viewersScore);
        game.setTeamScore(gameInProcess.getPts());

        userService.updateUser(List.of(captain, host));
        userService.updateUser(players);
        gameRepository.save(game);
        userService.updateLadder();
    }
}
