# AI Tool Testing Documentation

This document provides comprehensive API testing flows for the Quash Sampler Bug Reporting System, designed for AI tool validation and automated testing.

## Base URL
```
http://localhost:3000
```

## Authentication Flow

### 1. Request OTP
**Endpoint:** `POST /auth/login`

**Request:**
```json
{
  "identifier": "test@example.com"
}
```

**Response:**
```json
{
  "success": true,
  "message": "OTP sent to your email",
  "sessionId": "session_1759325757732_q2l661xvv"
}
```

**AI Tool Usage:**
- Use email or phone number as identifier
- Store the sessionId for OTP verification
- Valid formats: email (`user@example.com`) or phone (`+1234567890`)

### 2. Get OTP (AI Tool Endpoint)
**Endpoint:** `GET /ai/get-otp/:identifier`

**Example:** `GET /ai/get-otp/test@example.com`

**Response:**
```json
{
  "otp": "367888",
  "sessionId": "session_1759325757732_q2l661xvv",
  "identifier": "test@example.com"
}
```

**AI Tool Usage:**
- Use this endpoint to programmatically retrieve OTP
- Replace `:identifier` with the email/phone used in login
- This endpoint is specifically designed for AI testing

### 3. Verify OTP
**Endpoint:** `POST /auth/verify-otp`

**Request:**
```json
{
  "sessionId": "session_1759325757732_q2l661xvv",
  "otp": "367888"
}
```

**Success Response:**
```json
{
  "success": true,
  "message": "OTP verified successfully",
  "token": "token_1759325776928_9vo71ajbp",
  "user": {
    "id": "68dd2e50d69be892dbfc0269",
    "name": "New User",
    "email": "test@example.com",
    "phone": null,
    "role": "Reporter",
    "isProfileComplete": false
  }
}
```

**AI Tool Usage:**
- Store the user ID for subsequent API calls
- Store the token for authentication (future implementation)
- First-time users will have `isProfileComplete: false`

## Profile Management

### 4. Get User Profile
**Endpoint:** `GET /profile/:userId`

**Example:** `GET /profile/68dd2e50d69be892dbfc0269`

**Response:**
```json
{
  "success": true,
  "user": {
    "id": "68dd2e50d69be892dbfc0269",
    "name": "New User",
    "email": "test@example.com",
    "phone": null,
    "address": null,
    "dateOfBirth": null,
    "role": "Reporter",
    "isProfileComplete": false,
    "profileCompletionPercentage": 50
  }
}
```

### 5. Update User Profile
**Endpoint:** `PUT /profile/:userId`

**Request:**
```json
{
  "name": "John Doe",
  "address": "123 Main St, City, State",
  "dateOfBirth": "1990-01-15",
  "role": "Developer"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Profile updated successfully",
  "user": {
    "id": "68dd2e50d69be892dbfc0269",
    "name": "John Doe",
    "email": "test@example.com",
    "phone": null,
    "address": "123 Main St, City, State",
    "dateOfBirth": "1990-01-15T00:00:00.000Z",
    "role": "Developer",
    "isProfileComplete": true,
    "profileCompletionPercentage": 83
  }
}
```

**AI Tool Usage:**
- Valid roles: `Reporter`, `Developer`, `QA`
- Date format: `YYYY-MM-DD`
- Profile becomes complete when all required fields are filled

### 6. Check Profile Completion
**Endpoint:** `GET /profile/:userId/completion`

**Response:**
```json
{
  "success": true,
  "isComplete": false,
  "completionPercentage": 50,
  "missingFields": ["phone", "address", "dateOfBirth"]
}
```

## User Management

### 7. Search Users
**Endpoint:** `GET /users/search?q=searchterm&limit=10`

**Example:** `GET /users/search?q=john&limit=5`

**Response:**
```json
{
  "success": true,
  "users": [
    {
      "id": "68dd2e50d69be892dbfc0269",
      "name": "John Doe",
      "email": "test@example.com",
      "role": "Developer"
    }
  ]
}
```

### 8. Get User Statistics
**Endpoint:** `GET /users/stats`

**Response:**
```json
{
  "success": true,
  "stats": {
    "totalUsers": 1,
    "completeProfiles": 0,
    "incompleteProfiles": 1,
    "roleDistribution": {
      "Reporter": 1,
      "Developer": 0,
      "QA": 0
    }
  }
}
```

## System Health

### 9. Health Check
**Endpoint:** `GET /health`

**Response:**
```json
{
  "status": "ok",
  "timestamp": "2025-10-01T13:32:48.719Z"
}
```

## AI Tool Testing Workflows

### Complete User Registration Flow
```bash
# 1. Request OTP
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier": "newuser@example.com"}'

# 2. Get OTP (AI Tool)
curl -X GET http://localhost:3000/ai/get-otp/newuser@example.com

# 3. Verify OTP (use values from previous responses)
curl -X POST http://localhost:3000/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"sessionId": "SESSION_ID", "otp": "OTP_CODE"}'

# 4. Update Profile (use user ID from verification response)
curl -X PUT http://localhost:3000/profile/USER_ID \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Smith",
    "address": "456 Oak Ave, Town, State",
    "dateOfBirth": "1985-03-20",
    "role": "QA"
  }'

# 5. Verify Profile Completion
curl -X GET http://localhost:3000/profile/USER_ID/completion
```

### Data Validation Testing

**Email Validation:**
- Valid: `user@domain.com`, `test.email+tag@example.org`
- Invalid: `invalid-email`, `@domain.com`, `user@`

**Phone Validation:**
- Valid: `+1234567890`, `(555) 123-4567`, `555-123-4567`
- Invalid: `123`, `invalid-phone`

**Role Validation:**
- Valid: `Reporter`, `Developer`, `QA`
- Invalid: `Admin`, `Manager`, `User`

**Date Validation:**
- Valid: `1990-01-15`, `2000-12-31`
- Invalid: `15-01-1990`, `invalid-date`, `2025-13-45`

### Error Handling Test Cases

**400 Bad Request:**
```json
{
  "success": false,
  "message": "Phone number or email is required"
}
```

**404 Not Found:**
```json
{
  "success": false,
  "message": "User not found"
}
```

**401 Unauthorized:**
```json
{
  "success": false,
  "message": "Invalid OTP"
}
```

## Legacy Test Cases (Original Implementation)

### Phone Number Test
```bash
# Step 1: AI enters phone in app UI (9876543210)
# Step 2: Get OTP via API
curl http://localhost:3000/ai/get-otp/9876543210

# Response:
# {"otp":"482736","sessionId":"session_xxx","identifier":"9876543210"}

# Step 3: AI enters OTP (482736) in app → Success
```

### Email Test
```bash
# Step 1: AI enters email in app UI (test@example.com)
# Step 2: Get OTP via API
curl http://localhost:3000/ai/get-otp/test@example.com

# Response:
# {"otp":"636853","sessionId":"session_xxx","identifier":"test@example.com"}

# Step 3: AI enters OTP (636853) in app → Success
```

## Phase 2: Enhanced Onboarding (COMPLETED)

### Onboarding Flow Testing

After successful OTP verification, new users with `isProfileComplete: false` are automatically routed to the onboarding flow.

**4-Step Onboarding Wizard:**
1. **Welcome Step** - Introduction and overview
2. **Personal Info** - Name collection (required)
3. **Contact Details** - Address and date of birth (optional)
4. **Role Selection** - Choose from Reporter, Developer, or QA
5. **Welcome Completion** - Role-based success screen

### Profile Completion Logic (Updated)

**Required Fields for Completion:**
- `name` (required)
- `role` (required)
- Contact info: `email` OR `phone` (at least one)

**Optional Enhancement Fields:**
- `address`
- `dateOfBirth`

**Completion Percentage Calculation:**
- Core fields (name, role): 30 points each
- Contact info: 20 points
- Optional fields: 10 points each
- Total possible: 100 points

### Role Selection Options

**Reporter:**
- Find and report bugs to help improve software quality
- Responsibilities: Identify bugs, provide reproduction steps, test features

**Developer:**
- Build, fix, and enhance software applications
- Responsibilities: Write code, fix bugs, implement features

**QA Engineer:**
- Ensure software quality through systematic testing
- Responsibilities: Design test cases, perform testing, validate fixes

### AI Tool Testing: Complete Onboarding Flow

```bash
# 1. Verify OTP (new user)
curl -X POST http://localhost:3000/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"sessionId": "SESSION_ID", "otp": "OTP_CODE"}'

# Response will show isProfileComplete: false for new users

# 2. Complete profile via onboarding (simulates mobile app flow)
curl -X PUT http://localhost:3000/profile/USER_ID \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Doe",
    "address": "123 Developer St",
    "dateOfBirth": "1990-05-15",
    "role": "Developer"
  }'

# 3. Verify completion
curl -X GET http://localhost:3000/profile/USER_ID

# Response will now show isProfileComplete: true
```

### Conditional Routing Behavior

**For AI Tool Testing:**
- New users (first OTP): `isProfileComplete: false` → Onboarding required
- Returning users: `isProfileComplete: true` → Direct to home
- Skipped onboarding: Profile remains incomplete, can complete later

## Future Endpoints (Phases 3-5)

### Phase 3: Bug Reporting
- `POST /bugs` - Create bug report
- `GET /bugs` - List bug reports
- `GET /bugs/:bugId` - Get bug details
- `PUT /bugs/:bugId` - Update bug report
- `POST /bugs/:bugId/comments` - Add comment

### Phase 4: Dashboard
- `GET /dashboard/stats` - Real-time dashboard statistics
- `GET /dashboard/recent-bugs` - Recent bug reports
- `GET /dashboard/user-activity` - User activity feed

### Phase 5: AI Validation
- `POST /ai/validate-bug` - AI bug validation
- `GET /ai/validation-status/:bugId` - Check validation status
- `POST /ai/test-scenario` - Execute test scenarios

## Current Implementation Status

✅ **Phase 1 Complete:**
- MongoDB connection and schemas
- User authentication with OTP
- Profile management APIs
- User search and statistics
- File upload middleware (ready for bug attachments)

✅ **Phase 2 Complete:**
- 4-step onboarding wizard flow
- Conditional routing based on profile completion
- Role selection with detailed descriptions
- Welcome completion screen with role-based messaging
- Updated profile completion logic (realistic requirements)
- HomeScreen real-time user data integration
- Skip/complete later options throughout flow
- ProfileScreen for profile editing

🔄 **Next: Phase 3**
- Bug reporting core features
- Bug creation, editing, and management
- Comment system and status tracking

## Testing Notes for AI Tool

1. **Session Management:** OTP sessions expire after 5 minutes
2. **Rate Limiting:** Maximum 3 OTP attempts per session
3. **Data Persistence:** All user data is stored in MongoDB Atlas
4. **Unique Constraints:** Email and phone must be unique across users
5. **Profile Completion:** Requires name, role, and email/phone (realistic requirements)
6. **AI Endpoint:** `/ai/get-otp/:identifier` is specifically for automated testing
7. **Onboarding Flow:** New users automatically routed to 4-step wizard after OTP
8. **Conditional Navigation:** App behavior changes based on `isProfileComplete` status
9. **Real User Data:** HomeScreen displays actual user data, not hardcoded values

## Environment Configuration

```env
MONGODB_URI=mongodb+srv://...
NODE_ENV=development
PORT=3000
UPLOAD_DIR=uploads
MAX_FILE_SIZE=10MB
```

**Server Status:** ✅ Running on http://localhost:3000 with MongoDB Atlas connection