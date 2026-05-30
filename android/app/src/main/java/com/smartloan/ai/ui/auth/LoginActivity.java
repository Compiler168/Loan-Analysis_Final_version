package com.smartloan.ai.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.smartloan.ai.R;
import com.smartloan.ai.databinding.ActivityLoginBinding;
import com.smartloan.ai.ui.main.MainActivity;
import com.smartloan.ai.utils.TokenManager;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tokenManager = TokenManager.getInstance(this);

        // Auto-redirect if already logged in
        if (tokenManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        startEnterAnimations();
        setupObservers();
        setupListeners();
    }

    private void startEnterAnimations() {
        binding.ivLogo.setAlpha(0f);
        binding.tvWelcome.setAlpha(0f);
        binding.tvSubtitle.setAlpha(0f);
        binding.cardLogin.setAlpha(0f);
        
        binding.ivLogo.setTranslationY(-50f);
        binding.tvWelcome.setTranslationY(-30f);
        binding.tvSubtitle.setTranslationY(-20f);
        binding.cardLogin.setTranslationY(100f);

        binding.ivLogo.animate().alpha(1f).translationY(0).setDuration(800).setStartDelay(200).start();
        binding.tvWelcome.animate().alpha(1f).translationY(0).setDuration(800).setStartDelay(400).start();
        binding.tvSubtitle.animate().alpha(1f).translationY(0).setDuration(800).setStartDelay(500).start();
        binding.cardLogin.animate().alpha(1f).translationY(0).setDuration(1000).setStartDelay(600).start();
    }

    private void setupObservers() {
        viewModel.getAuthResult().observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnLogin.setEnabled(true);

            if (result.isSuccess()) {
                tokenManager.saveToken(result.getToken());
                tokenManager.saveUserInfo(
                        result.getUserId(),
                        result.getUserName(),
                        result.getUserEmail(),
                        result.getUserRole(),
                        result.getFirebaseToken()
                );
                navigateToMain();
            } else {
                showError(result.getError());
            }
        });
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            boolean isValid = true;

            if (email.isEmpty()) {
                binding.tilEmail.setError(getString(R.string.email_required));
                isValid = false;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.setError(getString(R.string.invalid_email));
                isValid = false;
            } else {
                binding.tilEmail.setError(null);
            }

            if (password.isEmpty()) {
                binding.tilPassword.setError(getString(R.string.password_required));
                isValid = false;
            } else if (password.length() < 6) {
                binding.tilPassword.setError(getString(R.string.password_too_short));
                isValid = false;
            } else {
                binding.tilPassword.setError(null);
            }

            if (!isValid) return;

            hideError();
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnLogin.setEnabled(false);

            viewModel.login(email, password);
        });

        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            // Placeholder for Forgot Password
        });

        binding.btnGoogle.setOnClickListener(v -> {
            // Placeholder for Google Login
        });

        binding.btnFacebook.setOnClickListener(v -> {
            // Placeholder for Facebook Login
        });

        binding.btnLinkedIn.setOnClickListener(v -> {
            // Placeholder for LinkedIn Login
        });
    }

    private void showError(String message) {
        binding.tvError.setText(message);
        binding.cardError.setVisibility(View.VISIBLE);
        binding.cardError.setAlpha(0f);
        binding.cardError.animate().alpha(1f).setDuration(300).start();
    }

    private void hideError() {
        binding.cardError.setVisibility(View.GONE);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}
