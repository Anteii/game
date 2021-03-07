package com.onlinegame.game.exceptions;

public class EmailClientException extends RuntimeException {
    public EmailClientException() {
        super();
    }

    public EmailClientException(String message) {
        super(message);
    }

    public EmailClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
