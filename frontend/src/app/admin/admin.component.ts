import { Component, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiService } from '../api.service';
import { Movie, Screening } from '../models';

/**
 * Admin panel: manage movies and screenings (a screening = movie + hall + time + seat layout).
 * NOTE: there is no login. For a real deployment protect /api writes with Spring Security.
 */
@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [FormsModule, DatePipe],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent implements OnInit {
  movies: Movie[] = [];
  screenings: Screening[] = [];
  error = '';

  // "new movie" form model
  newMovie: Movie = { title: '', description: '', genre: '', durationMinutes: 100 };

  // "new screening" form model (startTime comes from <input type="datetime-local">)
  newScreening = { movieId: 0, hallName: 'Hall 1', startTime: '', price: 10, rows: 6, seatsPerRow: 10 };

  constructor(private api: ApiService) {}

  ngOnInit() { this.load(); }

  load() {
    this.api.movies().subscribe(m => {
      this.movies = m;
      if (!this.newScreening.movieId && m.length) this.newScreening.movieId = m[0].id!;
    });
    this.api.screenings().subscribe(s => (this.screenings = s));
  }

  addMovie() {
    this.api.createMovie(this.newMovie).subscribe({
      next: () => {
        this.newMovie = { title: '', description: '', genre: '', durationMinutes: 100 };
        this.error = '';
        this.load();
      },
      error: e => this.fail(e)
    });
  }

  removeMovie(m: Movie) {
    if (confirm(`Delete "${m.title}" and all its screenings?`))
      this.api.deleteMovie(m.id!).subscribe({ next: () => this.load(), error: e => this.fail(e) });
  }

  addScreening() {
    this.api.createScreening(this.newScreening).subscribe({
      next: () => { this.error = ''; this.load(); },
      error: e => this.fail(e)
    });
  }

  removeScreening(s: Screening) {
    if (confirm('Delete this screening and its bookings?'))
      this.api.deleteScreening(s.id).subscribe({ next: () => this.load(), error: e => this.fail(e) });
  }

  private fail(e: HttpErrorResponse) { this.error = e.error?.message ?? 'Request failed.'; }
}
