const jwt = require('jsonwebtoken');

// Secret key for JWT signing (in production, use environment variable)
const JWT_SECRET = process.env.JWT_SECRET || 'quash-sampler-secret-key-change-in-production';
const JWT_EXPIRY = '24h'; // Token expires in 24 hours

// Master token that never expires (for testing/demo purposes)
const MASTER_TOKEN = process.env.MASTER_TOKEN || 'master_quash_sampler_token_2024';

// In-memory store for active tokens (use Redis in production)
const activeTokens = new Set();

// Generate JWT token
function generateToken(user) {
  const payload = {
    userId: user.id,
    identifier: user.email || user.phone,
    name: user.name,
    iat: Math.floor(Date.now() / 1000)
  };

  const token = jwt.sign(payload, JWT_SECRET, {
    expiresIn: JWT_EXPIRY
  });

  // Store token in active set
  activeTokens.add(token);

  return token;
}

// Verify JWT token
function verifyToken(token) {
  // Check if it's the master token
  if (token === MASTER_TOKEN) {
    return {
      valid: true,
      user: {
        userId: 'master_user',
        identifier: 'master@quash.io',
        name: 'Master User'
      }
    };
  }

  try {
    const decoded = jwt.verify(token, JWT_SECRET);

    // Check if token is in active set
    if (!activeTokens.has(token)) {
      return { valid: false, error: 'Token has been revoked' };
    }

    return {
      valid: true,
      user: {
        userId: decoded.userId,
        identifier: decoded.identifier,
        name: decoded.name
      }
    };
  } catch (error) {
    if (error.name === 'TokenExpiredError') {
      return { valid: false, error: 'Token has expired' };
    } else if (error.name === 'JsonWebTokenError') {
      return { valid: false, error: 'Invalid token' };
    }
    return { valid: false, error: 'Token verification failed' };
  }
}

// Revoke token (for logout)
function revokeToken(token) {
  return activeTokens.delete(token);
}

// Clear expired tokens periodically (cleanup)
setInterval(() => {
  const tokensToRemove = [];

  activeTokens.forEach(token => {
    try {
      jwt.verify(token, JWT_SECRET);
    } catch (error) {
      if (error.name === 'TokenExpiredError') {
        tokensToRemove.push(token);
      }
    }
  });

  tokensToRemove.forEach(token => activeTokens.delete(token));

  if (tokensToRemove.length > 0) {
    console.log(`🧹 Cleaned up ${tokensToRemove.length} expired tokens`);
  }
}, 60 * 60 * 1000); // Run every hour

module.exports = {
  generateToken,
  verifyToken,
  revokeToken
};