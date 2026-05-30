/**
 * SmartLoan AI+ — Financial Controller (Firestore)
 */
const axios = require('axios');
const Analysis = require('../models/Analysis');
const { updateDashboard } = require('../services/DashboardUpdater');

const ML_URL = process.env.ML_SERVICE_URL || 'http://localhost:8000';

exports.analyzeHealth = async (req, res) => {
  try {
    const response = await axios.post(`${ML_URL}/health-score`, req.body);
    const analysisData = response.data.data;

    const analysis = await Analysis.create({
      userId: req.user.id,
      type: 'health_score',
      input: req.body,
      result: analysisData
    });

    // Update dashboard synchronously to ensure UI reads latest data immediately
    await updateDashboard(req.user.id);

    res.status(201).json({ success: true, data: analysis.result });
  } catch (err) {
    res.status(500).json({ success: false, error: 'Health scoring service unavailable' });
  }
};

exports.analyzeRisk = async (req, res) => {
  try {
    const response = await axios.post(`${ML_URL}/risk-analysis`, req.body);
    const analysisData = response.data.data;

    const analysis = await Analysis.create({
      userId: req.user.id,
      type: 'risk_analysis',
      input: req.body,
      result: analysisData
    });

    // Update dashboard synchronously to ensure UI reads latest data immediately
    await updateDashboard(req.user.id);

    res.status(201).json({ success: true, data: analysis.result });
  } catch (err) {
    res.status(500).json({ success: false, error: 'Risk analysis service unavailable' });
  }
};

exports.simulateFinancials = async (req, res) => {
  try {
    const response = await axios.post(`${ML_URL}/simulate`, req.body);
    const analysisData = response.data.data;

    const analysis = await Analysis.create({
      userId: req.user.id,
      type: 'simulation',
      input: req.body,
      result: analysisData
    });

    // Update dashboard synchronously to ensure UI reads latest data immediately
    await updateDashboard(req.user.id);

    res.status(201).json({ success: true, data: analysis.result });
  } catch (err) {
    res.status(500).json({ success: false, error: 'Simulation service unavailable' });
  }
};

exports.getLatestAnalysis = async (req, res) => {
  try {
    const latest = await Analysis.findOne({ userId: req.user.id });
    if (!latest) return res.status(404).json({ success: false, error: 'No analysis found' });
    res.json({ success: true, data: latest });
  } catch (err) {
    res.status(500).json({ success: false, error: 'Fetch failed' });
  }
};

exports.getAnalysisHistory = async (req, res) => {
  try {
    const history = await Analysis.find({ userId: req.user.id });

    const formattedHistory = history.slice(0, 10).map(h => ({
      date: (h.createdAt.toDate ? h.createdAt.toDate() : new Date(h.createdAt)).toISOString().split('T')[0],
      score: h.result?.score || 0
    }));

    res.json({ success: true, data: formattedHistory });
  } catch (err) {
    res.status(500).json({ success: false, error: 'Fetch failed' });
  }
};

exports.getDashboard = async (req, res) => {
  try {
    const { initializeFirebase } = require('../config/firebase');
    const db = await initializeFirebase();
    const doc = await db.collection('dashboards').doc(req.user.id).get();

    if (doc.exists) {
      return res.json({ success: true, data: doc.data() });
    }

    // If dashboard document doesn't exist yet, force generation
    await updateDashboard(req.user.id);
    const updatedDoc = await db.collection('dashboards').doc(req.user.id).get();
    
    if (updatedDoc.exists) {
      res.json({ success: true, data: updatedDoc.data() });
    } else {
      res.status(404).json({ success: false, error: 'Dashboard data not generated' });
    }
  } catch (err) {
    console.error("Dashboard error:", err);
    res.status(500).json({ success: false, error: 'Dashboard data fetch failed' });
  }
};
