package com.smartloan.ai.ui.prediction;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.TypedValue;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.smartloan.ai.R;
import com.smartloan.ai.databinding.FragmentLoanPredictionBinding;
import com.smartloan.ai.data.models.PredictionResult;
import com.smartloan.ai.utils.ViewUtils;

import java.util.*;

public class LoanPredictionFragment extends Fragment {

    private FragmentLoanPredictionBinding binding;
    private PredictionViewModel viewModel;
    private int currentStep = 0;
    private final String[] stepTitles = {"Personal Info", "Financial Details", "Loan Parameters"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoanPredictionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PredictionViewModel.class);

        setupEmploymentDropdown();
        updateStep();
        setupListeners();
        observeData();
    }

    private void setupEmploymentDropdown() {
        String[] items = {"salaried", "self_employed", "freelancer", "business_owner", "retired"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, items);
        binding.spinnerEmployment.setAdapter(adapter);
    }

    private void updateStep() {
        binding.step1.setVisibility(currentStep == 0 ? View.VISIBLE : View.GONE);
        binding.step2.setVisibility(currentStep == 1 ? View.VISIBLE : View.GONE);
        binding.step3.setVisibility(currentStep == 2 ? View.VISIBLE : View.GONE);
        binding.tvStepTitle.setText(stepTitles[currentStep]);
        binding.btnBack.setVisibility(currentStep > 0 ? View.VISIBLE : View.GONE);
        binding.btnNext.setText(currentStep < 2 ? "Next" : "🧠 Predict");
        buildStepIndicator();
    }

    private void buildStepIndicator() {
        binding.stepIndicator.removeAllViews();
        for (int i = 0; i < 3; i++) {
            TextView circle = new TextView(requireContext());
            circle.setText(String.valueOf(i + 1));
            circle.setTextSize(12f);
            circle.setGravity(android.view.Gravity.CENTER);
            int size = 72;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(4, 0, 4, 0);
            circle.setLayoutParams(lp);
            circle.setBackgroundResource(i <= currentStep ?
                    R.drawable.bg_circle_gradient : R.drawable.bg_muted_rounded);
            circle.setTextColor(i <= currentStep ? Color.WHITE :
                    getResources().getColor(R.color.muted_foreground, null));
            binding.stepIndicator.addView(circle);

            if (i < 2) {
                View line = new View(requireContext());
                LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(60, 4);
                lineLp.gravity = android.view.Gravity.CENTER_VERTICAL;
                line.setLayoutParams(lineLp);
                line.setBackgroundColor(i < currentStep ?
                        getResources().getColor(R.color.primary, null) :
                        getResources().getColor(R.color.muted, null));
                binding.stepIndicator.addView(line);
            }
        }
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> {
            if (currentStep > 0) { currentStep--; updateStep(); }
        });

        binding.btnNext.setOnClickListener(v -> {
            if (currentStep < 2) {
                currentStep++;
                updateStep();
            } else {
                submitPrediction();
            }
        });

        binding.btnRetry.setOnClickListener(v -> {
            binding.resultsContainer.setVisibility(View.GONE);
            binding.emptyState.setVisibility(View.VISIBLE);
            currentStep = 0;
            updateStep();
            binding.scrollView.scrollTo(0, 0);
        });

        binding.btnSaveResult.setOnClickListener(v -> {
            ViewUtils.showSuccessSnackbar(binding.getRoot(), "Prediction result saved successfully!");
        });

        binding.btnDownloadReport.setOnClickListener(v -> {
            ViewUtils.showSuccessSnackbar(binding.getRoot(), "Generating PDF report...");
        });
    }

    private void submitPrediction() {
        Map<String, Object> form = new HashMap<>();
        form.put("age", getInt(binding.etAge));
        form.put("dependents", getInt(binding.etDependents));
        form.put("employment_status", binding.spinnerEmployment.getText().toString());
        form.put("employment_years", getInt(binding.etEmploymentYears));
        form.put("monthly_income", getDouble(binding.etIncome));
        form.put("monthly_expenses", getDouble(binding.etExpenses));
        form.put("credit_score", getInt(binding.etCreditScore));
        form.put("existing_loans", getInt(binding.etExistingLoans));
        form.put("existing_emi", getDouble(binding.etExistingEmi));
        form.put("savings_balance", getDouble(binding.etSavings));
        form.put("loan_amount", getDouble(binding.etLoanAmount));
        form.put("loan_term_months", getInt(binding.etLoanTerm));
        form.put("interest_rate", getDouble(binding.etInterestRate));
        form.put("property_value", getDouble(binding.etPropertyValue));
        form.put("missed_payments_last_year", getInt(binding.etMissedPayments));
        form.put("bankruptcies", getInt(binding.etBankruptcies));

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnNext.setEnabled(false);
        viewModel.predict(form);
    }

    private void observeData() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnNext.setEnabled(!loading);
        });

        viewModel.getResult().observe(getViewLifecycleOwner(), this::showResults);

        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) ViewUtils.showErrorSnackbar(binding.getRoot(), err);
        });
    }

    private void showResults(PredictionResult data) {
        if (data == null) return;
        binding.emptyState.setVisibility(View.GONE);
        binding.resultsContainer.setVisibility(View.VISIBLE);
        binding.resultsContainer.setAlpha(0f);
        binding.resultsContainer.animate().alpha(1f).setDuration(500).start();

        boolean approved = data.ensemble.approved;
        double prob = data.ensemble.probability * 100;

        binding.tvApprovalPercent.setText(String.format("%.0f%%", prob));
        binding.progressApproval.setProgress((int) prob);

        binding.tvApprovalStatus.setText(approved ? "Likely Approved" : "Likely Rejected");

        String riskLevel = "LOW RISK";
        int riskColor = Color.parseColor("#10B981");
        if (prob < 40) {
            riskLevel = "HIGH RISK";
            riskColor = Color.parseColor("#EF4444");
        } else if (prob < 70) {
            riskLevel = "MEDIUM RISK";
            riskColor = Color.parseColor("#F59E0B");
        }

        binding.tvRiskLevel.setText(riskLevel);
        binding.riskDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(riskColor));

        binding.tvConfidence.setText("AI Confidence Score: " + String.format("%.0f%%", data.ensemble.confidenceScore * 100));

        // Model breakdown
        binding.modelsContainer.removeAllViews();
        if (data.models != null) {
            for (Map.Entry<String, PredictionResult.ModelResult> entry : data.models.entrySet()) {
                double modelProb = entry.getValue().probability;
                int modelColor;
                int modelDrawable;

                if (modelProb >= 0.7) {
                    modelColor = Color.parseColor("#10B981"); // Success Green
                    modelDrawable = R.drawable.circular_progress_success;
                } else if (modelProb >= 0.4) {
                    modelColor = Color.parseColor("#F59E0B"); // Warning Orange
                    modelDrawable = R.drawable.circular_progress_warning;
                } else {
                    modelColor = Color.parseColor("#EF4444"); // Danger Red
                    modelDrawable = R.drawable.circular_progress_danger;
                }

                LinearLayout item = new LinearLayout(requireContext());
                item.setOrientation(LinearLayout.VERTICAL);
                item.setGravity(android.view.Gravity.CENTER);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        (int)(95 * getResources().getDisplayMetrics().density),
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(12, 0, 12, 0);
                item.setLayoutParams(lp);

                FrameLayout container = new FrameLayout(requireContext());
                int circleSize = (int)(72 * getResources().getDisplayMetrics().density);
                container.setLayoutParams(new LinearLayout.LayoutParams(circleSize, circleSize));
                
                // Outer glow effect
                android.graphics.drawable.GradientDrawable glow = new android.graphics.drawable.GradientDrawable();
                glow.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                glow.setColor(Color.WHITE);
                container.setBackground(glow);
                container.setElevation(4f);
                container.setPadding(6, 6, 6, 6);

                ProgressBar pb = new ProgressBar(requireContext(), null,
                        android.R.attr.progressBarStyleHorizontal);
                pb.setIndeterminate(false);
                pb.setMax(100);
                pb.setProgress((int) (modelProb * 100));
                pb.setProgressDrawable(getResources().getDrawable(modelDrawable, null));
                pb.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

                TextView percent = new TextView(requireContext());
                percent.setText(String.format("%.0f%%", modelProb * 100));
                percent.setTextSize(13f);
                percent.setTypeface(null, android.graphics.Typeface.BOLD);
                percent.setTextColor(modelColor);
                percent.setGravity(android.view.Gravity.CENTER);
                percent.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

                container.addView(pb);
                container.addView(percent);

                TextView name = new TextView(requireContext());
                String modelName = entry.getKey().replace("_", " ");
                name.setText(modelName.toUpperCase());
                name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f);
                name.setAllCaps(true);
                name.setLetterSpacing(0.08f);
                name.setTextColor(getResources().getColor(R.color.text_secondary, null));
                name.setGravity(android.view.Gravity.CENTER);
                name.setPadding(0, 12, 0, 0);
                name.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));

                item.addView(container);
                item.addView(name);
                binding.modelsContainer.addView(item);
            }
        }

        // Risk factors
        binding.riskContainer.removeAllViews();
        if (data.riskReasons != null) {
            for (PredictionResult.RiskReason r : data.riskReasons) {
                LinearLayout card = new LinearLayout(requireContext());
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(32, 24, 32, 24);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 8, 0, 8);
                card.setLayoutParams(lp);
                card.setBackground(getResources().getDrawable(R.drawable.bg_muted_rounded, null));

                TextView factor = new TextView(requireContext());
                factor.setText(r.factor + ": " + r.message);
                factor.setTextSize(13f);
                factor.setTextColor(getResources().getColor(R.color.text_primary, null));
                factor.setTypeface(null, android.graphics.Typeface.BOLD);

                TextView suggestion = new TextView(requireContext());
                suggestion.setText(r.suggestion);
                suggestion.setTextSize(12f);
                suggestion.setPadding(0, 4, 0, 0);
                suggestion.setTextColor(getResources().getColor(R.color.muted_foreground, null));

                card.addView(factor);
                card.addView(suggestion);
                binding.riskContainer.addView(card);
            }
        }

        // Feature importance (Insight Cards)
        if (data.topFactors != null) {
            buildInsightCards(data.topFactors);
        }

        // Derived Metrics
        if (data.derivedMetrics != null) {
            binding.derivedMetricsContainer.setVisibility(View.VISIBLE);
            binding.tvDtiRatio.setText(String.format("%.0f%%", data.derivedMetrics.dtiRatio * 100));
            binding.tvRequestedEmi.setText(String.format("$%,.0f", data.derivedMetrics.requestedEmi));
            binding.tvSavingsRatio.setText(String.format("%.0f%%", data.derivedMetrics.savingsRatio * 100));
            
            // Apply a small "pop" animation
            binding.derivedMetricsContainer.setAlpha(0f);
            binding.derivedMetricsContainer.setScaleX(0.95f);
            binding.derivedMetricsContainer.animate()
                .alpha(1f)
                .scaleX(1f)
                .setDuration(400)
                .setStartDelay(200)
                .start();
        } else {
            binding.derivedMetricsContainer.setVisibility(View.GONE);
        }

        // Scroll to results with a slight delay for better UX
        binding.scrollView.postDelayed(() -> {
            if (binding != null && binding.resultsContainer.getVisibility() == View.VISIBLE) {
                int scrollTo = binding.resultsContainer.getTop() - 40;
                binding.scrollView.smoothScrollTo(0, scrollTo);
            }
        }, 300);
    }

    private void buildInsightCards(Map<String, Double> factors) {
        binding.insightCardsContainer.removeAllViews();
        
        List<Map.Entry<String, Double>> sortedFactors = new ArrayList<>(factors.entrySet());
        Collections.sort(sortedFactors, (a, b) -> Double.compare(Math.abs(b.getValue()), Math.abs(a.getValue())));

        int count = 0;
        for (Map.Entry<String, Double> entry : sortedFactors) {
            if (count >= 6) break;
            int currentCount = count;
            count++;

            String key = entry.getKey().toLowerCase();
            String label = entry.getKey().replace("_", " ");
            label = label.substring(0, 1).toUpperCase() + label.substring(1);
            double value = entry.getValue();
            boolean isPositive = value >= 0;
            int percentage = (int) (Math.abs(value) * 100);

            // Select Icon (Refined Mapping)
            int iconRes = R.drawable.ic_ai_sparkle;
            if (key.contains("income") || key.contains("savings") || key.contains("amount") || key.contains("expenses") || key.contains("emi")) {
                iconRes = R.drawable.ic_nav_reports; // Financial values
            } else if (key.contains("credit") || key.contains("payment") || key.contains("bankrupt") || key.contains("score")) {
                iconRes = R.drawable.ic_nav_analysis; // Credit behavior
            } else if (key.contains("age") || key.contains("employment") || key.contains("dependents")) {
                iconRes = R.drawable.ic_nav_profile; // Demographic
            } else if (key.contains("property") || key.contains("value")) {
                iconRes = R.drawable.ic_nav_dashboard; // Collateral
            } else if (key.contains("interest") || key.contains("rate") || key.contains("term")) {
                iconRes = R.drawable.ic_nav_simulator; // Loan parameters
            }

            LinearLayout card = new LinearLayout(requireContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_insight_card);
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardLp.setMargins(0, 0, 0, (int)(16 * getResources().getDisplayMetrics().density));
            card.setLayoutParams(cardLp);
            card.setPadding((int)(20 * getResources().getDisplayMetrics().density), 
                           (int)(18 * getResources().getDisplayMetrics().density),
                           (int)(20 * getResources().getDisplayMetrics().density), 
                           (int)(18 * getResources().getDisplayMetrics().density));
            card.setElevation(2f);
            card.setAlpha(0f);
            card.setTranslationY(20f);

            // Content Container (Horizontal)
            LinearLayout contentRow = new LinearLayout(requireContext());
            contentRow.setOrientation(LinearLayout.HORIZONTAL);
            contentRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
            contentRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            // Icon Badge
            FrameLayout iconBadge = new FrameLayout(requireContext());
            int badgeSize = (int)(40 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(badgeSize, badgeSize);
            badgeLp.setMargins(0, 0, (int)(16 * getResources().getDisplayMetrics().density), 0);
            iconBadge.setLayoutParams(badgeLp);
            
            android.graphics.drawable.GradientDrawable badgeBg = new android.graphics.drawable.GradientDrawable();
            badgeBg.setCornerRadius(badgeSize / 2.5f);
            badgeBg.setColor(isPositive ? Color.parseColor("#F0FDF4") : Color.parseColor("#FEF2F2"));
            iconBadge.setBackground(badgeBg);

            ImageView ivIcon = new ImageView(requireContext());
            int iconPadding = (int)(10 * getResources().getDisplayMetrics().density);
            ivIcon.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
            ivIcon.setImageResource(iconRes);
            ivIcon.setColorFilter(isPositive ? Color.parseColor("#10B981") : Color.parseColor("#EF4444"));
            iconBadge.addView(ivIcon);

            // Label and Info
            LinearLayout infoCol = new LinearLayout(requireContext());
            infoCol.setOrientation(LinearLayout.VERTICAL);
            infoCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvLabel = new TextView(requireContext());
            tvLabel.setText(label);
            tvLabel.setTextColor(getResources().getColor(R.color.text_primary, null));
            tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            tvLabel.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
            infoCol.addView(tvLabel);

            TextView tvImpact = new TextView(requireContext());
            tvImpact.setText(isPositive ? "Positive Influence" : "Risk Contribution");
            tvImpact.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            tvImpact.setAllCaps(true);
            tvImpact.setLetterSpacing(0.05f);
            tvImpact.setTextColor(isPositive ? Color.parseColor("#10B981") : Color.parseColor("#EF4444"));
            infoCol.addView(tvImpact);

            // Percentage Value
            TextView tvValue = new TextView(requireContext());
            tvValue.setText((isPositive ? "+" : "-") + percentage + "%");
            tvValue.setTextColor(isPositive ? Color.parseColor("#10B981") : Color.parseColor("#EF4444"));
            tvValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            tvValue.setTypeface(null, android.graphics.Typeface.BOLD);
            
            contentRow.addView(iconBadge);
            contentRow.addView(infoCol);
            contentRow.addView(tvValue);
            card.addView(contentRow);

            // Progress Bar (Slim and Premium)
            FrameLayout progressContainer = new FrameLayout(requireContext());
            LinearLayout.LayoutParams progressContainerLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (int)(6 * getResources().getDisplayMetrics().density));
            progressContainerLp.setMargins(0, (int)(16 * getResources().getDisplayMetrics().density), 0, 0);
            progressContainer.setLayoutParams(progressContainerLp);
            progressContainer.setBackgroundResource(R.drawable.bg_insight_progress_track);

            View progressFill = new View(requireContext());
            FrameLayout.LayoutParams fillLp = new FrameLayout.LayoutParams(0, FrameLayout.LayoutParams.MATCH_PARENT);
            progressFill.setLayoutParams(fillLp);
            
            android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
            shape.setCornerRadius(100f);
            shape.setColor(isPositive ? Color.parseColor("#10B981") : Color.parseColor("#EF4444"));
            progressFill.setBackground(shape);

            progressContainer.addView(progressFill);
            card.addView(progressContainer);

            binding.insightCardsContainer.addView(card);

            // Staggered Animation
            card.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(100 + (currentCount * 100))
                .start();

            card.post(() -> {
                int availableWidth = progressContainer.getWidth();
                int finalWidth = (int) (availableWidth * (percentage / 100.0));
                android.view.ViewGroup.LayoutParams lp1 = progressFill.getLayoutParams();
                lp1.width = 0;
                progressFill.setLayoutParams(lp1);
                
                // Animate progress fill
                android.animation.ValueAnimator anim = android.animation.ValueAnimator.ofInt(0, finalWidth);
                anim.addUpdateListener(valueAnimator -> {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    android.view.ViewGroup.LayoutParams layoutParams = progressFill.getLayoutParams();
                    layoutParams.width = val;
                    progressFill.setLayoutParams(layoutParams);
                });
                anim.setDuration(1000);
                anim.setStartDelay(300 + (currentCount * 100));
                anim.setInterpolator(new android.view.animation.DecelerateInterpolator());
                anim.start();
            });
        }
    }

    private int getInt(com.google.android.material.textfield.TextInputEditText et) {
        try { return Integer.parseInt(et.getText().toString().trim()); } catch (Exception e) { return 0; }
    }

    private double getDouble(com.google.android.material.textfield.TextInputEditText et) {
        try { return Double.parseDouble(et.getText().toString().trim()); } catch (Exception e) { return 0; }
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); binding = null; }
}
