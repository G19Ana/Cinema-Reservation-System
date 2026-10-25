import { Component, OnDestroy, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription, timer } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiService } from '../api.service';
import { Screening, Seat } from '../models';

/**
 * Interactive seat map drawn as an SVG grid.
 * "Live" availability: the seat list is re-fetched every 3 seconds (simple polling),
 * so seats taken by other people turn red/yellow without refreshing the page.
 */
@Component({
  selector: 'app-seat-map',
  standalone: true,
  imports: [DatePipe, RouterLink],
  templateUrl: './seat-map.component.html',
  styleUrl: './seat-map.component.css'
})
export class SeatMapComponent implements OnInit, OnDestroy {
  // Drawing constants (pixels)
  readonly CELL = 42;      // distance between seat centres
  readonly SEAT = 34;      // seat square size
  readonly LEFT = 30;      // room on the left for row letters
  readonly TOP = 40;       // room on top for the screen bar

  screening?: Screening;
  seats: Seat[] = [];
  selected = new Set<number>();   // ids of seats the user clicked
  error = '';
  busy = false;

  private id = 0;
  private poll?: Subscription;
  private firstLoad = true;

  constructor(private route: ActivatedRoute, private api: ApiService, private router: Router) {}

  ngOnInit() {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.screening(this.id).subscribe(s => (this.screening = s));
    // timer(0, 3000): fire immediately, then every 3 s.
    this.poll = timer(0, 3000).subscribe(() => this.refresh());
  }

  // Stop polling when leaving the page (otherwise requests would continue forever).
  ngOnDestroy() { this.poll?.unsubscribe(); }

  private refresh() {
    this.api.seats(this.id).subscribe(seats => {
      this.seats = seats;
      if (this.firstLoad) {
        // Returning from checkout: seats this browser still holds are pre-selected.
        seats.filter(s => s.mine).forEach(s => this.selected.add(s.id));
        this.firstLoad = false;
      }
      // If someone else grabbed a seat we had selected, un-select it and tell the user.
      for (const s of seats) {
        if (this.selected.has(s.id) && s.status !== 'AVAILABLE' && !s.mine) {
          this.selected.delete(s.id);
          this.error = `Seat ${s.row}${s.number} was just taken by someone else.`;
        }
      }
    });
  }

  // ---- SVG geometry helpers (used by the template) ----
  rowLetters(): string[] { return [...new Set(this.seats.map(s => s.row))]; }
  x(seat: Seat) { return this.LEFT + (seat.number - 1) * this.CELL; }
  y(seat: Seat) { return this.TOP + (seat.row.charCodeAt(0) - 65) * this.CELL; }
  rowY(letter: string) { return this.TOP + (letter.charCodeAt(0) - 65) * this.CELL; }
  get width() { return this.LEFT + (this.screening?.seatsPerRow ?? 0) * this.CELL; }
  get height() { return this.TOP + (this.screening?.rows ?? 0) * this.CELL; }

  /** CSS class that decides the seat colour. */
  seatClass(seat: Seat): string {
    if (this.selected.has(seat.id)) return 'selected';
    if (seat.status === 'BOOKED') return 'booked';
    if (seat.status === 'HELD') return 'held';
    return 'free';
  }

  toggle(seat: Seat) {
    if (seat.status !== 'AVAILABLE' && !seat.mine) return;   // taken: ignore click
    this.error = '';
    if (this.selected.has(seat.id)) this.selected.delete(seat.id);
    else this.selected.add(seat.id);
  }

  get total(): number { return (this.screening?.price ?? 0) * this.selected.size; }

  /** Ask the server to hold the chosen seats for 10 minutes, then go to checkout. */
  continueToCheckout() {
    this.busy = true;
    this.api.hold(this.id, [...this.selected]).subscribe({
      next: res => {
        this.api.holdUntil = res.holdUntil;
        this.router.navigate(['/checkout', this.id]);
      },
      error: (e: HttpErrorResponse) => {
        // 409 = optimistic locking conflict or seat taken: show message and reload the map.
        this.error = e.error?.message ?? 'Something went wrong.';
        this.busy = false;
        this.refresh();
      }
    });
  }
}
