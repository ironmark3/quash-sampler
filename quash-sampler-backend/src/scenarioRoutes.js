const express = require('express');

const router = express.Router();

router.get('/status/ok', (req, res) => {
  res.json({
    success: true,
    message: 'Everything is operating normally',
    timestamp: new Date().toISOString()
  });
});

router.get('/status/not-found', (req, res) => {
  res.status(404).json({
    success: false,
    message: 'The resource you requested was not found',
    code: 'NOT_FOUND'
  });
});

router.get('/status/server-error', (req, res) => {
  res.status(500).json({
    success: false,
    message: 'Unexpected error occurred',
    code: 'INTERNAL_ERROR'
  });
});

router.get('/metrics/daily', (req, res) => {
  res.json({
    success: true,
    generatedAt: new Date().toISOString(),
    data: {
      totals: {
        signups: 128,
        otpRequests: 342,
        successfulSessions: 317
      },
      latestOtp: {
        code: '232423',
        channel: 'sms',
        sessionId: 'session_demo_123',
        issuedAt: new Date(Date.now() - 60 * 1000).toISOString()
      },
      recentSessions: [
        {
          id: 'session_demo_120',
          status: 'verified',
          latencyMs: 842,
          identifier: '+12025550120'
        },
        {
          id: 'session_demo_121',
          status: 'pending',
          latencyMs: 1295,
          identifier: 'user@example.com'
        }
      ]
    }
  });
});

router.get('/delayed', async (req, res) => {
  const delayMs = Math.min(Math.max(Number(req.query.ms) || 1500, 0), 10000);
  await new Promise((resolve) => setTimeout(resolve, delayMs));
  res.json({
    success: true,
    delayMs,
    message: `Responded after ${delayMs}ms`
  });
});

router.post('/orders', (req, res) => {
  const { productId, quantity, channel } = req.body || {};
  if (!productId || typeof productId !== 'string') {
    return res.status(422).json({
      success: false,
      message: 'productId is required',
      field: 'productId'
    });
  }
  const parsedQuantity = Number(quantity);
  if (!Number.isFinite(parsedQuantity) || parsedQuantity <= 0) {
    return res.status(422).json({
      success: false,
      message: 'quantity must be a positive number',
      field: 'quantity'
    });
  }
  res.status(201).json({
    success: true,
    order: {
      id: `order_${Date.now()}`,
      productId,
      quantity: parsedQuantity,
      channel: channel || 'app',
      status: 'received'
    }
  });
});

router.get('/otp/latest', (req, res) => {
  res.type('text/plain').send('Latest OTP is 232423 for session session_demo_123');
});

// ==================== 2xx Success Responses ====================

// POST /api/status/201 - Created with Location header
router.post('/status/201', (req, res) => {
  const resourceId = `resource_${Date.now()}`;
  res.status(201)
    .location(`/api/resources/${resourceId}`)
    .json({
      success: true,
      message: 'Resource created successfully',
      id: resourceId,
      createdAt: new Date().toISOString()
    });
});

// PUT /api/status/204 - No Content
router.put('/status/204', (req, res) => {
  res.status(204).send();
});

// GET /api/status/206 - Partial Content
router.get('/status/206', (req, res) => {
  const fullContent = 'This is the full content of the resource that can be partially retrieved.';
  const start = 0;
  const end = 20;
  const partial = fullContent.substring(start, end);

  res.status(206)
    .set({
      'Content-Range': `bytes ${start}-${end}/${fullContent.length}`,
      'Content-Type': 'text/plain'
    })
    .send(partial);
});

// ==================== 3xx Redirect Responses ====================

// GET /api/redirect/301 - Permanent Redirect
router.get('/redirect/301', (req, res) => {
  res.status(301)
    .location('/api/status/ok')
    .json({
      success: true,
      message: 'Resource has been moved permanently',
      redirectTo: '/api/status/ok'
    });
});

// GET /api/redirect/302 - Temporary Redirect
router.get('/redirect/302', (req, res) => {
  res.status(302)
    .location('/api/status/ok')
    .json({
      success: true,
      message: 'Resource temporarily redirected',
      redirectTo: '/api/status/ok'
    });
});

// GET /api/redirect/304 - Not Modified
router.get('/redirect/304', (req, res) => {
  const etag = '"abc123"';
  const clientEtag = req.headers['if-none-match'];

  if (clientEtag === etag) {
    res.status(304).send();
  } else {
    res.status(200)
      .set('ETag', etag)
      .json({
        success: true,
        message: 'Resource content',
        data: { value: 'sample data' }
      });
  }
});

// ==================== 4xx Client Error Responses ====================

// GET /api/status/400 - Bad Request
router.get('/status/400', (req, res) => {
  res.status(400).json({
    success: false,
    message: 'The request was malformed or invalid',
    code: 'BAD_REQUEST',
    errors: [
      { field: 'email', message: 'Invalid email format' }
    ]
  });
});

// GET /api/status/401 - Unauthorized
router.get('/status/401', (req, res) => {
  res.status(401)
    .set('WWW-Authenticate', 'Bearer realm="api"')
    .json({
      success: false,
      message: 'Authentication required to access this resource',
      code: 'UNAUTHORIZED'
    });
});

// GET /api/status/403 - Forbidden
router.get('/status/403', (req, res) => {
  res.status(403).json({
    success: false,
    message: 'You do not have permission to access this resource',
    code: 'FORBIDDEN',
    requiredRole: 'admin'
  });
});

// GET /api/status/429 - Too Many Requests (Rate Limited)
router.get('/status/429', (req, res) => {
  const retryAfter = 60; // seconds
  res.status(429)
    .set('Retry-After', retryAfter.toString())
    .set('X-RateLimit-Limit', '100')
    .set('X-RateLimit-Remaining', '0')
    .set('X-RateLimit-Reset', Math.floor(Date.now() / 1000 + retryAfter).toString())
    .json({
      success: false,
      message: 'Too many requests. Please try again later.',
      code: 'RATE_LIMIT_EXCEEDED',
      retryAfter: retryAfter
    });
});

// ==================== 5xx Server Error Responses ====================

// GET /api/status/502 - Bad Gateway
router.get('/status/502', (req, res) => {
  res.status(502).json({
    success: false,
    message: 'The upstream server returned an invalid response',
    code: 'BAD_GATEWAY',
    upstreamService: 'payment-service'
  });
});

// GET /api/status/503 - Service Unavailable
router.get('/status/503', (req, res) => {
  const retryAfter = 120; // seconds
  res.status(503)
    .set('Retry-After', retryAfter.toString())
    .json({
      success: false,
      message: 'Service is temporarily unavailable due to maintenance',
      code: 'SERVICE_UNAVAILABLE',
      retryAfter: retryAfter,
      maintenanceWindow: {
        start: new Date().toISOString(),
        estimatedEnd: new Date(Date.now() + retryAfter * 1000).toISOString()
      }
    });
});

// GET /api/status/504 - Gateway Timeout
router.get('/status/504', (req, res) => {
  res.status(504).json({
    success: false,
    message: 'The upstream server failed to respond in time',
    code: 'GATEWAY_TIMEOUT',
    upstreamService: 'database',
    timeout: '30s'
  });
});

module.exports = router;
