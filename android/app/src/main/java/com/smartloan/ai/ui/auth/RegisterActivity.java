package com.smartloan.ai.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.smartloan.ai.R;
import com.smartloan.ai.databinding.ActivityRegisterBinding;
import com.smartloan.ai.ui.main.MainActivity;
import com.smartloan.ai.utils.TokenManager;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel viewModel;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tokenManager = TokenManager.getInstance(this);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
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
        binding.cardRegister.setAlpha(0f);

        binding.ivLogo.setTranslationY(-50f);
        binding.tvWelcome.setTranslationY(-30f);
        binding.tvSubtitle.setTranslationY(-20f);
        binding.cardRegister.setTranslationY(100f);

        binding.ivLogo.animate().alpha(1f).translationY(0).setDuration(800).setStartDelay(200).start();
        binding.tvWelcome.animate().alpha(1f).translationY(0).setDuration(800).setStartDelay(400).start();
        binding.tvSubtitle.animate().alpha(1f).translationY(0).setDuration(800).setStartDelay(500).start();
        binding.cardRegister.animate().alpha(1f).translationY(0).setDuration(1000).setStartDelay(600).start();
    }

    private void setupObservers() {
        viewModel.getAuthResult().observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnRegister.setEnabled(true);

            if (result.isSuccess()) {
                tokenManager.saveToken(result.getToken());
                tokenManager.saveUserInfo(
                        result.getUserId(),
                        result.getUserName(),
                        result.getUserEmail(),
                        result.getUserRole(),
                        result.getFirebaseToken()
                );
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            } else {
                showError(result.getError());
            }
        });
    }

    private void setupListeners() {
        binding.btnRegister.setOnClickListener(v -> {
            String name = binding.etName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String confirm = binding.etConfirmPassword.getText().toString().trim();

            boolean isValid = true;

            if (name.isEmpty()) {
                binding.tilName.setError(getString(R.string.name_required));
                isValid = false;
            } else {
                binding.tilName.setError(null);
            }

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

            if (!password.equals(confirm)) {
                binding.tilConfirmPassword.setError(getString(R.string.passwords_dont_match));
                isValid = false;
            } else {
                binding.tilConfirmPassword.setError(null);
            }

            if (!isValid) return;

            hideError();
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnRegister.setEnabled(false);

            viewModel.register(name, email, "", password);
        });

        binding.tvLogin.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
