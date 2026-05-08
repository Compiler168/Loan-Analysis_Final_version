/**
 * SmartLoan AI+ — Chat Session Model
 */
const mongoose = require('mongoose');

const messageSchema = new mongoose.Schema({
  role: { type: String, enum: ['user', 'assistant'], required: true },
  content: { type: String, required: true },
  intent: String,
  confidence: Number,
  suggestions: [String],
}, { timestamps: true });

const chatSessionSchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, index: true },
  sessionId: { type: String, required: true },
  messages: [messageSchema],
  messageCount: { type: Number, default: 0 },
  lastActivity: { type: Date, default: Date.now },
}, { timestamps: true });

chatSessionSchema.index({ userId: 1, sessionId: 1 }, { unique: true });

module.exports = mongoose.model('ChatSession', chatSessionSchema);
