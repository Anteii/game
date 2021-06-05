package com.onlinegame.game.dto;

import com.onlinegame.game.model.Game;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActiveGame {
    Long gameId;
    String gameName;
    String hostUsername;

    public ActiveGame(Game game){
        gameId = game.getGameId();
        gameName = game.getGameName();
        if (game.getHost() != null)
            hostUsername = game.getHost().getUsername();
    }
}
