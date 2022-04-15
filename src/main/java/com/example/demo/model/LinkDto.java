package com.example.demo.model;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class LinkDto {

    private String url;

    private SearchType search;

    private List<String> ids;
    private List<String> titles;
}
