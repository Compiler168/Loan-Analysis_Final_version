"""
SmartLoan AI+ — Loan Prediction Engine
Ensemble prediction using trained ML models with explainability.
"""
import os
import numpy as np
import joblib
import json
from typing import Dict, Any, List


class PredictionEngine:
    def __init__(self, models_dir: str = None):
        if models_dir is None:
            models_dir = os.path.join(os.path.dirname(__file__), '..', 'models')
        self.models_dir = models_dir
        self.models = {}
        self.scaler = None
        self.label_encoder = None
        self.feature_columns = None
        self.metadata = None
        self._load_models()

    def _load_models(self):
        """Load all trained models and artifacts."""
        try:
            self.models['logistic_regression'] = joblib.load(os.path.join(self.models_dir, 'logistic_regression.pkl'))
            self.models['random_forest'] = joblib.load(os.path.join(self.models_dir, 'random_forest.pkl'))
            self.models['xgboost'] = joblib.load(os.path.join(self.models_dir, 'xgboost_model.pkl'))
            self.scaler = joblib.load(os.path.join(self.models_dir, 'scaler.pkl'))
            self.label_encoder = joblib.load(os.path.join(self.models_dir, 'label_encoder.pkl'))
            self.feature_columns = joblib.load(os.path.join(self.models_dir, 'feature_columns.pkl'))
            with open(os.path.join(self.models_dir, 'model_metadata.json'), 'r', encoding='utf-8') as f:
                self.metadata = json.load(f)
            print("All models loaded successfully")
        except Exception as e:
            print(f"Model loading error: {e}")
            print("   Run training pipeline first: python training/train_models.py")

    def _compute_derived(self, data: Dict) -> Dict:
        """Compute derived financial features."""
        income = max(data.get('monthly_income', 0), 1)
        expenses = data.get('monthly_expenses', 0)
        emi = data.get('existing_emi', 0)
        loan = data.get('loan_amount', 0)
        term = data.get('loan_term_months', 36)
        rate = data.get('interest_rate', 10.0)

        dti = min((expenses + emi) / income, 1.5)
        mr = rate / 100 / 12
        req_emi = (loan * mr * (1 + mr) ** term / ((1 + mr) ** term - 1)) if mr > 0 else loan / max(term, 1)
        total_burden = min((emi + req_emi) / income, 2.0)
        savings = max(min((income - expenses - emi) / income, 1.0), -0.5)
        lti = loan / max(income * 12, 1)

        data['dti_ratio'] = round(dti, 4)
        data['requested_emi'] = round(req_emi, 2)
        data['total_emi_burden'] = round(total_burden, 4)
        data['savings_ratio'] = round(savings, 4)
        data['loan_to_income_ratio'] = round(lti, 4)
        return data

    def _prepare_features(self, data: Dict) -> np.ndarray:
        """Prepare feature vector."""
        data = self._compute_derived(data)
        emp = data.get('employment_status', 'salaried')
        try:
            emp_enc = self.label_encoder.transform([emp])[0]
        except ValueError:
            emp_enc = self.label_encoder.transform(['salaried'])[0]

        features = [
            data.get('age', 35), data.get('dependents', 0), emp_enc,
            data.get('employment_years', 3), data.get('monthly_income', 5000),
            data.get('monthly_expenses', 2500), data.get('credit_score', 650),
            data.get('existing_loans', 0), data.get('existing_emi', 0),
            data.get('loan_amount', 50000), data.get('loan_term_months', 36),
            data.get('interest_rate', 10.0), data.get('property_value', 0),
            data.get('savings_balance', 10000), data.get('missed_payments_last_year', 0),
            data.get('bankruptcies', 0), data['dti_ratio'], data['requested_emi'],
            data['total_emi_burden'], data['savings_ratio'], data['loan_to_income_ratio']
        ]
        return np.array([features])

    def _risk_reasons(self, data: Dict) -> List[Dict]:
        """Generate human-readable risk factors."""
        reasons = []
        cs = data.get('credit_score', 650)
        if cs < 600:
            reasons.append({'factor': 'Credit Score', 'severity': 'high',
                'message': f'Credit score of {cs} is below the recommended 650 threshold.',
                'suggestion': 'Focus on timely payments and reducing credit utilization.'})
        elif cs < 700:
            reasons.append({'factor': 'Credit Score', 'severity': 'medium',
                'message': f'Credit score of {cs} is fair but could be improved.',
                'suggestion': 'Continue building credit history with consistent payments.'})

        dti = data.get('dti_ratio', 0)
        if dti > 0.5:
            reasons.append({'factor': 'Debt-to-Income Ratio', 'severity': 'high',
                'message': f'DTI of {dti:.0%} exceeds safe threshold of 50%.',
                'suggestion': 'Pay down existing debts before applying.'})
        elif dti > 0.35:
            reasons.append({'factor': 'Debt-to-Income Ratio', 'severity': 'medium',
                'message': f'DTI of {dti:.0%} is moderate. Lenders prefer below 35%.',
                'suggestion': 'Reducing monthly obligations strengthens your application.'})

        burden = data.get('total_emi_burden', 0)
        if burden > 0.5:
            reasons.append({'factor': 'EMI Burden', 'severity': 'high',
                'message': f'Total EMI burden of {burden:.0%} is very high.',
                'suggestion': 'Consider a longer loan term or smaller amount.'})

        missed = data.get('missed_payments_last_year', 0)
        if missed > 0:
            reasons.append({'factor': 'Payment History', 'severity': 'high' if missed > 2 else 'medium',
                'message': f'{missed} missed payment(s) last year impact creditworthiness.',
                'suggestion': 'Set up automatic payments to avoid future misses.'})

        if data.get('savings_ratio', 0) < 0.1:
            reasons.append({'factor': 'Savings Rate', 'severity': 'medium',
                'message': 'Low savings rate indicates limited financial buffer.',
                'suggestion': 'Build an emergency fund of 3-6 months expenses.'})

        lti = data.get('loan_to_income_ratio', 0)
        if lti > 5:
            reasons.append({'factor': 'Loan-to-Income', 'severity': 'high',
                'message': f'Requesting {lti:.1f}x annual income is aggressive.',
                'suggestion': 'Reduce loan amount or increase income sources.'})

        if data.get('bankruptcies', 0) > 0:
            reasons.append({'factor': 'Bankruptcy History', 'severity': 'high',
                'message': 'Past bankruptcy significantly impacts eligibility.',
                'suggestion': 'Work with a financial advisor to rebuild credit.'})

        if not reasons:
            reasons.append({'factor': 'Financial Profile', 'severity': 'low',
                'message': 'Your financial profile looks strong.',
                'suggestion': 'Continue maintaining good financial habits.'})
        return reasons

    def predict(self, input_data: Dict) -> Dict[str, Any]:
        """Run ensemble prediction with explainability."""
        if not self.models:
            return {'error': 'Models not loaded. Run training first.'}

        features = self._prepare_features(input_data)
        derived = self._compute_derived(input_data.copy())
        scaled = self.scaler.transform(features)

        weights = self.metadata.get('ensemble_weights', {
            'logistic_regression': 0.2, 'random_forest': 0.4, 'xgboost': 0.4})

        results = {}
        lr_p = float(self.models['logistic_regression'].predict_proba(scaled)[0][1])
        results['logistic_regression'] = {'probability': round(lr_p, 4), 'approved': lr_p >= 0.5, 'weight': weights.get('logistic_regression', 0.2)}

        rf_p = float(self.models['random_forest'].predict_proba(features)[0][1])
        results['random_forest'] = {'probability': round(rf_p, 4), 'approved': rf_p >= 0.5, 'weight': weights.get('random_forest', 0.4)}

        xgb_p = float(self.models['xgboost'].predict_proba(features)[0][1])
        results['xgboost'] = {'probability': round(xgb_p, 4), 'approved': xgb_p >= 0.5, 'weight': weights.get('xgboost', 0.4)}

        ensemble = lr_p * weights.get('logistic_regression', 0.2) + rf_p * weights.get('random_forest', 0.4) + xgb_p * weights.get('xgboost', 0.4)

        rf_imp = dict(zip(self.feature_columns, [float(x) for x in self.models['random_forest'].feature_importances_]))
        top = dict(sorted(rf_imp.items(), key=lambda x: x[1], reverse=True)[:8])

        agreement = sum(1 for m in results.values() if m['approved']) / len(results)
        conf = 'high' if agreement >= 0.66 else ('medium' if agreement >= 0.33 else 'low')

        return {
            'ensemble': {'probability': round(float(ensemble), 4), 'approved': bool(ensemble >= 0.5), 'confidence': conf, 'confidence_score': round(float(agreement), 2)},
            'models': results,
            'risk_reasons': self._risk_reasons(derived),
            'top_factors': top,
            'derived_metrics': {k: derived.get(k) for k in ['dti_ratio', 'total_emi_burden', 'savings_ratio', 'loan_to_income_ratio', 'requested_emi']}
        }

    def get_model_info(self) -> Dict:
        """Return model performance metadata."""
        return self.metadata if self.metadata else {'error': 'Metadata not available'}
