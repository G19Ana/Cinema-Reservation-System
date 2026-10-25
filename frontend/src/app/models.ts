// TypeScript shapes that mirror the JSON sent by the Spring Boot DTOs (see Dtos.java).

export interface Movie {
  id?: number;
  title: string;
  description: string;
  genre: string;
  durationMinutes: number;
}

export interface Screening {
  id: number;
  movieId: number;
  movieTitle: string;
  hallName: string;
  startTime: string;     // ISO date-time string
  price: number;
  rows: number;
  seatsPerRow: number;
}

export type SeatStatus = 'AVAILABLE' | 'HELD' | 'BOOKED';

export interface Seat {
  id: number;
  row: string;           // "A", "B" ...
  number: number;
  status: SeatStatus;
  mine: boolean;         // true if THIS browser holds it
}

export interface Booking {
  code: string;
  movieTitle: string;
  hallName: string;
  startTime: string;
  seats: string[];
  total: number;
  customerName: string;
}
