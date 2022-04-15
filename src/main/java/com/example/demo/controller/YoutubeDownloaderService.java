package com.example.demo.controller;

import com.example.demo.exception.MediaNotFoundException;
import com.example.demo.exception.WrongUrlException;
import com.example.demo.model.SearchType;
import com.example.demo.model.YoutubeDataInfo;
import com.example.demo.model.YoutubeDataRepository;
import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoDetails;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.AudioFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Transactional
@Slf4j
public class YoutubeDownloaderService {

    private final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";

    private final YoutubeDataRepository youtubeDataRepository;
    private final YoutubePlayListService youtubePlayListService;
    private final YoutubeDownloader ytDownloader;
    private final File outputPath;
    private final CompletionService<YoutubeDataInfo> completionService;

    public YoutubeDownloaderService(@Value("${server.save.path}") String outputPath,
                                    @Value("${server.download.numRetries:3}") int maxRetries,
                                    YoutubeDataRepository youtubeDataRepository,
                                    YoutubePlayListService youtubePlayListService) {
        this.outputPath = new File(outputPath);
        this.youtubeDataRepository = youtubeDataRepository;
        this.youtubePlayListService = youtubePlayListService;
        if (!this.outputPath.isDirectory())
            throw new IllegalArgumentException("Output path has to be a directory " + outputPath);
        ytDownloader = new YoutubeDownloader();
        ytDownloader.getConfig().setMaxRetries(maxRetries);
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        completionService = new ExecutorCompletionService<>(executorService);
        ytDownloader.getConfig().setExecutorService(executorService);
    }


    public List<YoutubeDataInfo> retrieveAll() {
        return youtubeDataRepository.findAll();
    }

    public Page<YoutubeDataInfo> retrieveAll(int page, int size) {
        Pageable pages = PageRequest.of(page, size);
        return youtubeDataRepository.findAll(pages);
    }

    public List<YoutubeDataInfo> dispatchCall(String urlId, SearchType searchType, SseEmitter emitter, int guid) {
        List<YoutubeDataInfo> members = new ArrayList<>();
        List<VideoInfo> videoInfos = scanForVideos(urlId, searchType);
        //TODO: Refactor and more elegant way to do this, KISS
        if(emitter == null) return members;
        try {
            emitter.send(SseEmitter.event().name(guid+"-total").data(videoInfos.size()));
        } catch (IOException e) {
//            emitter.completeWithError(e);
        }

        videoInfos.forEach(info -> completionService.submit(() -> downloadFile(info)));
        int fileProcessed = 1;
        for (VideoInfo info : videoInfos) {
            try {
                Future<YoutubeDataInfo> f = completionService.take();
                YoutubeDataInfo fileMember = f.get();
                if (fileMember != null){
                    members.add(fileMember);
                    emitter.send(SseEmitter.event().name(guid+"-progress").id(fileMember.getId().toString()).data(fileProcessed++));
                }
                else {
                    log.warn("File member returned empty for {}", info.details().videoId());
                }
            } catch (Exception e) {
                log.error("Error while processing", e);
            }
        }

        if (videoInfos.size() == 0)
            log.warn("No videos found for: {}", urlId);

        return members;
    }

    public YoutubeDataInfo findMediaById(Long id) {
        return youtubeDataRepository.findById(id)
                .orElseThrow(() -> new MediaNotFoundException(id));
    }

    public void removeMediaById(Long id) {
        YoutubeDataInfo info = findMediaById(id);
        try {
            if (!Files.deleteIfExists(Path.of(info.getPath()))) {
                log.warn("File doesn't exists {}", info.getPath());
            } else {
                log.info("Deleted file {}", info.getPath());
            }
        } catch (IOException ex) {
            log.error("Error while deleting file" + info.getPath(), ex);
        }
        youtubeDataRepository.deleteById(id);
    }

    public byte[] saveSongsIntoZip(List<String> ids) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {

            for (String id : ids) {
                try {
                    Optional<YoutubeDataInfo> fileMemberOptional = youtubeDataRepository.findById(Long.parseLong(id));
                    if (fileMemberOptional.isPresent()) {
                        YoutubeDataInfo fileMember = fileMemberOptional.get();
                        zipOutputStream.putNextEntry(new ZipEntry(fileMember.getFileWithExtension()));
                        FileInputStream fileInputStream = new FileInputStream(fileMember.getPath());
                        zipOutputStream.write(fileInputStream.readAllBytes());
                        zipOutputStream.closeEntry();
                    } else {
                        log.warn("Id {} was not founds in repository", id);
                    }
                } catch (Exception ex) {
                    log.error("Error processing id: " + id, ex);
                }
            }
            zipOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error", e);
        }

        return new byte[0];
    }

    private List<VideoInfo> scanForVideos(String urlId, SearchType searchType) {
        List<VideoInfo> videoInfos = new ArrayList<>();

        if (!StringUtils.hasLength(urlId))
            throw new WrongUrlException("Url id is empty");

        if ((!urlId.contains("youtube") && !urlId.contains("youtu.be")) &&
                (!urlId.contains("watch?") && !urlId.contains("list"))) {
            throw new WrongUrlException("Url is malformed: " + urlId);
        }

        //https://www.youtube.com/watch?v=cds_rfHsJ3Y&list=RDCLAK5uy_k5n4srrEB1wgvIjPNTXS9G1ufE9WQxhnA&index=7
        int pos = urlId.indexOf("?");
        String slice = urlId.substring(pos + 1);
        String[] limits = slice.split("&");
        Map<String, String> contentByParam = new HashMap<>();
        for (String limit : limits) {
            String[] delim = limit.split("=");
            if (delim.length != 2) {
                log.warn("delimiter {} doesn't have length 2", limit);
                continue;
            }
            contentByParam.put(delim[0], delim[1]);
        }

        if (SearchType.PLAYLIST.equals(searchType) && contentByParam.containsKey(searchType.getShortName())) {
            log.info("Currently downloading list: {}", contentByParam.get(searchType.getShortName()));
            List<String> playList = youtubePlayListService.findVideosInPlayList(contentByParam.get("list"));
            playList.forEach(videoId -> addVideoInfoToList(videoInfos, videoId));
        } else if (SearchType.VIDEO.equals(searchType) && contentByParam.containsKey(searchType.getShortName())) {
            String videoId = contentByParam.get(searchType.getShortName());
            addVideoInfoToList(videoInfos, videoId);
        }

        return videoInfos;
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

    private YoutubeDataInfo downloadFile(VideoInfo info) {

        File dataFile = null;
        YoutubeDataInfo youtubeData = null;
        try {
            VideoDetails details = info.details();
            AudioFormat audioFormat = info.bestAudioFormat();
            Optional<YoutubeDataInfo> file = youtubeDataRepository.findByTitle(details.title());
            if (file.isPresent()) {
                return file.get();
            }

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
            youtubeDataRepository.save(youtubeData);
            log.info("Saved {} to file {}", youtubeData, dataFile);
        } catch (Exception ex) {
            log.error("Error while fetching song", ex);
            if (dataFile != null && dataFile.exists()) dataFile.delete();
        }

        return youtubeData;
    }
}
