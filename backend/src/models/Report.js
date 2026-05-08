/**
 * SmartLoan AI+ — Report Model
 */
const mongoose = require('mongoose');

const reportSchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, index: true },
  type: { type: String, enum: ['financial_summary', 'loan_analysis', 'risk_report', 'ai_recommendations'], required: true },
  title: { type: String, required: true },
  sections: [mongoose.Schema.Types.Mixed],
  metadata: {
    health_score: Number,
    loan_probability: Number,
    risk_level: String,
    credit_score: Number,
  },
}, { timestamps: true });

reportSchema.index({ createdAt: -1 });

module.exports = mongoose.model('Report', reportSchema);
