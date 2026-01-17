# Home Cleaning Booking System

A Spring Boot backend service for managing cleaner availability and bookings for a home cleaning platform.

## 🧱 Tech Stack
- Java 17
- Spring Boot
- Spring Data JPA (Hibernate)
- PostgreSQL
- H2 (integration tests)
- Docker / Docker Compose
- Springdoc OpenAPI (Swagger)

## 🚀 Features
- View cleaner availability by date and time
- Create, reschedule, and cancel bookings
- Automatic cleaner allocation with conflict detection
- Timezone-safe booking validation
- Centralized scheduling configuration
- Global exception handling with consistent error responses
- Auto-generated API documentation

## 📦 Core Domain

- **Vehicle** → owns multiple cleaners
- **Cleaner** → assigned to exactly one vehicle
- **Booking** → date, time window, vehicle, cleaners
- **BookingCleaner** → join entity (unique per booking + cleaner)

## ⚙️ Configuration
```yaml
app:
  timezone: Asia/Kolkata
  working-hours:
    start: "08:00"
    end: "22:00"
  break-minutes: 30
  non-working-day: FRIDAY
  defaults:
    max-cleaner-per-vehicle: 5
```

## ▶️ Running the App
```bash
docker-compose up --build
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html

## 🔗 APIs
### Availability
- GET /availability/date – Daily availability grouped by vehicle
- GET /availability/cleaners – Availability for a given time window

### Bookings
- POST /bookings – Create a booking
- PUT /bookings/{id}/reschedule – Reschedule a booking
- DELETE /bookings/{id} – Cancel a booking

All APIs are documented in Swagger.

## 🧪 Testing
- Unit tests: Business logic using Mockito
- Integration tests: Full Spring context with H2 database
- Test data seeding disabled for integration tests
```bash
mvn test
```

## 📝 Assumptions
- Single-region deployment
- Single-master Relational Database
- Business logic operates in **Asia/Kolkata** timezone
- Cleaner ↔ Vehicle relationships are static (not day-based)
- Availability APIs are read-only and non-locking

## 🔮 Future Improvements
- Update booking **duration**
- Update booking **cleaner count**
- Day‑based cleaner ↔ vehicle mapping
- Holiday calendar support (public + custom)
- Day‑based assignments of cleaners and vehicles
- Idempotency for booking creation
- Pessimistic/Optimistic locking for higher concurrency

## 🧑‍💻 Author
Take‑home assignment implementation  
Focus: correctness, clarity, extensibility