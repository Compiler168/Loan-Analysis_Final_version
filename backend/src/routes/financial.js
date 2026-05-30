const express = require('express');
const router = express.Router();
const financialController = require('../controllers/FinancialController');
const { authMiddleware } = require('../middleware/auth');

router.get('/dashboard', authMiddleware, financialController.getDashboard);
router.post('/health-score', authMiddleware, financialController.analyzeHealth);
router.post('/risk-analysis', authMiddleware, financialController.analyzeRisk);
router.post('/simulate', authMiddleware, financialController.simulateFinancials);
router.get('/latest', authMiddleware, financialController.getLatestAnalysis);
router.get('/history', authMiddleware, financialController.getAnalysisHistory);

module.exports = router;
