package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class DownloaderAdvice {

    @ResponseBody
    @ExceptionHandler(MediaNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String mediaNotFoundHandler(MediaNotFoundException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(WrongUrlException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String wrongUrlHandler(WrongUrlException ex) {
        return ex.getMessage();
    }
}
