package com.example.controller;

import com.example.model.LinkDto;
import com.example.model.YoutubeDataAssembler;
import com.example.model.YoutubeDataInfo;
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
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/download")
@CrossOrigin
public class DownloaderController {

    private final YoutubeDownloaderService youtubeDownloaderService;
    private final YoutubeDataAssembler youtubeDataAssembler;
    private final PagedResourcesAssembler<YoutubeDataInfo> pagedResourcesAssembler;
    private final EmitterCacheService emitterCacheService;

    @Autowired
    public DownloaderController(YoutubeDownloaderService youtubeDownloaderService, YoutubeDataAssembler youtubeDataAssembler,
                                PagedResourcesAssembler<YoutubeDataInfo> pagedResourcesAssembler, EmitterCacheService emitterCacheService) {
        this.youtubeDownloaderService = youtubeDownloaderService;
        this.youtubeDataAssembler = youtubeDataAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.emitterCacheService = emitterCacheService;
    }

    @PostMapping("/media")
    public ResponseEntity<CollectionModel<EntityModel<YoutubeDataInfo>>> downloadYtbVideo(@Valid @RequestBody LinkDto member,
                                                                                          @RequestHeader("GUID") int guid) throws IOException {
        List<YoutubeDataInfo> fileMembers = youtubeDownloaderService.dispatchCall(member.getUrl(), member.getSearch(), guid);
        CollectionModel<EntityModel<YoutubeDataInfo>> model = youtubeDataAssembler.toCollectionModel(fileMembers);
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

    @PutMapping(value = "/media/{id}")
    public ResponseEntity<EntityModel<?>> updateTitleMedia(@PathVariable("id") Long id,
                                                           @Valid @RequestBody String newTitle) {
        youtubeDownloaderService.updateMediaById(id, newTitle);
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
    //TODO: Fix cancelation
    public SseEmitter eventEmitter() throws IOException {
        int guid = emitterCacheService.addNewEmitter();
        log.info("New GUID sent: {}", guid);
        emitterCacheService.sendEvent(guid, "GUI_ID", guid);
        return emitterCacheService.getCachedEmitter(guid);
    }
}
