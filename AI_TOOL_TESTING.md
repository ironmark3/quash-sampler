# AI Tool Testing - OTP Validation

## Test Case: Phone/Email OTP Validation

### Description
AI tool enters phone/email in app, retrieves OTP via API, then enters OTP to complete login.

## API Endpoint

**Get OTP by Phone/Email:**
```
GET /ai/get-otp/{identifier}
```

## Testing Steps

### 1. Phone Number Test
```bash
# Step 1: AI enters phone in app UI (9876543210)
# Step 2: Get OTP via API
curl http://localhost:3000/ai/get-otp/9876543210

# Response:
# {"otp":"482736","sessionId":"session_xxx","identifier":"9876543210"}

# Step 3: AI enters OTP (482736) in app → Success
```

### 2. Email Test
```bash
# Step 1: AI enters email in app UI (test@example.com)
# Step 2: Get OTP via API
curl http://localhost:3000/ai/get-otp/test@example.com

# Response:
# {"otp":"636853","sessionId":"session_xxx","identifier":"test@example.com"}

# Step 3: AI enters OTP (636853) in app → Success
```

## AI Tool Configuration

- **Base URL**: `http://localhost:3000` (or deployed URL)
- **Endpoint**: `/ai/get-otp/{identifier}`
- **Method**: GET
- **Response Field**: `otp`

## Test Flow
1. AI enters identifier in app
2. AI calls API to get OTP
3. AI enters OTP in app
4. Validate login success