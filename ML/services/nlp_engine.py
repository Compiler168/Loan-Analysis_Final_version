"""
SmartLoan AI+ — NLP Chatbot Engine
Custom NLP with intent classification, context memory, and financial reasoning.
"""
import re
from typing import Dict, Any, List, Optional
from datetime import datetime

INTENT_DATA = {
    "loan_inquiry": {
        "patterns": ["can i get a loan", "am i eligible", "loan eligibility", "qualify for loan",
                      "apply for loan", "loan approval", "will i be approved"],
        "category": "loan"
    },
    "credit_score_help": {
        "patterns": ["improve credit score", "credit score tips", "fix credit", "better credit",
                      "increase credit score", "credit repair", "what is credit score"],
        "category": "credit"
    },
    "budget_advice": {
        "patterns": ["budget tips", "save money", "reduce expenses", "spending advice",
                      "how to budget", "budgeting help", "financial planning"],
        "category": "budget"
    },
    "loan_explain_approval": {
        "patterns": ["why approved", "approval reason", "explain approval", "approval factors"],
        "category": "loan"
    },
    "loan_explain_rejection": {
        "patterns": ["why rejected", "rejection reason", "why denied", "explain rejection", "not approved"],
        "category": "loan"
    },
    "risk_explanation": {
        "patterns": ["what are my risks", "explain risk", "risk factors", "financial risks", "am i at risk"],
        "category": "risk"
    },
    "emi_calculation": {
        "patterns": ["calculate emi", "monthly payment", "emi amount", "loan installment",
                      "what would emi be", "payment schedule"],
        "category": "loan"
    },
    "debt_management": {
        "patterns": ["manage debt", "pay off debt", "debt strategy", "too much debt",
                      "debt consolidation", "reduce debt"],
        "category": "debt"
    },
    "savings_advice": {
        "patterns": ["how to save", "savings tips", "emergency fund", "save more",
                      "savings goal", "investment advice"],
        "category": "savings"
    },
    "financial_health": {
        "patterns": ["financial health", "financially healthy", "financial status",
                      "financial checkup", "financial score"],
        "category": "health"
    },
    "interest_rate": {
        "patterns": ["interest rate", "current rates", "best rate", "rate comparison", "lowest rate"],
        "category": "loan"
    },
    "dti_explain": {
        "patterns": ["debt to income", "dti ratio", "what is dti", "explain dti", "dti too high"],
        "category": "education"
    },
    "simulation_request": {
        "patterns": ["what if", "simulate", "if i earn more", "scenario", "future projection"],
        "category": "simulation"
    },
    "greeting": {
        "patterns": ["hello", "hi", "hey", "good morning", "good evening", "greetings"],
        "category": "general"
    },
    "goodbye": {
        "patterns": ["bye", "goodbye", "see you", "thanks bye", "that's all", "exit"],
        "category": "general"
    },
    "help": {
        "patterns": ["help", "what can you do", "features", "options", "guide me"],
        "category": "general"
    },
    "report_request": {
        "patterns": ["generate report", "financial report", "summary report", "pdf report"],
        "category": "report"
    },
    "loan_comparison": {
        "patterns": ["compare loans", "which loan is better", "loan options", "best loan", "loan types"],
        "category": "loan"
    }
}


class NLPEngine:
    """Custom NLP chatbot with intent classification and financial reasoning."""

    def __init__(self):
        self.context_memory: Dict[str, List] = {}
        self.pattern_index = []
        for intent, data in INTENT_DATA.items():
            for p in data['patterns']:
                self.pattern_index.append({'pattern': p.lower(), 'intent': intent, 'words': set(p.lower().split())})

    def _classify_intent(self, message: str) -> tuple:
        msg = message.lower().strip()
        words = set(re.findall(r'\w+', msg))
        best_intent, best_score = 'general_query', 0
        for entry in self.pattern_index:
            if entry['pattern'] in msg:
                score = len(entry['pattern']) / max(len(msg), 1) + 0.5
                if score > best_score:
                    best_score, best_intent = score, entry['intent']
                continue
            overlap = words & entry['words']
            if overlap:
                score = len(overlap) / max(len(entry['words']), 1)
                if score > best_score:
                    best_score, best_intent = score, entry['intent']
        return best_intent, min(best_score, 1.0)

    def _update_context(self, sid: str, role: str, content: str, intent: str = None):
        if sid not in self.context_memory:
            self.context_memory[sid] = []
        self.context_memory[sid].append({'role': role, 'content': content, 'intent': intent, 'ts': datetime.now().isoformat()})
        if len(self.context_memory[sid]) > 20:
            self.context_memory[sid] = self.context_memory[sid][-20:]

    def _respond(self, intent: str, ud: Optional[Dict], msg: str) -> Dict:
        parts, suggestions = [], []

        if intent == 'loan_inquiry':
            if ud:
                inc = ud.get('monthly_income', 0)
                cs = ud.get('credit_score', 0)
                dti = ud.get('dti_ratio', 0)
                parts.append(f"**Loan Eligibility Analysis**\n\n- Income: **${inc:,.0f}**")
                parts.append(f"- Credit Score: **{cs}** {'✅' if cs >= 700 else '⚠️'}")
                parts.append(f"- DTI: **{dti:.0%}** {'✅' if dti <= 0.35 else '⚠️'}")
                if cs >= 700 and dti <= 0.4:
                    parts.append("\n**Strong eligibility** — good chances of approval.")
                else:
                    parts.append("\n**Moderate eligibility** — some improvements could help.")
                suggestions = ["Check loan prediction", "Improve credit score", "Risk level"]
            else:
                parts.append("Visit the **Loan Prediction** page for a detailed ML-powered analysis.")
                suggestions = ["Go to Loan Prediction", "Budget help", "What do I need"]

        elif intent == 'credit_score_help':
            parts.append("**How to Improve Your Credit Score** 📈\n")
            parts.append("1. **Pay bills on time** — 35% of your score")
            parts.append("2. **Keep utilization below 30%**")
            parts.append("3. **Don't close old accounts**")
            parts.append("4. **Limit new applications**")
            parts.append("5. **Check for errors** on your report")
            parts.append("6. **Diversify credit types**")
            if ud and ud.get('credit_score'):
                parts.append(f"\nYour score: **{ud['credit_score']}**. {'Great!' if ud['credit_score'] >= 750 else 'Focus on tips above.'}")
            suggestions = ["Financial health", "DTI ratio", "Budget advice"]

        elif intent == 'budget_advice':
            parts.append("**Smart Budgeting — 50/30/20 Rule** 💰\n")
            parts.append("- **50%** → Needs (rent, food, utilities)")
            parts.append("- **30%** → Wants (entertainment, dining)")
            parts.append("- **20%** → Savings & debt repayment")
            if ud:
                inc = ud.get('monthly_income', 0)
                parts.append(f"\nFor **${inc:,.0f}/month**: Needs ${inc*0.5:,.0f} | Wants ${inc*0.3:,.0f} | Save ${inc*0.2:,.0f}")
            suggestions = ["Savings advice", "Reduce debt", "Simulate changes"]

        elif intent == 'risk_explanation':
            parts.append("**Financial Risk Analysis** ⚠️\n")
            if ud:
                dti = ud.get('dti_ratio', 0)
                cs = ud.get('credit_score', 650)
                sav = ud.get('savings_balance', 0)
                inc = max(ud.get('monthly_income', 1), 1)
                parts.append(f"- DTI: {dti:.0%} — {'Low ✅' if dti < 0.35 else 'Moderate ⚠️' if dti < 0.5 else 'High ❌'}")
                parts.append(f"- Credit: {cs} — {'Good ✅' if cs >= 700 else 'Fair ⚠️' if cs >= 600 else 'Poor ❌'}")
                parts.append(f"- Buffer: {sav/inc:.1f} months — {'Strong ✅' if sav/inc >= 6 else 'Build more ⚠️'}")
            else:
                parts.append("Provide financial data or visit **Risk Analyzer** page.")
            suggestions = ["Reduce risk", "Credit tips", "Savings advice"]

        elif intent == 'emi_calculation':
            parts.append("**EMI Calculator** 🧮\n")
            parts.append("EMI = P × r × (1+r)^n / ((1+r)^n - 1)\n")
            if ud and ud.get('loan_amount'):
                p = ud['loan_amount']
                r = ud.get('interest_rate', 10) / 100 / 12
                n = ud.get('loan_term_months', 36)
                emi = p * r * (1+r)**n / ((1+r)**n - 1) if r > 0 else p/n
                parts.append(f"Loan **${p:,.0f}**: EMI **${emi:,.0f}** | Total **${emi*n:,.0f}** | Interest **${emi*n-p:,.0f}**")
            suggestions = ["Change loan term", "Can I afford this", "Compare loans"]

        elif intent == 'debt_management':
            parts.append("**Debt Management Strategies** 📉\n")
            parts.append("**Avalanche**: Pay highest-interest first (saves most)")
            parts.append("**Snowball**: Pay smallest balance first (motivation)\n")
            parts.append("Tips: Never miss minimums, consider balance transfer, negotiate rates")
            suggestions = ["Calculate EMI", "Budget advice", "Risk level"]

        elif intent == 'savings_advice':
            parts.append("**Smart Savings Strategy** 💎\n")
            parts.append("1. Auto-transfer on payday\n2. Build 6-month emergency fund")
            parts.append("3. Use high-yield accounts\n4. Cut spending leaks\n5. Increase income")
            if ud:
                pot = ud.get('monthly_income', 0) - ud.get('monthly_expenses', 0)
                parts.append(f"\nPotential monthly savings: **${max(0, pot):,.0f}**")
            suggestions = ["Simulate savings", "Budget advice", "Investment tips"]

        elif intent == 'financial_health':
            parts.append("**Financial Health Check** 🏥\n")
            parts.append("Key areas: 💰 Savings | 📊 Spending | 💳 Debt | 📈 Payments | 🏠 Assets")
            parts.append("\nVisit **Financial Analysis** for your detailed 0-100 score.")
            suggestions = ["Check health score", "Budget advice", "Risk analysis"]

        elif intent == 'simulation_request':
            parts.append("**Financial Simulator** 🔮\n")
            parts.append("Scenarios available:")
            parts.append("- 📈 Income increase\n- 📉 Expense reduction\n- 💰 Extra savings\n- 🏦 Loan impact")
            parts.append("\nVisit **Simulation Engine** for interactive analysis!")
            suggestions = ["Go to Simulator", "What if salary increases", "Future projection"]

        elif intent == 'dti_explain':
            parts.append("**Debt-to-Income (DTI) Ratio** 📊\n")
            parts.append("DTI = (Monthly Debts + Expenses) / Monthly Income\n")
            parts.append("- **< 35%**: Excellent — most lenders approve\n- **35-50%**: Moderate — some concern")
            parts.append("- **> 50%**: High — significant risk factor")
            suggestions = ["My DTI ratio", "Reduce debt", "Loan eligibility"]

        elif intent == 'greeting':
            parts.append("Hello! 👋 I'm your **SmartLoan AI Financial Advisor**. How can I help you today?")
            suggestions = ["Check loan eligibility", "Financial health check", "Help me budget"]

        elif intent == 'goodbye':
            parts.append("Thank you for chatting! 👋 Remember, good financial habits lead to great outcomes.")
            suggestions = []

        elif intent == 'help':
            parts.append("I can help with:\n- 🏦 Loan eligibility\n- 📊 Financial health\n- ⚠️ Risk analysis")
            parts.append("- 💰 Budget & savings\n- 🧮 EMI calculations\n- 🔮 Simulations")
            suggestions = ["Loan eligibility", "Budget advice", "Credit score tips", "Risk analysis"]

        else:
            parts.append("I understand you're asking about finances. Here's what I can help with:\n")
            parts.append("🏦 Loans | 📊 Health Score | ⚠️ Risk | 💰 Budget | 🧮 EMI | 🔮 Simulations")
            suggestions = ["Loan eligibility", "Budget advice", "Risk analysis"]

        return {'text': '\n'.join(parts), 'suggestions': suggestions}

    def chat(self, message: str, session_id: str = 'default', user_data: Optional[Dict] = None) -> Dict[str, Any]:
        intent, confidence = self._classify_intent(message)
        self._update_context(session_id, 'user', message, intent)
        resp = self._respond(intent, user_data, message)
        self._update_context(session_id, 'assistant', resp['text'], intent)
        return {
            'response': resp['text'], 'intent': intent, 'confidence': round(confidence, 2),
            'suggestions': resp['suggestions'], 'session_id': session_id,
            'timestamp': datetime.now().isoformat()
        }

    def get_history(self, sid: str) -> List:
        return self.context_memory.get(sid, [])

    def clear_history(self, sid: str):
        self.context_memory.pop(sid, None)
