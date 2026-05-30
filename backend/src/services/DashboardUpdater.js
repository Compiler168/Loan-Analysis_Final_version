const { initializeFirebase } = require('../config/firebase');
const User = require('../models/User');
const Prediction = require('../models/Prediction');
const Analysis = require('../models/Analysis');

exports.updateDashboard = async (userId) => {
  try {
    // 1. Fetch latest Prediction (findOne now returns latest by createdAt)
    const latestPrediction = await Prediction.findOne({ userId: userId });
    const latestHealth = await Analysis.findOne({ userId: userId, type: 'health_score' });

    // 2. Extract the EXACT values from the user's latest form submission
    const input = latestPrediction?.input || {};
    
    const income = Number(input.monthly_income) || 0;
    const expenses = Number(input.monthly_expenses) || 0;
    const savings = Number(input.savings_balance) || 0;
    const creditScore = Number(input.credit_score) || 0;
    const loanAmount = Number(input.loan_amount) || 0;
    const loanTerm = Number(input.loan_term_months) || 24;
    const interestRate = Number(input.interest_rate) || 5.0;
    const existingEmi = Number(input.existing_emi) || 0;
    
    // 3. Calculate derived values from ACTUAL user inputs
    const dtiRatio = income > 0 ? (expenses + existingEmi) / income : 0;
    const monthlySavings = income - expenses;

    // 4. Get prediction result values — store as percentage (0-100)
    const rawProbability = latestPrediction?.result?.ensemble?.probability || 0;
    const loanProbability = Math.round(rawProbability * 100); // Convert 0.65 → 65

    // Extract health score from latest analysis (supports `score` or `health_score` fields)
    const healthScore = latestHealth?.result?.score ?? latestHealth?.result?.health_score ?? 0;
    
    // 5. Determine risk level from the actual probability
    let riskLevel = 'N/A';
    if (latestPrediction) {
      if (rawProbability < 0.4) riskLevel = 'High';
      else if (rawProbability < 0.7) riskLevel = 'Medium';
      else riskLevel = 'Low';
    }

    // 6. Calculate actual EMI using standard amortization formula
    const r = (interestRate / 100) / 12;
    let emi = 0;
    if (r === 0) {
      emi = loanAmount / (loanTerm || 1);
    } else {
      emi = loanAmount * r * Math.pow(1 + r, loanTerm) / (Math.pow(1 + r, loanTerm) - 1);
    }

    // EMI Forecast (Amortization for first 3 months)
    let currentBalance = loanAmount;
    const emi_forecast = [];
    for (let i = 1; i <= 3; i++) {
      let interestPayment = currentBalance * r;
      let principalPayment = emi - interestPayment;
      currentBalance -= principalPayment;
      if (currentBalance < 0) currentBalance = 0;
      emi_forecast.push({
        month: `M${i}`,
        emi: Math.round(emi),
        remaining: Math.round(currentBalance)
      });
    }

    // 7. Dynamic Insights based on live calculations
    const insights = [];
    if (income > 0 && monthlySavings > 0) {
      insights.push({ type: "info", icon: "💰", title: "Savings Goal", message: `You are saving $${Math.round(monthlySavings)} monthly.` });
    } else if (income > 0) {
      insights.push({ type: "warning", icon: "⚠️", title: "Negative Cash Flow", message: "Your expenses exceed your income." });
    }
    
    if (dtiRatio > 0.4) {
      insights.push({ type: "warning", icon: "📊", title: "High DTI Alert", message: `Your DTI is ${(dtiRatio * 100).toFixed(1)}%. Try to keep it below 40%.` });
    } else if (income > 0) {
      insights.push({ type: "success", icon: "✅", title: "Healthy DTI", message: "Your Debt-to-Income ratio is in a healthy range." });
    }

    if (creditScore > 0 && creditScore < 600) {
      insights.push({ type: "warning", icon: "📉", title: "Low Credit Score", message: `Your credit score is ${creditScore}. Aim for 700+.` });
    } else if (creditScore >= 700) {
      insights.push({ type: "success", icon: "⭐", title: "Strong Credit", message: `Your credit score of ${creditScore} is excellent!` });
    }

    // 8. Risk Radar (scaled 0-100 for the chart)
    const risk_radar = [
      { category: "Credit", value: Math.min(Math.round((creditScore / 850) * 100), 100) },
      { category: "DTI", value: Math.round(Math.max(1 - dtiRatio, 0) * 100) },
      { category: "Savings", value: loanAmount > 0 ? Math.min(Math.round((savings / loanAmount) * 100), 100) : 0 },
      { category: "Income", value: Math.min(Math.round((income / 20000) * 100), 100) },
      { category: "Approval", value: loanProbability }
    ];

    // 9. Income vs Expenses chart
    const income_vs_expenses = [
      { month: "M-2", income: Math.round(income * 0.95), expenses: Math.round(expenses * 0.9) },
      { month: "M-1", income: Math.round(income * 0.98), expenses: Math.round(expenses * 1.02) },
      { month: "Current", income: Math.round(income), expenses: Math.round(expenses) }
    ];

    // 10. Financial Growth chart
    const surplus = Math.max(monthlySavings, 0);
    const financial_growth = [
      { month: "M-2", savings: Math.round(Math.max(savings - surplus * 2, 0)), investments: 0, net_worth: Math.round(Math.max(savings - surplus * 2, 0)) },
      { month: "M-1", savings: Math.round(Math.max(savings - surplus, 0)), investments: 0, net_worth: Math.round(Math.max(savings - surplus, 0)) },
      { month: "Current", savings: Math.round(savings), investments: 0, net_worth: Math.round(savings) }
    ];

    // 11. Assemble the dashboard document
    const dashboardData = {
      loan_probability: loanProbability,
      health_score: healthScore,
      risk_level: riskLevel,
      credit_score: creditScore,
      monthly_savings: monthlySavings,
      dti_ratio: dtiRatio,
      insights,
      income_vs_expenses,
      financial_growth,
      risk_radar,
      emi_forecast,
      recent_activity: [
        { type: "prediction", message: "Loan Eligibility Updated", time: "Just now", result: `${loanProbability}%` },
        { type: "analysis", message: "Credit Score Recorded", time: "Just now", result: `${creditScore}` }
      ],
      updatedAt: new Date().toISOString()
    };

    console.log(`[DashboardUpdater] Writing dashboard for user ${userId}:`, JSON.stringify({
      loan_probability: loanProbability,
      credit_score: creditScore,
      monthly_savings: monthlySavings,
      dti_ratio: dtiRatio,
      risk_level: riskLevel,
      health_score: healthScore,
      hasLatestPrediction: !!latestPrediction,
      inputFields: Object.keys(input)
    }));

    const db = await initializeFirebase();
    await db.collection('dashboards').doc(userId).set(dashboardData);
    // Save health score as a separate analysis document for DashboardUpdater retrieval
    await db.collection('analyses').doc(`${userId}_health_score`).set({
      userId: userId,
      type: 'health_score',
      result: { score: healthScore },
      createdAt: new Date().toISOString()
    }, { merge: true });
    console.log(`[DashboardUpdater] Dashboard and health score updated for user ${userId}`);
  } catch (err) {
    console.error('[DashboardUpdater] Error updating dashboard for user:', userId, err);
  }
};

