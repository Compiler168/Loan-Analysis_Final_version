"""
SmartLoan AI+ — Risk Analysis Engine
Multi-dimensional financial risk assessment with heatmap data.
"""
from typing import Dict, Any, List


class RiskAnalyzer:
    """Financial risk analysis with severity classification and preventive suggestions."""

    def _compute_risk_dimensions(self, data: Dict) -> List[Dict]:
        """Compute risk scores across multiple dimensions."""
        income = max(data.get('monthly_income', 1), 1)
        expenses = data.get('monthly_expenses', 0)
        emi = data.get('existing_emi', 0)
        cs = data.get('credit_score', 650)
        loan = data.get('loan_amount', 0)
        term = data.get('loan_term_months', 36)
        rate = data.get('interest_rate', 10.0)
        savings = data.get('savings_balance', 0)
        missed = data.get('missed_payments_last_year', 0)
        bankr = data.get('bankruptcies', 0)
        loans = data.get('existing_loans', 0)

        dti = (expenses + emi) / income
        mr = rate / 100 / 12
        req_emi = (loan * mr * (1 + mr) ** term / ((1 + mr) ** term - 1)) if mr > 0 else loan / max(term, 1)
        total_burden = (emi + req_emi) / income
        months_buffer = savings / max(income, 1)

        dimensions = []

        # 1. DTI Risk
        dti_risk = min(dti / 0.6 * 100, 100)
        sev = 'critical' if dti > 0.6 else 'high' if dti > 0.45 else 'medium' if dti > 0.35 else 'low'
        dimensions.append({'dimension': 'Debt-to-Income', 'score': round(dti_risk), 'severity': sev,
            'value': f'{dti:.0%}', 'threshold': '< 35%',
            'message': f'DTI ratio is {dti:.0%}.' + (' Dangerously high.' if dti > 0.6 else ''),
            'suggestions': ['Reduce monthly obligations', 'Increase income streams', 'Pay off small debts first']})

        # 2. EMI Burden
        burden_risk = min(total_burden / 0.6 * 100, 100)
        sev = 'critical' if total_burden > 0.6 else 'high' if total_burden > 0.45 else 'medium' if total_burden > 0.3 else 'low'
        dimensions.append({'dimension': 'EMI Burden', 'score': round(burden_risk), 'severity': sev,
            'value': f'{total_burden:.0%}', 'threshold': '< 40%',
            'message': f'Total EMI burden is {total_burden:.0%} of income.',
            'suggestions': ['Consider longer loan term', 'Reduce loan amount', 'Consolidate existing EMIs']})

        # 3. Credit Risk
        credit_risk = max(0, min((750 - cs) / 4.5, 100))
        sev = 'critical' if cs < 550 else 'high' if cs < 620 else 'medium' if cs < 700 else 'low'
        dimensions.append({'dimension': 'Credit Risk', 'score': round(credit_risk), 'severity': sev,
            'value': str(cs), 'threshold': '> 700',
            'message': f'Credit score of {cs}.',
            'suggestions': ['Pay all bills on time', 'Reduce credit utilization', 'Dispute report errors']})

        # 4. Savings Adequacy
        sav_risk = max(0, min((6 - months_buffer) / 6 * 100, 100))
        sev = 'critical' if months_buffer < 1 else 'high' if months_buffer < 2 else 'medium' if months_buffer < 4 else 'low'
        dimensions.append({'dimension': 'Savings Adequacy', 'score': round(sav_risk), 'severity': sev,
            'value': f'{months_buffer:.1f} months', 'threshold': '> 6 months',
            'message': f'Emergency fund covers {months_buffer:.1f} months.',
            'suggestions': ['Build emergency fund', 'Automate savings', 'Cut discretionary spending']})

        # 5. Default Probability
        default_score = min(missed * 15 + bankr * 35 + max(0, (dti - 0.4) * 50) + max(0, (650 - cs) / 5), 100)
        sev = 'critical' if default_score > 70 else 'high' if default_score > 45 else 'medium' if default_score > 20 else 'low'
        dimensions.append({'dimension': 'Default Probability', 'score': round(default_score), 'severity': sev,
            'value': f'{default_score:.0f}%', 'threshold': '< 20%',
            'message': f'Estimated default risk: {default_score:.0f}%.',
            'suggestions': ['Set up payment reminders', 'Maintain financial discipline', 'Build credit history']})

        # 6. Overspending Risk
        expense_ratio = expenses / income
        overspend_risk = min(max(0, (expense_ratio - 0.5) * 200), 100)
        sev = 'high' if expense_ratio > 0.75 else 'medium' if expense_ratio > 0.6 else 'low'
        dimensions.append({'dimension': 'Overspending', 'score': round(overspend_risk), 'severity': sev,
            'value': f'{expense_ratio:.0%}', 'threshold': '< 60%',
            'message': f'Expense-to-income ratio is {expense_ratio:.0%}.',
            'suggestions': ['Track daily expenses', 'Use the 50/30/20 budget rule', 'Identify spending leaks']})

        return dimensions

    def analyze(self, data: Dict) -> Dict[str, Any]:
        """Perform comprehensive risk analysis."""
        dims = self._compute_risk_dimensions(data)
        avg = sum(d['score'] for d in dims) / len(dims)

        if avg >= 70:
            level, color = 'critical', '#EF4444'
        elif avg >= 50:
            level, color = 'high', '#F97316'
        elif avg >= 30:
            level, color = 'moderate', '#EAB308'
        else:
            level, color = 'low', '#22C55E'

        warnings = [d for d in dims if d['severity'] in ('high', 'critical')]
        heatmap = [{'name': d['dimension'], 'value': d['score'], 'severity': d['severity']} for d in dims]

        top_suggestions = []
        for d in sorted(dims, key=lambda x: x['score'], reverse=True)[:3]:
            for s in d['suggestions'][:1]:
                top_suggestions.append({'area': d['dimension'], 'action': s, 'priority': d['severity']})

        return {
            'overall_risk': round(avg), 'risk_level': level, 'risk_color': color,
            'dimensions': dims, 'heatmap': heatmap,
            'warnings': [{'dimension': w['dimension'], 'severity': w['severity'], 'message': w['message']} for w in warnings],
            'top_suggestions': top_suggestions,
            'summary': f"Overall risk level: {level.upper()} ({avg:.0f}/100). " +
                (f"{len(warnings)} area(s) require attention." if warnings else "All areas within safe limits.")
        }
