package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class YoutubeDataInfo {

    public @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "gen")
    @GenericGenerator(name = "gen", strategy = "increment")
    Long id;
    @Column(unique = true)
    private String title;
    private String urlId;
    @JsonIgnore
    private String path;
    private String ext;
    private long size;
    private long lengthSeconds;
    private boolean isVideo;

    public String getFileWithExtension(){
        return String.format("%s.%s", title, ext);
    }
}
