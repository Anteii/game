package com.onlinegame.game.exceptions;

public class NoSuchUserException extends RuntimeException {
    public NoSuchUserException() {
        super();
    }

    public NoSuchUserException(String message) {
        super(message);
    }

    public NoSuchUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
