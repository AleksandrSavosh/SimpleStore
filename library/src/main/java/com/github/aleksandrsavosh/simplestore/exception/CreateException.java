package com.github.aleksandrsavosh.simplestore.exception;

public class CreateException extends Exception {
    public CreateException(Exception e) {
        super(e);
    }

    public CreateException(String s, Exception e) {
        super(s, e);
    }

    public CreateException(String s) {
        super(s);
    }
}
