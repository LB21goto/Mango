package com.example.DrawStrategy;

public class DrawException extends RuntimeException {

    public DrawException() {
        super();
    }

    public DrawException(String message) {
        super(message);
    }

    public DrawException(String message, Throwable cause) {
        super(message, cause);
    }

    public DrawException(Throwable cause) {
        super(cause);
    }
}
