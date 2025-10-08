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

---

## Query Parameters & Filters

### GET /api/query/simple - Single Query Parameter

**Request:**
```bash
curl "http://localhost:3000/api/query/simple?key=testvalue"
```

**Response:**
```json
{
  "success": true,
  "params": { "key": "testvalue" },
  "count": 1,
  "receivedAt": "2025-10-08T15:27:20.021Z"
}
```

**Error (no params):**
```bash
curl "http://localhost:3000/api/query/simple"
```

**Response:**
```json
{
  "success": false,
  "message": "No query parameters provided",
  "code": "NO_PARAMS"
}
```

---

### GET /api/query/multiple - Multiple Query Parameters

**Request:**
```bash
curl "http://localhost:3000/api/query/multiple?name=John&age=30&city=NYC"
```

**Response:**
```json
{
  "success": true,
  "params": {
    "name": "John",
    "age": "30",
    "city": "NYC"
  },
  "count": 3,
  "keys": ["name", "age", "city"]
}
```

---

### GET /api/query/array - Array Query Parameters

**Request:**
```bash
curl "http://localhost:3000/api/query/array?tags[]=react&tags[]=nodejs&tags[]=express"
```

**Response:**
```json
{
  "success": true,
  "arrayParams": {
    "tags": ["react", "nodejs", "express"]
  },
  "scalarParams": {},
  "arrayCount": 1,
  "totalParams": 1
}
```

---

### GET /api/query/special-chars - Special Characters

**Request:**
```bash
curl "http://localhost:3000/api/query/special-chars?q=hello%20world%21"
```

**Response:**
```json
{
  "success": true,
  "decoded": "hello world!",
  "length": 12,
  "containsSpecialChars": true,
  "specialCharsReference": {
    "space": " ",
    "exclamation": "!",
    "hash": "#",
    "dollar": "$",
    "percent": "%",
    "ampersand": "&",
    "plus": "+"
  }
}
```

---

### GET /api/query/missing-required - Required Parameter Validation

**Request (without required param):**
```bash
curl "http://localhost:3000/api/query/missing-required"
```

**Response:**
```json
{
  "success": false,
  "message": "Missing required parameter: id",
  "code": "MISSING_PARAMETER",
  "requiredParams": ["id"]
}
```

**Request (with required param):**
```bash
curl "http://localhost:3000/api/query/missing-required?id=123"
```

**Response:**
```json
{
  "success": true,
  "message": "Required parameter provided",
  "id": "123"
}
```

---

## Headers Testing

### GET /api/headers/echo - Echo All Request Headers

**Request:**
```bash
curl "http://localhost:3000/api/headers/echo"
```

**Response:**
```json
{
  "success": true,
  "headers": {
    "host": "localhost:3000",
    "user-agent": "curl/8.9.1",
    "accept": "*/*"
  },
  "count": 3,
  "headerNames": ["host", "user-agent", "accept"]
}
```

---

### GET /api/headers/required - Require Specific Header

**Request (without header):**
```bash
curl "http://localhost:3000/api/headers/required"
```

**Response:**
```json
{
  "success": false,
  "message": "Missing required header: X-API-Key",
  "code": "MISSING_HEADER",
  "requiredHeaders": ["X-API-Key"]
}
```

**Request (with header):**
```bash
curl -H "X-API-Key: test123" "http://localhost:3000/api/headers/required"
```

**Response:**
```json
{
  "success": true,
  "message": "Required header provided",
  "apiKey": "test123"
}
```

---

### GET /api/headers/custom - Custom Response Headers

**Request:**
```bash
curl -i "http://localhost:3000/api/headers/custom"
```

**Response Headers:**
```
X-Custom-Header: Custom-Value
X-Request-Id: req_1759937280116_x6m9eo1rd
X-Rate-Limit: 100
X-Rate-Limit-Remaining: 99
X-Server-Version: 1.0.0
X-Response-Time: 42ms
```

**Response Body:**
```json
{
  "success": true,
  "message": "Custom headers set in response",
  "customHeaders": {
    "X-Custom-Header": "Custom-Value",
    "X-Request-Id": "req_1759937280116_x6m9eo1rd",
    "X-Rate-Limit": "100",
    "X-Rate-Limit-Remaining": "99",
    "X-Server-Version": "1.0.0",
    "X-Response-Time": "42ms"
  }
}
```

---

### GET /api/headers/case-sensitive - Header Case Sensitivity

**Request:**
```bash
curl -H "X-Test-Header: TestValue" "http://localhost:3000/api/headers/case-sensitive"
```

**Response:**
```json
{
  "success": true,
  "message": "Headers are case-insensitive in HTTP",
  "headerValue": "TestValue",
  "testedCases": ["x-test-header", "X-Test-Header", "X-TEST-HEADER"],
  "note": "All variations of the header name will return the same value"
}
```

---

### GET /api/headers/compression - Compression Support

**Request:**
```bash
curl -H "Accept-Encoding: gzip" --compressed "http://localhost:3000/api/headers/compression"
```

**Response:**
```json
{
  "success": true,
  "message": "This response can be compressed",
  "acceptEncoding": "gzip",
  "supportsCompression": true,
  "data": [
    /* 100 items */
  ]
}
```

---

## Timing & Performance

### GET /api/delay - Configurable Delay

**Request:**
```bash
curl "http://localhost:3000/api/delay?ms=500"
```

**Response:**
```json
{
  "success": true,
  "requestedDelay": 500,
  "actualDelay": 501,
  "message": "Responded after 501ms"
}
```

**Parameters:**
- `ms` - Delay in milliseconds (0-10000)

---

### GET /api/timeout - Timeout Testing

**Request:**
```bash
timeout 2 curl "http://localhost:3000/api/timeout"
```

**Behavior:** Never responds - connection hangs until client timeout

---

### GET /api/slow-stream - Chunked Streaming

**Request:**
```bash
curl "http://localhost:3000/api/slow-stream?chunks=3&delay=200"
```

**Response:**
```
Starting slow stream...

Chunk 1/3 - Timestamp: 2025-10-08T15:29:35.181Z
Chunk 2/3 - Timestamp: 2025-10-08T15:29:35.382Z
Chunk 3/3 - Timestamp: 2025-10-08T15:29:35.583Z

Stream completed!
```

**Parameters:**
- `chunks` - Number of chunks (default: 10)
- `delay` - Delay between chunks in ms (default: 500)

---

### GET /api/fast - Instant Response

**Request:**
```bash
curl "http://localhost:3000/api/fast"
```

**Response:**
```json
{
  "success": true,
  "message": "Fast response",
  "timestamp": "2025-10-08T15:29:26.628Z"
}
```

---

## Pagination

### GET /api/pagination/offset - Offset-Based Pagination

**Request:**
```bash
curl "http://localhost:3000/api/pagination/offset?page=2&limit=5"
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 6,
      "name": "Item 6",
      "description": "Description for item 6",
      "createdAt": "2025-07-05T15:27:44.534Z",
      "status": "completed"
    }
    /* 4 more items */
  ],
  "pagination": {
    "page": 2,
    "limit": 5,
    "totalItems": 100,
    "totalPages": 20,
    "hasNext": true,
    "hasPrevious": true,
    "nextPage": 3,
    "previousPage": 1
  }
}
```

**Parameters:**
- `page` - Page number (default: 1, min: 1)
- `limit` - Items per page (default: 10, max: 100)

---

### GET /api/pagination/cursor - Cursor-Based Pagination

**Request (first page):**
```bash
curl "http://localhost:3000/api/pagination/cursor?limit=5"
```

**Response:**
```json
{
  "success": true,
  "data": [
    /* 5 items */
  ],
  "pagination": {
    "cursor": null,
    "limit": 5,
    "nextCursor": 5,
    "hasMore": true,
    "itemsReturned": 5
  }
}
```

**Request (next page):**
```bash
curl "http://localhost:3000/api/pagination/cursor?cursor=5&limit=5"
```

**Response:**
```json
{
  "success": true,
  "data": [
    /* Next 5 items */
  ],
  "pagination": {
    "cursor": "5",
    "limit": 5,
    "nextCursor": 10,
    "hasMore": true,
    "itemsReturned": 5
  }
}
```

**Parameters:**
- `cursor` - Cursor value from previous response (optional)
- `limit` - Items per page (default: 10, max: 100)

---

### GET /api/pagination/link-header - RFC 5988 Link Headers

**Request:**
```bash
curl -i "http://localhost:3000/api/pagination/link-header?page=2&limit=5"
```

**Response Headers:**
```
Link: <http://localhost:3000/pagination/link-header?page=1&limit=5>; rel="first",
      <http://localhost:3000/pagination/link-header?page=1&limit=5>; rel="prev",
      <http://localhost:3000/pagination/link-header?page=3&limit=5>; rel="next",
      <http://localhost:3000/pagination/link-header?page=20&limit=5>; rel="last"
```

**Response Body:**
```json
{
  "success": true,
  "data": [
    /* 5 items */
  ],
  "page": 2,
  "totalPages": 20,
  "totalItems": 100,
  "itemsPerPage": 5
}
```

**Parameters:**
- `page` - Page number (default: 1)
- `limit` - Items per page (default: 10)

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