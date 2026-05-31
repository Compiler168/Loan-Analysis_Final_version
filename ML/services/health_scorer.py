"""
SmartLoan AI+ — Financial Health Scoring Engine
Custom AI scoring algorithm assessing 5 key financial metrics.
"""
from typing import Dict, Any, List


class HealthScorer:
    """Comprehensive financial health scoring system (0-100)."""

    CATEGORIES = {
        'savings_efficiency': {'weight': 0.22, 'label': 'Savings Efficiency'},
        'spending_discipline': {'weight': 0.20, 'label': 'Spending Discipline'},
        'debt_management': {'weight': 0.25, 'label': 'Debt Management'},
        'credit_standing': {'weight': 0.18, 'label': 'Credit Standing'},
        'financial_stability': {'weight': 0.15, 'label': 'Financial Stability'},
    }

    GRADES = [
        (95, 'A+', 'Exceptional'), (90, 'A', 'Excellent'), (85, 'A-', 'Very Good'),
        (80, 'B+', 'Good'), (75, 'B', 'Above Average'), (70, 'B-', 'Satisfactory'),
        (65, 'C+', 'Fair'), (60, 'C', 'Below Average'), (55, 'C-', 'Needs Work'),
        (50, 'D+', 'Poor'), (40, 'D', 'Very Poor'), (0, 'F', 'Critical'),
    ]

    def _score_savings(self, data: Dict) -> Dict:
        income = max(data.get('monthly_income', 1), 1)
        expenses = data.get('monthly_expenses', 0)
        savings = data.get('savings_balance', 0)
        emi = data.get('existing_emi', 0)
        savings_rate = max((income - expenses - emi) / income, 0)
        months_coverage = savings / max(income, 1)
        rate_score = min(savings_rate / 0.30, 1.0) * 50
        coverage_score = min(months_coverage / 6.0, 1.0) * 50
        score = round(rate_score + coverage_score)
        reasoning = []
        if savings_rate >= 0.25:
            reasoning.append(f"Strong savings rate of {savings_rate:.0%}.")
        elif savings_rate >= 0.10:
            reasoning.append(f"Moderate savings rate of {savings_rate:.0%}. Target 20%+.")
        else:
            reasoning.append(f"Low savings rate of {savings_rate:.0%}. Aim for 15-20%.")
        if months_coverage >= 6:
            reasoning.append(f"Excellent emergency fund: {months_coverage:.1f} months.")
        elif months_coverage >= 3:
            reasoning.append(f"Adequate buffer: {months_coverage:.1f} months. Build to 6.")
        else:
            reasoning.append(f"Emergency fund: {months_coverage:.1f} months. Target 3-6.")
        return {'score': min(score, 100), 'reasoning': reasoning,
                'metrics': {'savings_rate': round(savings_rate, 4), 'months_coverage': round(months_coverage, 1)}}

    def _score_spending(self, data: Dict) -> Dict:
        income = max(data.get('monthly_income', 1), 1)
        expenses = data.get('monthly_expenses', 0)
        ratio = expenses / income
        score = max(0, min(round(100 - (ratio - 0.4) * 200), 100))
        reasoning = []
        if ratio <= 0.50:
            reasoning.append(f"Excellent expense control at {ratio:.0%} of income.")
        elif ratio <= 0.65:
            reasoning.append(f"Moderate spending at {ratio:.0%}. Room for optimization.")
        else:
            reasoning.append(f"High spending at {ratio:.0%}. Review non-essentials.")
        return {'score': score, 'reasoning': reasoning, 'metrics': {'expense_ratio': round(ratio, 4)}}

    def _score_debt(self, data: Dict) -> Dict:
        income = max(data.get('monthly_income', 1), 1)
        emi = data.get('existing_emi', 0)
        loans = data.get('existing_loans', 0)
        missed = data.get('missed_payments_last_year', 0)
        bankr = data.get('bankruptcies', 0)
        dti = (data.get('monthly_expenses', 0) + emi) / income
        score = 100 - max(0, (dti - 0.30) * 150) - max(0, (emi / income - 0.15) * 100) - loans * 5 - missed * 12 - bankr * 30
        score = max(0, min(round(score), 100))
        reasoning = []
        if dti <= 0.35:
            reasoning.append(f"Healthy DTI ratio of {dti:.0%}.")
        elif dti <= 0.50:
            reasoning.append(f"DTI of {dti:.0%} is manageable but elevated.")
        else:
            reasoning.append(f"High DTI of {dti:.0%} — focus on debt reduction.")
        if missed > 0:
            reasoning.append(f"{missed} missed payment(s) last year.")
        return {'score': score, 'reasoning': reasoning, 'metrics': {'dti_ratio': round(dti, 4)}}

    def _score_credit(self, data: Dict) -> Dict:
        cs = data.get('credit_score', 650)
        score = max(0, min(round((cs - 300) / 550 * 100), 100))
        reasoning = []
        if cs >= 750:
            reasoning.append(f"Excellent credit score of {cs}.")
        elif cs >= 700:
            reasoning.append(f"Good credit score of {cs}.")
        elif cs >= 650:
            reasoning.append(f"Fair credit score of {cs}.")
        else:
            reasoning.append(f"Credit score of {cs} needs improvement.")
        return {'score': score, 'reasoning': reasoning, 'metrics': {'credit_score': cs}}

    def _score_stability(self, data: Dict) -> Dict:
        emp_years = data.get('employment_years', 0)
        age = data.get('age', 30)
        dependents = data.get('dependents', 0)
        prop = data.get('property_value', 0)
        income = max(data.get('monthly_income', 1), 1)
        score = min(round(
            min(emp_years / 8, 1.0) * 40 + min(max(age - 20, 0) / 25, 1.0) * 15 +
            max(0, 20 - dependents * 5) + min(prop / (income * 24), 1.0) * 25
        ), 100)
        reasoning = []
        if emp_years >= 5:
            reasoning.append(f"Strong tenure of {emp_years:.0f} years.")
        elif emp_years >= 2:
            reasoning.append(f"Moderate experience of {emp_years:.1f} years.")
        else:
            reasoning.append(f"Limited history ({emp_years:.1f} years).")
        return {'score': score, 'reasoning': reasoning, 'metrics': {'employment_years': emp_years}}

    def _get_grade(self, score: int):
        for threshold, grade, label in self.GRADES:
            if score >= threshold:
                return grade, label
        return 'F', 'Critical'

    def _generate_roadmap(self, categories: Dict) -> list:
        actions_map = {
            'savings_efficiency': ['Auto-transfer to savings on payday', 'Build 6-month emergency fund', 'Open high-yield savings'],
            'spending_discipline': ['Track expenses for 30 days', 'Apply 50/30/20 rule', 'Cancel unused subscriptions'],
            'debt_management': ['Pay more than minimums on highest-interest debt', 'Consider debt consolidation', 'Set up auto-payments'],
            'credit_standing': ['Keep utilization under 30%', 'Check credit report for errors', 'Avoid opening too many accounts'],
            'financial_stability': ['Build professional skills', 'Create diversified income', 'Invest in appreciating assets'],
        }
        roadmap = []
        for k, v in sorted(categories.items(), key=lambda x: x[1]['score']):
            if v['score'] >= 85:
                continue
            roadmap.append({
                'category': self.CATEGORIES[k]['label'], 'current_score': v['score'],
                'priority': 'high' if v['score'] < 50 else 'medium',
                'actions': actions_map.get(k, []), 'potential_gain': min(100 - v['score'], 25)
            })
        return roadmap[:3]

    def calculate_score(self, data: Dict) -> Dict[str, Any]:
        cats = {
            'savings_efficiency': self._score_savings(data),
            'spending_discipline': self._score_spending(data),
            'debt_management': self._score_debt(data),
            'credit_standing': self._score_credit(data),
            'financial_stability': self._score_stability(data),
        }
        total = sum(cats[k]['score'] * self.CATEGORIES[k]['weight'] for k in cats)
        overall = min(round(total), 100)
        grade, grade_label = self._get_grade(overall)
        roadmap = self._generate_roadmap(cats)
        breakdown = [{'category': self.CATEGORIES[k]['label'], 'score': v['score'],
            'weight': self.CATEGORIES[k]['weight'], 'weighted_score': round(v['score'] * self.CATEGORIES[k]['weight'], 1),
            'reasoning': v['reasoning'], 'metrics': v['metrics']} for k, v in cats.items()]
        return {
            'overall_score': overall, 'grade': grade, 'grade_label': grade_label,
            'breakdown': breakdown, 'roadmap': roadmap,
            'summary': f"Your financial health score is {overall}/100 ({grade} — {grade_label}). " +
                (f"Focus on {roadmap[0]['category'].lower()} for biggest impact." if roadmap else "Keep it up!")
        }
