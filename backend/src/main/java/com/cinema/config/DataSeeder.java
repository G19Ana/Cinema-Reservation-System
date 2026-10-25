package com.cinema.config;

import com.cinema.dto.Dtos.ScreeningRequest;
import com.cinema.entity.Movie;
import com.cinema.repository.MovieRepository;
import com.cinema.service.ScreeningService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Runs once at startup and inserts demo data if the database is empty. */
@Component
public class DataSeeder implements CommandLineRunner {
    private final MovieRepository movies;
    private final ScreeningService screenings;

    public DataSeeder(MovieRepository movies, ScreeningService screenings) {
        this.movies = movies;
        this.screenings = screenings;
    }

    @Override
    public void run(String... args) {
        if (movies.count() > 0) return;
        Movie m1 = movie("Midnight Express Lane", "A courier races a blizzard across the city.", "Thriller", 112);
        Movie m2 = movie("The Last Projectionist", "An old cinema fights to stay open.", "Drama", 98);
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0).withSecond(0).withNano(0);
        screenings.create(new ScreeningRequest(m1.getId(), "Hall 1", tomorrow, new BigDecimal("12.50"), 6, 10));
        screenings.create(new ScreeningRequest(m1.getId(), "Hall 2", tomorrow.plusHours(3), new BigDecimal("12.50"), 5, 8));
        screenings.create(new ScreeningRequest(m2.getId(), "Hall 1", tomorrow.plusDays(1), new BigDecimal("10.00"), 6, 10));
    }

    private Movie movie(String title, String desc, String genre, int minutes) {
        Movie m = new Movie();
        m.setTitle(title); m.setDescription(desc); m.setGenre(genre); m.setDurationMinutes(minutes);
        return movies.save(m);
    }
}
