const express = require('express');
const { generateOTP, generateSessionId, storeOTP, verifyOTP, getOTPForTesting, getOTPByIdentifier } = require('./otpService');
const UserService = require('./services/userService');

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
      ? "OTP sent to your phone"
      : "OTP sent to your email",
    sessionId
  });
});

// POST /auth/verify-otp - Verify OTP
router.post('/auth/verify-otp', async (req, res) => {
  const { sessionId, otp } = req.body;

  if (!sessionId || !otp) {
    return res.status(400).json({
      success: false,
      message: 'Session ID and OTP are required'
    });
  }

  const result = await verifyOTP(sessionId, otp);

  if (result.success) {
    // Generate a token (in production, use JWT)
    const token = `token_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;

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

// Testing endpoint to get OTP (development only)
router.get('/test/get-otp/:sessionId', (req, res) => {
  if (process.env.NODE_ENV === 'production') {
    return res.status(404).json({ message: 'Not found' });
  }

  const { sessionId } = req.params;
  const otp = getOTPForTesting(sessionId);

  if (otp) {
    res.json({ otp, sessionId });
  } else {
    res.status(404).json({ message: 'Session not found or expired' });
  }
});

// AI Tool endpoint - Get OTP by phone/email (for AI testing)
router.get('/ai/get-otp/:identifier', (req, res) => {

  const { identifier } = req.params;
  const result = getOTPByIdentifier(identifier);

  if (result) {
    res.json({
      otp: result.otp,
      sessionId: result.sessionId,
      identifier: result.identifier
    });
  } else {
    res.status(404).json({
      message: 'No OTP found for this phone/email. Make sure to request OTP first.'
    });
  }
});

// GET /profile/:userId - Get user profile
router.get('/profile/:userId', async (req, res) => {
  try {
    const { userId } = req.params;
    const user = await UserService.getUserById(userId);

    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'User not found'
      });
    }

    res.json({
      success: true,
      user: {
        id: user._id.toString(),
        name: user.name,
        email: user.email,
        phone: user.phone,
        address: user.address,
        dateOfBirth: user.dateOfBirth,
        role: user.role,
        isProfileComplete: user.isProfileComplete,
        profileCompletionPercentage: user.profileCompletionPercentage
      }
    });
  } catch (error) {
    console.error('Error fetching user profile:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch profile'
    });
  }
});

// PUT /profile/:userId - Update user profile
router.put('/profile/:userId', async (req, res) => {
  try {
    const { userId } = req.params;
    const { name, address, dateOfBirth, role } = req.body;

    // Validate required fields
    if (!name || name.trim().length === 0) {
      return res.status(400).json({
        success: false,
        message: 'Name is required'
      });
    }

    if (role && !['Reporter', 'Developer', 'QA'].includes(role)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid role. Must be Reporter, Developer, or QA'
      });
    }

    const profileData = {
      name: name.trim(),
      address: address ? address.trim() : undefined,
      dateOfBirth: dateOfBirth ? new Date(dateOfBirth) : undefined,
      role: role || undefined
    };

    // Remove undefined fields
    Object.keys(profileData).forEach(key => {
      if (profileData[key] === undefined) {
        delete profileData[key];
      }
    });

    const updatedUser = await UserService.updateUserProfile(userId, profileData);

    res.json({
      success: true,
      message: 'Profile updated successfully',
      user: {
        id: updatedUser._id.toString(),
        name: updatedUser.name,
        email: updatedUser.email,
        phone: updatedUser.phone,
        address: updatedUser.address,
        dateOfBirth: updatedUser.dateOfBirth,
        role: updatedUser.role,
        isProfileComplete: updatedUser.isProfileComplete,
        profileCompletionPercentage: updatedUser.profileCompletionPercentage
      }
    });
  } catch (error) {
    console.error('Error updating user profile:', error);
    if (error.message === 'User not found') {
      return res.status(404).json({
        success: false,
        message: 'User not found'
      });
    }
    res.status(500).json({
      success: false,
      message: 'Failed to update profile'
    });
  }
});

// GET /profile/:userId/completion - Check profile completion status
router.get('/profile/:userId/completion', async (req, res) => {
  try {
    const { userId } = req.params;
    const completionData = await UserService.checkProfileCompletion(userId);

    res.json({
      success: true,
      ...completionData
    });
  } catch (error) {
    console.error('Error checking profile completion:', error);
    if (error.message === 'User not found') {
      return res.status(404).json({
        success: false,
        message: 'User not found'
      });
    }
    res.status(500).json({
      success: false,
      message: 'Failed to check profile completion'
    });
  }
});

// GET /users/search - Search users by name or email
router.get('/users/search', async (req, res) => {
  try {
    const { q, limit } = req.query;

    if (!q || q.trim().length === 0) {
      return res.status(400).json({
        success: false,
        message: 'Search query is required'
      });
    }

    const users = await UserService.searchUsers(q.trim(), limit ? parseInt(limit) : 10);

    res.json({
      success: true,
      users: users.map(user => ({
        id: user._id.toString(),
        name: user.name,
        email: user.email,
        role: user.role
      }))
    });
  } catch (error) {
    console.error('Error searching users:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to search users'
    });
  }
});

// GET /users/stats - Get user statistics
router.get('/users/stats', async (req, res) => {
  try {
    const stats = await UserService.getUserStats();

    res.json({
      success: true,
      stats
    });
  } catch (error) {
    console.error('Error fetching user stats:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch user statistics'
    });
  }
});

// Health check
router.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    timestamp: new Date().toISOString()
  });
});

module.exports = router;