package com.api.api.exceptions;

public class TodayChatAlreadyExists extends RuntimeException {
    public TodayChatAlreadyExists(String message) {
        super(message);
    }
}
