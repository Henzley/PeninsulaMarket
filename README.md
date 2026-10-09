# Student Marketplace

Campus student marketplace group project.

## Project layout

- `frontend/` contains the website pages, styles, and JavaScript. Maven packages this directory under Spring Boot's `static/` resources.
- `frontend/admin.html` and `frontend/admin-users.html` provide the admin dashboard and account management pages.
- `backend/` contains the Spring Boot API, database configuration, and tests.
- `backend/src/main/resources/` contains Spring Boot configuration. The website source remains in `frontend/`.

## Running locally

Run the backend from one terminal:

```powershell
Set-Location backend
.\mvnw.cmd spring-boot:run
```

The Student Marketplace website and API are both served at `http://localhost:8080`. The application uses MySQL. Start MySQL locally, then configure `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` in the backend environment, or put their values in the ignored `backend/src/main/resources/application.properties`; use `application.properties.example` as a reference. The default connection targets `localhost:3306/student_marketplace` and creates the database if the MySQL user has permission.

Open `http://localhost:8080/` after starting the backend. The root URL and API now use the same origin, so IntelliJ's separate static preview server is not required.

Admin accounts use the existing `users` table with `role=ADMIN`. Set `MARKETPLACE_ADMIN_REGISTRATION_KEY` in the backend environment to enable first-admin registration; the registration page sends it in the `X-Admin-Registration-Key` header. Regular registration always creates a `STUDENT` account, regardless of the submitted role. Configure `MARKETPLACE_JWT_SECRET` to a private key of at least 32 bytes for stable, production-safe JWT signing. If unset, the app generates a temporary development key on startup, invalidating existing tokens when it restarts.

Backend tests use an isolated in-memory H2 database; application runtime uses MySQL.

Run backend tests from `backend/` with:

```powershell
.\mvnw.cmd test
```
