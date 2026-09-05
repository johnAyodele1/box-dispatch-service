package com.example.boxdispatch.box.exception;

public class InvalidBoxStateException extends RuntimeException {

    private static final long serialVersionUID = -8460466741405197247L;

	public InvalidBoxStateException(String message) {
        super(message);
    }
}
