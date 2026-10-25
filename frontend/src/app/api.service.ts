import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Booking, Movie, Screening, Seat } from './models';

/** The ONLY place that talks to the backend. Components call these methods. */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private base = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  /**
   * Random id that identifies this browser tab to the server (who holds which seats).
   * Kept in sessionStorage so a page refresh keeps the same identity.
   */
  get token(): string {
    let t = sessionStorage.getItem('seatToken');
    if (!t) {
      t = crypto.randomUUID();
      sessionStorage.setItem('seatToken', t);
    }
    return t;
  }

  /** When the current hold ends (ISO string) - saved so the checkout page can show a countdown. */
  get holdUntil(): string | null { return sessionStorage.getItem('holdUntil'); }
  set holdUntil(v: string | null) {
    if (v) sessionStorage.setItem('holdUntil', v); else sessionStorage.removeItem('holdUntil');
  }

  // ---- movies ----
  movies(): Observable<Movie[]> { return this.http.get<Movie[]>(`${this.base}/movies`); }
  createMovie(m: Movie) { return this.http.post<Movie>(`${this.base}/movies`, m); }
  deleteMovie(id: number) { return this.http.delete<void>(`${this.base}/movies/${id}`); }

  // ---- screenings ----
  screenings(): Observable<Screening[]> { return this.http.get<Screening[]>(`${this.base}/screenings`); }
  screening(id: number) { return this.http.get<Screening>(`${this.base}/screenings/${id}`); }
  createScreening(s: Partial<Screening>) { return this.http.post<Screening>(`${this.base}/screenings`, s); }
  deleteScreening(id: number) { return this.http.delete<void>(`${this.base}/screenings/${id}`); }

  // ---- seats ----
  seats(screeningId: number): Observable<Seat[]> {
    return this.http.get<Seat[]>(`${this.base}/screenings/${screeningId}/seats`, { params: { token: this.token } });
  }
  hold(screeningId: number, seatIds: number[]) {
    return this.http.post<{ holdUntil: string }>(`${this.base}/screenings/${screeningId}/hold`,
      { seatIds, token: this.token });
  }

  // ---- bookings ----
  checkout(name: string, email: string, cardNumber: string) {
    return this.http.post<Booking>(`${this.base}/bookings`, { token: this.token, name, email, cardNumber });
  }
  ticketUrl(code: string): string { return `${this.base}/bookings/${code}/ticket`; }
}
