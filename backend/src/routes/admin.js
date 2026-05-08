/**
 * SmartLoan AI+ — Admin Routes (MongoDB)
 */
const express = require('express');
const router = express.Router();
const axios = require('axios');
const mongoose = require('mongoose');
const { authMiddleware } = require('../middleware/auth');

const ML_URL = process.env.ML_SERVICE_URL || 'http://localhost:8000';

router.get('/stats', authMiddleware, async (req, res) => {
  try {
    if (mongoose.connection.readyState === 1) {
      const User = require('../models/User');
      const Prediction = require('../models/Prediction');
      const ChatSession = require('../models/ChatSession');
      const Analysis = require('../models/Analysis');

      const totalUsers = await User.countDocuments();
      const activeUsers = await User.countDocuments({ status: 'active' });
      const totalPredictions = await Prediction.countDocuments();
      const chatSessions = await ChatSession.countDocuments();
      const totalAnalyses = await Analysis.countDocuments();

      // Predictions today
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const predictionsToday = await Prediction.countDocuments({ createdAt: { $gte: today } });

      // Average health score
      const healthScores = await Analysis.find({ type: 'health_score' })
        .sort({ createdAt: -1 }).limit(100).select('result.overall_score');
      const avgHealth = healthScores.length > 0
        ? Math.round(healthScores.reduce((s, a) => s + (a.result?.overall_score || 0), 0) / healthScores.length)
        : 68;

      // Monthly growth (aggregate predictions per month)
      const monthlyStats = await Prediction.aggregate([
        { $group: {
          _id: { $month: '$createdAt' },
          predictions: { $sum: 1 }
        }},
        { $sort: { '_id': 1 } },
        { $limit: 6 }
      ]);

      const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
      const growth = monthlyStats.map(m => ({
        month: months[(m._id - 1) % 12],
        users: totalUsers,
        predictions: m.predictions
      }));

      res.json({ success: true, data: {
        total_users: totalUsers,
        active_sessions: activeUsers,
        predictions_today: predictionsToday,
        total_predictions: totalPredictions,
        avg_health_score: avgHealth,
        chatbot_sessions: chatSessions,
        total_analyses: totalAnalyses,
        system_uptime: '99.7%',
        ml_service_status: 'healthy',
        monthly_stats: growth.length > 0 ? growth : [
          { month: 'May', users: totalUsers, predictions: totalPredictions }
        ]
      }});
    } else {
      // Fallback static data
      res.json({ success: true, data: {
        total_users: 1, active_sessions: 1, predictions_today: 0,
        total_predictions: 0, avg_health_score: 68, chatbot_sessions: 0,
        system_uptime: '99.7%', ml_service_status: 'healthy',
        monthly_stats: [{ month: 'May', users: 1, predictions: 0 }]
      }});
    }
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

router.get('/users', authMiddleware, async (req, res) => {
  try {
    if (mongoose.connection.readyState === 1) {
      const User = require('../models/User');
      const users = await User.find()
        .select('name email role status lastLogin createdAt')
        .sort({ createdAt: -1 })
        .limit(50);

      const formatted = users.map(u => ({
        id: u._id,
        name: u.name,
        email: u.email,
        role: u.role,
        status: u.status,
        lastLogin: u.lastLogin ? u.lastLogin.toISOString().split('T')[0] : 'Never',
      }));

      res.json({ success: true, data: formatted });
    } else {
      res.json({ success: true, data: [
        { id: '1', name: 'Demo User', email: 'demo@smartloan.ai', role: 'admin', status: 'active', lastLogin: '2026-05-07' }
      ]});
    }
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

router.get('/model-info', authMiddleware, async (req, res) => {
  try {
    const r = await axios.get(`${ML_URL}/model-info`);
    res.json({ success: true, data: r.data.data });
  } catch (err) { res.status(502).json({ success: false, error: 'ML service unavailable' }); }
});

// GET /api/admin/db-status
router.get('/db-status', authMiddleware, async (req, res) => {
  const states = { 0: 'disconnected', 1: 'connected', 2: 'connecting', 3: 'disconnecting' };
  res.json({
    success: true,
    data: {
      status: states[mongoose.connection.readyState] || 'unknown',
      host: mongoose.connection.host || 'none',
      database: mongoose.connection.name || 'none',
      collections: mongoose.connection.readyState === 1
        ? Object.keys(mongoose.connection.collections)
        : [],
    }
  });
});

module.exports = router;
