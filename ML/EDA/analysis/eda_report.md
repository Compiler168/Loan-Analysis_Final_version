# Exploratory Data Analysis: Loan Dataset

## 1. Basic Dataset Information

- **Number of rows:** 12000
- **Number of columns:** 22

### Data Types & Missing Values

| Column | Data Type | Non-Null Count | Missing Values |
|---|---|---|---|
| age | int64 | 12000 | 0 |
| dependents | int64 | 12000 | 0 |
| employment_status | str | 12000 | 0 |
| employment_years | float64 | 12000 | 0 |
| monthly_income | float64 | 12000 | 0 |
| monthly_expenses | float64 | 12000 | 0 |
| credit_score | int64 | 12000 | 0 |
| existing_loans | int64 | 12000 | 0 |
| existing_emi | float64 | 12000 | 0 |
| loan_amount | float64 | 12000 | 0 |
| loan_term_months | int64 | 12000 | 0 |
| interest_rate | float64 | 12000 | 0 |
| property_value | float64 | 12000 | 0 |
| savings_balance | float64 | 12000 | 0 |
| missed_payments_last_year | int64 | 12000 | 0 |
| bankruptcies | int64 | 12000 | 0 |
| dti_ratio | float64 | 12000 | 0 |
| requested_emi | float64 | 12000 | 0 |
| total_emi_burden | float64 | 12000 | 0 |
| savings_ratio | float64 | 12000 | 0 |
| loan_to_income_ratio | float64 | 12000 | 0 |
| approved | int64 | 12000 | 0 |

### Summary Statistics (Numerical)

|       |      age |   dependents |   employment_years |   monthly_income |   monthly_expenses |   credit_score |   existing_loans |   existing_emi |   loan_amount |   loan_term_months |   interest_rate |   property_value |   savings_balance |   missed_payments_last_year |   bankruptcies |   dti_ratio |   requested_emi |   total_emi_burden |   savings_ratio |   loan_to_income_ratio |   approved |
|:------|---------:|-------------:|-------------------:|-----------------:|-------------------:|---------------:|-----------------:|---------------:|--------------:|-------------------:|----------------:|-----------------:|------------------:|----------------------------:|---------------:|------------:|----------------:|-------------------:|----------------:|-----------------------:|-----------:|
| count | 12000    |     12000    |           12000    |         12000    |           12000    |       12000    |         12000    |       12000    |         12000 |           12000    |        12000    |   12000          |          12000    |                    12000    |       12000    |    12000    |        12000    |           12000    |        12000    |               12000    |   12000    |
| mean  |    42.46 |         1.7  |               6.53 |          6220.04 |            3549.21 |         686.76 |             1.36 |        1477.56 |        184892 |              85.97 |           10.02 |  304390          |          23953    |                        0.96 |           0.1  |        0.8  |         5029.1  |               1.01 |            0.2  |                   2.66 |       0.54 |
| std   |    12.59 |         1.43 |               6.03 |          5177.85 |            3212.18 |          81.12 |             1.24 |        2218.98 |        137497 |              83.75 |            2.95 |  302859          |          32008    |                        1.35 |           0.36 |        0.29 |         5637.11 |               0.55 |            0.29 |                   1.26 |       0.5  |
| min   |    21    |         0    |               0.5  |          1500    |             375    |         408    |             0    |           0    |          9400 |              12    |            3.5  |       0          |              1    |                        0    |           0    |        0.25 |          104.4  |               0.05 |           -0.5  |                   0.5  |       0    |
| 25%   |    32    |         1    |               2.2  |          3110.75 |            1620    |         632    |             0    |           0    |         78500 |              36    |            8    |       0          |           4837.75 |                        0    |           0    |        0.59 |         1549.52 |               0.57 |            0.02 |                   1.59 |       0    |
| 50%   |    42    |         1    |               4.6  |          4784.5  |            2628.5  |         688    |             1    |         747    |        143600 |              48    |           10    |  260150          |          12649    |                        0    |           0    |        0.77 |         3228.72 |               0.93 |            0.23 |                   2.64 |       1    |
| 75%   |    53    |         3    |               8.9  |          7487.75 |            4348    |         743    |             2    |        1879.5  |        253900 |             120    |           12    |  467300          |          29501.2  |                        2    |           0    |        0.98 |         6336.8  |               1.41 |            0.41 |                   3.72 |       1    |
| max   |    64    |         5    |              35    |         50000    |           41919    |         850    |             4    |       15000    |        500000 |             360    |           22.2  |       1.2488e+06 |         200000    |                        5    |           2    |        1.5  |        45460.1  |               2    |            0.75 |                   5    |       1    |

## 2. Categorical Variables Analysis

### Distribution of `employment_status`

| employment_status   |   Count |   Percentage |
|:--------------------|--------:|-------------:|
| salaried            |    5382 |        44.85 |
| self_employed       |    2322 |        19.35 |
| freelancer          |    1866 |        15.55 |
| business_owner      |    1461 |        12.18 |
| retired             |     969 |         8.08 |

## 3. Target Variable Analysis: `approved`

![Loan Approvals](/C:/Users/Majid%20Wandar/.gemini/antigravity/brain/7caf465a-1438-4a16-be3a-097bf3135a66/target_dist.png)

- **Approved (1):** 6473 (53.94%)
- **Rejected (0):** 5527 (46.06%)

## 4. Correlation Analysis

![Correlation Heatmap](/C:/Users/Majid%20Wandar/.gemini/antigravity/brain/7caf465a-1438-4a16-be3a-097bf3135a66/correlation_heatmap.png)

## 5. Key Features by Approval Status

![credit_score vs Approval](/C:/Users/Majid%20Wandar/.gemini/antigravity/brain/7caf465a-1438-4a16-be3a-097bf3135a66/credit_score_vs_approved.png)

![monthly_income vs Approval](/C:/Users/Majid%20Wandar/.gemini/antigravity/brain/7caf465a-1438-4a16-be3a-097bf3135a66/monthly_income_vs_approved.png)

![loan_amount vs Approval](/C:/Users/Majid%20Wandar/.gemini/antigravity/brain/7caf465a-1438-4a16-be3a-097bf3135a66/loan_amount_vs_approved.png)

![dti_ratio vs Approval](/C:/Users/Majid%20Wandar/.gemini/antigravity/brain/7caf465a-1438-4a16-be3a-097bf3135a66/dti_ratio_vs_approved.png)
