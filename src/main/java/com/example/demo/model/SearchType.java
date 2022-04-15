package com.example.demo.model;

public enum SearchType {
    PLAYLIST("list"),
    VIDEO("v");

    private String shortName;

    SearchType(String shortName){
        this.shortName = shortName;
    }

    public String getShortName(){
        return this.shortName;
    }
}
