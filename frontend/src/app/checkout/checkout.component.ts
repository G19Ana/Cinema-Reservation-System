import { Component, OnDestroy, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subscription, timer } from 'rxjs';
import { ApiService } from '../api.service';
import { Booking, Screening, Seat } from '../models';

/** Checkout: shows the held seats, a countdown, a (simulated) payment form, then the confirmation. */
@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [FormsModule, RouterLink, DatePipe],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.css'
})
export class CheckoutComponent implements OnInit, OnDestroy {
  screening?: Screening;
  mySeats: Seat[] = [];
  secondsLeft = 0;
  expired = false;

  // form fields (two-way bound with [(ngModel)])
  name = ''; email = ''; cardNumber = '';

  error = '';
  busy = false;
  booking?: Booking;     // set after a successful payment

  private id = 0;
  private tick?: Subscription;

  constructor(private route: ActivatedRoute, private api: ApiService) {}

  ngOnInit() {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.screening(this.id).subscribe(s => (this.screening = s));
    // Only the seats THIS browser holds ("mine") belong to this checkout.
    this.api.seats(this.id).subscribe(seats => {
      this.mySeats = seats.filter(s => s.mine);
      if (this.mySeats.length === 0) this.expired = true;
    });
    // Countdown: recompute remaining seconds every second from the server's holdUntil time.
    this.tick = timer(0, 1000).subscribe(() => {
      const until = this.api.holdUntil;
      if (!until || this.booking) return;
      this.secondsLeft = Math.max(0, Math.floor((new Date(until).getTime() - Date.now()) / 1000));
      if (this.secondsLeft === 0) this.expired = true;
    });
  }

  ngOnDestroy() { this.tick?.unsubscribe(); }

  get minutes() { return Math.floor(this.secondsLeft / 60); }
  get seconds() { return String(this.secondsLeft % 60).padStart(2, '0'); }
  get total() { return (this.screening?.price ?? 0) * this.mySeats.length; }

  pay() {
    this.busy = true;
    this.error = '';
    this.api.checkout(this.name, this.email, this.cardNumber).subscribe({
      next: b => { this.booking = b; this.api.holdUntil = null; this.busy = false; },
      error: (e: HttpErrorResponse) => { this.error = e.error?.message ?? 'Payment failed.'; this.busy = false; }
    });
  }

  ticketUrl() { return this.booking ? this.api.ticketUrl(this.booking.code) : '#'; }
}
