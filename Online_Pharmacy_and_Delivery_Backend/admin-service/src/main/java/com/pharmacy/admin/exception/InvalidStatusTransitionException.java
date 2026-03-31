package com.pharmacy.admin.exception;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String from, String to) {
        super("Invalid order status transition: " + from + " -> " + to);
    }
}
