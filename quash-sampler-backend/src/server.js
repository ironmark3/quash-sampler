const express = require('express');
const cors = require('cors');
const routes = require('./routes');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Request logging
app.use((req, res, next) => {
  console.log(`${new Date().toISOString()} - ${req.method} ${req.path}`);
  next();
});

// Routes
app.use('/', routes);

// 404 handler
app.use((req, res) => {
  res.status(404).json({
    success: false,
    message: 'Endpoint not found'
  });
});

// Error handler
app.use((err, req, res, next) => {
  console.error('Error:', err);
  res.status(500).json({
    success: false,
    message: 'Internal server error'
  });
});

// Start server
app.listen(PORT, () => {
  console.log('\n🚀 Quash Sampler Backend Server');
  console.log('================================');
  console.log(`✅ Server running on http://localhost:${PORT}`);
  console.log(`\n📝 Available endpoints:`);
  console.log(`   POST /auth/login           - Request OTP`);
  console.log(`   POST /auth/verify-otp      - Verify OTP`);
  console.log(`   GET  /health               - Health check`);
  console.log(`   GET  /api/status/ok        - 200 success sample`);
  console.log(`   GET  /api/status/not-found - 404 sample`);
  console.log(`   GET  /api/status/server-error - 500 sample`);
  console.log(`   GET  /api/metrics/daily    - Nested JSON for JSONPath`);
  console.log(`   GET  /api/delayed?ms=1500  - Simulated latency`);
  console.log(`   POST /api/orders           - Validates request body`);
  console.log(`   GET  /api/otp/latest       - Plain text OTP log`);
  console.log('\n💡 Use ngrok to expose: ngrok http 3000');
  console.log('================================\n');
});
