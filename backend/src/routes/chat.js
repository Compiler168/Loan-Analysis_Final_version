/**
 * SmartLoan AI+ — Chat Routes (MongoDB)
 */
const express = require('express');
const router = express.Router();
const axios = require('axios');
const multer = require('multer');
const mongoose = require('mongoose');
const { authMiddleware } = require('../middleware/auth');

const ML_URL = process.env.ML_SERVICE_URL || (process.env.VERCEL_URL ? `https://${process.env.VERCEL_URL}/api/ml` : 'http://localhost:8000');
const upload = multer({ storage: multer.memoryStorage(), limits: { fileSize: 10 * 1024 * 1024 } });

router.post('/message', authMiddleware, async (req, res) => {
  try {
    const r = await axios.post(`${ML_URL}/chat`, req.body);
    const data = r.data.data;

    // Save chat to MongoDB
    if (mongoose.connection.readyState === 1) {
      const ChatSession = require('../models/ChatSession');
      const sessionId = req.body.session_id || 'default';

      await ChatSession.findOneAndUpdate(
        { userId: req.user.id, sessionId },
        {
          $push: {
            messages: {
              $each: [
                { role: 'user', content: req.body.message, intent: data.intent },
                { role: 'assistant', content: data.response, intent: data.intent, confidence: data.confidence, suggestions: data.suggestions }
              ]
            }
          },
          $inc: { messageCount: 2 },
          $set: { lastActivity: new Date() }
        },
        { upsert: true, new: true }
      );
    }

    res.json({ success: true, data });
  } catch (err) { res.status(502).json({ success: false, error: 'AI service unavailable' }); }
});

// GET /api/chat/history
router.get('/history', authMiddleware, async (req, res) => {
  try {
    if (mongoose.connection.readyState === 1) {
      const ChatSession = require('../models/ChatSession');
      const sessions = await ChatSession.find({ userId: req.user.id })
        .sort({ lastActivity: -1 }).limit(10)
        .select('sessionId messageCount lastActivity');
      res.json({ success: true, data: sessions });
    } else {
      res.json({ success: true, data: [] });
    }
  } catch (err) { res.status(500).json({ success: false, error: err.message }); }
});

// GET /api/chat/session/:sessionId
router.get('/session/:sessionId', authMiddleware, async (req, res) => {
  try {
    if (mongoose.connection.readyState === 1) {
      const ChatSession = require('../models/ChatSession');
      const session = await ChatSession.findOne({ userId: req.user.id, sessionId: req.params.sessionId });
      res.json({ success: true, data: session?.messages || [] });
    } else {
      res.json({ success: true, data: [] });
    }
  } catch (err) { res.status(500).json({ success: false, error: err.message }); }
});

router.post('/analyze-document', authMiddleware, upload.single('file'), async (req, res) => {
  try {
    if (!req.file) return res.status(400).json({ success: false, error: 'No file uploaded' });
    const FormData = (await import('form-data')).default || require('form-data');
    const form = new FormData();
    form.append('file', req.file.buffer, { filename: req.file.originalname, contentType: req.file.mimetype });
    const r = await axios.post(`${ML_URL}/analyze-document`, form, { headers: form.getHeaders() });
    res.json({ success: true, data: r.data.data });
  } catch (err) { res.status(502).json({ success: false, error: 'Document analysis failed' }); }
});

module.exports = router;
