package com.example.model;

import com.example.controller.DownloaderController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class YoutubeDataAssembler implements RepresentationModelAssembler<YoutubeDataInfo, EntityModel<YoutubeDataInfo>> {

    @Override
    public EntityModel<YoutubeDataInfo> toModel(YoutubeDataInfo entity) {
        return EntityModel.of(entity,
                WebMvcLinkBuilder.linkTo(methodOn(DownloaderController.class).returnInfoMedia(entity.getId())).withSelfRel(),
                linkTo(methodOn(DownloaderController.class).retrieveAllMedia()).withRel("all"));
    }

    @Override
    public CollectionModel<EntityModel<YoutubeDataInfo>> toCollectionModel(Iterable<? extends YoutubeDataInfo> entities) {
        List<EntityModel<YoutubeDataInfo>> data = new ArrayList<>();
        entities.forEach(youtubeDataInfo -> data.add(toModel(youtubeDataInfo)));
        return CollectionModel.of(data, linkTo(methodOn(DownloaderController.class).retrieveAllMedia()).withSelfRel());
    }
}
