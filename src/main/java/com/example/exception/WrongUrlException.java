package com.example.exception;

public class WrongUrlException extends RuntimeException{

    public WrongUrlException(String message) {
        super(message);
    }
}
