package com.github.aleksandrsavosh.simplestore.exception;

/**
 * Created by savosh on 11/10/15.
 */
public class DataNotFoundException extends ReadException {
    public DataNotFoundException(String s) {
        super(s);
    }

    public DataNotFoundException(Exception e) {
        super(e);
    }
}
