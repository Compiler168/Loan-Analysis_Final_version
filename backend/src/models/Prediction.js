/**
 * SmartLoan AI+ — Prediction Model
 */
const mongoose = require('mongoose');

const predictionSchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, index: true },
  input: {
    age: Number,
    dependents: Number,
    employment_status: String,
    employment_years: Number,
    monthly_income: Number,
    monthly_expenses: Number,
    credit_score: Number,
    existing_loans: Number,
    existing_emi: Number,
    loan_amount: Number,
    loan_term_months: Number,
    interest_rate: Number,
    property_value: Number,
    savings_balance: Number,
    missed_payments_last_year: Number,
    bankruptcies: Number,
  },
  result: {
    ensemble: {
      probability: Number,
      approved: Boolean,
      confidence: String,
      confidence_score: Number,
    },
    models: mongoose.Schema.Types.Mixed,
    risk_reasons: [mongoose.Schema.Types.Mixed],
    top_factors: mongoose.Schema.Types.Mixed,
    derived_metrics: mongoose.Schema.Types.Mixed,
  },
  status: { type: String, enum: ['approved', 'rejected', 'review'], default: 'review' },
}, { timestamps: true });

predictionSchema.index({ createdAt: -1 });

module.exports = mongoose.model('Prediction', predictionSchema);
