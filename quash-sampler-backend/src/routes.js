const express = require('express');
const { generateOTP, generateSessionId, storeOTP, verifyOTP } = require('./otpService');
const { generateToken, revokeToken } = require('./tokenService');
const { authenticate } = require('./authMiddleware');
const scenarioRoutes = require('./scenarioRoutes');

const router = express.Router();

// Validate phone number (basic validation)
function isValidPhone(phone) {
  const phoneRegex = /^[+]?[(]?[0-9]{3}[)]?[-\s.]?[0-9]{3}[-\s.]?[0-9]{4,6}$/;
  return phoneRegex.test(phone.replace(/\s/g, ''));
}

// Validate email
function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

// POST /auth/login - Request OTP
router.post('/auth/login', (req, res) => {
  const { identifier } = req.body;

  if (!identifier || identifier.trim().length === 0) {
    return res.status(400).json({
      success: false,
      message: 'Phone number or email is required'
    });
  }

  const trimmedIdentifier = identifier.trim();

  // Validate if it's a phone or email
  const isPhone = isValidPhone(trimmedIdentifier);
  const isEmail = isValidEmail(trimmedIdentifier);

  if (!isPhone && !isEmail) {
    return res.status(400).json({
      success: false,
      message: 'Please enter a valid phone number or email'
    });
  }

  // Generate OTP and session
  const otp = generateOTP();
  const sessionId = generateSessionId();

  // Store OTP
  storeOTP(sessionId, otp, trimmedIdentifier);

  // In production, send OTP via SMS/Email
  const type = isPhone ? '📱 Phone' : '📧 Email';
  console.log(`\n🔐 OTP for ${type}: ${trimmedIdentifier}`);
  console.log(`   Code: ${otp}`);
  console.log(`   Session: ${sessionId}\n`);

  res.json({
    success: true,
    message: isPhone
      ? `OTP sent to your phone. Use: ${otp}`
      : `OTP sent to your email. Use: ${otp}`,
    sessionId,
    // Demo mode - always show OTP
    _debug_otp: otp
  });
});

// POST /auth/verify-otp - Verify OTP
router.post('/auth/verify-otp', (req, res) => {
  const { sessionId, otp } = req.body;

  if (!sessionId || !otp) {
    return res.status(400).json({
      success: false,
      message: 'Session ID and OTP are required'
    });
  }

  const result = verifyOTP(sessionId, otp);

  if (result.success) {
    // Generate JWT token
    const token = generateToken(result.user);

    console.log(`✅ User authenticated: ${result.user.email || result.user.phone}`);

    res.json({
      success: true,
      message: result.message,
      token,
      user: result.user
    });
  } else {
    res.status(401).json(result);
  }
});

// Scenario routes for testing various responses
router.use('/api', scenarioRoutes);

// POST /auth/logout - Logout (revoke token)
router.post('/auth/logout', authenticate, (req, res) => {
  const { token } = req;

  revokeToken(token);

  console.log(`🔒 User logged out: ${req.user.identifier}`);

  res.json({
    success: true,
    message: 'Logged out successfully'
  });
});

// GET /auth/me - Get current user info (protected)
router.get('/auth/me', authenticate, (req, res) => {
  res.json({
    success: true,
    user: req.user
  });
});

// Health check
router.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    timestamp: new Date().toISOString()
  });
});

module.exports = router;
