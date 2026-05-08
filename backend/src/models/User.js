/**
 * SmartLoan AI+ — User Model
 */
const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const userSchema = new mongoose.Schema({
  name: { type: String, required: true, trim: true },
  email: { type: String, required: true, unique: true, lowercase: true, trim: true },
  password: { type: String, required: true, minlength: 6 },
  role: { type: String, enum: ['user', 'admin'], default: 'user' },
  profile: {
    monthly_income: { type: Number, default: 0 },
    monthly_expenses: { type: Number, default: 0 },
    credit_score: { type: Number, default: 650 },
    employment_status: { type: String, default: 'salaried' },
    employment_years: { type: Number, default: 0 },
    existing_loans: { type: Number, default: 0 },
    existing_emi: { type: Number, default: 0 },
    savings_balance: { type: Number, default: 0 },
    dependents: { type: Number, default: 0 },
    age: { type: Number, default: 30 },
    property_value: { type: Number, default: 0 },
  },
  lastLogin: { type: Date, default: Date.now },
  status: { type: String, enum: ['active', 'inactive', 'suspended'], default: 'active' },
}, { timestamps: true });

// Hash password before saving
userSchema.pre('save', async function () {
  if (!this.isModified('password')) return;
  this.password = await bcrypt.hash(this.password, 10);
});

// Compare password
userSchema.methods.comparePassword = async function (candidatePassword) {
  return bcrypt.compare(candidatePassword, this.password);
};

// Remove password from JSON output
userSchema.methods.toJSON = function () {
  const obj = this.toObject();
  delete obj.password;
  return obj;
};

module.exports = mongoose.model('User', userSchema);
