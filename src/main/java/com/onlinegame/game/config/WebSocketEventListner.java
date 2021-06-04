package com.onlinegame.game.config;


import com.onlinegame.game.dto.ActiveGame;
import com.onlinegame.game.model.User;
import com.onlinegame.game.repository.UserRepository;
import com.onlinegame.game.service.GameService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListner {

    private final UserRepository userRepository;
    private final GameService gameService;

    public WebSocketEventListner(UserRepository userRepository, GameService gameService) {
        this.userRepository = userRepository;
        this.gameService = gameService;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String username1 = event.getUser().getName();
        User currentUser = userRepository.findByUsername(username1).orElseThrow();
        System.out.println("WEBSOCKET CLOSED");
        if(username != null) {
            System.out.println("User Disconnected : " + username);
        }
        if(username1 != null) {
            ActiveGame game = gameService.getActiveGames().stream()
                    .filter(x -> x.getHostUsername().equals(username1))
                    .findAny().orElse(null);
            if (game != null && currentUser.getInGame())
                gameService.deleteActiveGame(game);
            System.out.println("User Disconnected1 : " + username1);
        }
    }
}
