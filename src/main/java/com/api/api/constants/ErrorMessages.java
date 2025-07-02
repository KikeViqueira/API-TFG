package com.api.api.constants;

public class ErrorMessages {

    public static final String USER_NOT_FOUND = "Usuario no encontrado";
    public static final String CHAT_NOT_FOUND = "Chat not found";
    public static final String MESSAGE_NOT_FOUND = "Message not found";
    public static final String INVALID_INPUT = "Invalid input provided";
    public static final String UNAUTHORIZED_ACCESS = "Unauthorized access";
    public static final String INTERNAL_SERVER_ERROR = "An unexpected error occurred";

    // Private constructor to prevent instantiation
    private ErrorMessages() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
