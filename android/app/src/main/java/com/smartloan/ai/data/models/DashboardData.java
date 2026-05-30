package com.smartloan.ai.data.models;

import com.google.gson.annotations.SerializedName;
import com.google.firebase.firestore.PropertyName;
import java.util.List;

/**
 * Dashboard data from GET /api/financial/dashboard
 */
public class DashboardData {

    @PropertyName("loan_probability")
    @SerializedName("loan_probability")
    public double loanProbability;

    @PropertyName("loan_probability")
    public double getLoanProbability() { return loanProbability; }

    @PropertyName("loan_probability")
    public void setLoanProbability(double loanProbability) { this.loanProbability = loanProbability; }

    @PropertyName("health_score")
    @SerializedName("health_score")
    public int healthScore;

    @PropertyName("health_score")
    public int getHealthScore() { return healthScore; }

    @PropertyName("health_score")
    public void setHealthScore(int healthScore) { this.healthScore = healthScore; }

    @PropertyName("risk_level")
    @SerializedName("risk_level")
    public String riskLevel;

    @PropertyName("risk_level")
    public String getRiskLevel() { return riskLevel; }

    @PropertyName("risk_level")
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    @PropertyName("credit_score")
    @SerializedName("credit_score")
    public int creditScore;

    @PropertyName("credit_score")
    public int getCreditScore() { return creditScore; }

    @PropertyName("credit_score")
    public void setCreditScore(int creditScore) { this.creditScore = creditScore; }

    @PropertyName("monthly_savings")
    @SerializedName("monthly_savings")
    public double monthlySavings;

    @PropertyName("monthly_savings")
    public double getMonthlySavings() { return monthlySavings; }

    @PropertyName("monthly_savings")
    public void setMonthlySavings(double monthlySavings) { this.monthlySavings = monthlySavings; }

    @PropertyName("dti_ratio")
    @SerializedName("dti_ratio")
    public double dtiRatio;

    @PropertyName("dti_ratio")
    public double getDtiRatio() { return dtiRatio; }

    @PropertyName("dti_ratio")
    public void setDtiRatio(double dtiRatio) { this.dtiRatio = dtiRatio; }

    @PropertyName("insights")
    @SerializedName("insights")
    public List<Insight> insights;

    @PropertyName("income_vs_expenses")
    @SerializedName("income_vs_expenses")
    public List<IncomeExpense> incomeVsExpenses;

    @PropertyName("financial_growth")
    @SerializedName("financial_growth")
    public List<FinancialGrowth> financialGrowth;

    @PropertyName("risk_radar")
    @SerializedName("risk_radar")
    public List<RiskRadar> riskRadar;

    @PropertyName("emi_forecast")
    @SerializedName("emi_forecast")
    public List<EmiForecast> emiForecast;

    @PropertyName("recent_activity")
    @SerializedName("recent_activity")
    public List<RecentActivity> recentActivity;

    public static class Insight {
        public String type, icon, title, message;
    }

    public static class IncomeExpense {
        public String month;
        public double income, expenses;
    }

    public static class FinancialGrowth {
        public String month;
        public double savings, investments;
        @PropertyName("net_worth")
        @SerializedName("net_worth")
        public double netWorth;
    }

    public static class RiskRadar {
        public String category;
        public float value;
    }

    public static class EmiForecast {
        public String month;
        public double emi, remaining;
    }

    public static class RecentActivity {
        public String type, message, time, result;
    }
}
