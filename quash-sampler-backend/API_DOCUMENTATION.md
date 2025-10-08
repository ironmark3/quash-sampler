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
| 401 | Unauthorized (missing, invalid, or expired token) |
| 404 | Endpoint not found |
| 500 | Internal server error |