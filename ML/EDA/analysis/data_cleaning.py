import pandas as pd
import numpy as np
import os

def clean_data():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    raw_data_path = os.path.join(base_dir, '..', 'data', 'raw', 'loan_dataset.csv')
    cleaned_data_dir = os.path.join(base_dir, '..', 'data', 'cleaned')
    cleaned_data_path = os.path.join(cleaned_data_dir, 'loan_dataset_cleaned.csv')
    
    os.makedirs(cleaned_data_dir, exist_ok=True)
    
    print(f"Loading raw data from {raw_data_path}...")
    df = pd.read_csv(raw_data_path)
    
    print(f"Initial shape: {df.shape}")
    
    # 1. Drop duplicates
    initial_rows = len(df)
    df.drop_duplicates(inplace=True)
    print(f"Dropped {initial_rows - len(df)} duplicate rows.")
    
    # 2. Handle missing values (though EDA showed 0 missing, it's good practice)
    missing = df.isnull().sum().sum()
    if missing > 0:
        print(f"Handling {missing} missing values...")
        df.fillna(df.median(numeric_only=True), inplace=True)
        # Fill categorical with mode
        for col in df.select_dtypes(include=['object']).columns:
            df[col].fillna(df[col].mode()[0], inplace=True)
    else:
        print("No missing values found.")
        
    # 3. Handle Outliers using 1st and 99th percentiles for continuous financial variables
    financial_cols = ['monthly_income', 'monthly_expenses', 'loan_amount', 'savings_balance']
    for col in financial_cols:
        if col in df.columns:
            p1 = df[col].quantile(0.01)
            p99 = df[col].quantile(0.99)
            df[col] = df[col].clip(lower=p1, upper=p99)
            
    print("Outliers clipped for financial columns at 1st and 99th percentiles.")
    
    # 4. Standardize text features (if any)
    if 'employment_status' in df.columns:
        df['employment_status'] = df['employment_status'].str.lower().str.strip()
        
    # 5. Save the cleaned dataset
    df.to_csv(cleaned_data_path, index=False)
    print(f"Cleaned data saved to {cleaned_data_path}")
    print(f"Final shape: {df.shape}")

if __name__ == "__main__":
    clean_data()
