package com.cinema.controller;

import com.cinema.entity.Movie;
import com.cinema.repository.MovieRepository;
import com.cinema.service.ScreeningService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** /api/movies - list is public; POST/DELETE are used by the admin panel. */
@RestController
@RequestMapping("/api/movies")
public class MovieController {
    private final MovieRepository repo;
    private final ScreeningService screeningService;

    public MovieController(MovieRepository repo, ScreeningService screeningService) {
        this.repo = repo; this.screeningService = screeningService;
    }

    @GetMapping
    public List<Movie> all() { return repo.findAll(); }

    @PostMapping
    public Movie create(@Valid @RequestBody Movie movie) {
        movie.setId(null);               // ignore any id sent by the client: always insert
        return repo.save(movie);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { screeningService.deleteMovie(id); }
}
