# Quash Sampler Backend - API Documentation

## Authentication Endpoints

### 1. Get Current User Info

**Endpoint:** `GET /auth/me`

**Description:** Returns the authenticated user's information.

**Authentication:** Required (Bearer token)

#### Success Response (200)

**Request:**
```bash
curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlF1YXNoIiwiaWF0IjoxNTE2MjM5MDIyfQ.pAxWIyKM0fMidy7lPfRH0DXOZvJgJR84zlA9pHJnEC0" \
  http://localhost:3000/auth/me
```

**Response:**
```json
{
  "success": true,
  "user": {
    "userId": "master_user",
    "identifier": "master@quash.io",
    "name": "Master User"
  }
}
```

#### Error Response - No Token (401)

**Request:**
```bash
curl http://localhost:3000/auth/me
```

**Response:**
```json
{
  "success": false,
  "message": "No authorization token provided",
  "code": "NO_TOKEN"
}
```

#### Error Response - Invalid Token Format (401)

**Request:**
```bash
curl -H "Authorization: InvalidTokenFormat" \
  http://localhost:3000/auth/me
```

**Response:**
```json
{
  "success": false,
  "message": "Invalid authorization format. Use: Bearer <token>",
  "code": "INVALID_AUTH_FORMAT"
}
```

#### Error Response - Invalid/Expired Token (401)

**Request:**
```bash
curl -H "Authorization: Bearer invalid_token_here" \
  http://localhost:3000/auth/me
```

**Response:**
```json
{
  "success": false,
  "message": "Invalid token",
  "code": "INVALID_TOKEN"
}
```

---

### 2. Logout (Revoke Token)

**Endpoint:** `POST /auth/logout`

**Description:** Revokes the current authentication token, logging the user out.

**Authentication:** Required (Bearer token)

#### Success Response (200)

**Request:**
```bash
curl -X POST \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlF1YXNoIiwiaWF0IjoxNTE2MjM5MDIyfQ.pAxWIyKM0fMidy7lPfRH0DXOZvJgJR84zlA9pHJnEC0" \
  http://localhost:3000/auth/logout
```

**Response:**
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

#### Error Response - No Token (401)

**Request:**
```bash
curl -X POST http://localhost:3000/auth/logout
```

**Response:**
```json
{
  "success": false,
  "message": "No authorization token provided",
  "code": "NO_TOKEN"
}
```

#### Error Response - Invalid Token Format (401)

**Request:**
```bash
curl -X POST \
  -H "Authorization: InvalidFormat" \
  http://localhost:3000/auth/logout
```

**Response:**
```json
{
  "success": false,
  "message": "Invalid authorization format. Use: Bearer <token>",
  "code": "INVALID_AUTH_FORMAT"
}
```

#### Error Response - Invalid/Expired Token (401)

**Request:**
```bash
curl -X POST \
  -H "Authorization: Bearer invalid_token_here" \
  http://localhost:3000/auth/logout
```

**Response:**
```json
{
  "success": false,
  "message": "Invalid token",
  "code": "INVALID_TOKEN"
}
```

---

### 3. Generate JWT Token from Email

**Endpoint:** `POST /auth/generate-token`

**Description:** Generates a JWT token directly from an email address without OTP verification. Useful for testing and development.

**Authentication:** Not required

#### Success Response (200)

**Request:**
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}' \
  http://localhost:3000/auth/generate-token
```

**Response:**
```json
{
  "success": true,
  "message": "Token generated successfully",
  "email": "test@example.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyXzE3NTk5MzU0MjI2MDAiLCJpZGVudGlmaWVyIjoidGVzdEBleGFtcGxlLmNvbSIsIm5hbWUiOiJ0ZXN0IiwiaWF0IjoxNzU5OTM1NDIyLCJleHAiOjE3NjAwMjE4MjJ9.yX8HICEqkv1hDsVeLYoK-5a4QyYNxOBTaSos9hopFNI"
}
```

#### Error Response - Missing Email (400)

**Request:**
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{}' \
  http://localhost:3000/auth/generate-token
```

**Response:**
```json
{
  "success": false,
  "message": "Email is required"
}
```

#### Error Response - Invalid Email Format (400)

**Request:**
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"email":"invalid-email"}' \
  http://localhost:3000/auth/generate-token
```

**Response:**
```json
{
  "success": false,
  "message": "Invalid email format"
}
```

---

### 4. Get JWT Token Claims

**Endpoint:** `GET /auth/token-claims`

**Description:** Decodes and returns the claims (payload and header) from the provided JWT token.

**Authentication:** Required (Bearer token)

#### Success Response (200)

**Request:**
```bash
curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyXzE3NTk5MzU0MjI2MDAiLCJpZGVudGlmaWVyIjoidGVzdEBleGFtcGxlLmNvbSIsIm5hbWUiOiJ0ZXN0IiwiaWF0IjoxNzU5OTM1NDIyLCJleHAiOjE3NjAwMjE4MjJ9.yX8HICEqkv1hDsVeLYoK-5a4QyYNxOBTaSos9hopFNI" \
  http://localhost:3000/auth/token-claims
```

**Response:**
```json
{
  "success": true,
  "claims": {
    "header": {
      "alg": "HS256",
      "typ": "JWT"
    },
    "payload": {
      "userId": "user_1759935422600",
      "identifier": "test@example.com",
      "name": "test",
      "iat": 1759935422,
      "exp": 1760021822
    }
  }
}
```

#### Error Response - No Token (401)

**Request:**
```bash
curl http://localhost:3000/auth/token-claims
```

**Response:**
```json
{
  "success": false,
  "message": "No authorization token provided",
  "code": "NO_TOKEN"
}
```

#### Error Response - Invalid Token Format (401)

**Request:**
```bash
curl -H "Authorization: Bearer invalid_token" \
  http://localhost:3000/auth/token-claims
```

**Response:**
```json
{
  "success": false,
  "message": "Invalid token",
  "code": "INVALID_TOKEN"
}
```

---

## Master Token

For testing purposes, a master token is available that never expires:

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlF1YXNoIiwiaWF0IjoxNTE2MjM5MDIyfQ.pAxWIyKM0fMidy7lPfRH0DXOZvJgJR84zlA9pHJnEC0
```

This token can be used for all authenticated endpoints without going through the OTP flow.

---

## Status Codes Summary

| Status Code | Description |
|-------------|-------------|
| 200 | Success |
| 400 | Bad Request (missing or invalid parameters) |
| 401 | Unauthorized (missing, invalid, or expired token) |
| 404 | Endpoint not found |
| 500 | Internal server error |

---

## HTTP Status Code Testing Endpoints

### 2xx Success Responses

#### POST /api/status/201 - Created

**Request:**
```bash
curl -X POST http://localhost:3000/api/status/201
```

**Response:**
```json
{
  "success": true,
  "message": "Resource created successfully",
  "id": "resource_1759936647266",
  "createdAt": "2025-10-08T15:17:27.266Z"
}
```

**Headers:** `Location: /api/resources/{id}`

---

#### PUT /api/status/204 - No Content

**Request:**
```bash
curl -X PUT http://localhost:3000/api/status/204
```

**Response:** Empty (no content)

**Status:** 204

---

#### GET /api/status/206 - Partial Content

**Request:**
```bash
curl http://localhost:3000/api/status/206
```

**Response:**
```
This is the full con
```

**Headers:** `Content-Range: bytes 0-20/76`

---

### 3xx Redirect Responses

#### GET /api/redirect/301 - Permanent Redirect

**Request:**
```bash
curl http://localhost:3000/api/redirect/301
```

**Response:**
```json
{
  "success": true,
  "message": "Resource has been moved permanently",
  "redirectTo": "/api/status/ok"
}
```

**Headers:** `Location: /api/status/ok`

**Status:** 301

---

#### GET /api/redirect/302 - Temporary Redirect

**Request:**
```bash
curl http://localhost:3000/api/redirect/302
```

**Response:**
```json
{
  "success": true,
  "message": "Resource temporarily redirected",
  "redirectTo": "/api/status/ok"
}
```

**Headers:** `Location: /api/status/ok`

**Status:** 302

---

#### GET /api/redirect/304 - Not Modified

**Request (without ETag):**
```bash
curl http://localhost:3000/api/redirect/304
```

**Response:**
```json
{
  "success": true,
  "message": "Resource content",
  "data": { "value": "sample data" }
}
```

**Request (with matching ETag):**
```bash
curl -H "If-None-Match: \"abc123\"" http://localhost:3000/api/redirect/304
```

**Response:** Empty (not modified)

**Status:** 304

---

### 4xx Client Error Responses

#### GET /api/status/400 - Bad Request

**Request:**
```bash
curl http://localhost:3000/api/status/400
```

**Response:**
```json
{
  "success": false,
  "message": "The request was malformed or invalid",
  "code": "BAD_REQUEST",
  "errors": [
    { "field": "email", "message": "Invalid email format" }
  ]
}
```

**Status:** 400

---

#### GET /api/status/401 - Unauthorized

**Request:**
```bash
curl http://localhost:3000/api/status/401
```

**Response:**
```json
{
  "success": false,
  "message": "Authentication required to access this resource",
  "code": "UNAUTHORIZED"
}
```

**Headers:** `WWW-Authenticate: Bearer realm="api"`

**Status:** 401

---

#### GET /api/status/403 - Forbidden

**Request:**
```bash
curl http://localhost:3000/api/status/403
```

**Response:**
```json
{
  "success": false,
  "message": "You do not have permission to access this resource",
  "code": "FORBIDDEN",
  "requiredRole": "admin"
}
```

**Status:** 403

---

#### GET /api/status/429 - Too Many Requests

**Request:**
```bash
curl http://localhost:3000/api/status/429
```

**Response:**
```json
{
  "success": false,
  "message": "Too many requests. Please try again later.",
  "code": "RATE_LIMIT_EXCEEDED",
  "retryAfter": 60
}
```

**Headers:**
- `Retry-After: 60`
- `X-RateLimit-Limit: 100`
- `X-RateLimit-Remaining: 0`
- `X-RateLimit-Reset: {timestamp}`

**Status:** 429

---

### 5xx Server Error Responses

#### GET /api/status/502 - Bad Gateway

**Request:**
```bash
curl http://localhost:3000/api/status/502
```

**Response:**
```json
{
  "success": false,
  "message": "The upstream server returned an invalid response",
  "code": "BAD_GATEWAY",
  "upstreamService": "payment-service"
}
```

**Status:** 502

---

#### GET /api/status/503 - Service Unavailable

**Request:**
```bash
curl http://localhost:3000/api/status/503
```

**Response:**
```json
{
  "success": false,
  "message": "Service is temporarily unavailable due to maintenance",
  "code": "SERVICE_UNAVAILABLE",
  "retryAfter": 120,
  "maintenanceWindow": {
    "start": "2025-10-08T15:17:45.838Z",
    "estimatedEnd": "2025-10-08T15:19:45.838Z"
  }
}
```

**Headers:** `Retry-After: 120`

**Status:** 503

---

#### GET /api/status/504 - Gateway Timeout

**Request:**
```bash
curl http://localhost:3000/api/status/504
```

**Response:**
```json
{
  "success": false,
  "message": "The upstream server failed to respond in time",
  "code": "GATEWAY_TIMEOUT",
  "upstreamService": "database",
  "timeout": "30s"
}
```

**Status:** 504

---

## Example Workflow

1. **Generate a token for testing:**
   ```bash
   curl -X POST -H "Content-Type: application/json" \
     -d '{"email":"test@example.com"}' \
     http://localhost:3000/auth/generate-token
   ```

2. **Use the token to access protected endpoints:**
   ```bash
   # Get user info
   curl -H "Authorization: Bearer <token>" \
     http://localhost:3000/auth/me

   # Get token claims
   curl -H "Authorization: Bearer <token>" \
     http://localhost:3000/auth/token-claims
   ```

3. **Logout when done:**
   ```bash
   curl -X POST -H "Authorization: Bearer <token>" \
     http://localhost:3000/auth/logout
   ```