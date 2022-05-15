package com.example.model.jpa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class YoutubeDataInfo {

    public @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "gen")
    @GenericGenerator(name = "gen", strategy = "increment")
    Long id;
    @NotBlank
    @NotEmpty
    @Length(message = "Title has content")
    @NotNull(message = "Title cannot be null")
    @Column(unique = true)
    private String title;
    @Column(unique = true)
    private String urlId;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "genre_id")
    private MusicGenre genre;
    @JsonIgnore
    private String path;
    private String ext;
    private long size;
    private long lengthSeconds;
    private boolean isVideo;
    @JsonIgnore
    @ColumnDefault("false")
    private boolean deleted;

    @JsonProperty("genre")
    public String getGenre() {
        return genre == null ? "Other" : genre.getName();
    }

    @JsonIgnore
    public String getFileWithExtension() {
        return String.format("%s.%s", title, ext);
    }
}
