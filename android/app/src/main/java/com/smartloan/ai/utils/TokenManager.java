package com.smartloan.ai.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages JWT token and user session data using SharedPreferences.
 */
public class TokenManager {

    private static TokenManager instance;
    private final SharedPreferences prefs;

    private TokenManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    public void saveToken(String token) {
        prefs.edit().putString(Constants.KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(Constants.KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }

    public void saveUserInfo(String id, String name, String email, String role, String firebaseToken) {
        prefs.edit()
                .putString(Constants.KEY_USER_ID, id)
                .putString(Constants.KEY_USER_NAME, name)
                .putString(Constants.KEY_USER_EMAIL, email)
                .putString(Constants.KEY_USER_ROLE, role)
                .putString(Constants.KEY_FIREBASE_TOKEN, firebaseToken)
                .apply();
    }

    public String getUserName() {
        return prefs.getString(Constants.KEY_USER_NAME, "User");
    }

    public String getUserEmail() {
        return prefs.getString(Constants.KEY_USER_EMAIL, "");
    }

    public String getUserRole() {
        return prefs.getString(Constants.KEY_USER_ROLE, "user");
    }

    public String getUserId() {
        return prefs.getString(Constants.KEY_USER_ID, "");
    }

    public String getFirebaseToken() {
        return prefs.getString(Constants.KEY_FIREBASE_TOKEN, null);
    }

    public void saveTheme(String theme) {
        prefs.edit().putString(Constants.KEY_THEME, theme).apply();
    }

    public String getTheme() {
        return prefs.getString(Constants.KEY_THEME, Constants.THEME_SYSTEM);
    }

    public void setOnboardingCompleted(boolean completed) {
        prefs.edit().putBoolean(Constants.KEY_ONBOARDING_COMPLETED, completed).apply();
    }

    public boolean isOnboardingCompleted() {
        return prefs.getBoolean(Constants.KEY_ONBOARDING_COMPLETED, false);
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
