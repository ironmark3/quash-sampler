# Demo Credentials

## For Testing the App

### Fixed OTP
**OTP Code:** `123456`

### Test Phone Numbers
You can use **ANY** phone number. Examples:
- `9876543210`
- `+919876543210`
- `1234567890`

### Test Emails
You can use **ANY** email. Examples:
- `test@example.com`
- `demo@app.com`
- `user@test.io`

## How It Works

1. **Enter any phone number or email** in the login screen
2. **Always use OTP:** `123456`
3. You'll be logged in and redirected to the home screen

## Backend Configuration

The backend is running in **DEMO MODE** where:
- All requests generate the same OTP: `123456`
- No real SMS/Email is sent
- OTP is shown in the API response for easy testing

To disable demo mode and use random OTPs:
- Edit `quash-sampler-backend/src/otpService.js`
- Change `DEMO_MODE = true` to `DEMO_MODE = false`