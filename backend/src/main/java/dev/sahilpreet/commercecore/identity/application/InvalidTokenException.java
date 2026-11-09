package dev.sahilpreet.commercecore.identity.application;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super("This token is invalid");
    }
}
