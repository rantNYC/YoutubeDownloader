package com.example.demo.exception;

public class MediaNotFoundException extends RuntimeException {

    public MediaNotFoundException(Long id) {
        super("Cannot find FileMember for id: " + id);
    }
}
