package com.cinema.repository;

import com.cinema.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data generates the implementation (save, findAll, deleteById ...) at startup. */
public interface MovieRepository extends JpaRepository<Movie, Long> { }
