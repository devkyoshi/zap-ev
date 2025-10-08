package com.ead.zap.ui.profile;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.ead.zap.R;
import com.ead.zap.adapters.VehicleAdapter;
import com.ead.zap.models.ProfileResponse;
import com.ead.zap.models.ProfileUpdateRequest;
import com.ead.zap.models.VehicleDetail;
import com.ead.zap.models.User;
import com.ead.zap.models.auth.AuthResponse;
import com.ead.zap.services.AuthService;
import com.ead.zap.services.OperatorProfileService;
import com.ead.zap.services.ProfileService;
import com.ead.zap.ui.auth.LoginActivity;
import com.ead.zap.ui.owner.ReactivationInfoActivity;
import com.ead.zap.utils.PreferenceManager;
import com.ead.zap.utils.ProfileValidator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    private TextInputEditText etFirstName, etLastName, etNic, etEmail, etPhone;
    private Button btnSaveProfile, btnLogout, btnDeleteAccount, btnAddVehicle;
    private RecyclerView recyclerViewVehicles;
    private View emptyVehicleState;
    private ScrollView scrollView;
    private com.google.android.material.card.MaterialCardView cardChangePassword;
    
    private AuthService authService;
    private ProfileService profileService;
    private OperatorProfileService operatorProfileService;
    private PreferenceManager preferenceManager;
    
    private ProfileResponse currentProfile;
    private User currentOperatorProfile;
    private boolean isOperator = false;
    private VehicleAdapter vehicleAdapter;
    private List<VehicleDetail> vehicleList;
    
    // Keyboard handling variables
    private View rootView;
    private int originalHeight;
    private boolean keyboardVisible = false;


    private TextView profileName;
    private LinearLayout vehicleInformationSection;
    private TextView profileTitle, profileDescription;

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
        operatorProfileService = new OperatorProfileService(requireContext());
        preferenceManager = new PreferenceManager(requireContext());
        
        // Determine user type from auth data
        determineUserType();
        
        rootView = view;
        initViews(view);
        setupKeyboardHandling();
        setupClickListeners();
        ensureBottomNavigationAccessible();
        loadUserData();
    }

    private void initViews(View view) {
        scrollView = view.findViewById(R.id.scrollView);
        etFirstName = view.findViewById(R.id.et_first_name);
        etLastName = view.findViewById(R.id.et_last_name);
        etNic = view.findViewById(R.id.et_nic);
        etEmail = view.findViewById(R.id.et_email);
        etPhone = view.findViewById(R.id.et_phone);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
        btnAddVehicle = view.findViewById(R.id.btnAddVehicle);
        recyclerViewVehicles = view.findViewById(R.id.recyclerViewVehicles);
        emptyVehicleState = view.findViewById(R.id.emptyVehicleState);
        profileName = view.findViewById(R.id.profileName);
        profileTitle = view.findViewById(R.id.profileTitle);
        profileDescription = view.findViewById(R.id.profileDescription);
        cardChangePassword = view.findViewById(R.id.cardChangePassword);
        
        // Find vehicle information section (the LinearLayout containing the heading and add button)
        vehicleInformationSection = findVehicleInformationSection(view);
        profileTitle = view.findViewById(R.id.profileTitle);
        profileDescription = view.findViewById(R.id.profileDescription);
        
        // Find vehicle information section (the LinearLayout containing the heading and add button)
        vehicleInformationSection = findVehicleInformationSection(view);

        // Configure UI based on user type
        configureUIForUserType();
        
        // Setup vehicle list (only for EV Owners)
        if (!isOperator) {
            setupVehicleList();
        }
    }

    private void setupVehicleList() {
        vehicleList = new ArrayList<>();
        vehicleAdapter = new VehicleAdapter(vehicleList, new VehicleAdapter.OnVehicleActionListener() {
            @Override
            public void onEditVehicle(VehicleDetail vehicle, int position) {
                showVehicleDialog(vehicle, position);
            }

            @Override
            public void onDeleteVehicle(VehicleDetail vehicle, int position) {
                showDeleteVehicleDialog(vehicle, position);
            }
        });
        
        recyclerViewVehicles.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewVehicles.setAdapter(vehicleAdapter);
        recyclerViewVehicles.setNestedScrollingEnabled(false);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupClickListeners() {
        btnSaveProfile.setOnClickListener(v -> {
            hideKeyboard();
            saveProfile();
        });
        btnLogout.setOnClickListener(v -> {
            hideKeyboard();
            logout();
        });
        btnDeleteAccount.setOnClickListener(v -> {
            hideKeyboard();
            showDeactivateAccountDialog();
        });
        btnAddVehicle.setOnClickListener(v -> {
            hideKeyboard();
            showVehicleDialog(null, -1);
        });
        
        cardChangePassword.setOnClickListener(v -> {
            hideKeyboard();
            showChangePasswordDialog();
        });
        
        // Hide keyboard when touching outside input fields
        if (scrollView != null) {
            scrollView.setOnTouchListener((v, event) -> {
                hideKeyboard();
                return false;
            });
        }
    }

    private void setupKeyboardHandling() {
        if (rootView != null) {
            // Track root view height changes to detect keyboard
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int currentHeight = rootView.getHeight();
                    
                    if (originalHeight == 0) {
                        originalHeight = currentHeight;
                        return;
                    }
                    
                    // Calculate height difference
                    int heightDifference = originalHeight - currentHeight;
                    boolean isKeyboardVisible = heightDifference > originalHeight * 0.20; // Threshold for keyboard detection (increased for better detection)
                    
                    if (isKeyboardVisible != keyboardVisible) {
                        keyboardVisible = isKeyboardVisible;
                        onKeyboardVisibilityChanged(isKeyboardVisible);
                    }
                }
            });
            
            // Setup focus listeners for input fields
            setupInputFieldFocusListeners();
        }
    }
    
    private void setupInputFieldFocusListeners() {
        TextInputEditText[] inputFields = {etFirstName, etLastName, etNic, etEmail, etPhone};
        
        for (TextInputEditText field : inputFields) {
            if (field != null) {
                field.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) {
                        // Delay scroll to ensure keyboard is fully opened
                        v.postDelayed(() -> scrollToView(v), 300);
                    }
                });
            }
        }
    }
    
    private void scrollToView(View view) {
        if (scrollView != null && view != null && getActivity() != null) {
            view.post(() -> {
                // Get screen height and keyboard height approximation
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                int keyboardHeight = screenHeight / 3; // Approximate keyboard height
                int availableHeight = screenHeight - keyboardHeight;
                
                // Calculate position to scroll the focused field into view
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                int viewY = location[1];
                
                // Get ScrollView location
                int[] scrollLocation = new int[2];
                scrollView.getLocationOnScreen(scrollLocation);
                int scrollViewY = scrollLocation[1];
                
                // Calculate relative position
                int relativeY = viewY - scrollViewY;
                
                // Calculate desired scroll position (field at 1/4 from top of available area)
                int desiredY = relativeY - (availableHeight / 4);
                
                // Ensure we don't scroll past the content
                int maxScroll = scrollView.getChildAt(0).getHeight() - scrollView.getHeight();
                int finalScrollY = Math.max(0, Math.min(desiredY, maxScroll));
                
                scrollView.smoothScrollTo(0, finalScrollY);
            });
        }
    }
    
    private void onKeyboardVisibilityChanged(boolean isVisible) {
        if (scrollView != null && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (isVisible) {
                    // Keyboard is open - remove system UI fitting to prevent white space
                    scrollView.setFitsSystemWindows(false);
                    
                    // Ensure content is visible above keyboard
                    View focusedView = getActivity().getCurrentFocus();
                    if (focusedView != null) {
                        scrollToView(focusedView);
                    }
                } else {
                    // Keyboard is closed - restore system UI fitting
                    scrollView.post(() -> scrollView.setFitsSystemWindows(false));
                }
            });
        }
    }
    
    private void hideKeyboard() {
        if (getActivity() != null) {
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
    }
    
    /**
     * Ensures bottom navigation remains accessible by limiting scroll range
     */
    private void ensureBottomNavigationAccessible() {
        if (scrollView != null && getActivity() != null) {
            // Reserve space for bottom navigation (approximately 56dp + 16dp margins)
            int bottomNavHeight = (int) (72 * getResources().getDisplayMetrics().density);
            scrollView.setPadding(
                scrollView.getPaddingLeft(),
                scrollView.getPaddingTop(),
                scrollView.getPaddingRight(),
                Math.max(bottomNavHeight, scrollView.getPaddingBottom())
            );
        }
    }

    private void loadUserData() {
        // Get user ID from preferences
        String userId = preferenceManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "No user ID found in preferences");
            showErrorAndLoadFallback();
            return;
        }

        if (isOperator) {
            loadOperatorProfile(userId);
        } else {
            loadEVOwnerProfile(userId);
        }
    }

    private void loadEVOwnerProfile(String userId) {
        // First try to load cached data for immediate display
        ProfileResponse cachedProfile = profileService.getCachedProfile();
        if (cachedProfile != null) {
            populateFields(cachedProfile);
        }

        // Load fresh data from API
        profileService.getUserProfile(userId, new ProfileService.ProfileCallback() {
            @Override
            public void onSuccess(ProfileResponse profile) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        currentProfile = profile;
                        populateFields(profile);
                        Log.d(TAG, "EV Owner profile loaded successfully");
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load EV Owner profile: " + error);
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Unable to load profile data", Toast.LENGTH_SHORT).show();
                        showErrorAndLoadFallback();
                    });
                }
            }
        });
    }

    private void loadOperatorProfile(String userId) {
        // First try to load cached data for immediate display
        User cachedProfile = operatorProfileService.getCachedProfile();
        if (cachedProfile != null) {
            populateOperatorFields(cachedProfile);
        }

        // Load fresh data from API
        operatorProfileService.getOperatorProfile(userId, new OperatorProfileService.OperatorProfileCallback() {
            @Override
            public void onSuccess(User profile) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        currentOperatorProfile = profile;
                        populateOperatorFields(profile);
                        Log.d(TAG, "Operator profile loaded successfully");
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load operator profile: " + error);
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Unable to load profile data", Toast.LENGTH_SHORT).show();
                        showOperatorErrorAndLoadFallback();
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
            profileName.setText(profile.getDisplayName());
            
            // Update vehicle list
            updateVehicleList(profile.getVehicleDetails());
        }
    }

    private void updateVehicleList(List<VehicleDetail> vehicles) {
        if (vehicles != null && !vehicles.isEmpty()) {
            vehicleList.clear();
            vehicleList.addAll(vehicles);
            vehicleAdapter.notifyDataSetChanged();
            
            recyclerViewVehicles.setVisibility(View.VISIBLE);
            emptyVehicleState.setVisibility(View.GONE);
        } else {
            vehicleList.clear();
            vehicleAdapter.notifyDataSetChanged();
            
            recyclerViewVehicles.setVisibility(View.GONE);
            emptyVehicleState.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Updates the visibility of vehicle list based on current vehicle count
     * Used after individual add/edit/delete operations to avoid double refresh
     */
    private void updateVehicleVisibility() {
        if (vehicleList != null && !vehicleList.isEmpty()) {
            recyclerViewVehicles.setVisibility(View.VISIBLE);
            emptyVehicleState.setVisibility(View.GONE);
        } else {
            recyclerViewVehicles.setVisibility(View.GONE);
            emptyVehicleState.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Saves vehicle changes to backend automatically when vehicles are added/edited/deleted
     */
    private void saveVehicleChangesToBackend() {
        // Validate profile form before saving vehicle changes
        if (!validateForm()) {
            Toast.makeText(getContext(), 
                "Please fix profile information errors before vehicle changes can be saved", 
                Toast.LENGTH_LONG).show();
            return;
        }
        
        // Get current form values
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String nic = etNic.getText().toString().trim();

        // Get user ID from preferences
        String userId = preferenceManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Unable to save vehicle changes: User ID not found");
            Toast.makeText(getContext(), "Unable to save vehicle changes", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create update request with current profile data + updated vehicles
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest();
        updateRequest.setNic(nic);
        updateRequest.setFirstName(firstName);
        updateRequest.setLastName(lastName);
        updateRequest.setEmail(email);
        updateRequest.setPhoneNumber(phone);
        updateRequest.setVehicleDetails(new ArrayList<>(vehicleList));

        // Show subtle saving indicator
        Toast.makeText(getContext(), "Saving vehicle changes...", Toast.LENGTH_SHORT).show();

        // Update profile via API
        profileService.updateUserProfile(userId, updateRequest, new ProfileService.ProfileUpdateCallback() {
            @Override
            public void onSuccess(ProfileResponse updatedProfile) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        currentProfile = updatedProfile;
                        Toast.makeText(getContext(), "Vehicle changes saved!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Vehicle changes saved successfully");
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to save vehicle changes: " + error);
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        String errorMessage = "Failed to save vehicle changes";
                        if (error != null && !error.isEmpty()) {
                            errorMessage += ": " + error;
                        }
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
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

        // Get user ID from preferences
        String userId = preferenceManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(getContext(), "Unable to save: User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable save button and show loading state
        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving...");

        if (isOperator) {
            saveOperatorProfile(userId);
        } else {
            saveEVOwnerProfile(userId);
        }
    }

    private void saveEVOwnerProfile(String userId) {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String nic = etNic.getText().toString().trim();

        // Create update request
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest();
        updateRequest.setNic(nic);
        updateRequest.setFirstName(firstName);
        updateRequest.setLastName(lastName);
        updateRequest.setEmail(email);
        updateRequest.setPhoneNumber(phone);
        
        // Include current vehicle details
        updateRequest.setVehicleDetails(new ArrayList<>(vehicleList));

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
                        Log.d(TAG, "EV Owner profile updated successfully");
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to update EV Owner profile: " + error);
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

    private void saveOperatorProfile(String userId) {
        String username = etFirstName.getText().toString().trim(); // Using firstName field for username
        String email = etEmail.getText().toString().trim();

        // Create update request using current profile as base
        if (currentOperatorProfile == null) {
            Toast.makeText(getContext(), "Profile data not loaded", Toast.LENGTH_SHORT).show();
            btnSaveProfile.setEnabled(true);
            btnSaveProfile.setText("Save Profile");
            return;
        }

        User updateUser = OperatorProfileService.createUpdateRequest(currentOperatorProfile);
        updateUser.setUsername(username);
        updateUser.setEmail(email);

        // Update profile via API
        operatorProfileService.updateOperatorProfile(userId, updateUser, new OperatorProfileService.OperatorProfileUpdateCallback() {
            @Override
            public void onSuccess(User updatedProfile) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        currentOperatorProfile = updatedProfile;
                        btnSaveProfile.setEnabled(true);
                        btnSaveProfile.setText("Save Profile");
                        
                        Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Operator profile updated successfully");
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to update operator profile: " + error);
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
        
        // Reset keyboard state when resuming
        keyboardVisible = false;
        if (scrollView != null) {
            scrollView.setFitsSystemWindows(false);
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Hide keyboard when leaving fragment
        hideKeyboard();
    }

    private void showVehicleDialog(VehicleDetail vehicle, int position) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_vehicle, null);
        
        TextInputEditText etMake = dialogView.findViewById(R.id.etVehicleMake);
        TextInputEditText etModel = dialogView.findViewById(R.id.etVehicleModel);
        TextInputEditText etLicensePlate = dialogView.findViewById(R.id.etLicensePlate);
        TextInputEditText etYear = dialogView.findViewById(R.id.etVehicleYear);
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        
        // Pre-populate if editing
        boolean isEditing = vehicle != null;
        if (isEditing) {
            tvTitle.setText("Edit Vehicle");
            etMake.setText(vehicle.getMake());
            etModel.setText(vehicle.getModel());
            etLicensePlate.setText(vehicle.getLicensePlate());
            etYear.setText(String.valueOf(vehicle.getYear()));
        } else {
            tvTitle.setText("Add Vehicle");
        }
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        
        btnSave.setOnClickListener(v -> {
            if (validateVehicleForm(etMake, etModel, etLicensePlate, etYear)) {
                VehicleDetail newVehicle = new VehicleDetail(
                    etMake.getText().toString().trim(),
                    etModel.getText().toString().trim(),
                    etLicensePlate.getText().toString().trim(),
                    Integer.parseInt(etYear.getText().toString().trim())
                );
                
                if (isEditing) {
                    vehicleAdapter.updateVehicle(position, newVehicle);
                } else {
                    vehicleAdapter.addVehicle(newVehicle);
                }
                
                // Update visibility based on vehicle list size (don't call updateVehicleList as it causes double refresh)
                updateVehicleVisibility();
                
                // Auto-save the profile to sync vehicle changes with backend
                saveVehicleChangesToBackend();
                
                dialog.dismiss();
            }
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private boolean validateVehicleForm(TextInputEditText etMake, TextInputEditText etModel,
                                       TextInputEditText etLicensePlate, TextInputEditText etYear) {
        boolean isValid = true;
        
        // Clear previous errors
        etMake.setError(null);
        etModel.setError(null);
        etLicensePlate.setError(null);
        etYear.setError(null);
        
        String make = etMake.getText().toString().trim();
        ProfileValidator.ValidationResult makeResult = ProfileValidator.validateVehicleMake(make);
        if (makeResult.hasError()) {
            etMake.setError(makeResult.getErrorMessage());
            isValid = false;
        }
        
        String model = etModel.getText().toString().trim();
        ProfileValidator.ValidationResult modelResult = ProfileValidator.validateVehicleModel(model);
        if (modelResult.hasError()) {
            etModel.setError(modelResult.getErrorMessage());
            isValid = false;
        }
        
        String licensePlate = etLicensePlate.getText().toString().trim();
        ProfileValidator.ValidationResult plateResult = ProfileValidator.validateLicensePlate(licensePlate);
        if (plateResult.hasError()) {
            etLicensePlate.setError(plateResult.getErrorMessage());
            isValid = false;
        }
        
        String yearStr = etYear.getText().toString().trim();
        if (yearStr.isEmpty()) {
            etYear.setError("Year is required");
            isValid = false;
        } else {
            try {
                int year = Integer.parseInt(yearStr);
                ProfileValidator.ValidationResult yearResult = ProfileValidator.validateVehicleYear(year);
                if (yearResult.hasError()) {
                    etYear.setError(yearResult.getErrorMessage());
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etYear.setError("Please enter a valid year");
                isValid = false;
            }
        }
        
        return isValid;
    }

    private void showDeleteVehicleDialog(VehicleDetail vehicle, int position) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Remove Vehicle")
               .setMessage("Are you sure you want to remove " + vehicle.getVehicleDisplayName() + "?")
               .setPositiveButton("Remove", (dialog, which) -> {
                   vehicleAdapter.removeVehicle(position);
                   // Update visibility based on vehicle list size (don't call updateVehicleList as it causes double refresh)
                   updateVehicleVisibility();
                   
                   // Auto-save the profile to sync vehicle changes with backend
                   saveVehicleChangesToBackend();
               })
               .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
               .setIcon(android.R.drawable.ic_dialog_alert)
               .show();
    }

    /**
     * Determine if the current user is an operator or EV owner
     */
    private void determineUserType() {
        // Check if user has a role indicating they're an operator
        String userType = preferenceManager.getUserType();
        
        if ("User".equals(userType)) {
            // For User type, we need to check the role from auth response or stored data
            // Station operators have User type but StationOperator role
            isOperator = true;
        } else if ("EVOwner".equals(userType)) {
            isOperator = false;
        } else {
            // Default to EV Owner for backwards compatibility
            isOperator = false;
        }
        
        Log.d(TAG, "User type determined: " + (isOperator ? "Operator" : "EV Owner"));
    }

    /**
     * Configure UI elements based on user type
     */
    private void configureUIForUserType() {
        if (isOperator) {
            // Hide entire vehicle information section for operators
            if (vehicleInformationSection != null) vehicleInformationSection.setVisibility(View.GONE);
            if (btnAddVehicle != null) btnAddVehicle.setVisibility(View.GONE);
            if (recyclerViewVehicles != null) recyclerViewVehicles.setVisibility(View.GONE);
            if (emptyVehicleState != null) emptyVehicleState.setVisibility(View.GONE);
            
            // Update profile heading for operators
            if (profileTitle != null) profileTitle.setText("Operator Profile");
            if (profileDescription != null) profileDescription.setText("View and update your operator information.");
            
            // Configure form fields for operators - only show Username and Email
            updateFirstNameFieldForOperator();
            
            // Hide unnecessary fields and their TextInputLayout containers
            hideFieldAndContainer(etLastName);
            hideFieldAndContainer(etNic);
            hideFieldAndContainer(etPhone);
            
            // Change account deletion to deactivation for operators
            if (btnDeleteAccount != null) {
                btnDeleteAccount.setText("Deactivate Account");
            }
        } else {
            // EV Owner configuration (default)
            if (etNic != null) etNic.setEnabled(false); // NIC is primary key, non-editable
            
            // Show all fields for EV Owners
            showFieldAndContainer(etLastName);
            showFieldAndContainer(etNic);
            showFieldAndContainer(etPhone);
        }
    }

    /**
     * Hide a field and its TextInputLayout container
     */
    private void hideFieldAndContainer(TextInputEditText field) {
        if (field != null && field.getParent() != null && field.getParent().getParent() instanceof View) {
            View container = (View) field.getParent().getParent();
            container.setVisibility(View.GONE);
        }
    }

    /**
     * Show a field and its TextInputLayout container
     */
    private void showFieldAndContainer(TextInputEditText field) {
        if (field != null && field.getParent() != null && field.getParent().getParent() instanceof View) {
            View container = (View) field.getParent().getParent();
            container.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Find the vehicle information section by looking for the LinearLayout containing the heading and add button
     */
    private LinearLayout findVehicleInformationSection(View view) {
        // The vehicle information section is the LinearLayout that contains the "Vehicle Information" TextView and Add button
        // We can find it by looking for the parent of the btnAddVehicle
        if (btnAddVehicle != null && btnAddVehicle.getParent() instanceof LinearLayout) {
            return (LinearLayout) btnAddVehicle.getParent();
        }
        return null;
    }

    /**
     * Update the first name field hint and label for operators
     */
    private void updateFirstNameFieldForOperator() {
        if (etFirstName != null) {
            etFirstName.setHint("Username");
            // Update the TextInputLayout hint as well
            if (etFirstName.getParent() instanceof com.google.android.material.textfield.TextInputLayout) {
                com.google.android.material.textfield.TextInputLayout layout = 
                    (com.google.android.material.textfield.TextInputLayout) etFirstName.getParent();
                layout.setHint("Username");
            }
        }
    }

    /**
     * Populate form fields with operator profile data
     * Only populates visible fields for operators
     */
    private void populateOperatorFields(User profile) {
        if (profile != null) {
            // Only populate visible fields for operators
            etFirstName.setText(profile.getUsername() != null ? profile.getUsername() : "");
            etEmail.setText(profile.getEmail() != null ? profile.getEmail() : "");
            
            // Show username and email in profile heading for operators
            String profileText = (profile.getUsername() != null ? profile.getUsername() : "Operator") + 
                               " • " + (profile.getEmail() != null ? profile.getEmail() : "No email");
            profileName.setText(profileText);
            
            // Clear hidden fields (in case they were previously populated)
            etLastName.setText("");
            etNic.setText("");
            etPhone.setText("");
        }
    }

    /**
     * Show error for operator and load fallback data
     */
    private void showOperatorErrorAndLoadFallback() {
        // Load fallback data from preferences
        String userName = preferenceManager.getUserName();
        String email = preferenceManager.getUserEmail();
        
        // Only populate visible fields for operators
        etFirstName.setText(userName != null ? userName : "");
        etEmail.setText(email != null ? email : "");
        
        // Show username and email in profile heading for operators (fallback)
        String profileText = (userName != null ? userName : "Operator") + 
                           " • " + (email != null ? email : "No email");
        profileName.setText(profileText);
        
        // Clear hidden fields
        etLastName.setText("");
        etNic.setText("");
        etEmail.setText(email != null ? email : "");
        etPhone.setText("");
        
        if (profileName != null) {
            profileName.setText(userName != null ? userName : "Operator");
        }
    }

    /**
     * Show change password dialog
     */
    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password, null);
        
        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        Button btnChangePassword = dialogView.findViewById(R.id.btnChangePassword);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        
        btnChangePassword.setOnClickListener(v -> {
            if (validateChangePasswordForm(etCurrentPassword, etNewPassword, etConfirmPassword)) {
                String currentPassword = etCurrentPassword.getText().toString().trim();
                String newPassword = etNewPassword.getText().toString().trim();
                
                // Disable button and show loading
                btnChangePassword.setEnabled(false);
                btnChangePassword.setText("Changing...");
                
                authService.changePassword(currentPassword, newPassword, new AuthService.AuthCallback() {
                    @Override
                    public void onSuccess(AuthResponse response) {
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                dialog.dismiss();
                                Toast.makeText(getContext(), "Password changed successfully! Please log in again.", Toast.LENGTH_LONG).show();
                                
                                // Navigate to login screen since all sessions are invalidated
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                getActivity().finish();
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                btnChangePassword.setEnabled(true);
                                btnChangePassword.setText("Change Password");
                                
                                String errorMessage = "Failed to change password";
                                if (error != null && !error.isEmpty()) {
                                    errorMessage += ": " + error;
                                }
                                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                            });
                        }
                    }
                });
            }
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    /**
     * Validate change password form
     */
    private boolean validateChangePasswordForm(TextInputEditText etCurrentPassword, 
                                             TextInputEditText etNewPassword, 
                                             TextInputEditText etConfirmPassword) {
        boolean isValid = true;
        
        // Clear previous errors
        etCurrentPassword.setError(null);
        etNewPassword.setError(null);
        etConfirmPassword.setError(null);
        
        String currentPassword = etCurrentPassword.getText().toString().trim();
        if (currentPassword.isEmpty()) {
            etCurrentPassword.setError("Current password is required");
            isValid = false;
        }
        
        String newPassword = etNewPassword.getText().toString().trim();
        if (newPassword.isEmpty()) {
            etNewPassword.setError("New password is required");
            isValid = false;
        } else if (newPassword.length() < 8) {
            etNewPassword.setError("Password must be at least 8 characters");
            isValid = false;
        } else if (!isValidPassword(newPassword)) {
            etNewPassword.setError("Password must contain uppercase, lowercase, number, and special character");
            isValid = false;
        }
        
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Confirm password is required");
            isValid = false;
        } else if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }
        
        return isValid;
    }

    /**
     * Check if password meets requirements
     */
    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }
}
