package com.example.controller.connection;

import com.example.model.SearchType;
import com.example.model.jpa.YoutubeDataInfo;
import com.github.kiulian.downloader.model.videos.VideoInfo;

import java.io.IOException;
import java.util.List;

public interface IExternalConnection<T> {
    List<VideoInfo> scanForVideos(String urlId, SearchType searchType) throws IOException;
    YoutubeDataInfo downloadFile(T info);
}
