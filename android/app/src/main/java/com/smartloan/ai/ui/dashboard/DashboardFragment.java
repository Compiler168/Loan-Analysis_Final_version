package com.smartloan.ai.ui.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.card.MaterialCardView;
import com.smartloan.ai.R;
import com.smartloan.ai.databinding.FragmentDashboardBinding;
import com.smartloan.ai.data.models.DashboardData;
import com.smartloan.ai.utils.TokenManager;
import com.smartloan.ai.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        String userName = TokenManager.getInstance(requireContext()).getUserName();
        binding.tvUserName.setText(userName);

        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadDashboard());

        observeData();
        viewModel.loadDashboard();

        setupHeaderActions();
        setupScrollListener();
    }

    private void setupScrollListener() {
        binding.dashboardScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            // Animate header border and elevation based on scroll position
            float threshold = ViewUtils.dpToPx(requireContext(), 20);
            float alpha = Math.min((float) scrollY / threshold, 1f);
            
            binding.headerBorder.setAlpha(alpha * 0.4f);
            binding.headerContainer.setElevation(alpha * 12f);
        });
    }

    private void setupHeaderActions() {
        binding.ivNotification.setOnClickListener(v -> 
            ViewUtils.showToast(requireContext(), getString(R.string.notifications) + " coming soon"));
        
        binding.ivHeaderProfile.setOnClickListener(v -> {
            try {
                androidx.navigation.Navigation.findNavController(v).navigate(R.id.nav_settings);
            } catch (Exception e) {
                // Navigation failed
            }
        });
    }

    private void observeData() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.loadingLayout.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.contentLayout.setVisibility(loading ? View.GONE : View.VISIBLE);
            binding.headerContainer.setVisibility(loading ? View.GONE : View.VISIBLE);
            binding.swipeRefresh.setRefreshing(false);
        });

        viewModel.getDashboardData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) populateDashboard(data);
        });
    }

    private void populateDashboard(DashboardData data) {
        buildStatCards(data);
        buildGrowthChart(data);
        buildInsights(data);
        buildIncomeExpensesChart(data);
        buildRadarChart(data);
        buildRecentActivity(data);
    }

    private void buildStatCards(DashboardData data) {
        binding.statsGrid.removeAllViews();
        
        Object[][] stats = {
                {getString(R.string.loan_approval), data.loanProbability + "%", R.color.primary, R.drawable.ic_nav_prediction},
                {getString(R.string.health_score), data.healthScore + "/100", R.color.secondary, R.drawable.ic_nav_analysis},
                {getString(R.string.risk_level), capitalize(data.riskLevel), R.color.warning, R.drawable.ic_nav_simulator},
                {getString(R.string.credit_score), String.valueOf(data.creditScore), R.color.tertiary, R.drawable.ic_user_premium},
                {getString(R.string.monthly_savings), ViewUtils.formatCurrency(data.monthlySavings), R.color.primary, R.drawable.ic_nav_dashboard},
                {getString(R.string.dti_ratio), ViewUtils.formatPercentage(data.dtiRatio * 100), R.color.error, R.drawable.ic_nav_analysis},
        };

        for (Object[] stat : stats) {
            MaterialCardView card = new MaterialCardView(requireContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.setMargins(12, 12, 12, 12);
            card.setLayoutParams(params);
            card.setCardElevation(0f);
            card.setRadius(getResources().getDimension(R.dimen.card_radius));
            card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface));
            card.setStrokeWidth(2);
            card.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.card_stroke));

            LinearLayout inner = new LinearLayout(requireContext());
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setPadding(32, 32, 32, 32);

            // Icon with subtle background
            ImageView icon = new ImageView(requireContext());
            icon.setImageResource((Integer) stat[3]);
            int color = ContextCompat.getColor(requireContext(), (Integer) stat[2]);
            icon.setColorFilter(color);
            
            LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(
                    (int)(32 * getResources().getDisplayMetrics().density), 
                    (int)(32 * getResources().getDisplayMetrics().density));
            iconLp.setMargins(0, 0, 0, 16);
            icon.setLayoutParams(iconLp);
            
            // Add a subtle background to the icon
            android.graphics.drawable.GradientDrawable iconBg = new android.graphics.drawable.GradientDrawable();
            iconBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            iconBg.setCornerRadius(16f);
            iconBg.setColor(color);
            iconBg.setAlpha(25); // ~10% opacity
            icon.setBackground(iconBg);
            icon.setPadding(12, 12, 12, 12);

            TextView value = new TextView(requireContext());
            value.setText((String) stat[1]);
            value.setTextSize(18f);
            value.setTextColor(color);
            value.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD));

            TextView title = new TextView(requireContext());
            title.setText((String) stat[0]);
            title.setTextSize(12f);
            title.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            title.setPadding(0, 4, 0, 0);

            inner.addView(icon);
            inner.addView(value);
            inner.addView(title);
            card.addView(inner);
            binding.statsGrid.addView(card);
        }
    }

    private void buildGrowthChart(DashboardData data) {
        if (data.financialGrowth == null || data.financialGrowth.isEmpty()) return;

        LineChart chart = binding.chartGrowth;
        chart.clear();
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < data.financialGrowth.size(); i++) {
            DashboardData.FinancialGrowth g = data.financialGrowth.get(i);
            entries.add(new Entry(i, (float) g.netWorth));
            labels.add(g.month);
        }

        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.net_worth));
        int primaryColor = ContextCompat.getColor(requireContext(), R.color.primary);
        
        dataSet.setColor(primaryColor);
        dataSet.setLineWidth(4f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(primaryColor);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleRadius(2f);
        dataSet.setCircleHoleColor(Color.WHITE);
        
        dataSet.setDrawFilled(true);
        dataSet.setFillDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_gradient_chart_fill));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(false);
        dataSet.setHighLightColor(primaryColor);
        dataSet.setDrawHorizontalHighlightIndicator(false);

        chart.setData(new LineData(dataSet));
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
        chart.getXAxis().setAxisLineColor(Color.TRANSPARENT);
        chart.getXAxis().setYOffset(10f);
        
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(Color.parseColor("#08000000")); // Very subtle grid
        chart.getAxisLeft().setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
        chart.getAxisLeft().setAxisLineColor(Color.TRANSPARENT);
        chart.getAxisLeft().setXOffset(10f);
        chart.getAxisLeft().setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 1000) return "$" + (int)(value/1000) + "k";
                return "$" + (int)value;
            }
        });
        
        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setExtraOffsets(0, 0, 0, 10);

        CustomMarkerView mv = new CustomMarkerView(requireContext(), R.layout.layout_chart_marker);
        mv.setChartView(chart);
        chart.setMarker(mv);

        chart.animateX(1500);
        chart.invalidate();
    }

    private void buildInsights(DashboardData data) {
        binding.insightsContainer.removeAllViews();
        if (data.insights == null) return;

        for (DashboardData.Insight insight : data.insights) {
            MaterialCardView rowCard = new MaterialCardView(requireContext());
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardLp.setMargins(0, 8, 0, 8);
            rowCard.setLayoutParams(cardLp);
            rowCard.setRadius(getResources().getDimension(R.dimen.card_radius));
            rowCard.setCardElevation(0f);
            rowCard.setStrokeWidth(1);
            rowCard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.divider_color));
            rowCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.input_bg));

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(32, 28, 32, 28);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);

            // Icon background
            TextView iconView = new TextView(requireContext());
            iconView.setText(insight.icon);
            iconView.setTextSize(22f);
            iconView.setGravity(android.view.Gravity.CENTER);
            
            LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(
                    (int)(48 * getResources().getDisplayMetrics().density),
                    (int)(48 * getResources().getDisplayMetrics().density));
            iconLp.setMargins(0, 0, 24, 0);
            iconView.setLayoutParams(iconLp);
            
            android.graphics.drawable.GradientDrawable iconBg = new android.graphics.drawable.GradientDrawable();
            iconBg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            iconBg.setColor(ContextCompat.getColor(requireContext(), R.color.white));
            iconView.setBackground(iconBg);
            iconView.setElevation(2f);

            LinearLayout textCol = new LinearLayout(requireContext());
            textCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            textCol.setLayoutParams(textLp);

            TextView title = new TextView(requireContext());
            title.setText(insight.title);
            title.setTextSize(14f);
            title.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD));
            title.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));

            TextView msg = new TextView(requireContext());
            msg.setText(insight.message);
            msg.setTextSize(13f);
            msg.setLineSpacing(0f, 1.2f);
            msg.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            msg.setPadding(0, 4, 0, 0);

            textCol.addView(title);
            textCol.addView(msg);
            
            // AI Badge
            TextView aiBadge = new TextView(requireContext());
            aiBadge.setText(R.string.ai_label);
            aiBadge.setTextSize(10f);
            aiBadge.setTypeface(null, android.graphics.Typeface.BOLD);
            aiBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
            aiBadge.setPadding(12, 4, 12, 4);
            
            android.graphics.drawable.GradientDrawable badgeBg = new android.graphics.drawable.GradientDrawable();
            badgeBg.setCornerRadius(20f);
            badgeBg.setColor(ContextCompat.getColor(requireContext(), R.color.primary_light));
            aiBadge.setBackground(badgeBg);
            
            row.addView(iconView);
            row.addView(textCol);
            row.addView(aiBadge);
            
            rowCard.addView(row);
            binding.insightsContainer.addView(rowCard);
        }
    }

    private void buildIncomeExpensesChart(DashboardData data) {
        if (data.incomeVsExpenses == null || data.incomeVsExpenses.isEmpty()) return;

        BarChart chart = binding.chartIncomeExpenses;
        chart.clear();
        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < data.incomeVsExpenses.size(); i++) {
            DashboardData.IncomeExpense ie = data.incomeVsExpenses.get(i);
            incomeEntries.add(new BarEntry(i, (float) ie.income));
            expenseEntries.add(new BarEntry(i, (float) ie.expenses));
            labels.add(ie.month);
        }

        BarDataSet incomeSet = new BarDataSet(incomeEntries, getString(R.string.income));
        incomeSet.setColor(ContextCompat.getColor(requireContext(), R.color.secondary));
        incomeSet.setDrawValues(false);

        BarDataSet expenseSet = new BarDataSet(expenseEntries, getString(R.string.expenses));
        expenseSet.setColor(ContextCompat.getColor(requireContext(), R.color.error));
        expenseSet.setDrawValues(false);

        BarData barData = new BarData(incomeSet, expenseSet);
        float groupSpace = 0.3f, barSpace = 0.05f, barWidth = 0.3f;
        barData.setBarWidth(barWidth);

        chart.setData(barData);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setCenterAxisLabels(true);
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
        chart.getXAxis().setAxisLineColor(ContextCompat.getColor(requireContext(), R.color.divider_color));

        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(ContextCompat.getColor(requireContext(), R.color.divider_color));
        chart.getAxisLeft().setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
        chart.getAxisLeft().setAxisLineColor(Color.TRANSPARENT);

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        chart.groupBars(0f, groupSpace, barSpace);
        chart.setFitBars(true);
        chart.animateY(1000);
        chart.invalidate();
    }

    private void buildRadarChart(DashboardData data) {
        if (data.riskRadar == null || data.riskRadar.isEmpty()) return;

        RadarChart chart = binding.chartRadar;
        chart.clear();
        List<RadarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (DashboardData.RiskRadar r : data.riskRadar) {
            entries.add(new RadarEntry(r.value));
            labels.add(r.category);
        }

        RadarDataSet dataSet = new RadarDataSet(entries, getString(R.string.score));
        int accentColor = ContextCompat.getColor(requireContext(), R.color.primary);
        dataSet.setColor(accentColor);
        dataSet.setFillColor(accentColor);
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(80);
        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(false);

        chart.setData(new RadarData(dataSet));
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setTextSize(11f);
        chart.getXAxis().setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        chart.getYAxis().setAxisMinimum(0f);
        chart.getYAxis().setAxisMaximum(100f);
        chart.getYAxis().setDrawLabels(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setWebLineWidth(1f);
        chart.setWebColor(ContextCompat.getColor(requireContext(), R.color.divider_color));
        chart.setWebLineWidthInner(1f);
        chart.setWebColorInner(ContextCompat.getColor(requireContext(), R.color.divider_color));
        chart.animateXY(800, 800);
        chart.invalidate();
    }

    private void buildRecentActivity(DashboardData data) {
        binding.activityContainer.removeAllViews();
        if (data.recentActivity == null) return;

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (DashboardData.RecentActivity activity : data.recentActivity) {
            View itemView = inflater.inflate(R.layout.item_recent_activity, binding.activityContainer, false);

            TextView tvMessage = itemView.findViewById(R.id.tvActivityMessage);
            TextView tvTime = itemView.findViewById(R.id.tvActivityTime);
            TextView tvResult = itemView.findViewById(R.id.tvActivityResult);
            ImageView ivIcon = itemView.findViewById(R.id.ivActivityIcon);
            View iconContainer = itemView.findViewById(R.id.iconContainer);

            tvMessage.setText(activity.message);
            tvTime.setText(activity.time);
            tvResult.setText(activity.result.toUpperCase());

            // Status-based styling
            int statusColor;
            int bgColor;
            int iconRes = R.drawable.ic_ai_sparkle;

            String resultLower = activity.result.toLowerCase();
            if (resultLower.contains("approved") || resultLower.contains("high") || resultLower.contains("success")) {
                statusColor = ContextCompat.getColor(requireContext(), R.color.secondary);
                bgColor = ContextCompat.getColor(requireContext(), R.color.secondary_light);
                iconRes = R.drawable.ic_nav_analysis;
            } else if (resultLower.contains("rejected") || resultLower.contains("risk") || resultLower.contains("low")) {
                statusColor = ContextCompat.getColor(requireContext(), R.color.error);
                bgColor = Color.parseColor("#FEE2E2"); // Soft error red
                iconRes = R.drawable.ic_nav_prediction;
            } else {
                statusColor = ContextCompat.getColor(requireContext(), R.color.primary);
                bgColor = ContextCompat.getColor(requireContext(), R.color.primary_light);
                iconRes = R.drawable.ic_ai_sparkle;
            }

            // Apply styling
            tvResult.setTextColor(statusColor);
            android.graphics.drawable.GradientDrawable resultBg = new android.graphics.drawable.GradientDrawable();
            resultBg.setCornerRadius(24);
            resultBg.setColor(Color.argb(30, Color.red(statusColor), Color.green(statusColor), Color.blue(statusColor)));
            tvResult.setBackground(resultBg);

            ivIcon.setImageResource(iconRes);
            ivIcon.setColorFilter(statusColor);
            
            android.graphics.drawable.GradientDrawable iconBg = new android.graphics.drawable.GradientDrawable();
            iconBg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            iconBg.setColor(bgColor);
            iconContainer.setBackground(iconBg);

            // Add animation
            itemView.setAlpha(0f);
            itemView.setTranslationY(20f);
            itemView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(400)
                    .setStartDelay(binding.activityContainer.getChildCount() * 100L)
                    .start();

            binding.activityContainer.addView(itemView);
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
