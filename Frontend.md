# InkWell — Frontend Architecture & Integration Guide

## 1. Overview

**Framework:** Angular 17+ (standalone components)  
**Styling:** SCSS + Bootstrap 5 or Angular Material  
**Rich Text Editor:** TipTap (ProseMirror-based) or Quill.js  
**Code Highlighting:** Prism.js  
**State Management:** Angular Signals / NgRx (optional)  
**HTTP:** Angular HttpClient with interceptors  
**API Base URL:** `http://localhost:8080/api` (all requests go through Gateway)

---

## 2. Project Structure

```
src/
├── app/
│   ├── core/                          # Singleton services & guards
│   │   ├── interceptors/
│   │   │   └── auth.interceptor.ts    # Attaches JWT to requests
│   │   ├── guards/
│   │   │   ├── auth.guard.ts          # Protects authenticated routes
│   │   │   ├── guest.guard.ts         # Redirects logged-in users away from login
│   │   │   ├── author.guard.ts        # Author-only routes
│   │   │   └── admin.guard.ts         # Admin-only routes
│   │   ├── services/
│   │   │   ├── auth.service.ts        # Login, register, OAuth, token management
│   │   │   ├── user.service.ts        # Profile, avatar, password
│   │   │   ├── post.service.ts        # CRUD posts
│   │   │   ├── comment.service.ts     # Comments
│   │   │   ├── category.service.ts    # Categories & tags
│   │   │   ├── media.service.ts       # File uploads
│   │   │   ├── newsletter.service.ts  # Subscribe/unsubscribe
│   │   │   ├── notification.service.ts# In-app notifications
│   │   │   └── admin.service.ts       # Admin operations
│   │   └── models/
│   │       ├── user.model.ts
│   │       ├── post.model.ts
│   │       ├── comment.model.ts
│   │       └── ...
│   │
│   ├── features/
│   │   ├── auth/                      # Auth feature module
│   │   │   ├── login/
│   │   │   ├── register/
│   │   │   ├── forgot-password/
│   │   │   ├── reset-password/
│   │   │   └── oauth-redirect/        # Handles /oauth2/redirect?token=...
│   │   │
│   │   ├── home/                      # Public homepage with feed
│   │   ├── post/
│   │   │   ├── post-list/             # Browse/search posts
│   │   │   ├── post-detail/           # Read single post (/blog/:slug)
│   │   │   ├── post-editor/           # Create/edit post (Author only)
│   │   │   └── post-drafts/           # My drafts (Author only)
│   │   │
│   │   ├── profile/
│   │   │   ├── my-profile/            # View/edit own profile
│   │   │   └── public-profile/        # View another user's profile
│   │   │
│   │   ├── comments/                  # Comment components (embedded in post-detail)
│   │   │
│   │   ├── notifications/             # Notification center
│   │   │
│   │   ├── admin/                     # Admin panel
│   │   │   ├── dashboard/
│   │   │   ├── user-management/
│   │   │   ├── post-management/
│   │   │   ├── comment-moderation/
│   │   │   ├── category-management/
│   │   │   └── analytics/
│   │   │
│   │   └── newsletter/                # Subscribe page
│   │
│   ├── shared/                        # Reusable UI components
│   │   ├── components/
│   │   │   ├── navbar/
│   │   │   ├── footer/
│   │   │   ├── post-card/
│   │   │   ├── comment-thread/
│   │   │   ├── avatar/
│   │   │   ├── loading-spinner/
│   │   │   └── pagination/
│   │   ├── pipes/
│   │   │   ├── time-ago.pipe.ts
│   │   │   └── truncate.pipe.ts
│   │   └── directives/
│   │
│   ├── app.routes.ts
│   ├── app.component.ts
│   └── app.config.ts
│
├── environments/
│   ├── environment.ts                  # apiUrl: 'http://localhost:8080/api'
│   └── environment.prod.ts
```

---

## 3. Routing Table

```typescript
// app.routes.ts
export const routes: Routes = [
  // ── Public ─────────────────────────────────────
  { path: '', component: HomeComponent },
  { path: 'blog/:slug', component: PostDetailComponent },
  { path: 'categories', component: CategoryListComponent },
  { path: 'categories/:slug', component: CategoryPostsComponent },
  { path: 'tags/:slug', component: TagPostsComponent },
  { path: 'author/:username', component: PublicProfileComponent },

  // ── Auth (Guest only) ──────────────────────────
  { path: 'login', component: LoginComponent, canActivate: [guestGuard] },
  { path: 'register', component: RegisterComponent, canActivate: [guestGuard] },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'oauth2/redirect', component: OAuthRedirectComponent },

  // ── Authenticated ──────────────────────────────
  { path: 'profile', component: MyProfileComponent, canActivate: [authGuard] },
  { path: 'notifications', component: NotificationsComponent, canActivate: [authGuard] },

  // ── Author ─────────────────────────────────────
  { path: 'write', component: PostEditorComponent, canActivate: [authorGuard] },
  { path: 'edit/:id', component: PostEditorComponent, canActivate: [authorGuard] },
  { path: 'my-posts', component: MyPostsComponent, canActivate: [authorGuard] },
  { path: 'drafts', component: PostDraftsComponent, canActivate: [authorGuard] },

  // ── Admin ──────────────────────────────────────
  { path: 'admin', canActivate: [adminGuard], children: [
    { path: '', component: AdminDashboardComponent },
    { path: 'users', component: UserManagementComponent },
    { path: 'posts', component: PostManagementComponent },
    { path: 'comments', component: CommentModerationComponent },
    { path: 'categories', component: CategoryManagementComponent },
    { path: 'analytics', component: AnalyticsComponent },
  ]},

  { path: '**', component: NotFoundComponent },
];
```

---

## 4. API Integration

### 4.1 Environment Config
```typescript
// environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  gatewayUrl: 'http://localhost:8080',
};
```

### 4.2 Auth Interceptor
```typescript
// core/interceptors/auth.interceptor.ts
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('inkwell_token');
  if (token) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }
  return next(req);
};
```

### 4.3 Auth Service — API Mapping

```typescript
// core/services/auth.service.ts
@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = environment.apiUrl;

  // POST /api/auth/register → Gateway → auth-service /auth/register
  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/register`, data);
  }

  // POST /api/auth/login → Gateway → auth-service /auth/login
  login(data: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/login`, data);
  }

  // OAuth: redirect browser to Gateway (NOT api url)
  loginWithGoogle(): void {
    window.location.href = `${environment.gatewayUrl}/oauth2/authorization/google`;
  }

  loginWithGitHub(): void {
    window.location.href = `${environment.gatewayUrl}/oauth2/authorization/github`;
  }

  // POST /api/auth/logout
  logout(): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/logout`, {});
  }

  // GET /api/auth/profile
  getProfile(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/auth/profile`);
  }

  // PUT /api/auth/profile
  updateProfile(data: ProfileUpdateRequest): Observable<any> {
    return this.http.put(`${this.apiUrl}/auth/profile`, data);
  }

  // PUT /api/auth/password
  changePassword(data: ChangePasswordRequest): Observable<any> {
    return this.http.put(`${this.apiUrl}/auth/password`, data);
  }

  // POST /api/auth/forgot-password
  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/forgot-password`, { email });
  }

  // POST /api/auth/reset-password
  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/reset-password`, { token, newPassword });
  }
}
```

### 4.4 Complete API → Service Mapping

| Angular Service | Method | HTTP Call | Gateway Routes To |
|---|---|---|---|
| `auth.service` | `register()` | `POST /api/auth/register` | AUTH → `/auth/register` |
| `auth.service` | `login()` | `POST /api/auth/login` | AUTH → `/auth/login` |
| `auth.service` | `loginWithGoogle()` | Browser redirect to `/oauth2/authorization/google` | AUTH → OAuth flow |
| `auth.service` | `getProfile()` | `GET /api/auth/profile` | AUTH → `/auth/profile` |
| `post.service` | `getFeed()` | `GET /api/posts/feed` | POST → `/posts/feed` |
| `post.service` | `getBySlug()` | `GET /api/posts/:slug` | POST → `/posts/:slug` |
| `post.service` | `create()` | `POST /api/posts` | POST → `/posts` |
| `post.service` | `like()` | `POST /api/posts/:id/like` | POST → `/posts/:id/like` |
| `comment.service` | `getForPost()` | `GET /api/comments/post/:id` | COMMENT → `/comments/post/:id` |
| `comment.service` | `create()` | `POST /api/comments` | COMMENT → `/comments` |
| `category.service` | `getAll()` | `GET /api/categories` | CATEGORY → `/categories` |
| `media.service` | `upload()` | `POST /api/media/upload` | MEDIA → `/media/upload` |
| `newsletter.service` | `subscribe()` | `POST /api/newsletter/subscribe` | NEWSLETTER → `/newsletter/subscribe` |
| `notification.service` | `getAll()` | `GET /api/notifications` | NOTIFICATION → `/notifications` |
| `admin.service` | `getUsers()` | `GET /api/admin/users` | ADMIN → `/admin/users` |

---

## 5. OAuth2 Redirect Component

This is the **most critical component** for OAuth to work:

```typescript
// features/auth/oauth-redirect/oauth-redirect.component.ts
@Component({
  selector: 'app-oauth-redirect',
  template: '<div class="loading">Signing you in...</div>',
  standalone: true,
})
export class OAuthRedirectComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);

  ngOnInit() {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (token) {
      localStorage.setItem('inkwell_token', token);
      // Fetch user profile to get role
      this.authService.getProfile().subscribe({
        next: (user) => {
          localStorage.setItem('inkwell_user', JSON.stringify(user));
          // Redirect based on role
          if (user.role === 'ADMIN') {
            this.router.navigate(['/admin']);
          } else if (user.role === 'AUTHOR') {
            this.router.navigate(['/my-posts']);
          } else {
            this.router.navigate(['/']);  // Reader → homepage
          }
        },
        error: () => this.router.navigate(['/login'])
      });
    } else {
      this.router.navigate(['/login']);
    }
  }
}
```

---

## 6. Post-Login Redirects

| Role | Redirects To | Sees in Navbar |
|---|---|---|
| READER | `/` (homepage) | Home, Feed, Profile, Notifications |
| AUTHOR | `/my-posts` | Home, Feed, Write, My Posts, Drafts, Profile, Notifications |
| ADMIN | `/admin` | Home, Feed, Write, Admin Panel, Profile, Notifications |

---

## 7. Navbar Logic

```typescript
// shared/components/navbar/navbar.component.ts
// Show different items based on auth state and role:

interface NavItem { label: string; route: string; roles?: string[]; }

const NAV_ITEMS: NavItem[] = [
  { label: 'Home',          route: '/' },
  { label: 'Feed',          route: '/feed' },
  { label: 'Write',         route: '/write',      roles: ['AUTHOR', 'ADMIN'] },
  { label: 'My Posts',      route: '/my-posts',   roles: ['AUTHOR', 'ADMIN'] },
  { label: 'Admin Panel',   route: '/admin',      roles: ['ADMIN'] },
];

// Guest: Show Login + Register buttons
// Authenticated: Show avatar dropdown with Profile, Notifications, Logout
```

---

## 8. Key Data Models

```typescript
// core/models/user.model.ts
export interface User {
  userId: number;
  username: string;
  email: string;
  fullName: string;
  role: 'READER' | 'AUTHOR' | 'ADMIN';
  bio?: string;
  avatarUrl?: string;
  provider: 'LOCAL' | 'GOOGLE' | 'GITHUB';
  isActive: boolean;
  createdAt: string;
}

// core/models/auth.model.ts
export interface LoginRequest { email: string; password: string; }
export interface RegisterRequest { username: string; fullName: string; email: string; password: string; }
export interface AuthResponse { token: string; }

// core/models/post.model.ts
export interface Post {
  id: number;
  authorId: number;
  authorName?: string;
  title: string;
  slug: string;
  content: string;
  excerpt: string;
  featuredImageUrl?: string;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  isFeatured: boolean;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  categories: Category[];
  tags: Tag[];
  publishedAt?: string;
  createdAt: string;
}

// core/models/comment.model.ts
export interface Comment {
  id: number;
  postId: number;
  userId: number;
  username: string;
  avatarUrl?: string;
  parentId?: number;
  content: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  likeCount: number;
  replies?: Comment[];
  createdAt: string;
}
```

---

## 9. Login & Register Flows

### 9.1 Email Login
```
1. User enters email + password on /login
2. Angular calls POST /api/auth/login { email, password }
3. Backend returns { token: "<JWT>" }
4. Angular stores token in localStorage
5. Angular calls GET /api/auth/profile (with JWT in header)
6. Angular stores user object, redirects based on role
```

### 9.2 Email Register
```
1. User fills form on /register (username, fullName, email, password)
2. Angular calls POST /api/auth/register { username, fullName, email, password }
3. Backend creates user, sends welcome email, returns { token: "<JWT>" }
4. Angular stores token + fetches profile → redirects to /
```

### 9.3 Google/GitHub OAuth
```
1. User clicks "Continue with Google" button
2. Angular does: window.location.href = 'http://localhost:8080/oauth2/authorization/google'
3. Browser goes to Gateway → Auth-Service → Google consent screen
4. After consent, Google redirects back to Gateway callback URL
5. Auth-Service processes callback → creates/updates user → generates JWT
6. Auth-Service redirects to: http://localhost:4200/oauth2/redirect?token=<JWT>
7. OAuthRedirectComponent extracts token → stores → fetches profile → redirects
```

---

## 10. Error Handling

```typescript
// In each service call:
this.authService.login(credentials).subscribe({
  next: (res) => { /* store token, redirect */ },
  error: (err) => {
    if (err.status === 401) this.errorMsg = 'Invalid email or password';
    else if (err.status === 409) this.errorMsg = 'Email already registered';
    else if (err.status === 400) this.errorMsg = err.error?.password || 'Validation failed';
    else this.errorMsg = 'Something went wrong. Please try again.';
  }
});
```

---

## 11. Token Management

```typescript
// Store
localStorage.setItem('inkwell_token', token);

// Read (in interceptor)
const token = localStorage.getItem('inkwell_token');

// Logout
localStorage.removeItem('inkwell_token');
localStorage.removeItem('inkwell_user');

// Check expiry (decode JWT payload)
isTokenExpired(): boolean {
  const token = localStorage.getItem('inkwell_token');
  if (!token) return true;
  const payload = JSON.parse(atob(token.split('.')[1]));
  return payload.exp * 1000 < Date.now();
}
```

---

## 12. Critical Rules to Avoid Conflicts

1. **ALL HTTP requests** go to `http://localhost:8080/api/...` — NEVER call microservices directly
2. **OAuth redirects** go to `http://localhost:8080/oauth2/authorization/...` (Gateway URL, NOT api URL)
3. **CORS is handled ONLY at the Gateway** — do NOT add CORS config to individual services
4. **JWT is stored in localStorage** under key `inkwell_token`
5. **Auth interceptor** automatically attaches Bearer token to every request
6. **Route guards** check both token existence and role from stored user object
7. **Each feature** is independent — adding post-service doesn't affect auth components
8. **Use the `/api/` prefix** for all REST calls — the Gateway strips it via RewritePath
