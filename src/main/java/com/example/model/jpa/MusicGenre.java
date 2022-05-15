package com.example.model.jpa;

import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "id")
public class MusicGenre {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(unique = true)
    public String name;

}
