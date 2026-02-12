# Volt à vous - Backend API 🔌

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)

> RESTful API for peer-to-peer electric vehicle charging station sharing

**Academic Project** | Developed for Concepteur développeur d'applications (CDA) certification at Human Booster

---

## 🎯 Overview

Backend API powering a platform for sharing EV charging stations between individuals. Implements secure authentication, booking system, payment processing, geolocation search, and automated notifications.

---

## ✨ Key Features

- **JWT Authentication** (RS256) with refresh tokens
- **Geospatial search** using Haversine formula
- **Booking system** with availability verification & anti-double-booking
- **PDF receipt** generation
- **Rating & review system**
- **Email notifications** (Thymeleaf templates)
- **Excel exports** for financial reports
- **Role-based access control** (USER, ADMIN)
- **GDPR compliance** with soft delete mechanism

---

## 🛠️ Tech Stack

**Core**: Java 21, Spring Boot 3.5.10, Maven

**Spring Modules**: Spring Web, Spring Data JPA, Spring Security, Spring Mail, Spring Validation, Actuator

**Database**: MySQL 8.0 (production), H2 (tests)

**Security**: JWT (java-jwt), BCrypt, RSA-256

**Utilities**: Lombok, MapStruct, DataFaker, Imgscalr

**Documentation**: SpringDoc OpenAPI, Swagger UI

**File Generation**: OpenHTMLtoPDF (receipts), Apache POI (Excel), Thymeleaf

**Deployment**: Docker, Docker Compose, GitHub Actions

---

## 📁 Architecture

```
┌─────────────────┐
│   Controllers   │  REST endpoints, input validation
└────────┬────────┘
         │
┌────────▼────────┐
│    Services     │  Business logic, orchestration
└────────┬────────┘
         │
┌────────▼────────┐
│  Repositories   │  Data access (Spring Data JPA)
└────────┬────────┘
         │
┌────────▼────────┐
│  MySQL Database │  Data persistence
└─────────────────┘
```

**Project Structure**:
```
src/main/java/art/lapov/vavapi/
├── config/          # Application configuration
├── controller/      # REST endpoints
├── service/         # Business logic
├── repository/      # Data access
├── model/           # JPA entities
├── dto/             # Data transfer objects
├── mapper/          # Entity-DTO mapping (MapStruct)
├── security/        # JWT filter, security config
├── exception/       # Custom exceptions
├── handler/         # Global exception handler
├── enums/           # Enumerations
├── utils/           # Utility classes
└── VavapiApplication.java
```

---

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- MySQL 8.0+ (or Docker)

### Setup

1. **Clone repository**
   ```bash
   git clone https://github.com/yourusername/vavapi.git
   cd vavapi
   ```

2. **Configure database**
   ```sql
   CREATE DATABASE vavapi CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. **Configure properties** (`application-dev.properties`)
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/vavapi
   spring.datasource.username=your_user
   spring.datasource.password=your_password
   app.frontend.base-url=http://localhost:4200
   ```

4. **Run**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```
   API available at: `http://localhost:8081`

### Using Docker
```bash
docker-compose up -d
```

---

## 📚 API Documentation

**Swagger UI**: `http://localhost:8081/swagger-ui.html`

**OpenAPI Spec**: `http://localhost:8081/v3/api-docs`

### Main Endpoints

| Endpoint | Description | Auth |
|----------|-------------|------|
| `POST /api/auth/register` | Register new user | ❌ |
| `POST /api/auth/login` | Login | ❌ |
| `POST /api/auth/refresh` | Refresh token | ❌ |
| `GET /api/stations/nearby` | Search stations by location | ✅ |
| `POST /api/stations` | Create station | ✅ |
| `POST /api/reservations` | Create reservation | ✅ |
| `PATCH /api/reservations/{id}/accept` | Accept reservation | ✅ |
| `POST /api/reservations/{id}/pay` | Pay reservation | ✅ |
| `POST /api/reviews` | Create review | ✅ |

---

## 🔒 Security

- **JWT tokens** (RS256 algorithm, 30min expiry)
- **Refresh tokens** (30-day expiry, stored in DB)
- **BCrypt** password hashing
- **CORS** configured per environment
- **OWASP protection** (SQL injection, XSS, CSRF)
- **Role-based authorization**
- **Audit logging** for sensitive operations

---

## 🗄️ Database Schema

**Main Entities**:
- `User`: Platform users (drivers/owners)
- `Location`: Geographic locations
- `Station`: Charging stations
- `PricingInterval`: Hourly rates per time slot
- `Reservation`: Bookings (PENDING → ACCEPTED → PAID → COMPLETED)
- `Payment`: Transaction records
- `Review`: Ratings and comments
- `RefreshToken`: Authentication tokens

---

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=ReservationControllerTest

# With coverage report
mvn test jacoco:report
```

**Test Types**:
- Unit tests (JUnit 5, Mockito)
- Integration tests (SpringBootTest, H2)

---

## 🚀 Deployment

**Production Stack**: Docker on VPS with Nginx reverse proxy

**CI/CD**: GitHub Actions automated deployment on push to `main`

```yaml
Test → Build Docker Image → Deploy to VPS → Health Check → Rollback on Failure
```

---

## 📝 Academic Context

This backend validates three CDA certification competencies:

1. **Secure UI components** - Well-documented REST API
2. **Data persistence with security** - Normalized database, GDPR compliance
3. **Multi-tier distributed applications** - N-tier architecture, Docker deployment

**Training**: Human Booster | **Certification**: Level 6 | **Year**: 2026

---

## 🙏 Credits

- **Human Booster** - CDA training program
- **Spring Framework** - Comprehensive documentation
- **Open Source Community** - Essential libraries

---

**Developed as part of CDA Certification | 2026**