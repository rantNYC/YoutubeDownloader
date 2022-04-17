package com.example.controller;

import com.github.kiulian.downloader.downloader.YoutubeProgressCallback;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class YoutubeDownloaderCallback implements YoutubeProgressCallback<File> {

    private final String title;

    public YoutubeDownloaderCallback(String title) {
        this.title = title;
    }

    @Override
    public void onDownloading(int i) {
        log.info("{} Progress {}", title, i);
    }

    @Override
    public void onFinished(File file) {
        log.info("Finished file: {}", file);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Error downloading file", throwable);
    }
}
