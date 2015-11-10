package com.github.aleksandrsavosh.simplestore.exception;

public class ReadException extends Exception {

    public ReadException(String s) {
        super(s);
    }

    public ReadException(Exception e) {
        super(e);
    }
}
