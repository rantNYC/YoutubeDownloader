package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class YoutubeDownloaderApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(YoutubeDownloaderApplication.class, args);
    }

}
