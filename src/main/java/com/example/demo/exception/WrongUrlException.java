package com.example.demo.exception;

public class WrongUrlException extends RuntimeException{

    public WrongUrlException(String message) {
        super(message);
    }
}
