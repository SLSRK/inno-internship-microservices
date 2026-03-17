package org.innowise.userservice.exception;

public class NotActiveException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NotActiveException(String message) {
        super(message);
    }
}
