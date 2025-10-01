const UserService = require('./services/userService');

// In-memory storage for OTPs (use Redis in production)
const otpStore = new Map();
// Reverse mapping: identifier -> sessionId for AI tool
const identifierToSession = new Map();

// Demo mode - fixed OTP for testing
const DEMO_MODE = false;
const DEMO_OTP = '232423';

// Generate random 6-digit OTP
function generateOTP() {
  if (DEMO_MODE) {
    return DEMO_OTP;
  }
  return Math.floor(100000 + Math.random() * 900000).toString();
}

// Generate session ID
function generateSessionId() {
  return `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
}

// Store OTP with expiry (5 minutes)
function storeOTP(sessionId, otp, identifier) {
  const expiryTime = Date.now() + 5 * 60 * 1000; // 5 minutes
  otpStore.set(sessionId, {
    otp,
    identifier,
    expiryTime,
    attempts: 0
  });

  // LOG the OTP for testing (remove in production)
  console.log(`🔐 OTP Generated for ${identifier}: ${otp} (Session: ${sessionId})`);

  // Store reverse mapping for AI tool
  identifierToSession.set(identifier, sessionId);

  // Auto-cleanup after expiry
  setTimeout(() => {
    otpStore.delete(sessionId);
    identifierToSession.delete(identifier);
  }, 5 * 60 * 1000);

  return sessionId;
}

// Verify OTP
async function verifyOTP(sessionId, otp) {
  const data = otpStore.get(sessionId);

  if (!data) {
    return { success: false, message: 'Invalid or expired session' };
  }

  if (Date.now() > data.expiryTime) {
    otpStore.delete(sessionId);
    return { success: false, message: 'OTP expired' };
  }

  if (data.attempts >= 3) {
    otpStore.delete(sessionId);
    return { success: false, message: 'Too many attempts. Please try again.' };
  }

  data.attempts++;

  if (data.otp === otp) {
    otpStore.delete(sessionId);

    try {
      // Find or create user in database
      const user = await UserService.findOrCreateUser(data.identifier);

      return {
        success: true,
        message: 'OTP verified successfully',
        user: {
          id: user._id.toString(),
          name: user.name,
          email: user.email,
          phone: user.phone,
          role: user.role,
          isProfileComplete: user.isProfileComplete
        }
      };
    } catch (error) {
      console.error('Error creating/finding user:', error);
      return {
        success: false,
        message: 'User creation failed. Please try again.'
      };
    }
  }

  return { success: false, message: 'Invalid OTP' };
}

// Function to get OTP for testing
function getOTPForTesting(sessionId) {
  const data = otpStore.get(sessionId);
  return data ? data.otp : null;
}

// Function to get OTP by identifier (for AI tool)
function getOTPByIdentifier(identifier) {
  const sessionId = identifierToSession.get(identifier);
  if (sessionId) {
    const data = otpStore.get(sessionId);
    return data ? {
      otp: data.otp,
      sessionId: sessionId,
      identifier: identifier
    } : null;
  }
  return null;
}

module.exports = {
  generateOTP,
  generateSessionId,
  storeOTP,
  verifyOTP,
  getOTPForTesting,
  getOTPByIdentifier
};