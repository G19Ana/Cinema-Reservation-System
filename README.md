# Cinema Seat Reservation System

Spring Boot 3 (Java 17) + PostgreSQL backend, Angular 17 frontend.

## Run it

### 1. Database
```sql
CREATE DATABASE cinema;
```
Edit username/password in `backend/src/main/resources/application.properties` if yours differ.

### 2. Backend (http://localhost:8080)
```bash
cd backend
mvn spring-boot:run
```
Tables are created automatically and demo movies/screenings are inserted on first start.

### 3. Frontend (http://localhost:4200)
The Angular CLI generates the config files (angular.json, tsconfig, package.json), so create the
project shell first, then drop in the `src` folder from this repo:
```bash
npx @angular/cli@17 new cinema-ui --routing --style=css --skip-git
cd cinema-ui
# replace the generated src/ with this repo's frontend/src/
npm start
```
(Angular 17 matters: newer versions rename root files such as app.component.ts to app.ts.)

## How the key requirements work

| Requirement | Where |
|---|---|
| Interactive SVG seat map, live availability | `seat-map.component.*` (SVG + 3 s polling) |
| Optimistic locking, no double booking | `@Version` in `ScreeningSeat.java`, handled in `SeatService.java` and `GlobalExceptionHandler.java` |
| 10-minute hold with @Scheduled | `SeatService` sets `heldUntil`, `HoldExpiryJob` frees expired holds every 30 s |
| Checkout / payment flow (simulated) | `BookingService.checkout`, `checkout.component.*` |
| E-ticket PDF with iText | `TicketPdfService.java`, `GET /api/bookings/{code}/ticket` |
| Admin panel | `admin.component.*`, `MovieController`, `ScreeningController` |

## API summary
- `GET/POST /api/movies`, `DELETE /api/movies/{id}`
- `GET/POST /api/screenings`, `GET/DELETE /api/screenings/{id}`
- `GET /api/screenings/{id}/seats?token=...` seat map
- `POST /api/screenings/{id}/hold` hold seats (409 on conflict)
- `POST /api/bookings` checkout, `GET /api/bookings/{code}/ticket` PDF

## Try the concurrency protection
Open the same screening in two different browsers (or one normal + one private window), select the
same seat in both and press "Hold seats" at almost the same time. One succeeds, the other gets a 409
and a message to pick again.

## Known simplifications
- No login: the admin panel is open. Add Spring Security before deploying anywhere public.
- Payment is simulated and card numbers are never stored.
- Live updates use polling; WebSockets/SSE would be the next step.
