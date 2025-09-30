# Quash Sampler Backend

Simple OTP authentication backend for the Quash Sampler Android app.

## Features

- Generate and send OTP
- Verify OTP with session management
- In-memory storage (no database required)
- CORS enabled for mobile apps
- 5-minute OTP expiry
- 3 attempts limit per session

## Setup

### Prerequisites

- Node.js (v14 or higher)
- npm or yarn

### Installation

```bash
cd quash-sampler-backend
npm install
```

### Run the Server

```bash
npm start
```

Or with auto-reload during development:

```bash
npm run dev
```

Server will start on `http://localhost:3000`

## Expose with ngrok

To use with the Android app, expose the server using ngrok:

```bash
# Install ngrok if you haven't
# https://ngrok.com/download

# Run ngrok
ngrok http 3000
```

Copy the generated HTTPS URL (e.g., `https://abc123.ngrok.io`) and use it in your Android app.

## API Endpoints

### 1. Request OTP (Login)

**POST** `/auth/login`

Request:
```json
{
  "identifier": "test@example.com"
}
```

Response:
```json
{
  "success": true,
  "message": "OTP sent successfully",
  "sessionId": "session_1234567890_abc123",
  "_debug_otp": "123456"
}
```

**Note:** The OTP is also printed in the server console. Remove `_debug_otp` from response in production.

### 2. Verify OTP

**POST** `/auth/verify-otp`

Request:
```json
{
  "sessionId": "session_1234567890_abc123",
  "otp": "123456"
}
```

Response (Success):
```json
{
  "success": true,
  "message": "OTP verified successfully",
  "token": "token_1234567890_xyz789",
  "user": {
    "id": "user_1234567890",
    "name": "Test User",
    "email": "test@example.com",
    "phone": null
  }
}
```

Response (Failure):
```json
{
  "success": false,
  "message": "Invalid OTP"
}
```

### 3. Health Check

**GET** `/health`

Response:
```json
{
  "status": "ok",
  "timestamp": "2025-09-30T10:30:00.000Z"
}
```

## Testing with curl

```bash
# Request OTP
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"test@example.com"}'

# Verify OTP
curl -X POST http://localhost:3000/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"sessionId":"SESSION_ID_HERE","otp":"123456"}'
```

## Connect Android App

1. Start the backend server
2. Run ngrok: `ngrok http 3000`
3. Copy the ngrok URL (e.g., `https://abc123.ngrok.io/`)
4. Update Android app base URL:
   - Edit `NetworkModule.kt` line 51
   - Or set environment variable: `export QUASH_API_BASE_URL=https://abc123.ngrok.io/`
5. Build and run the Android app

## Production Deployment

For production:
- Use a real database (Redis recommended for OTP storage)
- Implement actual SMS/Email sending
- Remove `_debug_otp` from responses
- Add rate limiting
- Use JWT for tokens
- Add authentication middleware
- Use environment variables for configuration
- Deploy to a cloud provider (Heroku, AWS, GCP, etc.)