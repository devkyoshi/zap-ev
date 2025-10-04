package com.ead.zap.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ead.zap.R;
import com.ead.zap.models.auth.AuthResponse;
import com.ead.zap.services.AuthService;
import com.ead.zap.ui.auth.LoginActivity;
import com.ead.zap.ui.owner.ReactivationInfoActivity;
import com.ead.zap.utils.PreferenceManager;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileFragment extends Fragment {

    private TextInputEditText etFirstName, etLastName, etNic, etEmail, etPhone;
    private Button btnSaveProfile, btnLogout, btnDeleteAccount;
    
    private AuthService authService;
    private PreferenceManager preferenceManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        authService = new AuthService(requireContext());
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
        // In a real app, this would load user data from the database or API
        // For now, we'll use mock data
        etFirstName.setText("Alex");
        etLastName.setText("Johnson");
        etNic.setText("123456789V");
        etEmail.setText("alex.johnson@email.com");
        etPhone.setText("+94 77 123 4567");
    }

    private void saveProfile() {
        if (!validateForm()) {
            return;
        }

        // In a real app, this would save the data to the database or API
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Simulate save operation
        Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
    }

    private boolean validateForm() {
        boolean isValid = true;

        String firstName = etFirstName.getText().toString().trim();
        if (firstName.isEmpty()) {
            etFirstName.setError("First name is required");
            isValid = false;
        }

        String lastName = etLastName.getText().toString().trim();
        if (lastName.isEmpty()) {
            etLastName.setError("Last name is required");
            isValid = false;
        }

        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            isValid = false;
        }

        String phone = etPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
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
}
