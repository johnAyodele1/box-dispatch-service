package com.example.boxdispatch.box.exception;

public class DuplicateItemCodeException extends RuntimeException {

    private static final long serialVersionUID = 3091793097606680043L;

	public DuplicateItemCodeException(String message) {
        super(message);
    }
}
