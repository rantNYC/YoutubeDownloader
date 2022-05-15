package com.example.controller.connection;

import com.example.controller.YoutubeDownloaderCallback;
import com.example.model.SearchType;
import com.example.model.jpa.YoutubeDataInfo;
import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoDetails;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.AudioFormat;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class YoutubeExternalService implements IExternalConnection<VideoInfo> {

    private final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";

    private final YouTube youtube;
    private final YoutubeDownloader ytDownloader;
    private final File outputPath;

    public YoutubeExternalService(@Value("${server.save.path}") String outputPath,
                                  @Value("${server.download.numRetries:3}") int maxRetries,
                                  @Value("${server.download.numRetries:10}") int numOfThreads) throws GeneralSecurityException, IOException {
        this.outputPath = new File(outputPath);
        if (!this.outputPath.isDirectory())
            throw new IllegalArgumentException("Output path has to be a directory " + outputPath);
        ytDownloader = new YoutubeDownloader();
        ytDownloader.getConfig().setMaxRetries(maxRetries);
        ExecutorService executorService = Executors.newFixedThreadPool(numOfThreads);
        ytDownloader.getConfig().setExecutorService(executorService);
        youtube = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), new GsonFactory(), request -> {
        }).build();
    }

    @Override
    public List<VideoInfo> scanForVideos(String urlId, SearchType searchType) throws IOException {
        List<VideoInfo> videoInfos = new ArrayList<>();

        if (SearchType.PLAYLIST.equals(searchType)) {
            log.info("Currently downloading list: {}", urlId);
            List<String> playList = findVideosInPlayList(urlId);
            playList.forEach(videoId -> addVideoInfoToList(videoInfos, videoId));
        } else if (SearchType.VIDEO.equals(searchType)) {
            addVideoInfoToList(videoInfos, urlId);
        }

        return videoInfos;
    }

    @Override
    public YoutubeDataInfo downloadFile(VideoInfo info) {
        File dataFile = null;
        YoutubeDataInfo youtubeData = null;
        try {
            VideoDetails details = info.details();
            AudioFormat audioFormat = info.bestAudioFormat();

            log.info("Downloading file {}", details.title());
            RequestVideoFileDownload requestVideoFileDownload = new RequestVideoFileDownload(audioFormat)
                    .saveTo(outputPath)
                    .renameTo(details.title())
                    .overwriteIfExists(true).callback(new YoutubeDownloaderCallback(details.title()));
            Response<File> fileResponse = ytDownloader.downloadVideoFile(requestVideoFileDownload);
            dataFile = fileResponse.data();
            youtubeData = YoutubeDataInfo.builder()
                    .title(details.title())
                    .urlId(YOUTUBE_URL + details.videoId())
                    .path(dataFile.toString())
                    .ext(audioFormat.extension().value())
                    .size(dataFile.length())
                    .lengthSeconds(details.lengthSeconds())
                    .isVideo(false)
                    .build();
            log.info("Saved {} to file {}", youtubeData, dataFile);
        } catch (Exception ex) {
            log.error("Error while fetching song: " + info.details().title(), ex);
            if (dataFile != null && dataFile.exists()) dataFile.delete();
        }

        return youtubeData;
    }

    private void addVideoInfoToList(List<VideoInfo> videoInfos, String videoId) {
        RequestVideoInfo requestVideoInfo = new RequestVideoInfo(videoId);
        Response<VideoInfo> infoResponse = ytDownloader.getVideoInfo(requestVideoInfo);
        VideoInfo data = infoResponse.data();
        if (data != null) {
            videoInfos.add(infoResponse.data());
        } else {
            log.warn("No videos found for video {}", videoId);
        }
    }

    private List<String> findVideosInPlayList(String playlistId) throws IOException {
        //TODO: Add exception handling
        List<String> videoIds = new ArrayList<>();
        YouTube.PlaylistItems.List playlistItemsListByPlaylistIdRequest = youtube.playlistItems()
                .list(Collections.singletonList("snippet,contentDetails"));
        playlistItemsListByPlaylistIdRequest.setMaxResults((long) 50);
        playlistItemsListByPlaylistIdRequest.setKey("AIzaSyBR-pEDtDhux9lnyfBzH44ZzS6yCGevQzc");
        playlistItemsListByPlaylistIdRequest.setPlaylistId(playlistId);

        try{
            PlaylistItemListResponse response = playlistItemsListByPlaylistIdRequest.execute();
            List<PlaylistItem> playlistItems = response.getItems();
            playlistItems.forEach(playList -> videoIds.add(playList.getContentDetails().getVideoId()));
        } catch (GoogleJsonResponseException ex){
            log.error("Error code {} and message {}", ex.getDetails().getCode(), ex.getDetails().getMessage());
        }

        return videoIds;
    }

}
