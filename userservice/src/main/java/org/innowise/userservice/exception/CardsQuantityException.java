package org.innowise.userservice.exception;

public class CardsQuantityException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CardsQuantityException(String message) {
        super(message);
    }
}
