import { Routes } from '@angular/router';
import { MoviesComponent } from './movies/movies.component';
import { SeatMapComponent } from './seat-map/seat-map.component';
import { CheckoutComponent } from './checkout/checkout.component';
import { AdminComponent } from './admin/admin.component';

// URL -> page. ":id" is a variable part of the URL (the screening id).
export const routes: Routes = [
  { path: '', component: MoviesComponent },
  { path: 'screening/:id', component: SeatMapComponent },
  { path: 'checkout/:id', component: CheckoutComponent },
  { path: 'admin', component: AdminComponent },
  { path: '**', redirectTo: '' }
];
