// In-memory storage for OTPs (use Redis in production)
const otpStore = new Map();

// Demo mode - fixed OTP for testing
const DEMO_MODE = true;
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

  // Auto-cleanup after expiry
  setTimeout(() => {
    otpStore.delete(sessionId);
  }, 5 * 60 * 1000);

  return sessionId;
}

// Verify OTP
function verifyOTP(sessionId, otp) {
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
    return {
      success: true,
      message: 'OTP verified successfully',
      user: {
        id: `user_${Date.now()}`,
        name: 'Test User',
        email: data.identifier.includes('@') ? data.identifier : null,
        phone: data.identifier.includes('@') ? null : data.identifier
      }
    };
  }

  return { success: false, message: 'Invalid OTP' };
}

module.exports = {
  generateOTP,
  generateSessionId,
  storeOTP,
  verifyOTP
};