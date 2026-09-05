package com.example.boxdispatch.box.exception;

public class WeightLimitExceededException extends RuntimeException {

    private static final long serialVersionUID = -664543607917153030L;

	public WeightLimitExceededException(String message) {
        super(message);
    }
}
