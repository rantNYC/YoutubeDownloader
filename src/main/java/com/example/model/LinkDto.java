package com.example.model;

import lombok.Data;

import java.util.List;

@Data
public class LinkDto {

    private String url;
    private String genre;
    private SearchType search;


    private List<String> ids;
    private List<String> titles;
}
