package com.onlinegame.game.config;


import com.onlinegame.game.model.Game;
import com.onlinegame.game.model.GameInProcess;
import com.onlinegame.game.model.User;
import com.onlinegame.game.repository.UserRepository;
import com.onlinegame.game.restController.GameController;
import com.onlinegame.game.service.GameService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListner {

    private final UserRepository userRepository;
    private final GameService gameService;
    private final GameController gameController;

    public WebSocketEventListner(UserRepository userRepository, GameService gameService, GameController gameController) {
        this.userRepository = userRepository;
        this.gameService = gameService;
        this.gameController = gameController;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String username1 = event.getUser().getName();

        System.out.println("WEBSOCKET CLOSED");
        if(username != null) {
            System.out.println("User Disconnected : " + username);
        }
        if(username1 != null) {
            User currentUser = userRepository.findByUsername(username1).orElseThrow();
            Game game = gameService.getActiveGames().stream()
                    .filter(x -> gameService.isUserInThisGame(currentUser, x))
                    .findAny().orElse(null);
            if (game != null && currentUser.getInGame()) {
                disconnectPlayer(currentUser, game);
            }
            game = gameService.getOngoingGames().stream()
                    .filter(x -> gameService.isUserInThisGame(currentUser, x.getGame()))
                    .findAny().map(GameInProcess::getGame).orElse(null);
            if (game != null && currentUser.getInGame()) {
                disconnectPlayer(currentUser, game);
            }
            System.out.println(gameService.getActiveGames().size());
            System.out.println("User Disconnected1 : " + username1);
        }
    }
    private void disconnectPlayer(User user, Game game){
        gameService.disconnectFromGame(user, game);
        gameController.updateInGameUsersData(game.getGameId());
        user.setInGame(false);
        if (gameService.isGameEmpty(game)) {
            gameService.deleteActiveGame(game);
        }
        userRepository.save(user);
    }
}
