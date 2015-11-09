package com.github.aleksandrsavosh.simplestore.exception;

public class UpdateException extends Exception {

    public UpdateException(String s) {
        super(s);
    }

    public UpdateException(Exception e) {
        super(e);
    }
}
