"""
SmartLoan AI+ — Financial Simulation Engine
Monte Carlo-style what-if scenario projections.
"""
from typing import Dict, Any, List
import math


class SimulationEngine:
    """Interactive financial simulator with projection capabilities."""

    def simulate(self, data: Dict) -> Dict[str, Any]:
        income = data.get('monthly_income', 5000)
        expenses = data.get('monthly_expenses', 2500)
        savings = data.get('savings_balance', 10000)
        emi = data.get('existing_emi', 0)
        loan = data.get('loan_amount', 0)
        term = data.get('loan_term_months', 36)
        rate = data.get('interest_rate', 10.0)
        inc_chg = data.get('income_change_pct', 0) / 100
        exp_chg = data.get('expense_change_pct', 0) / 100
        new_loan = data.get('new_loan_amount', loan)
        months = min(max(data.get('projection_months', 24), 6), 60)

        # Compute EMIs
        mr = rate / 100 / 12
        curr_emi = (loan * mr * (1+mr)**term / ((1+mr)**term - 1)) if (mr > 0 and loan > 0) else (loan / max(term, 1) if loan > 0 else 0)
        new_emi_val = (new_loan * mr * (1+mr)**term / ((1+mr)**term - 1)) if (mr > 0 and new_loan > 0) else (new_loan / max(term, 1) if new_loan > 0 else 0)

        new_income = income * (1 + inc_chg)
        new_expenses = expenses * (1 + exp_chg)

        # Baseline trajectory (no changes)
        baseline = []
        b_savings = savings
        for m in range(months):
            b_monthly = income - expenses - emi - curr_emi
            b_savings += b_monthly
            baseline.append({
                'month': m + 1, 'savings': round(b_savings),
                'net_income': round(income - expenses - emi - curr_emi),
                'cumulative_interest': round(curr_emi * (m + 1) - (loan / max(term, 1)) * (m + 1)) if loan > 0 else 0
            })

        # Projected trajectory (with changes)
        projected = []
        p_savings = savings
        for m in range(months):
            p_monthly = new_income - new_expenses - emi - new_emi_val
            p_savings += p_monthly
            projected.append({
                'month': m + 1, 'savings': round(p_savings),
                'net_income': round(new_income - new_expenses - emi - new_emi_val),
                'cumulative_interest': round(new_emi_val * (m + 1) - (new_loan / max(term, 1)) * (m + 1)) if new_loan > 0 else 0
            })

        # Comparison chart data
        chart_data = [{'month': f'M{i+1}', 'baseline': baseline[i]['savings'], 'projected': projected[i]['savings']} for i in range(months)]

        # Compute deltas
        baseline_final = baseline[-1]['savings']
        projected_final = projected[-1]['savings']
        savings_diff = projected_final - baseline_final
        monthly_diff = (new_income - new_expenses - emi - new_emi_val) - (income - expenses - emi - curr_emi)
        emi_diff = new_emi_val - curr_emi

        # Recommendations
        recs = []
        if inc_chg > 0:
            extra = income * inc_chg
            recs.append({'type': 'positive', 'message': f'Income increase adds ${extra:,.0f}/month. Save at least 50% of the raise.'})
        if exp_chg < 0:
            saved = expenses * abs(exp_chg)
            recs.append({'type': 'positive', 'message': f'Expense reduction saves ${saved:,.0f}/month. Redirect to savings.'})
        if exp_chg > 0:
            extra_exp = expenses * exp_chg
            recs.append({'type': 'warning', 'message': f'Expense increase of ${extra_exp:,.0f}/month. Ensure it stays under 60% of income.'})
        if new_loan > loan:
            recs.append({'type': 'warning', 'message': f'Higher loan increases EMI by ${emi_diff:,.0f}/month. Verify affordability.'})
        elif new_loan < loan:
            recs.append({'type': 'positive', 'message': f'Lower loan reduces EMI by ${abs(emi_diff):,.0f}/month.'})
        if projected_final < 0:
            recs.append({'type': 'danger', 'message': 'Projected savings go negative. Adjust parameters to avoid financial stress.'})
        if not recs:
            recs.append({'type': 'info', 'message': 'Current trajectory looks stable. Consider optimizing savings rate.'})

        return {
            'baseline': {'trajectory': baseline, 'final_savings': baseline_final,
                'monthly_net': round(income - expenses - emi - curr_emi), 'current_emi': round(curr_emi)},
            'projected': {'trajectory': projected, 'final_savings': projected_final,
                'monthly_net': round(new_income - new_expenses - emi - new_emi_val), 'new_emi': round(new_emi_val)},
            'comparison': {'savings_difference': round(savings_diff), 'monthly_difference': round(monthly_diff),
                'emi_difference': round(emi_diff), 'projection_months': months},
            'chart_data': chart_data,
            'recommendations': recs,
            'summary': f"Over {months} months: {'Gain' if savings_diff >= 0 else 'Loss'} of ${abs(savings_diff):,.0f} vs baseline. "
                + f"Monthly net {'improves' if monthly_diff >= 0 else 'decreases'} by ${abs(monthly_diff):,.0f}."
        }
