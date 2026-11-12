package com.decms.exception;

public class IntegrityBreachException extends RuntimeException {
    public IntegrityBreachException(String message) {
        super(message);
    }
}
