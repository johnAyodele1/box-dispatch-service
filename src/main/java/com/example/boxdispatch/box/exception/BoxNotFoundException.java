package com.example.boxdispatch.box.exception;

public class BoxNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -1572380244336425227L;

	public BoxNotFoundException(String txref) {
        super("Box with txref '" + txref + "' not found");
    }
}
