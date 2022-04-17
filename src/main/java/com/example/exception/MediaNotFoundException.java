package com.example.exception;

public class MediaNotFoundException extends RuntimeException {

    public MediaNotFoundException(Long id) {
        super("Cannot find FileMember for id: " + id);
    }
}
