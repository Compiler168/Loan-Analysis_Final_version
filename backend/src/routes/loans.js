/**
 * SmartLoan AI+ — Loan Routes (MongoDB)
 */
const express = require('express');
const router = express.Router();
const axios = require('axios');
const mongoose = require('mongoose');
const { authMiddleware } = require('../middleware/auth');

const ML_URL = process.env.ML_SERVICE_URL || 'http://localhost:8000';

// POST /api/loans/predict
router.post('/predict', authMiddleware, async (req, res) => {
  try {
    const response = await axios.post(`${ML_URL}/predict`, req.body);
    const resultData = response.data.data;

    // Save prediction to MongoDB
    if (mongoose.connection.readyState === 1) {
      const Prediction = require('../models/Prediction');
      await Prediction.create({
        userId: req.user.id,
        input: req.body,
        result: resultData,
        status: resultData.ensemble.approved ? 'approved' : 'rejected',
      });
    }

    res.json({ success: true, data: resultData });
  } catch (err) {
    const msg = err.response?.data?.detail || 'ML service unavailable';
    res.status(502).json({ success: false, error: msg });
  }
});

// GET /api/loans/history
router.get('/history', authMiddleware, async (req, res) => {
  try {
    if (mongoose.connection.readyState === 1) {
      const Prediction = require('../models/Prediction');
      const predictions = await Prediction.find({ userId: req.user.id })
        .sort({ createdAt: -1 })
        .limit(20)
        .select('input.loan_amount result.ensemble.probability status createdAt');

      const history = predictions.map((p, i) => ({
        id: p._id,
        date: p.createdAt.toISOString().split('T')[0],
        amount: p.input.loan_amount,
        status: p.status,
        probability: p.result?.ensemble?.probability || 0,
      }));

      res.json({ success: true, data: history });
    } else {
      // Fallback static data
      res.json({ success: true, data: [
        { id: 1, date: '2026-05-01', amount: 50000, status: 'approved', probability: 0.82 },
        { id: 2, date: '2026-04-15', amount: 75000, status: 'review', probability: 0.65 },
        { id: 3, date: '2026-03-20', amount: 30000, status: 'approved', probability: 0.91 },
      ]});
    }
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/loans/stats
router.get('/stats', authMiddleware, async (req, res) => {
  try {
    if (mongoose.connection.readyState === 1) {
      const Prediction = require('../models/Prediction');
      const total = await Prediction.countDocuments({ userId: req.user.id });
      const approved = await Prediction.countDocuments({ userId: req.user.id, status: 'approved' });
      const latest = await Prediction.findOne({ userId: req.user.id }).sort({ createdAt: -1 });

      res.json({ success: true, data: {
        total_predictions: total,
        approved_count: approved,
        rejection_count: total - approved,
        approval_rate: total > 0 ? (approved / total * 100).toFixed(1) : 0,
        latest_probability: latest?.result?.ensemble?.probability || null,
      }});
    } else {
      res.json({ success: true, data: { total_predictions: 3, approved_count: 2, rejection_count: 1, approval_rate: '66.7', latest_probability: 0.82 } });
    }
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

module.exports = router;
