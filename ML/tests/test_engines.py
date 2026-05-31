"""SmartLoan AI+ — ML Engine Tests"""
import pytest
import sys, os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from services.health_scorer import HealthScorer
from services.risk_analyzer import RiskAnalyzer
from services.nlp_engine import NLPEngine
from services.simulation_engine import SimulationEngine
from services.document_analyzer import DocumentAnalyzer

SAMPLE = {
    'age': 35, 'dependents': 1, 'employment_status': 'salaried',
    'employment_years': 5, 'monthly_income': 7500, 'monthly_expenses': 3000,
    'credit_score': 720, 'existing_loans': 1, 'existing_emi': 500,
    'loan_amount': 50000, 'loan_term_months': 36, 'interest_rate': 10.0,
    'property_value': 150000, 'savings_balance': 25000,
    'missed_payments_last_year': 0, 'bankruptcies': 0
}

def test_health_scorer():
    hs = HealthScorer()
    r = hs.calculate_score(SAMPLE)
    assert 'overall_score' in r
    assert 0 <= r['overall_score'] <= 100
    assert r['grade'] in ['A+','A','A-','B+','B','B-','C+','C','C-','D+','D','F']
    assert len(r['breakdown']) == 5

def test_risk_analyzer():
    ra = RiskAnalyzer()
    r = ra.analyze(SAMPLE)
    assert 'overall_risk' in r
    assert r['risk_level'] in ['low', 'moderate', 'high', 'critical']
    assert len(r['dimensions']) >= 5

def test_nlp_engine():
    nlp = NLPEngine()
    r = nlp.chat("How can I improve my credit score?")
    assert 'response' in r
    assert len(r['response']) > 0
    assert r['intent'] == 'credit_score_help'
    r2 = nlp.chat("Hello!")
    assert r2['intent'] == 'greeting'

def test_simulation():
    se = SimulationEngine()
    r = se.simulate({**SAMPLE, 'income_change_pct': 10, 'expense_change_pct': -5,
                     'new_loan_amount': 50000, 'projection_months': 24})
    assert 'baseline' in r and 'projected' in r
    assert len(r['chart_data']) == 24
    assert r['projected']['final_savings'] >= r['baseline']['final_savings']

def test_document_analyzer():
    da = DocumentAnalyzer()
    text = "Salary Slip\nEmployee: John\nGross Pay: $7,500\nDeductions: $1,200\nNet Pay: $6,300"
    r = da.analyze(text)
    assert r['document_type'] == 'salary_slip'
    assert len(r['extracted']['incomes']) > 0

if __name__ == '__main__':
    pytest.main([__file__, '-v'])
