# 🔗 smol — URL Shortener

> Because life's too short for long URLs 🐛

A production-ready URL shortener built with **Java 17 + Spring Boot**.  
Shorten links, scan QR codes, track clicks — all in one place.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square)
![Redis](https://img.shields.io/badge/Redis-7.0-red?style=flat-square)

---

## ✨ Features

- 🔗 **URL Shortening** — turn any long URL into a short smol link
- 🏷️ **Custom Aliases** — choose your own short code (e.g. `/my-link`)
- 📱 **QR Code Generation** — every short link gets a scannable QR code
- 📊 **Click Analytics** — track total clicks, last 24h, last 7 days
- ⚡ **Redis Caching** — redirects resolve in under 50ms
- 🛡️ **Rate Limiting** — 100 requests/minute per IP
- ✅ **Input Validation** — rejects invalid URLs with clear error messages
- 🔄 **Async Analytics** — click logging never slows down redirects

---

## 🏗️ Architecture
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   Browser   │────▶│  Spring Boot │────▶│    MySQL    │
│  (Frontend) │     │   REST API   │     │  (storage)  │
└─────────────┘     └──────┬───────┘     └─────────────┘
│
┌──────▼───────┐
│    Redis     │
│   (cache +   │
│ rate limit)  │
└──────────────┘

### Layer Breakdown

| Layer | Package | Responsibility |
|-------|---------|----------------|
| Controller | `controller/` | Parse HTTP requests, return responses |
| Service | `service/` | All business logic |
| Repository | `repository/` | Database access via Spring Data JPA |
| Entity | `entity/` | JPA table mappings |
| DTO | `dto/` | API request/response shapes |
| Config | `config/` | Redis, Async, App properties |
| Util | `util/` | Base62 encoding |

---

## 🧠 Key Design Decisions

### Base62 Encoding
The database auto-increments an ID (1, 2, 3...).
We encode that ID in Base62 (characters: `0-9a-zA-Z`).
Since every DB ID is unique, every short code is guaranteed unique — zero collisions, no retries needed.
ID 1    → "1"
ID 100  → "1C"
ID 9999 → "2Bh"

### Cache-Aside Pattern
On every redirect, we check Redis first (`@Cacheable`).
- **Cache hit** (~1ms) → return immediately
- **Cache miss** → query MySQL, store in Redis, return

Popular links warm the cache naturally through real traffic.

### Async Analytics
Without async: user waits for DB write (adds ~50ms to every redirect).
With `@Async`: DB write fires in background thread, user gets redirect immediately.

### Rate Limiting
Redis `INCR` is atomic. Each IP gets a counter with a TTL.
If count exceeds 100/min → HTTP 429. Counter resets automatically when TTL expires.

---

## 🚀 Quick Start

### Prerequisites

| Tool | Version | Check |
|------|---------|-------|
| Java JDK | 17+ | `java -version` |
| MySQL | 8.0+ | `mysql --version` |
| Redis | 6+ | `redis-server --version` |
| Maven | 3.8+ | `./mvnw --version` |

### 1. Clone
```bash
git clone https://github.com/YOURUSERNAME/smol.git
cd smol
```

### 2. Create Database
```sql
CREATE DATABASE smol_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configure
Open `src/main/resources/application.yml` and set your MySQL password:
```yaml
spring:
  datasource:
    password: your_mysql_password
```

### 4. Start Redis
```bash
redis-server
```

### 5. Run
```bash
./mvnw spring-boot:run
```

### 6. Open
http://localhost:8080

---

## 📡 API Reference

### Create Short URL
```http
POST /api/urls
Content-Type: application/json

{
  "originalUrl": "https://www.example.com/very/long/url",
  "customAlias": "my-link"
}
```
**Response 201:**
```json
{
  "shortCode": "abc",
  "shortUrl": "http://localhost:8080/abc",
  "originalUrl": "https://www.example.com/very/long/url",
  "createdAt": "2025-01-01T12:00:00",
  "qrCodeUrl": "http://localhost:8080/api/qr/abc"
}
```

### Redirect
```http
GET /{shortCode}
→ 302 redirect to original URL
```

### Analytics
```http
GET /api/urls/{shortCode}/analytics
```
```json
{
  "totalClicks": 142,
  "clicksLast24Hours": 23,
  "clicksLast7Days": 98,
  "expired": false
}
```

### QR Code
```http
GET /api/qr/{shortCode}?size=300
→ PNG image
```

### Health Check
```http
GET /api/health
```
```json
{
  "status": "UP",
  "database": "UP",
  "redis": "UP"
}
```

---

## 🛠️ Tech Stack

| Technology | Purpose |
|------------|---------|
| Java 17 | Language |
| Spring Boot 3.2.5 | Framework |
| Spring Data JPA | Database ORM |
| Spring Data Redis | Caching + Rate Limiting |
| MySQL 8.0 | Primary database |
| Redis 7 | Cache + Rate limit store |
| ZXing | QR code generation |
| Lombok | Boilerplate reduction |
| Maven | Build tool |

---

## 📁 Project Structure
src/main/java/com/smol/shortener/
├── config/
│   ├── AppProperties.java       # Binds app.* from application.yml
│   ├── AsyncConfig.java         # Thread pool for async analytics
│   └── RedisConfig.java         # Redis template + cache manager
├── controller/
│   ├── HealthController.java    # GET /api/health
│   ├── QrCodeController.java    # GET /api/qr/{code}
│   ├── RedirectController.java  # GET /{shortCode}
│   └── UrlController.java       # POST /api/urls, GET analytics
├── dto/
│   ├── ApiError.java            # Standard error response shape
│   └── UrlDto.java              # Request + response DTOs
├── entity/
│   ├── AccessLog.java           # Click event table
│   └── Url.java                 # Short URL table
├── exception/
│   ├── GlobalExceptionHandler.java  # @ControllerAdvice
│   └── SmolExceptions.java          # Custom exception types
├── repository/
│   ├── AccessLogRepository.java
│   └── UrlRepository.java
├── service/impl/
│   ├── AnalyticsService.java    # Async click logging
│   ├── QrCodeService.java       # ZXing QR generation
│   ├── RateLimitService.java    # Redis rate limiting
│   └── UrlServiceImpl.java      # Core URL logic + caching
├── util/
│   └── Base62Encoder.java       # ID → short code
└── ShortenerApplication.java    # Entry point

---

## ⚠️ Common Issues

| Error | Fix |
|-------|-----|
| `Access denied for user 'root'` | Wrong MySQL password in `application.yml` |
| `Connection refused` port 6379 | Run `redis-server` |
| `Unknown database 'smol_db'` | Run `CREATE DATABASE smol_db` |
| Port 8080 in use | Change `server.port` in `application.yml` |

---

## 🙋 Author

Built by **[Your Name]** as a backend portfolio project.  
Feel free to fork, star ⭐, and contribute!