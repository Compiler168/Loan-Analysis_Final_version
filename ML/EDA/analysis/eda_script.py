import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os
import json
import base64
from io import BytesIO

# Configure paths
data_path = r"c:\Users\Majid Wandar\Desktop\Loan\ml-service\data\loan_dataset.csv"
output_dir = r"C:\Users\Majid Wandar\.gemini\antigravity\artifacts"
os.makedirs(output_dir, exist_ok=True)
report_path = os.path.join(output_dir, "eda_report.md")

# Load data
print("Loading data...")
df = pd.read_csv(data_path)

# Initialize markdown content
md_content = "# Exploratory Data Analysis: Loan Dataset\n\n"

# 1. Basic Information
md_content += "## 1. Basic Dataset Information\n\n"
md_content += f"- **Number of rows:** {df.shape[0]}\n"
md_content += f"- **Number of columns:** {df.shape[1]}\n\n"

# Data Types
md_content += "### Data Types & Missing Values\n\n"
md_content += "| Column | Data Type | Non-Null Count | Missing Values |\n"
md_content += "|---|---|---|---|\n"
for col in df.columns:
    dtype = str(df[col].dtype)
    non_null = df[col].count()
    missing = df[col].isnull().sum()
    md_content += f"| {col} | {dtype} | {non_null} | {missing} |\n"
md_content += "\n"

# Summary Statistics
md_content += "### Summary Statistics (Numerical)\n\n"
desc = df.describe().round(2)
md_content += desc.to_markdown() + "\n\n"

# 2. Categorical Variables
md_content += "## 2. Categorical Variables Analysis\n\n"
categorical_cols = df.select_dtypes(include=['object', 'category']).columns.tolist()

for col in categorical_cols:
    md_content += f"### Distribution of `{col}`\n\n"
    val_counts = df[col].value_counts().reset_index()
    val_counts.columns = [col, 'Count']
    val_counts['Percentage'] = (val_counts['Count'] / len(df) * 100).round(2)
    md_content += val_counts.to_markdown(index=False) + "\n\n"

# 3. Target Variable Analysis
md_content += "## 3. Target Variable Analysis: `approved`\n\n"
if 'approved' in df.columns:
    plt.figure(figsize=(6, 4))
    sns.countplot(data=df, x='approved', palette='viridis')
    plt.title("Distribution of Loan Approvals")
    plt.xlabel("Approved (0 = No, 1 = Yes)")
    plt.ylabel("Count")
    plt.tight_layout()
    target_plot_path = os.path.join(output_dir, "target_dist.png")
    plt.savefig(target_plot_path)
    plt.close()
    
    md_content += f"![Loan Approvals]({target_plot_path})\n\n"
    
    counts = df['approved'].value_counts()
    md_content += f"- **Approved (1):** {counts.get(1, 0)} ({counts.get(1, 0)/len(df)*100:.2f}%)\n"
    md_content += f"- **Rejected (0):** {counts.get(0, 0)} ({counts.get(0, 0)/len(df)*100:.2f}%)\n\n"
    
# 4. Correlation Analysis
md_content += "## 4. Correlation Analysis\n\n"
numeric_df = df.select_dtypes(include=['number'])
if not numeric_df.empty:
    plt.figure(figsize=(12, 10))
    corr = numeric_df.corr()
    sns.heatmap(corr, annot=False, cmap='coolwarm', fmt=".2f", vmin=-1, vmax=1)
    plt.title("Correlation Heatmap of Numeric Features")
    plt.tight_layout()
    corr_plot_path = os.path.join(output_dir, "correlation_heatmap.png")
    plt.savefig(corr_plot_path)
    plt.close()
    
    md_content += f"![Correlation Heatmap]({corr_plot_path})\n\n"

# 5. Key Features Analysis vs Target
md_content += "## 5. Key Features by Approval Status\n\n"

key_features = ['credit_score', 'monthly_income', 'loan_amount', 'dti_ratio']
for feature in key_features:
    if feature in df.columns and 'approved' in df.columns:
        plt.figure(figsize=(8, 5))
        sns.boxplot(data=df, x='approved', y=feature, palette='Set2')
        plt.title(f"{feature} vs Loan Approval")
        plt.xlabel("Approved (0 = No, 1 = Yes)")
        plt.tight_layout()
        plot_path = os.path.join(output_dir, f"{feature}_vs_approved.png")
        plt.savefig(plot_path)
        plt.close()
        md_content += f"![{feature} vs Approval]({plot_path})\n\n"

# Write report
with open(report_path, "w") as f:
    f.write(md_content)

print(f"EDA Report generated at: {report_path}")
