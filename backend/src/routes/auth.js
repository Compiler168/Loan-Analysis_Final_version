/**
 * SmartLoan AI+ — Auth Routes (MongoDB)
 */
const express = require('express');
const router = express.Router();
const jwt = require('jsonwebtoken');
const mongoose = require('mongoose');
const { authMiddleware } = require('../middleware/auth');

const SECRET = process.env.JWT_SECRET || 'smartloan_ai_plus_jwt_secret_2026';

// Check if MongoDB is connected
const isDbConnected = () => mongoose.connection.readyState === 1;

// In-memory fallback (for when DB is not available)
const bcrypt = require('bcryptjs');
const { v4: uuidv4 } = require('uuid');
const memoryUsers = new Map();

// Seed in-memory demo user
const demoId = uuidv4();
memoryUsers.set('demo@smartloan.ai', {
  id: demoId, email: 'demo@smartloan.ai', name: 'Demo User',
  password: bcrypt.hashSync('demo123', 10), role: 'admin',
  profile: { monthly_income: 7500, monthly_expenses: 3000, credit_score: 720,
    employment_status: 'salaried', employment_years: 5, existing_loans: 1,
    existing_emi: 500, savings_balance: 25000, dependents: 1, age: 35 },
  createdAt: new Date().toISOString()
});

function signToken(user) {
  return jwt.sign(
    { id: user._id || user.id, email: user.email, name: user.name, role: user.role },
    SECRET, { expiresIn: '7d' }
  );
}

// POST /api/auth/register
router.post('/register', async (req, res) => {
  try {
    const { name, email, password } = req.body;
    if (!name || !email || !password) return res.status(400).json({ success: false, error: 'All fields required' });

    if (isDbConnected()) {
      const User = require('../models/User');
      const exists = await User.findOne({ email });
      if (exists) return res.status(409).json({ success: false, error: 'Email already registered' });
      const user = await User.create({ name, email, password });
      const token = signToken(user);
      res.status(201).json({ success: true, data: { token, user: { id: user._id, email, name, role: user.role } } });
    } else {
      if (memoryUsers.has(email)) return res.status(409).json({ success: false, error: 'Email already registered' });
      const hashed = await bcrypt.hash(password, 10);
      const user = { id: uuidv4(), email, name, password: hashed, role: 'user', profile: {}, createdAt: new Date().toISOString() };
      memoryUsers.set(email, user);
      const token = signToken(user);
      res.status(201).json({ success: true, data: { token, user: { id: user.id, email, name, role: 'user' } } });
    }
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/auth/login
router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;

    if (isDbConnected()) {
      const User = require('../models/User');
      const user = await User.findOne({ email }).select('+password');
      if (!user) return res.status(401).json({ success: false, error: 'Invalid credentials' });

      const valid = await user.comparePassword(password);
      if (!valid) return res.status(401).json({ success: false, error: 'Invalid credentials' });

      user.lastLogin = new Date();
      await user.save();

      const token = signToken(user);
      res.json({ success: true, data: {
        token,
        user: { id: user._id, email: user.email, name: user.name, role: user.role, profile: user.profile }
      }});
    } else {
      const user = memoryUsers.get(email);
      if (!user) return res.status(401).json({ success: false, error: 'Invalid credentials' });
      const valid = await bcrypt.compare(password, user.password);
      if (!valid) return res.status(401).json({ success: false, error: 'Invalid credentials' });
      const token = signToken(user);
      res.json({ success: true, data: {
        token,
        user: { id: user.id, email: user.email, name: user.name, role: user.role, profile: user.profile }
      }});
    }
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/auth/me
router.get('/me', authMiddleware, async (req, res) => {
  try {
    if (isDbConnected()) {
      const User = require('../models/User');
      const user = await User.findById(req.user.id);
      if (!user) return res.status(404).json({ success: false, error: 'User not found' });
      res.json({ success: true, data: { id: user._id, email: user.email, name: user.name, role: user.role, profile: user.profile } });
    } else {
      const user = [...memoryUsers.values()].find(u => u.id === req.user.id);
      if (!user) return res.status(404).json({ success: false, error: 'User not found' });
      res.json({ success: true, data: { id: user.id, email: user.email, name: user.name, role: user.role, profile: user.profile } });
    }
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// PUT /api/auth/profile
router.put('/profile', authMiddleware, async (req, res) => {
  try {
    if (isDbConnected()) {
      const User = require('../models/User');
      const updateData = {};
      if (req.body.name) updateData.name = req.body.name;

      // Build profile updates
      const profileFields = ['monthly_income', 'monthly_expenses', 'credit_score', 'employment_status',
        'employment_years', 'existing_loans', 'existing_emi', 'savings_balance', 'dependents', 'age', 'property_value'];
      profileFields.forEach(f => {
        if (req.body[f] !== undefined) updateData[`profile.${f}`] = req.body[f];
      });

      const user = await User.findByIdAndUpdate(req.user.id, { $set: updateData }, { new: true });
      if (!user) return res.status(404).json({ success: false, error: 'User not found' });
      res.json({ success: true, data: { id: user._id, email: user.email, name: user.name, role: user.role, profile: user.profile } });
    } else {
      const user = [...memoryUsers.values()].find(u => u.id === req.user.id);
      if (!user) return res.status(404).json({ success: false, error: 'User not found' });
      user.profile = { ...user.profile, ...req.body };
      if (req.body.name) user.name = req.body.name;
      res.json({ success: true, data: { id: user.id, email: user.email, name: user.name, role: user.role, profile: user.profile } });
    }
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

module.exports = router;
