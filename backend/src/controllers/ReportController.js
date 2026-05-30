/**
 * SmartLoan AI+ — Report Controller (Firestore)
 */
const Report = require('../models/Report');

exports.generateReport = async (req, res) => {
  try {
    const { type = 'financial_summary' } = req.body;
    
    const reportData = {
      userId: req.user.id,
      type,
      title: type === 'loan_analysis' ? 'Loan Analysis Report' :
             type === 'risk_report' ? 'Risk Assessment Report' :
             type === 'ai_recommendations' ? 'AI Recommendations Report' : 'Financial Summary Report',
      sections: [
        { title: 'Executive Summary', content: 'Comprehensive analysis of financial position based on AI metrics.' },
        { title: 'Key Metrics', items: [
          { label: 'Financial Health Score', value: '72/100', status: 'good' },
          { label: 'Loan Approval Probability', value: '78.5%', status: 'good' }
        ]},
        { title: 'AI Recommendations', items: [
          { label: 'Savings', value: 'Increase emergency fund' },
          { label: 'Credit', value: 'Keep utilization below 30%' }
        ]}
      ],
      metadata: { health_score: 72, loan_probability: 78.5 }
    };

    const savedReport = await Report.create(reportData);
    res.status(201).json({ success: true, data: savedReport });
  } catch (err) {
    res.status(500).json({ success: false, error: 'Failed to generate report' });
  }
};

exports.getHistory = async (req, res) => {
  try {
    const reports = await Report.find({ userId: req.user.id });
    const history = reports.map(r => ({ id: r.id, type: r.type, title: r.title, date: (r.createdAt.toDate ? r.createdAt.toDate() : new Date(r.createdAt)).toISOString().split('T')[0] }));
    res.json({ success: true, data: history });
  } catch (err) {
    res.status(500).json({ success: false, error: 'Failed to fetch history' });
  }
};

exports.getReportDetail = async (req, res) => {
  try {
    const report = await Report.findOne({ _id: req.params.id, userId: req.user.id });
    if (!report) return res.status(404).json({ success: false, error: 'Report not found' });
    res.json({ success: true, data: report });
  } catch (err) {
    res.status(500).json({ success: false, error: 'Failed to fetch report' });
  }
};

exports.deleteReport = async (req, res) => {
  try {
    await Report.findOneAndDelete({ _id: req.params.id, userId: req.user.id });
    res.json({ success: true, message: 'Report deleted' });
  } catch (err) {
    res.status(500).json({ success: false, error: 'Delete failed' });
  }
};
