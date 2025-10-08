const { verifyToken } = require('./tokenService');

// Authentication middleware
function authenticate(req, res, next) {
  // Extract token from Authorization header
  const authHeader = req.headers.authorization;

  if (!authHeader) {
    return res.status(401).json({
      success: false,
      message: 'No authorization token provided',
      code: 'NO_TOKEN'
    });
  }

  // Expected format: "Bearer <token>"
  const parts = authHeader.split(' ');

  if (parts.length !== 2 || parts[0] !== 'Bearer') {
    return res.status(401).json({
      success: false,
      message: 'Invalid authorization format. Use: Bearer <token>',
      code: 'INVALID_AUTH_FORMAT'
    });
  }

  const token = parts[1];

  // Verify token
  const result = verifyToken(token);

  if (!result.valid) {
    return res.status(401).json({
      success: false,
      message: result.error,
      code: 'INVALID_TOKEN'
    });
  }

  // Attach user info to request
  req.user = result.user;
  req.token = token;

  next();
}

// Optional authentication (doesn't fail if no token)
function optionalAuth(req, res, next) {
  const authHeader = req.headers.authorization;

  if (!authHeader) {
    req.user = null;
    return next();
  }

  const parts = authHeader.split(' ');

  if (parts.length === 2 && parts[0] === 'Bearer') {
    const token = parts[1];
    const result = verifyToken(token);

    if (result.valid) {
      req.user = result.user;
      req.token = token;
    } else {
      req.user = null;
    }
  }

  next();
}

module.exports = {
  authenticate,
  optionalAuth
};