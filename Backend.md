# InkWell — Backend Architecture & Lifecycle

## 1. Project Overview

**InkWell** is a microservices-based blogging platform built with Spring Boot 4.x, Spring Cloud 2025.x, and Java 21. All inter-service communication flows through a central API Gateway. Each service owns its own MySQL database and registers with Eureka for service discovery.

---

## 2. Service Registry

| Service | Port | Eureka Name | Database | Base Package |
|---|---|---|---|---|
| eureka-server | 8761 | — | — | `com.inkWell.eureka` |
| api-gateway | 8080 | API-GATEWAY | — | `com.inkWell.api_gateway` |
| auth-service | 8081 | AUTH-SERVICE | `inkwell_auth` | `com.inkWell.auth` |
| admin-server | 8082 | ADMIN-SERVER | — (calls auth-service) | `com.inkWell.admin` |
| post-service | 8083 | POST-SERVICE | `inkwell_posts` | `com.inkWell.post` |
| comment-service | 8084 | COMMENT-SERVICE | `inkwell_comments` | `com.inkWell.comment` |
| category-service | 8085 | CATEGORY-SERVICE | `inkwell_categories` | `com.inkWell.category` |
| media-service | 8086 | MEDIA-SERVICE | `inkwell_media` | `com.inkWell.media` |
| newsletter-service | 8087 | NEWSLETTER-SERVICE | `inkwell_newsletter` | `com.inkWell.newsletter` |
| notification-service | 8088 | NOTIFICATION-SERVICE | `inkwell_notifications` | `com.inkWell.notification` |

---

## 3. Startup Order

```
1. eureka-server  (must be running first)
2. auth-service   (registers with Eureka)
3. All other services (register with Eureka)
4. api-gateway    (last — discovers all services via Eureka)
```

---

## 4. API Gateway (Reactive — Spring WebFlux)

### Architecture
- **Dependency:** `spring-cloud-starter-gateway` (Reactive/WebFlux)
- **CORS:** Handled ONLY at the Gateway via `CorsConfig.java` (CorsWebFilter)
- **JWT Validation:** `JwtAuthenticationFilter` (reactive `GlobalFilter`)
- **Internal Secret:** The GlobalFilter injects `X-Internal-Secret` header into every request forwarded to downstream services

### Public Paths (no JWT required)
```
/auth/**        → registration, login, forgot-password, reset-password
/login/**       → OAuth2 login flows
/oauth2/**      → OAuth2 callbacks
/error          → error page
```

### Route Mapping
All frontend requests go to `http://localhost:8080` (Gateway). The Gateway routes to the correct service:

| Frontend calls | Gateway routes to | Downstream path |
|---|---|---|
| `POST /api/auth/register` | AUTH-SERVICE | `/auth/register` |
| `POST /api/auth/login` | AUTH-SERVICE | `/auth/login` |
| `GET /api/posts` | POST-SERVICE | `/posts` |
| `POST /api/comments` | COMMENT-SERVICE | `/comments` |
| `GET /api/categories` | CATEGORY-SERVICE | `/categories` |
| `POST /api/media/upload` | MEDIA-SERVICE | `/media/upload` |
| `GET /api/admin/users` | ADMIN-SERVER | `/admin/users` |

The `RewritePath` filter strips `/api/` prefix: `/api/auth/register` → `/auth/register`

---

## 5. Auth Service — Deep Dive

### 5.1 Database: `inkwell_auth`

#### `users` Table
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| username | VARCHAR(255) | UNIQUE, NOT NULL |
| email | VARCHAR(255) | UNIQUE, NOT NULL |
| password_hash | VARCHAR(255) | NOT NULL |
| full_name | VARCHAR(255) | NOT NULL |
| role | ENUM('READER','AUTHOR','ADMIN') | NOT NULL, default READER |
| bio | TEXT | nullable |
| avatar_url | VARCHAR(500) | nullable |
| provider | ENUM('LOCAL','GOOGLE','GITHUB') | NOT NULL |
| is_active | BOOLEAN | NOT NULL, default true |
| reset_token | VARCHAR(255) | nullable |
| reset_token_expiry | DATETIME | nullable |
| created_at | DATETIME | auto |
| updated_at | DATETIME | auto |

### 5.2 REST Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/auth/register` | Public | Register with email/password |
| POST | `/auth/login` | Public | Login, returns JWT |
| POST | `/auth/logout` | JWT | Invalidate token |
| POST | `/auth/refresh` | Public | Refresh JWT token |
| GET | `/auth/profile` | JWT | Get current user profile |
| PUT | `/auth/profile` | JWT | Update profile (name, bio, avatar) |
| PUT | `/auth/password` | JWT | Change password |
| GET | `/auth/search?username=` | JWT | Search users by username |
| DELETE | `/auth/deactivate` | JWT | Soft-delete account |
| POST | `/auth/forgot-password` | Public | Send reset token to email |
| POST | `/auth/reset-password` | Public | Reset password with token |

### 5.3 OAuth2 Flow (Google / GitHub)
```
1. Angular → GET http://localhost:8080/oauth2/authorization/google
2. Gateway → forwards to AUTH-SERVICE
3. Auth-Service → redirects browser to Google consent screen
4. Google → callback to http://localhost:8080/login/oauth2/code/google
5. Gateway → forwards callback to AUTH-SERVICE
6. Auth-Service → CustomOAuth2UserService creates/updates User in DB
7. Auth-Service → OAuth2SuccessHandler generates JWT
8. Auth-Service → redirects to http://localhost:4200/oauth2/redirect?token=<JWT>
9. Angular → extracts token from URL, stores in localStorage
```

### 5.4 Security Layers
1. **GatewayOnlyFilter** — Rejects any request NOT coming through the Gateway (checks `X-Internal-Secret` header)
2. **Spring Security** — Permits `/auth/**`, `/login/**`, `/oauth2/**`; requires auth for everything else
3. **Session Policy:** `IF_REQUIRED` (OAuth2 needs a temp session for state; JWT handles the rest)

### 5.5 JWT Structure
```json
{
  "sub": "user@email.com",
  "role": "READER",
  "iat": 1713600000,
  "exp": 1713686400
}
```
- Access token expiry: 24 hours (configurable via `JWT_EXPIRATION`)
- Refresh token expiry: 7 days
- Signing: HMAC-SHA256 with Base64-encoded secret

---

## 6. Admin Server

### 6.1 Database
Uses AUTH-SERVICE's database via REST calls (no direct DB access).

### 6.2 Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/admin/users` | ADMIN JWT | List all users |
| GET | `/admin/users/{id}` | ADMIN JWT | Get user details |
| PUT | `/admin/users/{id}/role` | ADMIN JWT | Change user role |
| PUT | `/admin/users/{id}/plan` | ADMIN JWT | Change subscription plan |
| PUT | `/admin/users/{id}/suspend` | ADMIN JWT | Suspend user |
| PUT | `/admin/users/{id}/reactivate` | ADMIN JWT | Reactivate user |
| DELETE | `/admin/users/{id}` | ADMIN JWT | Permanently delete user |
| GET | `/admin/posts` | ADMIN JWT | List all posts |
| PUT | `/admin/posts/{id}/feature` | ADMIN JWT | Pin post to top |
| DELETE | `/admin/posts/{id}` | ADMIN JWT | Delete any post |
| GET | `/admin/comments` | ADMIN JWT | List all comments |
| PUT | `/admin/comments/{id}/approve` | ADMIN JWT | Approve comment |
| PUT | `/admin/comments/{id}/reject` | ADMIN JWT | Reject comment |
| DELETE | `/admin/comments/{id}` | ADMIN JWT | Delete comment |
| GET | `/admin/analytics` | ADMIN JWT | Platform analytics |

### 6.3 Subscription Plans
| Plan | Price | Features |
|---|---|---|
| FREE | $0 | Read, comment, 1 post/month |
| STARTER | $5/mo | Unlimited posts, basic analytics |
| PRO | $15/mo | Custom domain, priority support, advanced analytics |
| ENTERPRISE | $49/mo | Team accounts, API access, white-label |

---

## 7. Post Service

### 7.1 Database: `inkwell_posts`

#### `posts` Table
| Column | Type | Description |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| author_id | BIGINT | User ID from auth-service |
| title | VARCHAR(500) | Post title |
| slug | VARCHAR(500) UNIQUE | URL-safe slug |
| content | LONGTEXT | HTML content (sanitized) |
| excerpt | TEXT | Short summary |
| featured_image_url | VARCHAR(500) | Cover image |
| status | ENUM('DRAFT','PUBLISHED','ARCHIVED') | Post status |
| is_featured | BOOLEAN | Pinned by admin |
| view_count | INT | View counter |
| like_count | INT | Like counter |
| comment_count | INT | Comment counter |
| published_at | DATETIME | Publish timestamp |
| created_at | DATETIME | Auto |
| updated_at | DATETIME | Auto |

#### `post_likes` Table
| Column | Type |
|---|---|
| id | BIGINT PK |
| post_id | BIGINT FK |
| user_id | BIGINT |
| created_at | DATETIME |

#### `post_categories` Table (join table)
| Column | Type |
|---|---|
| post_id | BIGINT FK |
| category_id | BIGINT |

#### `post_tags` Table (join table)
| Column | Type |
|---|---|
| post_id | BIGINT FK |
| tag_id | BIGINT |

### 7.2 Key Endpoints
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/posts` | Public | List published posts (paginated) |
| GET | `/posts/{slug}` | Public | Get post by slug (increments view) |
| POST | `/posts` | AUTHOR JWT | Create post |
| PUT | `/posts/{id}` | AUTHOR JWT | Update own post |
| DELETE | `/posts/{id}` | AUTHOR JWT | Delete own post |
| POST | `/posts/{id}/like` | JWT | Like/unlike toggle |
| PUT | `/posts/{id}/publish` | AUTHOR JWT | Publish draft |
| PUT | `/posts/{id}/unpublish` | AUTHOR JWT | Unpublish |
| GET | `/posts/author/{authorId}` | Public | Posts by author |
| GET | `/posts/feed` | Public | Homepage feed |

---

## 8. Comment Service

### 8.1 Database: `inkwell_comments`

#### `comments` Table
| Column | Type | Description |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| post_id | BIGINT | From post-service |
| user_id | BIGINT | Commenter |
| parent_id | BIGINT nullable | For threaded replies (2-level) |
| content | TEXT | Comment body |
| status | ENUM('PENDING','APPROVED','REJECTED') | Moderation |
| like_count | INT | Like counter |
| created_at | DATETIME | Auto |
| updated_at | DATETIME | Auto |

#### `comment_likes` Table
| Column | Type |
|---|---|
| id | BIGINT PK |
| comment_id | BIGINT FK |
| user_id | BIGINT |

### 8.2 Key Endpoints
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/comments/post/{postId}` | Public | Get approved comments for post |
| POST | `/comments` | JWT | Add comment |
| PUT | `/comments/{id}` | JWT | Edit own comment (time-limited) |
| DELETE | `/comments/{id}` | JWT | Delete own comment |
| POST | `/comments/{id}/like` | JWT | Like/unlike comment |
| PUT | `/comments/{id}/approve` | AUTHOR/ADMIN | Approve comment |
| PUT | `/comments/{id}/reject` | AUTHOR/ADMIN | Reject comment |

---

## 9. Category Service

### 9.1 Database: `inkwell_categories`

#### `categories` Table
| Column | Type | Description |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| name | VARCHAR(255) UNIQUE | Category name |
| slug | VARCHAR(255) UNIQUE | URL slug |
| description | TEXT | Description |
| parent_id | BIGINT nullable | Hierarchical parent |
| post_count | INT | Cached count |

#### `tags` Table
| Column | Type | Description |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| name | VARCHAR(100) UNIQUE | Tag name |
| slug | VARCHAR(100) UNIQUE | URL slug |
| usage_count | INT | Trending metric |

---

## 10. Media Service

### 10.1 Database: `inkwell_media`

#### `media` Table
| Column | Type | Description |
|---|---|---|
| id | BIGINT PK | Auto-increment |
| user_id | BIGINT | Uploader |
| file_name | VARCHAR(500) | Original filename |
| s3_key | VARCHAR(500) | S3 object key |
| url | VARCHAR(1000) | CloudFront URL |
| content_type | VARCHAR(100) | MIME type |
| size_bytes | BIGINT | File size |
| alt_text | VARCHAR(500) | Accessibility |
| created_at | DATETIME | Auto |

### 10.2 Constraints
- Max file size: 10MB
- Allowed types: JPEG, PNG, GIF, WebP, PDF
- Storage: AWS S3 + CloudFront CDN

---

## 11. Newsletter Service

### 11.1 Database: `inkwell_newsletter`

#### `subscribers` Table
| Column | Type |
|---|---|
| id | BIGINT PK |
| email | VARCHAR(255) UNIQUE |
| is_confirmed | BOOLEAN |
| confirmation_token | VARCHAR(255) |
| token_expiry | DATETIME |
| preferences | JSON |
| subscribed_at | DATETIME |
| unsubscribed_at | DATETIME nullable |

#### `campaigns` Table
| Column | Type |
|---|---|
| id | BIGINT PK |
| subject | VARCHAR(500) |
| content | LONGTEXT |
| sent_at | DATETIME |
| recipient_count | INT |

### 11.2 Flow
```
1. User subscribes → confirmation email sent (double opt-in)
2. User clicks confirmation link → is_confirmed = true
3. Admin creates campaign → RabbitMQ fans out emails
4. New post published → auto-alert to subscribers
```

---

## 12. Notification Service

### 12.1 Database: `inkwell_notifications`

#### `notifications` Table
| Column | Type |
|---|---|
| id | BIGINT PK |
| user_id | BIGINT |
| type | ENUM('COMMENT_REPLY','NEW_POST','MENTION','NEWSLETTER','SYSTEM') |
| title | VARCHAR(500) |
| message | TEXT |
| reference_id | BIGINT |
| reference_type | VARCHAR(50) |
| is_read | BOOLEAN |
| created_at | DATETIME |

### 12.2 Delivery
- **In-app:** REST polling or WebSocket
- **Email:** Via RabbitMQ → EmailService

---

## 13. Inter-Service Communication

```
┌──────────────┐
│   Angular    │ ── HTTP ──▶ ┌──────────────┐
│  :4200       │             │  API Gateway │
└──────────────┘             │  :8080       │
                             └──────┬───────┘
                                    │ (routes via Eureka lb://)
              ┌─────────────────────┼─────────────────────┐
              ▼                     ▼                     ▼
        ┌───────────┐     ┌──────────────┐     ┌──────────────┐
        │auth-service│     │ post-service │     │ comment-svc  │
        │  :8081     │     │   :8083      │     │   :8084      │
        └───────────┘     └──────────────┘     └──────────────┘
```

- Services NEVER call each other directly for user-facing requests
- For internal service-to-service calls (e.g., post-service needs user info), use `RestTemplate`/`WebClient` via Eureka service names
- All services validate `X-Internal-Secret` to reject direct access

---

## 14. Environment Variables (.env per service)

### Shared across services:
```properties
JWT_SECRET=<same-base64-key-everywhere>
INTERNAL_SECRET=<same-secret-everywhere>
EUREKA_URL=http://localhost:8761/eureka/
```

### Auth-service specific:
```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=inkwell_auth
DB_USERNAME=root
DB_PASSWORD=root123
GOOGLE_CLIENT_ID=<from-google-console>
GOOGLE_CLIENT_SECRET=<from-google-console>
GITHUB_CLIENT_ID=<from-github-settings>
GITHUB_CLIENT_SECRET=<from-github-settings>
MAIL_USERNAME=<smtp-email>
MAIL_PASSWORD=<smtp-app-password>
```

---

## 15. Technology Stack Summary

| Layer | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Cloud | Spring Cloud 2025.1.1 |
| Gateway | Spring Cloud Gateway (Reactive/WebFlux) |
| Security | Spring Security + JWT (jjwt 0.12.5) |
| OAuth2 | Spring Security OAuth2 Client |
| Database | MySQL 8+ per service |
| ORM | Spring Data JPA / Hibernate |
| Cache | Redis (future: post feed, rate limiting) |
| Messaging | RabbitMQ (future: notifications, newsletter) |
| File Storage | AWS S3 + CloudFront |
| Email | Spring JavaMailSender (Gmail SMTP / AWS SES) |
| Discovery | Netflix Eureka |
| Build | Maven |
| Containerization | Docker + Docker Compose |
