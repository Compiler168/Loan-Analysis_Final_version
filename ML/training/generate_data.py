"""
SmartLoan AI+ — Synthetic Financial Dataset Generator
Generates realistic loan application data for model training.
"""
import pandas as pd
import numpy as np
import os


def generate_financial_data(n_samples: int = 12000, seed: int = 42) -> pd.DataFrame:
    """Generate a realistic synthetic financial dataset."""
    np.random.seed(seed)

    # --- Base Demographics ---
    age = np.random.randint(21, 65, n_samples)
    dependents = np.random.choice([0, 1, 2, 3, 4, 5], n_samples, p=[0.25, 0.25, 0.22, 0.15, 0.08, 0.05])

    employment_statuses = ['salaried', 'self_employed', 'freelancer', 'business_owner', 'retired']
    employment_probs = [0.45, 0.2, 0.15, 0.12, 0.08]
    employment_status = np.random.choice(employment_statuses, n_samples, p=employment_probs)
    employment_years = np.clip(np.random.exponential(6, n_samples) + 0.5, 0.5, 35).round(1)

    # --- Financial Variables ---
    base_income = np.where(
        employment_status == 'salaried', np.random.lognormal(8.5, 0.5, n_samples),
        np.where(employment_status == 'self_employed', np.random.lognormal(8.7, 0.7, n_samples),
        np.where(employment_status == 'freelancer', np.random.lognormal(8.2, 0.6, n_samples),
        np.where(employment_status == 'business_owner', np.random.lognormal(9.0, 0.8, n_samples),
                 np.random.lognormal(7.8, 0.4, n_samples))))
    )
    monthly_income = np.clip(base_income, 1500, 50000).round(0)

    expense_ratio = np.clip(np.random.beta(3, 4, n_samples) + 0.15, 0.25, 0.85)
    monthly_expenses = (monthly_income * expense_ratio).round(0)

    credit_score = np.clip(
        np.random.normal(680, 80, n_samples) +
        (employment_years * 2) -
        (dependents * 5) +
        np.where(employment_status == 'salaried', 20, -10),
        300, 850
    ).astype(int)

    existing_loans = np.random.choice([0, 1, 2, 3, 4], n_samples, p=[0.3, 0.3, 0.2, 0.12, 0.08])
    existing_emi = np.where(
        existing_loans > 0,
        np.clip(monthly_income * np.random.uniform(0.05, 0.3, n_samples) * existing_loans, 100, 15000),
        0
    ).round(0)

    loan_amount = np.clip(
        monthly_income * np.random.uniform(6, 60, n_samples),
        5000, 500000
    ).round(-2)

    loan_term_months = np.random.choice([12, 24, 36, 48, 60, 84, 120, 180, 240, 360], n_samples,
                                         p=[0.05, 0.1, 0.2, 0.15, 0.15, 0.1, 0.1, 0.05, 0.05, 0.05])
    interest_rate = np.clip(np.random.normal(10, 3, n_samples), 3.5, 24).round(1)

    property_value = np.where(
        loan_amount > 100000,
        loan_amount * np.random.uniform(1.2, 2.5, n_samples),
        0
    ).round(-2)

    savings_balance = np.clip(
        monthly_income * np.random.exponential(4, n_samples),
        0, 200000
    ).round(0)

    missed_payments_last_year = np.random.choice(
        [0, 1, 2, 3, 4, 5], n_samples,
        p=[0.55, 0.2, 0.1, 0.08, 0.04, 0.03]
    )
    bankruptcies = np.random.choice([0, 1, 2], n_samples, p=[0.92, 0.06, 0.02])

    # --- Derived Features ---
    dti_ratio = np.clip((monthly_expenses + existing_emi) / np.maximum(monthly_income, 1), 0, 1.5).round(4)

    monthly_rate = interest_rate / 100 / 12
    requested_emi = np.where(
        monthly_rate > 0,
        loan_amount * monthly_rate * (1 + monthly_rate) ** loan_term_months /
        ((1 + monthly_rate) ** loan_term_months - 1),
        loan_amount / np.maximum(loan_term_months, 1)
    ).round(2)

    total_emi_burden = np.clip(
        (existing_emi + requested_emi) / np.maximum(monthly_income, 1), 0, 2.0
    ).round(4)

    savings_ratio = np.clip(
        (monthly_income - monthly_expenses - existing_emi) / np.maximum(monthly_income, 1),
        -0.5, 1.0
    ).round(4)

    loan_to_income_ratio = (loan_amount / np.maximum(monthly_income * 12, 1)).round(4)

    # --- Target: Approval Decision ---
    score = np.zeros(n_samples)
    score += np.where(credit_score >= 750, 30, np.where(credit_score >= 700, 25, np.where(credit_score >= 650, 18, np.where(credit_score >= 600, 10, 0))))
    score += np.where(dti_ratio <= 0.3, 20, np.where(dti_ratio <= 0.4, 15, np.where(dti_ratio <= 0.5, 8, 0)))
    score += np.where(total_emi_burden <= 0.35, 15, np.where(total_emi_burden <= 0.5, 10, np.where(total_emi_burden <= 0.65, 5, 0)))
    score += np.where(savings_ratio >= 0.3, 10, np.where(savings_ratio >= 0.15, 7, np.where(savings_ratio >= 0.05, 3, 0)))
    score += np.where(employment_years >= 5, 8, np.where(employment_years >= 2, 5, 2))
    score += np.where(missed_payments_last_year == 0, 8, np.where(missed_payments_last_year <= 1, 4, 0))
    score += np.where(bankruptcies == 0, 5, -10)
    score += np.where(loan_to_income_ratio <= 3, 4, np.where(loan_to_income_ratio <= 5, 2, 0))

    noise = np.random.normal(0, 5, n_samples)
    approval_threshold = 45
    approved = (score + noise >= approval_threshold).astype(int)

    # --- Build DataFrame ---
    df = pd.DataFrame({
        'age': age,
        'dependents': dependents,
        'employment_status': employment_status,
        'employment_years': employment_years,
        'monthly_income': monthly_income,
        'monthly_expenses': monthly_expenses,
        'credit_score': credit_score,
        'existing_loans': existing_loans,
        'existing_emi': existing_emi,
        'loan_amount': loan_amount,
        'loan_term_months': loan_term_months,
        'interest_rate': interest_rate,
        'property_value': property_value,
        'savings_balance': savings_balance,
        'missed_payments_last_year': missed_payments_last_year,
        'bankruptcies': bankruptcies,
        'dti_ratio': dti_ratio,
        'requested_emi': requested_emi,
        'total_emi_burden': total_emi_burden,
        'savings_ratio': savings_ratio,
        'loan_to_income_ratio': loan_to_income_ratio,
        'approved': approved
    })

    print(f"✅ Generated {len(df)} samples")
    print(f"   Approval rate: {approved.mean():.1%}")
    print(f"   Features: {len(df.columns) - 1}")
    return df


if __name__ == '__main__':
    base_dir = os.path.dirname(os.path.abspath(__file__))
    data_dir = os.path.join(base_dir, '..', '..', 'eda', 'data', 'raw')
    os.makedirs(data_dir, exist_ok=True)
    df = generate_financial_data(12000)
    path = os.path.join(data_dir, 'loan_dataset.csv')
    df.to_csv(path, index=False)
    print(f"📁 Saved to {path}")
