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

module.exports = router;
