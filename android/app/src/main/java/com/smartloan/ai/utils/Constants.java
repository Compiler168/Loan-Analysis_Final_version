package com.smartloan.ai.utils;

import com.smartloan.ai.BuildConfig;

/**
 * Application-wide constants for SmartLoan AI+.
 */
public final class Constants {

    private Constants() {}

    // API Configuration
    // For Android emulator: 10.0.2.2 maps to host machine's localhost
    // For physical device: use your machine's local IP or deployed URL
    public static final String BASE_URL = BuildConfig.API_BASE_URL;

    // SharedPreferences
    public static final String PREFS_NAME = "smartloan_prefs";
    public static final String KEY_TOKEN = "jwt_token";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_USER_ROLE = "user_role";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_FIREBASE_TOKEN = "firebase_token";
    public static final String KEY_THEME = "app_theme";
    public static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";

    // Theme values
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_SYSTEM = "system";

    // Chat
    public static final String DEFAULT_SESSION_ID = "main";

    // Validation
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MIN_CREDIT_SCORE = 300;
    public static final int MAX_CREDIT_SCORE = 850;
}
