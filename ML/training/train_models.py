"""
SmartLoan AI+ — Model Training Pipeline
Trains Logistic Regression, Random Forest, and XGBoost ensemble.
"""
import pandas as pd
import numpy as np
import os
import json
import joblib
from datetime import datetime
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.preprocessing import StandardScaler, LabelEncoder
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import (
    accuracy_score, precision_score, recall_score, f1_score,
    roc_auc_score, classification_report
)

try:
    from xgboost import XGBClassifier
    HAS_XGBOOST = True
except ImportError:
    HAS_XGBOOST = False
    print("⚠️  XGBoost not available — using GradientBoosting fallback")
    from sklearn.ensemble import GradientBoostingClassifier


def load_and_preprocess(data_path: str):
    """Load dataset and prepare for training."""
    df = pd.read_csv(data_path)
    le = LabelEncoder()
    df['employment_encoded'] = le.fit_transform(df['employment_status'])
    feature_cols = [
        'age', 'dependents', 'employment_encoded', 'employment_years',
        'monthly_income', 'monthly_expenses', 'credit_score',
        'existing_loans', 'existing_emi', 'loan_amount',
        'loan_term_months', 'interest_rate', 'property_value',
        'savings_balance', 'missed_payments_last_year', 'bankruptcies',
        'dti_ratio', 'requested_emi', 'total_emi_burden',
        'savings_ratio', 'loan_to_income_ratio'
    ]
    return df[feature_cols], df['approved'], feature_cols, le


def train_and_evaluate(X_train, X_test, y_train, y_test, model, name: str):
    """Train a model and return metrics."""
    print(f"\n{'='*50}\nTraining: {name}\n{'='*50}")
    model.fit(X_train, y_train)
    y_pred = model.predict(X_test)
    y_proba = model.predict_proba(X_test)[:, 1]
    cv = cross_val_score(model, X_train, y_train, cv=5, scoring='accuracy')
    metrics = {
        'accuracy': float(accuracy_score(y_test, y_pred)),
        'precision': float(precision_score(y_test, y_pred)),
        'recall': float(recall_score(y_test, y_pred)),
        'f1_score': float(f1_score(y_test, y_pred)),
        'roc_auc': float(roc_auc_score(y_test, y_proba)),
        'cv_mean': float(cv.mean()),
        'cv_std': float(cv.std()),
    }
    print(f"\n📊 {name} Results:")
    for k, v in metrics.items():
        print(f"   {k}: {v:.4f}")
    print(f"\n{classification_report(y_test, y_pred)}")
    return model, metrics


def get_feature_importance(model, cols):
    """Extract feature importance."""
    if hasattr(model, 'feature_importances_'):
        imp = model.feature_importances_
    elif hasattr(model, 'coef_'):
        imp = np.abs(model.coef_[0])
    else:
        return {}
    return dict(sorted(
        zip(cols, [float(x) for x in imp]),
        key=lambda x: x[1], reverse=True
    ))


def main():
    print("=" * 60)
    print("SmartLoan AI+ — Model Training Pipeline")
    print("=" * 60)

    base_dir = os.path.dirname(os.path.abspath(__file__))
    data_path = os.path.join(base_dir, '..', '..', 'eda', 'data', 'raw', 'loan_dataset.csv')
    models_dir = os.path.join(base_dir, '..', 'models')
    os.makedirs(models_dir, exist_ok=True)

    if not os.path.exists(data_path):
        print("📁 Dataset not found. Generating...")
        from generate_data import generate_financial_data
        os.makedirs(os.path.dirname(data_path), exist_ok=True)
        df = generate_financial_data(12000)
        df.to_csv(data_path, index=False)
        print(f"✅ Generated at {data_path}")

    X, y, feature_cols, label_encoder = load_and_preprocess(data_path)
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42, stratify=y)

    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)

    print(f"\n📊 Dataset: {len(X)} samples | Train: {len(X_train)} | Test: {len(X_test)}")
    print(f"   Features: {len(feature_cols)} | Approval rate: {y.mean():.1%}")

    # Model 1: Logistic Regression
    lr, lr_m = train_and_evaluate(X_train_scaled, X_test_scaled, y_train, y_test,
        LogisticRegression(max_iter=1000, C=1.0, solver='lbfgs', random_state=42), "Logistic Regression")
    lr_imp = get_feature_importance(lr, feature_cols)

    # Model 2: Random Forest
    rf, rf_m = train_and_evaluate(X_train, X_test, y_train, y_test,
        RandomForestClassifier(n_estimators=200, max_depth=15, min_samples_split=5,
            min_samples_leaf=2, random_state=42, n_jobs=-1), "Random Forest")
    rf_imp = get_feature_importance(rf, feature_cols)

    # Model 3: XGBoost
    if HAS_XGBOOST:
        xgb_model = XGBClassifier(n_estimators=200, max_depth=8, learning_rate=0.1,
            subsample=0.8, colsample_bytree=0.8, random_state=42,
            use_label_encoder=False, eval_metric='logloss')
        xgb_name = "XGBoost"
    else:
        xgb_model = GradientBoostingClassifier(n_estimators=200, max_depth=8,
            learning_rate=0.1, subsample=0.8, random_state=42)
        xgb_name = "Gradient Boosting"
    xgb, xgb_m = train_and_evaluate(X_train, X_test, y_train, y_test, xgb_model, xgb_name)
    xgb_imp = get_feature_importance(xgb, feature_cols)

    # Save all artifacts
    print(f"\n{'='*50}\nSaving Models\n{'='*50}")
    joblib.dump(lr, os.path.join(models_dir, 'logistic_regression.pkl'))
    joblib.dump(rf, os.path.join(models_dir, 'random_forest.pkl'))
    joblib.dump(xgb, os.path.join(models_dir, 'xgboost_model.pkl'))
    joblib.dump(scaler, os.path.join(models_dir, 'scaler.pkl'))
    joblib.dump(label_encoder, os.path.join(models_dir, 'label_encoder.pkl'))
    joblib.dump(feature_cols, os.path.join(models_dir, 'feature_columns.pkl'))

    metadata = {
        'trained_at': datetime.now().isoformat(),
        'n_samples': len(X),
        'n_features': len(feature_cols),
        'feature_columns': feature_cols,
        'models': {
            'logistic_regression': {'metrics': lr_m, 'feature_importance': lr_imp, 'requires_scaling': True},
            'random_forest': {'metrics': rf_m, 'feature_importance': rf_imp, 'requires_scaling': False},
            xgb_name.lower().replace(' ', '_'): {'metrics': xgb_m, 'feature_importance': xgb_imp, 'requires_scaling': False}
        },
        'ensemble_weights': {'logistic_regression': 0.2, 'random_forest': 0.4, 'xgboost': 0.4},
        'employment_classes': list(label_encoder.classes_)
    }
    with open(os.path.join(models_dir, 'model_metadata.json'), 'w') as f:
        json.dump(metadata, f, indent=2)

    print("\n✅ All models saved!")
    print(f"\n{'Model':<25} {'Accuracy':>10} {'F1':>10} {'AUC':>10}")
    print("-" * 55)
    print(f"{'Logistic Regression':<25} {lr_m['accuracy']:>10.4f} {lr_m['f1_score']:>10.4f} {lr_m['roc_auc']:>10.4f}")
    print(f"{'Random Forest':<25} {rf_m['accuracy']:>10.4f} {rf_m['f1_score']:>10.4f} {rf_m['roc_auc']:>10.4f}")
    print(f"{xgb_name:<25} {xgb_m['accuracy']:>10.4f} {xgb_m['f1_score']:>10.4f} {xgb_m['roc_auc']:>10.4f}")
    print("=" * 55)


if __name__ == '__main__':
    main()
