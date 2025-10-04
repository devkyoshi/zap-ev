package com.ead.zap.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ead.zap.R;
import com.ead.zap.models.ProfileResponse;
import com.ead.zap.models.ProfileUpdateRequest;
import com.ead.zap.models.auth.AuthResponse;
import com.ead.zap.services.AuthService;
import com.ead.zap.services.ProfileService;
import com.ead.zap.ui.auth.LoginActivity;
import com.ead.zap.ui.owner.ReactivationInfoActivity;
import com.ead.zap.utils.PreferenceManager;
import com.ead.zap.utils.ProfileValidator;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    private TextInputEditText etFirstName, etLastName, etNic, etEmail, etPhone;
    private Button btnSaveProfile, btnLogout, btnDeleteAccount;
    
    private AuthService authService;
    private ProfileService profileService;
    private PreferenceManager preferenceManager;
    
    private ProfileResponse currentProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        authService = new AuthService(requireContext());
        profileService = new ProfileService(requireContext());
        preferenceManager = new PreferenceManager(requireContext());
        
        initViews(view);
        setupClickListeners();
        loadUserData();
    }

    private void initViews(View view) {
        etFirstName = view.findViewById(R.id.et_first_name);
        etLastName = view.findViewById(R.id.et_last_name);
        etNic = view.findViewById(R.id.et_nic);
        etEmail = view.findViewById(R.id.et_email);
        etPhone = view.findViewById(R.id.et_phone);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);

        // Make NIC field non-editable as it's the primary key
        etNic.setEnabled(false);
    }

    private void setupClickListeners() {
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnLogout.setOnClickListener(v -> logout());
        btnDeleteAccount.setOnClickListener(v -> showDeactivateAccountDialog());
    }

    private void loadUserData() {
        // First try to load cached data for immediate display
        ProfileResponse cachedProfile = profileService.getCachedProfile();
        if (cachedProfile != null) {
            populateFields(cachedProfile);
        }

        // Get user ID from preferences
        String userId = preferenceManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "No user ID found in preferences");
            showErrorAndLoadFallback();
            return;
        }

        // Load fresh data from API
        profileService.getUserProfile(userId, new ProfileService.ProfileCallback() {
            @Override
            public void onSuccess(ProfileResponse profile) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        currentProfile = profile;
                        populateFields(profile);
                        Log.d(TAG, "Profile loaded successfully");
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load profile: " + error);
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Unable to load profile data", Toast.LENGTH_SHORT).show();
                        // Keep any cached data that was already displayed
                        showErrorAndLoadFallback();
                    });
                }
            }
        });
    }

    private void populateFields(ProfileResponse profile) {
        if (profile != null) {
            etFirstName.setText(profile.getFirstName() != null ? profile.getFirstName() : "");
            etLastName.setText(profile.getLastName() != null ? profile.getLastName() : "");
            etNic.setText(profile.getNic() != null ? profile.getNic() : "");
            etEmail.setText(profile.getEmail() != null ? profile.getEmail() : "");
            etPhone.setText(profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "");
        }
    }

    private void showErrorAndLoadFallback() {
        // Load fallback data from preferences
        String userName = preferenceManager.getUserName();
        String email = preferenceManager.getUserEmail();
        String nic = preferenceManager.getUserNIC();
        
        // Parse name into first and last name if available
        if (userName != null && userName.contains(" ")) {
            String[] nameParts = userName.split(" ", 2);
            etFirstName.setText(nameParts[0]);
            etLastName.setText(nameParts[1]);
        } else {
            etFirstName.setText(userName != null ? userName : "");
            etLastName.setText("");
        }
        
        etNic.setText(nic != null ? nic : "");
        etEmail.setText(email != null ? email : "");
        etPhone.setText(""); // Phone not stored in current PreferenceManager
    }

    private void saveProfile() {
        if (!validateForm()) {
            return;
        }

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String nic = etNic.getText().toString().trim();

        // Get user ID from preferences
        String userId = preferenceManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(getContext(), "Unable to save: User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create update request
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest();
        updateRequest.setNic(nic);
        updateRequest.setFirstName(firstName);
        updateRequest.setLastName(lastName);
        updateRequest.setEmail(email);
        updateRequest.setPhoneNumber(phone);
        
        // Keep existing vehicle details if available
        if (currentProfile != null && currentProfile.getVehicleDetails() != null) {
            updateRequest.setVehicleDetails(currentProfile.getVehicleDetails());
        }

        // Disable save button and show loading state
        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving...");

        // Update profile via API
        profileService.updateUserProfile(userId, updateRequest, new ProfileService.ProfileUpdateCallback() {
            @Override
            public void onSuccess(ProfileResponse updatedProfile) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        currentProfile = updatedProfile;
                        btnSaveProfile.setEnabled(true);
                        btnSaveProfile.setText("Save Profile");
                        
                        Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Profile updated successfully");
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to update profile: " + error);
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        btnSaveProfile.setEnabled(true);
                        btnSaveProfile.setText("Save Profile");
                        
                        String errorMessage = "Failed to update profile";
                        if (error != null && !error.isEmpty()) {
                            errorMessage += ": " + error;
                        }
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Clear previous errors
        etFirstName.setError(null);
        etLastName.setError(null);
        etEmail.setError(null);
        etPhone.setError(null);
        etNic.setError(null);

        // Validate first name
        String firstName = etFirstName.getText().toString().trim();
        ProfileValidator.ValidationResult firstNameResult = ProfileValidator.validateFirstName(firstName);
        if (firstNameResult.hasError()) {
            etFirstName.setError(firstNameResult.getErrorMessage());
            isValid = false;
        }

        // Validate last name
        String lastName = etLastName.getText().toString().trim();
        ProfileValidator.ValidationResult lastNameResult = ProfileValidator.validateLastName(lastName);
        if (lastNameResult.hasError()) {
            etLastName.setError(lastNameResult.getErrorMessage());
            isValid = false;
        }

        // Validate email
        String email = etEmail.getText().toString().trim();
        ProfileValidator.ValidationResult emailResult = ProfileValidator.validateEmail(email);
        if (emailResult.hasError()) {
            etEmail.setError(emailResult.getErrorMessage());
            isValid = false;
        }

        // Validate phone number
        String phone = etPhone.getText().toString().trim();
        ProfileValidator.ValidationResult phoneResult = ProfileValidator.validatePhoneNumber(phone);
        if (phoneResult.hasError()) {
            etPhone.setError(phoneResult.getErrorMessage());
            isValid = false;
        }

        // Validate NIC (though it should be read-only)
        String nic = etNic.getText().toString().trim();
        ProfileValidator.ValidationResult nicResult = ProfileValidator.validateNIC(nic);
        if (nicResult.hasError()) {
            etNic.setError(nicResult.getErrorMessage());
            isValid = false;
        }

        return isValid;
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Logout")
               .setMessage("Are you sure you want to logout?")
               .setPositiveButton("Logout", (dialog, which) -> {
                   performLogout();
               })
               .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
               .show();
    }
    
    /**
     * Perform logout using AuthService
     */
    private void performLogout() {
        // Show progress (you can add a progress dialog here if needed)
        btnLogout.setEnabled(false);
        btnLogout.setText("Logging out...");
        
        authService.logout(new AuthService.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Clear profile cache on successful logout
                        profileService.clearCache();
                        
                        Toast.makeText(getContext(), "Logout successful", Toast.LENGTH_SHORT).show();
                        
                        // Navigate to login screen
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        getActivity().finish();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Even if logout API fails, still clear local data and navigate to login
                        profileService.clearCache();
                        
                        Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();
                        
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        getActivity().finish();
                    });
                }
            }
        });
    }

    private void showDeactivateAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Deactivate Account")
               .setMessage("Are you sure you want to deactivate your account?\n\n" +
                          "⚠️ WARNING:\n" +
                          "• Your account will be temporarily disabled\n" +
                          "• You won't be able to make new bookings\n" +
                          "• Existing bookings will remain valid\n" +
                          "• You'll need to contact support to reactivate\n\n" +
                          "This action can only be reversed by our backoffice team.")
               .setPositiveButton("Deactivate", (dialog, which) -> {
                   showConfirmDeactivationDialog();
               })
               .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
               .setIcon(android.R.drawable.ic_dialog_alert)
               .show();
    }

    private void showConfirmDeactivationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Final Confirmation")
               .setMessage("This is your final confirmation.\n\n" +
                          "Type 'DEACTIVATE' below to confirm account deactivation:")
               .setView(createDeactivationInputView())
               .setPositiveButton("Confirm", null) // We'll set the listener after creating the dialog
               .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
               .setIcon(android.R.drawable.ic_dialog_alert);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override the positive button click listener to validate input
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            TextInputEditText confirmText = dialog.findViewById(R.id.et_confirm_deactivation);
            if (confirmText != null && "DEACTIVATE".equals(confirmText.getText().toString().trim())) {
                dialog.dismiss();
                deactivateAccount();
            } else {
                Toast.makeText(getContext(), "Please type 'DEACTIVATE' to confirm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private View createDeactivationInputView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirm_deactivation, null);
        return view;
    }

    private void deactivateAccount() {
        // In a real app, this would make an API call to deactivate the account
        // For now, we'll simulate the deactivation and navigate to the reactivation info screen
        
        Toast.makeText(getContext(), "Account deactivated successfully", Toast.LENGTH_SHORT).show();
        
        // Navigate to reactivation info activity
        Intent intent = new Intent(getActivity(), ReactivationInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh profile data when fragment becomes visible
        // Only if we don't have current profile data
        if (currentProfile == null) {
            loadUserData();
        }
    }
}
