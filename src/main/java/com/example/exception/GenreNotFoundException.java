package com.example.exception;

public class GenreNotFoundException extends RuntimeException {

    public GenreNotFoundException(String name) {
        super("Cannot find MusicGenre for name: " + name);
    }
}
