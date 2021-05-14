package com.onlinegame.game.exceptions;

public class UserBannedException extends RuntimeException {
    public UserBannedException() {
        super();
    }

    public UserBannedException(String message) {
        super(message);
    }

    public UserBannedException(String message, Throwable cause) {
        super(message, cause);
    }
}
