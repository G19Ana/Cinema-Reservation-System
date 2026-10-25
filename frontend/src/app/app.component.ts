import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

/** Root component: a header with navigation, and <router-outlet> where each page is shown. */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {}
