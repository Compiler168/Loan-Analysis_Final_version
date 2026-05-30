/**
 * SmartLoan AI+ — Auth Controller (Firestore)
 */
const jwt = require('jsonwebtoken');
const User = require('../models/User');
const { updateDashboard } = require('../services/DashboardUpdater');
const { initializeFirebase } = require('../config/firebase');

const SECRET = process.env.JWT_SECRET || 'smartloan_ai_plus_jwt_secret_2026';

const signToken = (user) => {
  return jwt.sign(
    { id: user.id, email: user.email, name: user.name, role: user.role },
    SECRET, 
    { expiresIn: '30d' }
  );
};

exports.register = async (req, res) => {
  try {
    const { name, email, password } = req.body;
    if (!name || !email || !password) {
      return res.status(400).json({ success: false, error: 'Name, email and password are required' });
    }

    const emailLower = email.toLowerCase();
    const existingUser = await User.findOne({ email: emailLower });
    if (existingUser) {
      return res.status(409).json({ success: false, error: 'Email already registered' });
    }

    const user = await User.create({ name, email: emailLower, password });
    const token = signToken(user);
    
    await updateDashboard(user.id);
    
    let firebaseCustomToken = null;
    try {
      await initializeFirebase();
      const admin = require('firebase-admin');
      if (admin.apps.length > 0) {
        firebaseCustomToken = await admin.auth().createCustomToken(user.id.toString());
      }
    } catch (err) {
      console.warn('⚠️ Failed to generate Firebase custom token:', err.message);
    }
    
    res.status(201).json({ 
      success: true, 
      data: { token, firebaseCustomToken, user: { id: user.id, email: user.email, name: user.name, role: user.role } } 
    });
  } catch (err) {
    res.status(500).json({ success: false, error: 'Server error during registration' });
  }
};

exports.login = async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) {
      return res.status(400).json({ success: false, error: 'Email and password are required' });
    }

    const user = await User.findOne({ email: email.toLowerCase() });
    if (!user || !(await user.comparePassword(password))) {
      return res.status(401).json({ success: false, error: 'Invalid email or password' });
    }

    user.lastLogin = new Date();
    await user.save();

    await updateDashboard(user.id);

    const token = signToken(user);

    let firebaseCustomToken = null;
    try {
      await initializeFirebase();
      const admin = require('firebase-admin');
      if (admin.apps.length > 0) {
        firebaseCustomToken = await admin.auth().createCustomToken(user.id.toString());
      }
    } catch (err) {
      console.warn('⚠️ Failed to generate Firebase custom token:', err.message);
    }

    res.json({ 
      success: true, 
      data: { token, firebaseCustomToken, user: { id: user.id, email: user.email, name: user.name, role: user.role, profile: user.profile } }
    });
  } catch (err) {
    res.status(500).json({ success: false, error: 'Server error during login' });
  }
};

exports.getMe = async (req, res) => {
  try {
    const user = await User.findById(req.user.id);
    if (!user) return res.status(404).json({ success: false, error: 'User not found' });
    res.json({ success: true, data: { id: user.id, email: user.email, name: user.name, role: user.role, profile: user.profile } });
  } catch (err) {
    res.status(500).json({ success: false, error: 'Server error' });
  }
};

exports.updateProfile = async (req, res) => {
  try {
    const user = await User.findById(req.user.id);
    if (!user) return res.status(404).json({ success: false, error: 'User not found' });

    if (req.body.name) user.name = req.body.name;

    const profileFields = [
      'monthly_income', 'monthly_expenses', 'credit_score', 'employment_status',
      'employment_years', 'existing_loans', 'existing_emi', 'savings_balance', 
      'dependents', 'age', 'property_value'
    ];

    profileFields.forEach(field => {
      if (req.body[field] !== undefined) user.profile[field] = req.body[field];
    });

    await user.save();
    
    await updateDashboard(user.id);
    
    res.json({ success: true, data: { id: user.id, email: user.email, name: user.name, role: user.role, profile: user.profile } });
  } catch (err) {
    res.status(500).json({ success: false, error: 'Failed to update profile' });
  }
};
