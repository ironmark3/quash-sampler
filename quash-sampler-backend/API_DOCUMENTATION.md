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

---

## 11. Edge Cases & Special Scenarios

### 1. Random Response

**Endpoint:** `GET /api/random/response`

**Description:** Returns a random HTTP status code (200, 201, 400, 404, 500, 503) with each request.

**Request:**
```bash
curl http://localhost:3000/api/random/response
```

**Response (varies):**
```json
{
  "success": true,
  "message": "Randomly generated 200 response",
  "statusCode": 200,
  "timestamp": "2025-10-08T15:41:22.771Z",
  "note": "Each request returns a different random status code"
}
```

**Headers:**
- `X-Random-Status`: The randomly selected status code

---

### 2. Random Data

**Endpoint:** `GET /api/random/data`

**Description:** Generates random data structures (object, array, or nested) with random content.

**Request:**
```bash
curl http://localhost:3000/api/random/data
```

**Response (varies):**
```json
{
  "success": true,
  "dataType": "object",
  "itemCount": 1,
  "data": {
    "level1": {
      "data": "ugplinzre",
      "level2": {
        "data": "rtq2lwkki",
        "level3": {
          "value": 747
        }
      }
    }
  },
  "generatedAt": "2025-10-08T15:41:26.344Z"
}
```

---

### 3. Flaky Endpoint

**Endpoint:** `GET /api/flaky?failure_rate=0.3`

**Description:** Simulates intermittent failures based on the specified failure rate (0.0-1.0).

**Query Parameters:**
- `failure_rate` (optional): Probability of failure (default: 0.3, range: 0.0-1.0)

**Success Response (200):**
```bash
curl "http://localhost:3000/api/flaky?failure_rate=0.5"
```

```json
{
  "success": true,
  "message": "Request succeeded this time",
  "failureRate": 0.5,
  "timestamp": "2025-10-08T15:41:29.048Z"
}
```

**Failure Response (500):**
```json
{
  "success": false,
  "message": "Simulated intermittent failure",
  "code": "FLAKY_ERROR",
  "failureRate": 0.5,
  "timestamp": "2025-10-08T15:41:29.048Z"
}
```

**Headers:**
- `X-Flaky-Result`: success or failure
- `X-Failure-Rate`: Configured failure rate

---

### 4. Large Response

**Endpoint:** `GET /api/large-response?size=1mb`

**Description:** Generates a response of the specified size for testing large payloads.

**Query Parameters:**
- `size` (optional): Response size (examples: 1kb, 10kb, 100kb, 1mb, 10mb) (default: 1kb)

**Request:**
```bash
curl "http://localhost:3000/api/large-response?size=5kb"
```

**Response:**
```json
{
  "success": true,
  "message": "Large response generated",
  "requestedSize": "5kb",
  "targetBytes": 5120,
  "itemsGenerated": 20,
  "items": [
    {
      "id": 0,
      "name": "Item 1",
      "description": "This is a sample item with some text to fill up space. Item number 1.",
      "timestamp": "2025-10-08T15:41:32.123Z",
      "data": "xxxxxxxxxxxx..."
    }
  ]
}
```

**Headers:**
- `X-Generated-Size`: Actual byte count of response
- `Content-Length`: Size of response

---

### 5. Deeply Nested JSON

**Endpoint:** `GET /api/nested-deep?depth=10`

**Description:** Generates deeply nested JSON structure (10+ levels) for testing JSON parsing.

**Query Parameters:**
- `depth` (optional): Nesting depth (default: 10, max: 50)

**Request:**
```bash
curl "http://localhost:3000/api/nested-deep?depth=15"
```

**Response:**
```json
{
  "success": true,
  "message": "Generated deeply nested JSON with 15 levels",
  "depth": 15,
  "nested": {
    "level": 1,
    "message": "Level 1 of 15",
    "data": {
      "level": 2,
      "message": "Level 2 of 15",
      "data": {
        "level": 3,
        "message": "Level 3 of 15",
        "data": "..."
      }
    }
  }
}
```

**Headers:**
- `X-Nesting-Depth`: Configured depth level

---

### 6. Empty Array

**Endpoint:** `GET /api/empty-array`

**Description:** Returns an empty array `[]` for testing empty response handling.

**Request:**
```bash
curl http://localhost:3000/api/empty-array
```

**Response:**
```json
[]
```

**Headers:**
- `X-Result-Count`: 0

---

### 7. Null Response

**Endpoint:** `GET /api/null-response`

**Description:** Returns `null` as a valid JSON response for testing null handling.

**Request:**
```bash
curl http://localhost:3000/api/null-response
```

**Response:**
```json
null
```

**Headers:**
- `X-Response-Type`: null

---

### 8. Unicode Data

**Endpoint:** `GET /api/unicode`

**Description:** Returns data with Unicode characters, emojis, and international text.

**Request:**
```bash
curl http://localhost:3000/api/unicode
```

**Response:**
```json
{
  "success": true,
  "message": "Unicode and emoji data 🚀",
  "emojis": {
    "celebration": "🎉",
    "rocket": "🚀",
    "light": "💡",
    "lightning": "⚡",
    "star": "🌟",
    "heart": "❤️",
    "check": "✅",
    "fire": "🔥"
  },
  "international": {
    "chinese": "你好世界",
    "arabic": "مرحبا بالعالم",
    "hindi": "नमस्ते दुनिया",
    "japanese": "こんにちは世界",
    "korean": "안녕하세요 세계",
    "russian": "Привет мир"
  },
  "mathematical": {
    "summation": "∑",
    "integral": "∫",
    "squareRoot": "√",
    "pi": "π",
    "infinity": "∞",
    "delta": "Δ",
    "lambda": "λ"
  },
  "symbols": {
    "copyright": "©",
    "registered": "®",
    "trademark": "™",
    "currency": "€£¥₹",
    "arrows": "←→↑↓"
  }
}
```

---

### 9. Special Characters

**Endpoint:** `GET /api/special-chars`

**Description:** Returns data with special characters (quotes, backslashes, newlines, HTML entities).

**Request:**
```bash
curl http://localhost:3000/api/special-chars
```

**Response:**
```json
{
  "success": true,
  "message": "Response with special characters",
  "examples": {
    "quotes": {
      "single": "It's a beautiful day",
      "double": "She said \"Hello\"",
      "backtick": "Template `literal` example"
    },
    "backslashes": {
      "path": "C:\\Users\\Documents\\file.txt",
      "regex": "\\d+\\s+\\w+"
    },
    "newlines": {
      "multiline": "Line 1\nLine 2\nLine 3",
      "withTabs": "Column1\tColumn2\tColumn3"
    },
    "html": {
      "escaped": "&lt;div&gt;Content&lt;/div&gt;",
      "entities": "&amp; &quot; &apos; &lt; &gt;"
    },
    "control": {
      "carriageReturn": "Text with\rcarriage return",
      "formFeed": "Text with\fform feed",
      "verticalTab": "Text with\vvertical tab"
    },
    "special": {
      "mixed": "Mix'd \"special\" chars: <tag> & symbol's!"
    }
  }
}
```

---

## 12. CORS Testing

### 1. Simple CORS

**Endpoint:** `OPTIONS /api/cors/simple` or `GET /api/cors/simple`

**Description:** Basic CORS configuration with wildcard origin.

**OPTIONS Request (Preflight):**
```bash
curl -X OPTIONS http://localhost:3000/api/cors/simple -I
```

**Response Headers:**
```
HTTP/1.1 204 No Content
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, OPTIONS
Access-Control-Allow-Headers: Content-Type
```

**GET Request:**
```bash
curl http://localhost:3000/api/cors/simple
```

**Response:**
```json
{
  "success": true,
  "message": "Simple CORS enabled",
  "allowedOrigins": "*",
  "allowedMethods": ["GET", "POST", "OPTIONS"]
}
```

---

### 2. CORS with Credentials

**Endpoint:** `OPTIONS /api/cors/credentials` or `GET /api/cors/credentials`

**Description:** CORS configuration that allows credentials (cookies, authentication).

**OPTIONS Request (Preflight):**
```bash
curl -X OPTIONS -H "Origin: http://example.com" \
  http://localhost:3000/api/cors/credentials -I
```

**Response Headers:**
```
HTTP/1.1 204 No Content
Access-Control-Allow-Origin: http://example.com
Access-Control-Allow-Credentials: true
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: Content-Type, Authorization
Access-Control-Max-Age: 86400
```

**GET Request:**
```bash
curl -H "Origin: http://example.com" \
  http://localhost:3000/api/cors/credentials
```

**Response:**
```json
{
  "success": true,
  "message": "CORS with credentials enabled",
  "origin": "http://example.com",
  "credentialsAllowed": true,
  "note": "This endpoint allows cookies and authentication headers"
}
```

---

### 3. CORS with Custom Headers

**Endpoint:** `OPTIONS /api/cors/custom-headers` or `GET /api/cors/custom-headers`

**Description:** CORS configuration with custom allowed and exposed headers.

**OPTIONS Request (Preflight):**
```bash
curl -X OPTIONS http://localhost:3000/api/cors/custom-headers -I
```

**Response Headers:**
```
HTTP/1.1 204 No Content
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, PATCH, DELETE, OPTIONS
Access-Control-Allow-Headers: X-Custom-Header, X-Api-Key, X-Request-ID, Content-Type
Access-Control-Expose-Headers: X-Total-Count, X-Page-Number
Access-Control-Max-Age: 3600
```

**GET Request:**
```bash
curl http://localhost:3000/api/cors/custom-headers -I | grep -i "x-total"
```

**Response:**
```json
{
  "success": true,
  "message": "CORS with custom headers enabled",
  "allowedHeaders": ["X-Custom-Header", "X-Api-Key", "X-Request-ID", "Content-Type"],
  "exposedHeaders": ["X-Total-Count", "X-Page-Number"],
  "data": {
    "totalCount": 100,
    "pageNumber": 1
  }
}
```

**Response Headers:**
```
X-Total-Count: 100
X-Page-Number: 1
Access-Control-Expose-Headers: X-Total-Count, X-Page-Number
```