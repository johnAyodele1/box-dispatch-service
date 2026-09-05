package com.example.boxdispatch.box.exception;

public class DuplicateBoxTxrefException extends RuntimeException {

    private static final long serialVersionUID = -7627200909853846648L;

	public DuplicateBoxTxrefException(String txref) {
        super("A box with txref '" + txref + "' already exists");
    }
}
