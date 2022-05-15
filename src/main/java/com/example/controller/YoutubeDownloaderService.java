package com.example.controller;

import com.example.controller.connection.YoutubeExternalService;
import com.example.exception.GenreNotFoundException;
import com.example.exception.MediaNotFoundException;
import com.example.model.DataModification;
import com.example.model.SearchType;
import com.example.model.jpa.MusicGenre;
import com.example.model.jpa.YoutubeDataInfo;
import com.example.model.jpa.repository.MusicGenreRepository;
import com.example.model.jpa.repository.YoutubeDataRepository;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Transactional
@Slf4j
public class YoutubeDownloaderService {

    private final YoutubeDataRepository youtubeDataRepository;
    private final MusicGenreRepository musicGenreRepository;
    private final YoutubeExternalService youtubeExternalService;
    private final EmitterCacheService emitterCacheService;
    private final CompletionService<YoutubeDataInfo> completionService;


    public YoutubeDownloaderService(YoutubeDataRepository youtubeDataRepository,
                                    MusicGenreRepository musicGenreRepository,
                                    YoutubeExternalService youtubeExternalService,
                                    EmitterCacheService emitterCacheService,
                                    @Value("${server.download.numRetries:10}") int numOfThreads) {
        this.youtubeDataRepository = youtubeDataRepository;
        this.musicGenreRepository = musicGenreRepository;
        this.youtubeExternalService = youtubeExternalService;
        this.emitterCacheService = emitterCacheService;
        ExecutorService executorService = Executors.newFixedThreadPool(numOfThreads);
        completionService = new ExecutorCompletionService<>(executorService);
    }


    public List<YoutubeDataInfo> retrieveAllMedia() {
        return youtubeDataRepository.findAll();
    }

    public List<MusicGenre> retrieveAllGenres() {
        return musicGenreRepository.findAll();
    }

    public List<YoutubeDataInfo> retrieveMediaByGenre(String genre) {
        MusicGenre musicGenre = musicGenreRepository.findByName(genre).orElseThrow(() -> new GenreNotFoundException(genre));
        return youtubeDataRepository.findAllByGenre(musicGenre);
    }

    public Page<YoutubeDataInfo> retrieveAllMedia(int page, int size) {
        Pageable pages = PageRequest.of(page, size);
        return youtubeDataRepository.findAllByDeleted(false, pages);
    }

    public List<YoutubeDataInfo> dispatchCall(String urlId, SearchType searchType, String genre, int guid) throws IOException {
        List<YoutubeDataInfo> members = new ArrayList<>();
        List<VideoInfo> videoInfos = youtubeExternalService.scanForVideos(urlId, searchType);
        emitterCacheService.sendEvent(guid, guid + "-total", videoInfos.size());
        videoInfos.forEach(info -> completionService.submit(() -> startDownloadFlow(info)));
        int fileProcessed = 1;
        for (VideoInfo info : videoInfos) {
            try {
                Future<YoutubeDataInfo> f = completionService.take();
                YoutubeDataInfo fileMember = f.get();
                if (fileMember != null && !fileMember.isDeleted()) {
                    if (StringUtils.hasText(genre)) {
                        Optional<MusicGenre> optionalMusicGenre = musicGenreRepository.findByName(genre);
                        optionalMusicGenre.ifPresentOrElse(fileMember::setGenre, () -> {
                            MusicGenre newGenre = musicGenreRepository.save(MusicGenre.builder().name(genre).build());
                            fileMember.setGenre(newGenre);
                        });
                    }
                    fileMember.setGenre(musicGenreRepository.findByName(genre).orElse(null));
                    youtubeDataRepository.findByTitleOrUrlId(fileMember.getTitle(), fileMember.getUrlId()).ifPresentOrElse(item -> {
                        log.warn("Youtube video already downloaded {} with id {}", fileMember.getTitle(), fileMember.getId());
                    }, () -> {
                        youtubeDataRepository.save(fileMember);
                    });
                    members.add(fileMember);
                    emitterCacheService.sendEvent(guid, guid + "-progress", fileMember.getId().toString(), fileProcessed++);
                } else {
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
        info.setDeleted(true);
        youtubeDataRepository.save(info);
    }

    public byte[] saveSongsIntoZip(List<String> ids) {
        //TODO: Abstract to interface
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

    public void updateMediaById(Long id, DataModification dataModification) {
        Optional<YoutubeDataInfo> optional = youtubeDataRepository.findById(id);
        YoutubeDataInfo dataInfo = optional.orElseThrow(() -> new MediaNotFoundException(id));
        if (StringUtils.hasText(dataModification.getTitle())) dataInfo.setTitle(dataModification.getTitle());
        if (StringUtils.hasText(dataModification.getGenre())) {
            Optional<MusicGenre> optionalMusicGenre = musicGenreRepository.findByName(dataModification.getGenre());
            optionalMusicGenre.ifPresentOrElse(dataInfo::setGenre, () -> {
                MusicGenre newGenre = musicGenreRepository.save(MusicGenre.builder().name(dataModification.getGenre()).build());
                dataInfo.setGenre(newGenre);
            });
        }
        youtubeDataRepository.save(dataInfo);
    }

    private YoutubeDataInfo startDownloadFlow(VideoInfo info) {
        Optional<YoutubeDataInfo> file = youtubeDataRepository.findByTitleOrUrlId(info.details().title(), info.details().videoId());
        return file.orElseGet(() -> youtubeExternalService.downloadFile(info));

    }
}
