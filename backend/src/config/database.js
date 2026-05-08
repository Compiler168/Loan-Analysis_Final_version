/**
 * SmartLoan AI+ — MongoDB Connection
 */
const mongoose = require('mongoose');

const connectDB = async () => {
  try {
    const uri = process.env.MONGODB_URI;
    if (!uri) {
      console.log('⚠️  MONGODB_URI not set — running without database');
      return false;
    }

    await mongoose.connect(uri, {
      serverSelectionTimeoutMS: 30000,
      socketTimeoutMS: 45000,
      family: 4,
    });

    console.log(`✅ MongoDB connected: ${mongoose.connection.host}`);
    return true;
  } catch (err) {
    console.error(`❌ MongoDB connection error: ${err.message}`);
    console.log('⚠️  Continuing without database — using in-memory fallback');
    return false;
  }
};

mongoose.connection.on('disconnected', () => {
  console.log('⚠️  MongoDB disconnected');
});

mongoose.connection.on('error', (err) => {
  console.error('❌ MongoDB error:', err.message);
});

module.exports = connectDB;
