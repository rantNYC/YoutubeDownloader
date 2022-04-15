package com.example.demo.controller;

import com.example.demo.model.LinkDto;
import com.example.demo.model.YoutubeDataAssembler;
import com.example.demo.model.YoutubeDataInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequestMapping("/download")
public class DownloaderController {

    private final YoutubeDownloaderService youtubeDownloaderService;
    private final YoutubeDataAssembler youtubeDataAssembler;
    private final PagedResourcesAssembler<YoutubeDataInfo> pagedResourcesAssembler;
    private final Map<Integer, SseEmitter> sseEmitterMap;
    private final AtomicInteger counter;

    @Autowired
    public DownloaderController(YoutubeDownloaderService youtubeDownloaderService, YoutubeDataAssembler youtubeDataAssembler,
                                PagedResourcesAssembler<YoutubeDataInfo> pagedResourcesAssembler) {
        this.youtubeDownloaderService = youtubeDownloaderService;
        this.youtubeDataAssembler = youtubeDataAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.sseEmitterMap = new ConcurrentHashMap<>();
        this.counter = new AtomicInteger();
    }

    @PostMapping("/media")
    public ResponseEntity<CollectionModel<EntityModel<YoutubeDataInfo>>> downloadYtbVideo(@Valid @RequestBody LinkDto member,
                                                                                          @RequestHeader("GUID") int guid) throws IOException {
        List<YoutubeDataInfo> fileMembers = youtubeDownloaderService.dispatchCall(member.getUrl(),
                member.getSearch(), sseEmitterMap.get(guid), guid);
        CollectionModel<EntityModel<YoutubeDataInfo>> model = youtubeDataAssembler.toCollectionModel(fileMembers);
//        if(sseEmitterMap.get(guid) != null) sseEmitterMap.get(guid).complete();
        if (fileMembers.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity //
                .created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
                .body(model);
    }

    @GetMapping("/media")
    public ResponseEntity<PagedModel<EntityModel<YoutubeDataInfo>>> retrievePageVideos(@RequestParam Optional<Integer> page, @RequestParam Optional<Integer> size) {
        Page<YoutubeDataInfo> data = youtubeDownloaderService.retrieveAll(page.orElse(0), size.orElse(50));
        PagedModel<EntityModel<YoutubeDataInfo>> collModel = pagedResourcesAssembler
                .toModel(data, youtubeDataAssembler);
        return ResponseEntity.ok(collModel);
    }

    @GetMapping("/media/all")
    public ResponseEntity<CollectionModel<EntityModel<YoutubeDataInfo>>> retrieveAllMedia() {
        List<YoutubeDataInfo> data = youtubeDownloaderService.retrieveAll();
        return ResponseEntity.ok(youtubeDataAssembler.toCollectionModel(data));

    }

    @GetMapping(value = "/media/{id}")
    public ResponseEntity<EntityModel<YoutubeDataInfo>> returnInfoMedia(@PathVariable("id") Long id) {
        YoutubeDataInfo youtubeDataInfo = youtubeDownloaderService.findMediaById(id);
        return ResponseEntity.ok(youtubeDataAssembler.toModel(youtubeDataInfo));
    }

    @DeleteMapping(value = "/media/{id}")
    public ResponseEntity<EntityModel<?>> deleteInfoMedia(@PathVariable("id") Long id) {
        youtubeDownloaderService.removeMediaById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/media/stream/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<FileSystemResource> returnStreamMedia(@PathVariable("id") Long id,
                                                                HttpServletResponse response) {
        YoutubeDataInfo filePathById = youtubeDownloaderService.findMediaById(id);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                String.format("attachment; filename=%s.%s", filePathById.getTitle(), filePathById.getExt()));
        return ResponseEntity.ok(new FileSystemResource(filePathById.getPath()));
    }

    @PostMapping(value = "/file/zip", produces = "application/zip")
    public ResponseEntity<byte[]> returnZipFiles(@RequestBody LinkDto linkDto,
                                                 HttpServletResponse response) {
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                String.format("attachment; filename=%s.%s", "audio_files", "zip"));
        return ResponseEntity.ok(youtubeDownloaderService.saveSongsIntoZip(linkDto.getIds()));
    }

    @GetMapping("/progress")
    public SseEmitter eventEmitter() throws IOException {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        int guid = counter.incrementAndGet();
        sseEmitter.send(SseEmitter.event().name("GUI_ID").data(guid));
        sseEmitter.onCompletion(() -> log.info("Completed"));
        sseEmitter.onTimeout(() -> sseEmitterMap.remove(guid));
        sseEmitter.onError((ex) -> sseEmitterMap.remove(guid));
        sseEmitterMap.put(guid, sseEmitter);

        return sseEmitter;
    }
}
