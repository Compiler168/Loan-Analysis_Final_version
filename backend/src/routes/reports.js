/**
 * SmartLoan AI+ — Report Routes (MongoDB)
 */
const express = require('express');
const router = express.Router();
const mongoose = require('mongoose');
const { authMiddleware } = require('../middleware/auth');

router.post('/generate', authMiddleware, async (req, res) => {
  const { type = 'financial_summary' } = req.body;
  const report = {
    type,
    generated_at: new Date().toISOString(),
    title: type === 'loan_analysis' ? 'Loan Analysis Report' :
           type === 'risk_report' ? 'Risk Assessment Report' :
           type === 'ai_recommendations' ? 'AI Recommendations Report' : 'Financial Summary Report',
    sections: [
      { title: 'Executive Summary', content: 'This report provides a comprehensive analysis of your financial position based on AI-driven metrics.' },
      { title: 'Key Metrics', items: [
        { label: 'Financial Health Score', value: '72/100', status: 'good' },
        { label: 'Loan Approval Probability', value: '78.5%', status: 'good' },
        { label: 'Risk Level', value: 'Moderate', status: 'warning' },
        { label: 'DTI Ratio', value: '38%', status: 'warning' },
        { label: 'Credit Score', value: '720', status: 'good' }
      ]},
      { title: 'AI Recommendations', items: [
        { label: 'Savings', value: 'Increase emergency fund to 6 months of expenses' },
        { label: 'Debt', value: 'Pay off highest-interest loans first (avalanche method)' },
        { label: 'Credit', value: 'Keep credit utilization below 30%' },
        { label: 'Income', value: 'Explore additional income streams for financial resilience' }
      ]},
      { title: 'Conclusion', content: 'Your financial health is satisfactory with room for improvement. Focus on reducing DTI ratio and building emergency savings.' }
    ]
  };

  // Save report to MongoDB
  if (mongoose.connection.readyState === 1) {
    try {
      const Report = require('../models/Report');
      const saved = await Report.create({
        userId: req.user.id,
        type: report.type,
        title: report.title,
        sections: report.sections,
        metadata: { health_score: 72, loan_probability: 78.5, risk_level: 'moderate', credit_score: 720 }
      });
      report.id = saved._id;
    } catch (err) {
      report.id = Date.now().toString();
    }
  } else {
    report.id = Date.now().toString();
  }

  res.json({ success: true, data: report });
});

router.get('/history', authMiddleware, async (req, res) => {
  try {
    if (mongoose.connection.readyState === 1) {
      const Report = require('../models/Report');
      const reports = await Report.find({ userId: req.user.id })
        .sort({ createdAt: -1 }).limit(20)
        .select('type title createdAt');

      const history = reports.map(r => ({
        id: r._id,
        type: r.type,
        title: r.title,
        date: r.createdAt.toISOString().split('T')[0],
      }));
      res.json({ success: true, data: history });
    } else {
      res.json({ success: true, data: [
        { id: '1', type: 'financial_summary', title: 'May 2026 Summary', date: '2026-05-01' },
        { id: '2', type: 'loan_analysis', title: 'Loan Analysis', date: '2026-04-28' },
        { id: '3', type: 'risk_report', title: 'Q1 Risk Report', date: '2026-04-15' }
      ]});
    }
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

module.exports = router;
