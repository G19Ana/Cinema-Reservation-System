import { Component, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../api.service';
import { Movie, Screening } from '../models';

/** Home page: every movie with its upcoming screenings. Click a time to pick seats. */
@Component({
  selector: 'app-movies',
  standalone: true,
  imports: [RouterLink, DatePipe],
  templateUrl: './movies.component.html',
  styleUrl: './movies.component.css'
})
export class MoviesComponent implements OnInit {
  movies: Movie[] = [];
  screenings: Screening[] = [];

  constructor(private api: ApiService) {}

  // ngOnInit runs once when the page opens: load data from the backend.
  ngOnInit() {
    this.api.movies().subscribe(m => (this.movies = m));
    this.api.screenings().subscribe(s => (this.screenings = s));
  }

  screeningsOf(movie: Movie): Screening[] {
    return this.screenings.filter(s => s.movieId === movie.id);
  }
}
