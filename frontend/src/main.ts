import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

// Starts the app with AppComponent as the root.
bootstrapApplication(AppComponent, appConfig).catch(err => console.error(err));
