const mongoose = require('mongoose');

const connectDB = async () => {
  try {
    const conn = await mongoose.connect(process.env.MONGODB_URI);

    console.log(`✅ MongoDB Connected: ${conn.connection.host}`);

    // Log database events
    mongoose.connection.on('error', (err) => {
      console.error('❌ MongoDB connection error:', err);
    });

    mongoose.connection.on('disconnected', () => {
      console.log('⚠️ MongoDB disconnected');
    });

    return conn;
  } catch (error) {
    console.error('❌ MongoDB connection failed:', error.message);

    // In development, continue without DB for OTP testing
    if (process.env.NODE_ENV === 'development') {
      console.log('⚠️ Continuing without database in development mode');
      return null;
    }

    process.exit(1);
  }
};

module.exports = connectDB;