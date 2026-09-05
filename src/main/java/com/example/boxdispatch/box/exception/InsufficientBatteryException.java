package com.example.boxdispatch.box.exception;

public class InsufficientBatteryException extends RuntimeException {

    private static final long serialVersionUID = 6450066927659399330L;

	public InsufficientBatteryException(String message) {
        super(message);
    }
}
