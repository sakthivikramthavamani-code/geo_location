# Geo-Based Public Issue Reporting System

A real-time, location-aware civic issue reporting platform built with Java Spring Boot and vanilla JavaScript.

## Features

- **Citizen Module**: Report issues with map-based location selection, image uploads, and real-time status tracking
- **Admin Module**: Interactive dashboard with filtering, statistics, and issue management
- **Real-time Updates**: WebSocket-powered live notifications
- **Geospatial Queries**: PostGIS-powered nearby search and area filtering
- **JWT Authentication**: Secure role-based access control

## Technology Stack

### Backend
- Java 17+
- Spring Boot 3.2
- Spring Security (JWT)
- Spring Data JPA
- Spring WebSocket (STOMP)
- MySQL

### Frontend
- HTML5 / CSS3 / Vanilla JavaScript
- Tailwind CSS
- Leaflet.js + OpenStreetMap
- Material Icons

## Prerequisites

1. **Java 17+** - [Download JDK](https://adoptium.net/)
2. **MySQL 8.0+** - [Download MySQL](https://dev.mysql.com/downloads/)
3. **Maven** (or use included wrapper)

## Database Setup

The application uses **MySQL** with Hibernate auto-DDL. No manual database setup required!

1. Ensure MySQL is running on localhost:3306
2. Update `src/main/resources/application.yml` with your MySQL credentials:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/georeport_db?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
    username: root
    password: your_password
```

The application will automatically:
- Create the database if it doesn't exist
- Create all required tables
- Initialize roles and categories

## Running the Application

### Using Maven Wrapper (Recommended)
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### Using Maven
```bash
mvn spring-boot:run
```

Access the application at: **http://localhost:8080**

## User Registration

All users must register through the application. The first registered user can be assigned admin role through the database if needed.

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register new citizen |
| POST | /api/auth/login | Login and get JWT |

### Issues
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/issues | Create new issue |
| GET | /api/issues/{id} | Get issue by ID |
| GET | /api/issues/my-issues | Get current user's issues |
| GET | /api/issues/public/nearby | Get nearby issues |

### Admin
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/admin/stats | Dashboard statistics |
| GET | /api/admin/issues | All issues with filters |
| PUT | /api/admin/issues/{id}/status | Update issue status |

## Project Structure

```
src/main/
├── java/com/georeport/
│   ├── config/          # Spring configurations
│   ├── controller/      # REST controllers
│   ├── dto/             # Data transfer objects
│   ├── entity/          # JPA entities
│   ├── exception/       # Exception handlers
│   ├── mapper/          # Entity-DTO mappers
│   ├── repository/      # JPA repositories
│   ├── security/        # JWT security
│   ├── service/         # Business logic
│   └── websocket/       # WebSocket handlers
└── resources/
    ├── static/          # Frontend files
    ├── db/              # SQL scripts
    └── application.yml  # Configuration
```

## WebSocket Events

### Topics
- `/topic/issues` - Broadcast all issue updates
- `/user/{id}/queue/notifications` - Personal notifications

### Event Types
- `NEW_ISSUE` - New issue created
- `STATUS_UPDATE` - Issue status changed
- `NOTIFICATION` - Personal notification

## Building for Production

```bash
mvn clean package -DskipTests
java -jar target/geo-issue-reporter-1.0.0.jar
```

## License

MIT License - See LICENSE file for details

---

Built for Smart City Initiatives | © 2024 GeoReport
