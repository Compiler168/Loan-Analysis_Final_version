/**
 * SmartLoan AI+ — Analysis History Model
 */
const mongoose = require('mongoose');

const analysisSchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, index: true },
  type: { type: String, enum: ['health_score', 'risk_analysis', 'simulation'], required: true },
  input: mongoose.Schema.Types.Mixed,
  result: mongoose.Schema.Types.Mixed,
}, { timestamps: true });

analysisSchema.index({ createdAt: -1 });

module.exports = mongoose.model('Analysis', analysisSchema);
