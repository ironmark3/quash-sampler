require('dotenv').config();
const express = require('express');
const cors = require('cors');
const path = require('path');
const connectDB = require('./config/database');
const routes = require('./routes');

const app = express();
const PORT = process.env.PORT || 3000;
const NODE_ENV = process.env.NODE_ENV || 'development';

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

// Serve uploaded files statically
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));

// Start server
app.listen(PORT, async () => {
  // Connect to database
  await connectDB();
  console.log('\n🚀 Quash Sampler Backend Server');
  console.log('================================');
  console.log(`✅ Server running on http://localhost:${PORT}`);
  console.log(`Environment: ${NODE_ENV}`);
  console.log(`\n📝 Available endpoints:`);
  console.log(`   POST /auth/login - Request OTP`);
  console.log(`   POST /auth/verify-otp - Verify OTP`);
  console.log(`   GET  /profile/:userId - Get user profile`);
  console.log(`   PUT  /profile/:userId - Update user profile`);
  console.log(`   GET  /profile/:userId/completion - Check profile completion`);
  console.log(`   GET  /users/search - Search users`);
  console.log(`   GET  /users/stats - Get user statistics`);
  console.log(`   GET  /health - Health check`);
  console.log(`   GET  /ai/get-otp/:identifier - Get OTP by phone/email (AI tool)`);
  if (NODE_ENV !== 'production') {
    console.log(`   GET  /test/get-otp/:sessionId - Get OTP for testing`);
  }
  console.log('\n💡 Use ngrok to expose: ngrok http 3000');
  console.log('================================\n');
});