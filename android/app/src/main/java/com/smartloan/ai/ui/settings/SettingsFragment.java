package com.smartloan.ai.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.smartloan.ai.R;
import com.smartloan.ai.databinding.FragmentSettingsBinding;
import com.smartloan.ai.ui.auth.LoginActivity;
import com.smartloan.ai.ui.reports.ReportsViewModel;
import com.smartloan.ai.utils.Constants;
import com.smartloan.ai.utils.TokenManager;
import com.smartloan.ai.utils.ViewUtils;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private TokenManager tokenManager;
    private ReportsViewModel reportsViewModel;
    private ActivityResultLauncher<String> documentPickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        documentPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        handleDocumentSelected(uri);
                    }
                }
        );
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tokenManager = TokenManager.getInstance(requireContext());
        reportsViewModel = new ViewModelProvider(this).get(ReportsViewModel.class);

        setupProfile();
        setupTheme();
        setupActions();
    }

    private void setupProfile() {
        String name = tokenManager.getUserName();
        String email = tokenManager.getUserEmail();
        
        binding.etName.setText(name);
        binding.etEmail.setText(email);
        binding.tvProfileName.setText(name);
        binding.tvProfileEmail.setText(email);

        binding.btnUpdateProfile.setOnClickListener(v -> {
            String newName = binding.etName.getText().toString().trim();
            if (newName.isEmpty()) {
                binding.etName.setError(getString(R.string.name_required));
                return;
            }
            tokenManager.saveUserInfo(
                    tokenManager.getUserId(),
                    newName,
                    tokenManager.getUserEmail(),
                    tokenManager.getUserRole()
            );
            binding.tvProfileName.setText(newName);
            ViewUtils.showSuccessSnackbar(binding.getRoot(), getString(R.string.saved));
        });
    }

    private void setupTheme() {
        String theme = tokenManager.getTheme();
        if(Constants.THEME_LIGHT.equals(theme)) binding.rbLight.setChecked(true);
        else if(Constants.THEME_DARK.equals(theme)) binding.rbDark.setChecked(true);
        else binding.rbSystem.setChecked(true);

        binding.rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            String newTheme = Constants.THEME_SYSTEM;
            if(checkedId == R.id.rbLight) newTheme = Constants.THEME_LIGHT;
            else if(checkedId == R.id.rbDark) newTheme = Constants.THEME_DARK;
            
            String oldTheme = tokenManager.getTheme();
            if (!newTheme.equals(oldTheme)) {
                tokenManager.saveTheme(newTheme);
                applyTheme(newTheme);
            }
        });
    }

    private void applyTheme(String theme) {
        switch (theme) {
            case Constants.THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case Constants.THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    private void setupActions() {
        binding.btnEditAvatar.setOnClickListener(v -> {
            String[] options = {getString(R.string.change_avatar), getString(R.string.remove_avatar)};
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.profile)
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            // Pick image
                            documentPickerLauncher.launch("image/*");
                        } else {
                            // Remove image (Reset to default)
                            binding.ivProfileLarge.setImageResource(R.drawable.ic_user_premium);
                            ViewUtils.showSuccessSnackbar(binding.getRoot(), "Avatar removed");
                        }
                    })
                    .show();
        });

        binding.btnUploadDocs.setOnClickListener(v -> {
            documentPickerLauncher.launch("*/*");
        });

        binding.btnDownloadReport.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.nav_reports);
        });

        binding.btnLogout.setOnClickListener(v -> {
            tokenManager.clearSession();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    private void handleDocumentSelected(Uri uri) {
        // In a real app, you would upload this to a server
        String mimeType = requireContext().getContentResolver().getType(uri);
        if (mimeType != null && mimeType.startsWith("image/")) {
            binding.ivProfileLarge.setImageURI(uri);
            ViewUtils.showSuccessSnackbar(binding.getRoot(), "Profile image updated");
        } else {
            ViewUtils.showSuccessSnackbar(binding.getRoot(), "Document attached: " + uri.getLastPathSegment());
        }
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
