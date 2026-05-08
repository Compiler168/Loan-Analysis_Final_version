/**
 * SmartLoan AI+ — Express.js Backend Server
 */
require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const connectDB = require('./config/database');

const app = express();
const PORT = process.env.PORT || 5000;

// Security
app.use(helmet());
app.use(cors({ origin: ['http://localhost:3000', 'http://localhost:3001'], credentials: true }));
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// Rate Limiting
app.use('/api/', rateLimit({ windowMs: 15 * 60 * 1000, max: 200, message: { error: 'Too many requests' } }));
const aiLimiter = rateLimit({ windowMs: 15 * 60 * 1000, max: 50, message: { error: 'AI rate limit reached' } });

// Routes
app.use('/api/auth', require('./routes/auth'));
app.use('/api/loans', aiLimiter, require('./routes/loans'));
app.use('/api/financial', aiLimiter, require('./routes/financial'));
app.use('/api/chat', aiLimiter, require('./routes/chat'));
app.use('/api/reports', require('./routes/reports'));
app.use('/api/admin', require('./routes/admin'));

// Health
app.get('/api/health', (req, res) => {
  const mongoose = require('mongoose');
  res.json({
    status: 'healthy',
    service: 'SmartLoan AI+',
    database: mongoose.connection.readyState === 1 ? 'connected' : 'disconnected',
    timestamp: new Date().toISOString()
  });
});

// Error handler
app.use((err, req, res, next) => {
  console.error('Error:', err.message);
  res.status(err.status || 500).json({ success: false, error: process.env.NODE_ENV === 'production' ? 'Internal error' : err.message });
});

app.use((req, res) => res.status(404).json({ success: false, error: 'Not found' }));

// Start server with DB connection
const startServer = async () => {
  const dbConnected = await connectDB();

  // Seed demo user if DB is connected and no users exist
  if (dbConnected) {
    try {
      const User = require('./models/User');
      const count = await User.countDocuments();
      if (count === 0) {
        await User.create({
          name: 'Demo User',
          email: 'demo@smartloan.ai',
          password: 'demo123',
          role: 'admin',
          status: 'active',
          profile: {
            monthly_income: 7500, monthly_expenses: 3000, credit_score: 720,
            employment_status: 'salaried', employment_years: 5, existing_loans: 1,
            existing_emi: 500, savings_balance: 25000, dependents: 1, age: 35
          }
        });
        console.log('✅ Demo user seeded: demo@smartloan.ai / demo123');
      }
    } catch (err) {
      if (err.code !== 11000) console.error('Seed error:', err.message);
    }
  }

  app.listen(PORT, () => {
    console.log(`\n🚀 SmartLoan AI+ Backend on port ${PORT}`);
    console.log(`📡 ML Service: ${process.env.ML_SERVICE_URL || 'http://localhost:8000'}`);
    console.log(`💾 Database: ${dbConnected ? 'MongoDB Atlas' : 'In-memory fallback'}`);
    console.log(`🔗 API: http://localhost:${PORT}/api\n`);
  });
};

startServer();

module.exports = app;
