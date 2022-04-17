package com.example.controller;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
@Slf4j
public class YoutubePlayListService {

    private YouTube youtube;

    @SneakyThrows
    public YoutubePlayListService() {
        youtube = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), new GsonFactory(), request -> {
        }).build();
    }

    @SneakyThrows
    //TODO: Abstract to interface for api youtube
    public List<String> findVideosInPlayList(String playlistId) {
        //TODO: Add exception handling
        List<String> videoIds = new ArrayList<>();
        YouTube.PlaylistItems.List playlistItemsListByPlaylistIdRequest = youtube.playlistItems()
                .list(Collections.singletonList("snippet,contentDetails"));
        playlistItemsListByPlaylistIdRequest.setMaxResults((long) 25);
        playlistItemsListByPlaylistIdRequest.setKey("AIzaSyBR-pEDtDhux9lnyfBzH44ZzS6yCGevQzc");
        playlistItemsListByPlaylistIdRequest.setPlaylistId(playlistId);

        PlaylistItemListResponse response = playlistItemsListByPlaylistIdRequest.execute();
        List<PlaylistItem> playlistItems = response.getItems();
        playlistItems.forEach(playList -> videoIds.add(playList.getContentDetails().getVideoId()));

        return videoIds;
    }
}
