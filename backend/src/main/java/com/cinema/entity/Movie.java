package com.cinema.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/** A movie that can be screened. Table: movie */
@Entity
@Getter @Setter
public class Movie {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @Column(length = 1000)
    private String description;

    private String genre;

    @Min(1)
    private int durationMinutes;
}
