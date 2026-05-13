package com.smartloan.ai.ui.analysis;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.card.MaterialCardView;
import com.smartloan.ai.R;
import com.smartloan.ai.databinding.FragmentAnalysisBinding;
import com.smartloan.ai.data.models.*;
import com.smartloan.ai.utils.ViewUtils;
import java.util.*;

public class AnalysisFragment extends Fragment {
    private FragmentAnalysisBinding binding;
    private AnalysisViewModel viewModel;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAnalysisBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AnalysisViewModel.class);

        binding.btnHealth.setOnClickListener(v -> {
            showLoadingState();
            viewModel.runHealthScore(buildFormData());
        });
        binding.btnRisk.setOnClickListener(v -> {
            showLoadingState();
            viewModel.runRiskAnalysis(buildFormData());
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), l ->
                binding.progressBar.setVisibility(l ? View.VISIBLE : View.GONE));
        viewModel.getHealthResult().observe(getViewLifecycleOwner(), this::showHealthResult);
        viewModel.getRiskResult().observe(getViewLifecycleOwner(), this::showRiskResult);
        viewModel.getError().observe(getViewLifecycleOwner(), e -> {
            if (e != null) ViewUtils.showErrorSnackbar(binding.getRoot(), e);
        });
    }

    private void showLoadingState() {
        binding.resultsContainer.removeAllViews();
        binding.progressBar.setVisibility(View.VISIBLE);
        // Smooth scroll to where results will appear
        binding.resultsContainer.post(() -> {
            View scrollView = binding.getRoot().findViewById(R.id.scrollView);
            if (scrollView instanceof ScrollView) {
                ((ScrollView) scrollView).smoothScrollTo(0, binding.resultsContainer.getTop());
            }
        });
    }

    private Map<String, Object> buildFormData() {
        Map<String, Object> m = new HashMap<>();
        m.put("monthly_income", getD(binding.etIncome)); m.put("monthly_expenses", getD(binding.etExpenses));
        m.put("savings_balance", getD(binding.etSavings)); m.put("credit_score", getI(binding.etCreditScore));
        m.put("existing_emi", getD(binding.etEmi)); m.put("existing_loans", 1);
        m.put("employment_years", 5); m.put("missed_payments_last_year", 0);
        m.put("bankruptcies", 0); m.put("age", 35); m.put("dependents", 1);
        m.put("property_value", 150000); m.put("loan_amount", 50000);
        m.put("loan_term_months", 36); m.put("interest_rate", 10);
        return m;
    }

    private void showHealthResult(HealthScoreResult data) {
        if (data == null) return;
        binding.resultsContainer.removeAllViews();
        binding.resultsContainer.setAlpha(0f);
        binding.resultsContainer.setTranslationY(30f);

        // --- Premium Header Section ---
        MaterialCardView headerCard = new MaterialCardView(requireContext());
        headerCard.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        headerCard.setRadius(dpToPx(28));
        headerCard.setCardElevation(dpToPx(8));
        headerCard.setStrokeWidth(0);

        LinearLayout headerInner = new LinearLayout(requireContext());
        headerInner.setOrientation(LinearLayout.VERTICAL);
        headerInner.setBackgroundResource(R.drawable.bg_prediction_result_header);
        headerInner.setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24));

        // AI Label Row
        RelativeLayout labelRow = new RelativeLayout(requireContext());
        TextView aiLabel = new TextView(requireContext());
        aiLabel.setText(getString(R.string.ai_health_score_engine));
        aiLabel.setTextColor(Color.parseColor("#CCFFFFFF"));
        aiLabel.setTextSize(10);
        aiLabel.setAllCaps(true);
        aiLabel.setLetterSpacing(0.15f);
        aiLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        labelRow.addView(aiLabel);

        ImageView sparkle = new ImageView(requireContext());
        sparkle.setImageResource(R.drawable.ic_ai_sparkle);
        sparkle.setColorFilter(Color.WHITE);
        RelativeLayout.LayoutParams sparkleLp = new RelativeLayout.LayoutParams(dpToPx(20), dpToPx(20));
        sparkleLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        labelRow.addView(sparkle, sparkleLp);
        headerInner.addView(labelRow);

        // Score Row (Radial + Text)
        LinearLayout scoreRow = new LinearLayout(requireContext());
        scoreRow.setOrientation(LinearLayout.HORIZONTAL);
        scoreRow.setGravity(Gravity.CENTER_VERTICAL);
        scoreRow.setPadding(0, dpToPx(24), 0, 0);

        FrameLayout radialContainer = new FrameLayout(requireContext());
        int radialSize = dpToPx(110);
        radialContainer.setLayoutParams(new LinearLayout.LayoutParams(radialSize, radialSize));

        ProgressBar pb = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
        pb.setMax(100);
        pb.setProgress(0); // Animate later
        pb.setProgressDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.circular_progress_bar));
        radialContainer.addView(pb, new FrameLayout.LayoutParams(-1, -1));

        TextView scoreTv = new TextView(requireContext());
        scoreTv.setText(String.valueOf(data.overallScore));
        scoreTv.setTextColor(Color.WHITE);
        scoreTv.setTextSize(28);
        scoreTv.setTypeface(null, android.graphics.Typeface.BOLD);
        FrameLayout.LayoutParams scoreLp = new FrameLayout.LayoutParams(-2, -2);
        scoreLp.gravity = Gravity.CENTER;
        radialContainer.addView(scoreTv, scoreLp);
        scoreRow.addView(radialContainer);

        LinearLayout textCol = new LinearLayout(requireContext());
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setPadding(dpToPx(24), 0, 0, 0);
        
        TextView gradeTv = new TextView(requireContext());
        gradeTv.setText(data.grade + " — " + data.gradeLabel);
        gradeTv.setTextColor(Color.WHITE);
        gradeTv.setTextSize(24);
        gradeTv.setTypeface(null, android.graphics.Typeface.BOLD);
        textCol.addView(gradeTv);

        // Status Chip
        LinearLayout statusChip = new LinearLayout(requireContext());
        statusChip.setBackgroundResource(R.drawable.bg_status_chip);
        statusChip.setPadding(dpToPx(12), dpToPx(4), dpToPx(12), dpToPx(4));
        LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(-2, -2);
        chipLp.topMargin = dpToPx(8);
        statusChip.setLayoutParams(chipLp);
        statusChip.setGravity(Gravity.CENTER_VERTICAL);

        View dot = new View(requireContext());
        int dotSize = dpToPx(8);
        dot.setLayoutParams(new LinearLayout.LayoutParams(dotSize, dotSize));
        dot.setBackgroundResource(R.drawable.bg_circle_gradient);
        int statusColor = data.overallScore >= 75 ? Color.parseColor("#10B981") : 
                         data.overallScore >= 50 ? Color.parseColor("#F59E0B") : Color.parseColor("#EF4444");
        dot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(statusColor));
        statusChip.addView(dot);

        TextView statusTv = new TextView(requireContext());
        statusTv.setText(data.overallScore >= 75 ? getString(R.string.excellent) : data.overallScore >= 50 ? getString(R.string.stable) : getString(R.string.critical));
        statusTv.setTextColor(Color.WHITE);
        statusTv.setTextSize(10);
        statusTv.setPadding(dpToPx(6), 0, 0, 0);
        statusTv.setTypeface(null, android.graphics.Typeface.BOLD);
        statusChip.addView(statusTv);
        textCol.addView(statusChip);

        scoreRow.addView(textCol);
        headerInner.addView(scoreRow);
        headerCard.addView(headerInner);
        binding.resultsContainer.addView(headerCard);

        // AI Summary Card
        addAiSummaryCard(data.summary);

        // Breakdown Section
        if (data.breakdown != null) {
            TextView sectionTitle = new TextView(requireContext());
            sectionTitle.setText(getString(R.string.analysis_breakdown));
            sectionTitle.setTextSize(14);
            sectionTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            sectionTitle.setPadding(dpToPx(4), dpToPx(24), 0, dpToPx(12));
            sectionTitle.setTextColor(getResources().getColor(R.color.text_primary, null));
            binding.resultsContainer.addView(sectionTitle);

            for (int i = 0; i < data.breakdown.size(); i++) {
                HealthScoreResult.Breakdown b = data.breakdown.get(i);
                addBreakdownCard(b, i);
            }
        }

        animateResultsEntrance(pb, data.overallScore);
    }

    private void showRiskResult(RiskAnalysisResult data) {
        if (data == null) return;
        binding.resultsContainer.removeAllViews();
        binding.resultsContainer.setAlpha(0f);
        binding.resultsContainer.setTranslationY(30f);

        // --- Premium Risk Header ---
        MaterialCardView headerCard = new MaterialCardView(requireContext());
        headerCard.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        headerCard.setRadius(dpToPx(28));
        headerCard.setCardElevation(dpToPx(8));
        
        LinearLayout headerInner = new LinearLayout(requireContext());
        headerInner.setOrientation(LinearLayout.VERTICAL);
        headerInner.setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24));
        int riskHeaderColor = Color.parseColor("#1E293B"); // Dark professional slate
        try { 
            if (data.riskColor != null) {
                // If it's a simple color, we blend it or use it as a tint
                riskHeaderColor = Color.parseColor(data.riskColor);
            }
        } catch (Exception e) {}
        headerInner.setBackgroundColor(riskHeaderColor);

        TextView label = new TextView(requireContext());
        label.setText(getString(R.string.financial_risk_analysis));
        label.setTextColor(Color.parseColor("#CCFFFFFF"));
        label.setTextSize(10);
        label.setAllCaps(true);
        label.setLetterSpacing(0.1f);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        headerInner.addView(label);

        LinearLayout valRow = new LinearLayout(requireContext());
        valRow.setOrientation(LinearLayout.HORIZONTAL);
        valRow.setGravity(Gravity.BOTTOM);
        valRow.setPadding(0, dpToPx(16), 0, 0);

        TextView levelTv = new TextView(requireContext());
        levelTv.setText(data.riskLevel.toUpperCase());
        levelTv.setTextColor(Color.WHITE);
        levelTv.setTextSize(32);
        levelTv.setTypeface(null, android.graphics.Typeface.BOLD);
        valRow.addView(levelTv);

        TextView scoreTv = new TextView(requireContext());
        scoreTv.setText(getString(R.string.score_suffix, String.valueOf(data.overallRisk)));
        scoreTv.setTextColor(Color.parseColor("#B3FFFFFF"));
        scoreTv.setTextSize(16);
        scoreTv.setPadding(dpToPx(8), 0, 0, dpToPx(6));
        valRow.addView(scoreTv);
        headerInner.addView(valRow);

        headerCard.addView(headerInner);
        binding.resultsContainer.addView(headerCard);

        addAiSummaryCard(data.summary);

        // Dimensions as Premium Insight Cards
        if (data.dimensions != null) {
            for (int i = 0; i < data.dimensions.size(); i++) {
                RiskAnalysisResult.Dimension d = data.dimensions.get(i);
                addRiskDimensionCard(d, i);
            }
        }

        animateResultsEntrance(null, 0);
    }

    private void addAiSummaryCard(String summary) {
        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dpToPx(16);
        card.setLayoutParams(lp);
        card.setRadius(dpToPx(16));
        card.setCardElevation(0f);
        card.setStrokeWidth(dpToPx(1));
        card.setStrokeColor(Color.parseColor("#0D000000"));
        card.setCardBackgroundColor(Color.parseColor("#050F172A"));

        LinearLayout inner = new LinearLayout(requireContext());
        inner.setOrientation(LinearLayout.HORIZONTAL);
        inner.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        inner.setGravity(Gravity.TOP);

        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(R.drawable.ic_ai_sparkle);
        icon.setColorFilter(getResources().getColor(R.color.primary, null));
        inner.addView(icon, new LinearLayout.LayoutParams(dpToPx(18), dpToPx(18)));

        TextView tv = new TextView(requireContext());
        tv.setText(summary);
        tv.setTextSize(13);
        tv.setTextColor(getResources().getColor(R.color.text_secondary, null));
        tv.setPadding(dpToPx(12), 0, 0, 0);
        tv.setLineSpacing(0, 1.3f);
        inner.addView(tv);

        card.addView(inner);
        binding.resultsContainer.addView(card);
    }

    private void addBreakdownCard(HealthScoreResult.Breakdown b, int index) {
        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dpToPx(12);
        card.setLayoutParams(lp);
        card.setRadius(dpToPx(16));
        card.setCardBackgroundColor(Color.WHITE);
        card.setStrokeWidth(dpToPx(1));
        card.setStrokeColor(getResources().getColor(R.color.card_stroke, null));

        LinearLayout inner = new LinearLayout(requireContext());
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(16));

        RelativeLayout top = new RelativeLayout(requireContext());
        TextView title = new TextView(requireContext());
        title.setText(b.category);
        title.setTextSize(15);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(getResources().getColor(R.color.text_primary, null));
        top.addView(title);

        TextView score = new TextView(requireContext());
        score.setText(b.score + "/100");
        score.setTextSize(14);
        score.setTypeface(null, android.graphics.Typeface.BOLD);
        score.setTextColor(getResources().getColor(R.color.primary, null));
        RelativeLayout.LayoutParams scoreLp = new RelativeLayout.LayoutParams(-2, -2);
        scoreLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        top.addView(score, scoreLp);
        inner.addView(top);

        // Slim Progress Bar
        FrameLayout track = new FrameLayout(requireContext());
        LinearLayout.LayoutParams trackLp = new LinearLayout.LayoutParams(-1, dpToPx(6));
        trackLp.topMargin = dpToPx(12);
        track.setLayoutParams(trackLp);
        track.setBackgroundResource(R.drawable.bg_insight_progress_track);

        View fill = new View(requireContext());
        android.graphics.drawable.GradientDrawable fillShape = new android.graphics.drawable.GradientDrawable();
        fillShape.setCornerRadius(100f);
        fillShape.setColor(getResources().getColor(R.color.primary, null));
        fill.setBackground(fillShape);
        track.addView(fill, new FrameLayout.LayoutParams(0, -1));

        inner.addView(track);

        card.addView(inner);
        binding.resultsContainer.addView(card);

        // Animate fill
        card.post(() -> {
            int targetWidth = (int) (track.getWidth() * (b.score / 100f));
            animateProgressFill(fill, targetWidth, index);
        });
    }

    private void addRiskDimensionCard(RiskAnalysisResult.Dimension d, int index) {
        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dpToPx(12);
        card.setLayoutParams(lp);
        card.setRadius(dpToPx(16));
        card.setCardBackgroundColor(Color.WHITE);
        card.setStrokeWidth(dpToPx(1));
        card.setStrokeColor(getResources().getColor(R.color.card_stroke, null));

        LinearLayout inner = new LinearLayout(requireContext());
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(dpToPx(20), dpToPx(18), dpToPx(20), dpToPx(18));

        LinearLayout contentRow = new LinearLayout(requireContext());
        contentRow.setOrientation(LinearLayout.HORIZONTAL);
        contentRow.setGravity(Gravity.CENTER_VERTICAL);

        // Icon Badge
        FrameLayout badge = new FrameLayout(requireContext());
        int bSize = dpToPx(40);
        badge.setLayoutParams(new LinearLayout.LayoutParams(bSize, bSize));
        android.graphics.drawable.GradientDrawable bBg = new android.graphics.drawable.GradientDrawable();
        bBg.setCornerRadius(dpToPx(12));
        boolean isHigh = "high".equalsIgnoreCase(d.severity);
        bBg.setColor(isHigh ? Color.parseColor("#FEF2F2") : Color.parseColor("#F0FDF4"));
        badge.setBackground(bBg);

        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(isHigh ? R.drawable.ic_nav_analysis : R.drawable.ic_nav_dashboard);
        icon.setColorFilter(isHigh ? Color.parseColor("#EF4444") : Color.parseColor("#10B981"));
        int p = dpToPx(10); icon.setPadding(p, p, p, p);
        badge.addView(icon);
        contentRow.addView(badge);

        LinearLayout textCol = new LinearLayout(requireContext());
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setPadding(dpToPx(16), 0, 0, 0);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));

        TextView title = new TextView(requireContext());
        title.setText(d.dimension);
        title.setTextSize(15);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(getResources().getColor(R.color.text_primary, null));
        textCol.addView(title);

        TextView sub = new TextView(requireContext());
        sub.setText(getString(R.string.impact_suffix, d.severity.toUpperCase(), d.value));
        sub.setTextSize(11);
        sub.setLetterSpacing(0.05f);
        sub.setTextColor(isHigh ? Color.parseColor("#EF4444") : Color.parseColor("#10B981"));
        textCol.addView(sub);
        contentRow.addView(textCol);

        TextView score = new TextView(requireContext());
        score.setText(d.score + "%");
        score.setTextSize(18);
        score.setTypeface(null, android.graphics.Typeface.BOLD);
        score.setTextColor(getResources().getColor(R.color.text_primary, null));
        contentRow.addView(score);
        inner.addView(contentRow);

        TextView msg = new TextView(requireContext());
        msg.setText(d.message);
        msg.setTextSize(12);
        msg.setTextColor(getResources().getColor(R.color.text_secondary, null));
        msg.setPadding(0, dpToPx(12), 0, 0);
        msg.setLineSpacing(0, 1.2f);
        inner.addView(msg);

        card.addView(inner);
        binding.resultsContainer.addView(card);
    }

    private void animateResultsEntrance(ProgressBar radial, int targetScore) {
        binding.resultsContainer.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setInterpolator(new DecelerateInterpolator())
            .start();

        if (radial != null) {
            android.animation.ValueAnimator anim = android.animation.ValueAnimator.ofInt(0, targetScore);
            anim.addUpdateListener(animation -> radial.setProgress((int) animation.getAnimatedValue()));
            anim.setDuration(1200);
            anim.setStartDelay(200);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.start();
        }
    }

    private void animateProgressFill(View fill, int targetWidth, int index) {
        android.animation.ValueAnimator anim = android.animation.ValueAnimator.ofInt(0, targetWidth);
        anim.addUpdateListener(animation -> {
            ViewGroup.LayoutParams lp = fill.getLayoutParams();
            lp.width = (int) animation.getAnimatedValue();
            fill.setLayoutParams(lp);
        });
        anim.setDuration(800);
        anim.setStartDelay(400 + (index * 100));
        anim.setInterpolator(new DecelerateInterpolator());
        anim.start();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private int getI(com.google.android.material.textfield.TextInputEditText e) {
        try { return Integer.parseInt(e.getText().toString().trim()); } catch (Exception ex) { return 0; }
    }

    private double getD(com.google.android.material.textfield.TextInputEditText e) {
        try { return Double.parseDouble(e.getText().toString().trim()); } catch (Exception ex) { return 0; }
    }

    @Override public void onDestroyView() { 
        super.onDestroyView(); 
        binding = null; 
    }
}
