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