package com.orderanddelivery.exception;

public class InvalidOrderStateException extends RuntimeException {
	public InvalidOrderStateException(String message) {
		super(message);
	}
}