# InkWell — Implementation Status & API Documentation

This document tracks the implementation of the InkWell blogging platform against the core requirements.

## 1. System Architecture
- **Eureka Server**: Port 8761 (Service Discovery)
- **API Gateway**: Port 8080 (Security, CORS, Routing, Swagger UI)
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) (Aggregated docs)
- **Web MVC**: Port 9000 (Thymeleaf Frontend)

---

## 2. Service Implementation Status

| Service | Port | Status | Key Features Implemented |
|---|---|---|---|
| **auth-service** | 8081 | ✅ Full | JWT, Roles, OAuth2, Profile, Admin User Mgmt |
| **post-service** | 8083 | ✅ Full | Slug Gen, Read-time, Atomic Views/Likes, Draft/Publish |
| **comment-service** | 8084 | ✅ Full | 2-level Threading, Soft-delete, Moderation |
| **category-service** | 8085 | ✅ Full | Hierarchical Categories, Tag Usage Tracking |
| **media-service** | 8086 | ✅ Full | Mock S3 Upload, Metadata, Post Linking, Soft-delete |
| **newsletter-service**| 8087 | ✅ Full | Double Opt-in, Post Notifications, Unsubscribe |
| **notification-service**| 8088 | ✅ Full | In-app Alerts, Read/Unread State, Badge Count |
| **admin-server** | 8082 | ✅ Partial | Orchestration for Users, Posts, Comments, Categories |

---

## 3. Detailed API Catalog (via Gateway :8080)

### Auth Service (`/auth`)
- `POST /auth/register`: Register new user
- `POST /auth/login`: Authentication (returns JWT)
- `GET /auth/profile`: Get current user info
- `PUT /auth/admin/users/{id}/role`: (Admin) Update user role
- `DELETE /auth/admin/users/{id}`: (Admin) Delete user

### Post Service (`/posts`)
- `GET /posts`: List all posts
- `GET /posts/slug/{slug}`: Get post by SEO slug
- `POST /posts`: Create post (auto-slug, auto-readtime)
- `POST /posts/{id}/like`: Atomic like increment
- `PUT /posts/admin/{id}/publish`: (Admin/Author) Publish post

### Comment Service (`/comments`)
- `POST /comments`: Add comment (supports `parentId`)
- `GET /comments/post/{postId}`: Get threaded comments
- `DELETE /comments/{id}`: Soft-delete (replaces content with placeholder)

---

## 4. Pending / Future Implementations

| Feature | Status | Reason / Requirement |
|---|---|---|
| **RabbitMQ** | ❌ Pending | Currently using sync REST calls. Needs message broker for async Newsletter/Notifications. |
| **Redis** | ❌ Pending | Needed for caching post feeds to hit the < 1.5s performance target. |
| **Analytics** | ❌ Pending | Admin dashboard charts and engagement metrics. |
| **Audit Logs** | ❌ Pending | Persistence of admin actions for security audit. |
| **HTTPS** | ❌ Pending | Local development currently uses HTTP. |

---

## 5. Development Tools
- **Unit Testing**: Implemented for `auth-service` and `admin-server` using JUnit 5 and Mockito.
- **Service Discovery**: All services register with Eureka and communicate via LoadBalanced RestTemplate.
