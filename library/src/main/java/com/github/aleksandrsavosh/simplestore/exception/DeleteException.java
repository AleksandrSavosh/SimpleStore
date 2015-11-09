package com.github.aleksandrsavosh.simplestore.exception;


public class DeleteException extends Exception {
    public DeleteException(String s) {
        super(s);
    }

    public DeleteException(Exception e) {
        super(e);
    }
}
