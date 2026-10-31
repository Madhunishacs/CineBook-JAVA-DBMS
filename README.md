# CineBook — Java + MySQL starter project

This project keeps your existing CineBook HTML/CSS/JavaScript frontend and adds a Java Servlet/JDBC backend starter with MySQL.

## Requirements
- JDK 17 or newer
- Apache Maven
- Apache Tomcat 10.1 (Jakarta Servlet 6 compatible)
- MySQL Server
- VS Code (Java Extension Pack recommended) or IntelliJ IDEA

## 1. Configure MySQL
1. Open MySQL Workbench and connect to your local server.
2. Open `database/schema.sql`.
3. Execute the script.
4. If your MySQL root account has a password, set `CINEBOOK_DB_PASSWORD` in your environment before starting Tomcat.

Default DB connection:
- URL: `jdbc:mysql://localhost:3306/cinebook_db?useSSL=false&serverTimezone=UTC`
- User: `root`
- Password: blank by default; change via environment variable for your installation.

Windows PowerShell for the current terminal session:
```powershell
$env:CINEBOOK_DB_USER="root"
$env:CINEBOOK_DB_PASSWORD="YOUR_MYSQL_PASSWORD"
$env:CINEBOOK_DB_URL="jdbc:mysql://localhost:3306/cinebook_db?useSSL=false&serverTimezone=UTC"
```
Do not put a real password into source code or commit it to version control.

## 2. Build the Java web app
From this folder in a terminal:
```powershell
mvn clean package
```
This should create `target/cinebook.war`.

## 3. Run with Tomcat 10.1
1. Copy `target/cinebook.war` into Tomcat's `webapps` folder.
2. Start Tomcat.
3. Open `http://localhost:8080/cinebook/`.

The existing frontend is preserved as `src/main/webapp/index.html`. Its current demo booking flow still uses browser `localStorage`; the APIs below are backend starter endpoints and the frontend must be integrated with them before the full website uses MySQL for bookings.

## API endpoints included
- `GET /cinebook/api/movies` — active movie list from MySQL
- `GET /cinebook/api/shows?movieId=1` — future showtimes for a movie
- `POST /cinebook/api/bookings` — create a demo booking
- `GET /cinebook/api/bookings?code=BOOKING_CODE` — retrieve a ticket

Example booking JSON:
```json
{
  "showId": 1,
  "seats": ["A1", "A2"],
  "paymentMethod": "UPI (simulation)",
  "snacks": true,
  "coupon": "CINE10"
}
```

## Important limitations (starter build)
- Payments are simulated only; no real money is collected.
- Authentication and customer/admin roles are not implemented yet.
- Cancellation API and live seat-map API are not implemented yet.
- The frontend's current booking flow has not yet been switched from `localStorage` to the Java APIs.
- For production-grade concurrent seat reservations, add a robust transactional seat inventory model and database-enforced active-seat uniqueness; the current schema's status-based unique key is only a starter approach.
- Do not deploy publicly or use real customer/payment data until authentication, authorization, validation, transaction safety, and security review are complete.

## Troubleshooting
- `mvn` not recognized: install Maven and reopen the terminal.
- Database connection error: check MySQL service, database name, username/password and environment variables.
- Tomcat startup errors: ensure Tomcat 10.1 and JDK 17+ are being used.
- 404 at `/cinebook/`: check that `cinebook.war` is in Tomcat's `webapps` directory and Tomcat started successfully.
