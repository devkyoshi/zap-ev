package com.ead.zap.ui.auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import com.ead.zap.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFirstName, etLastName, etNic, etEmail, etPhone, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin, tvEvOwner, tvStationOperator;

    private CardView cardEvOwner, cardStationOperator;
    private NestedScrollView scrollView;
    private String selectedRole = "EV_OWNER"; // Default role

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Adjust layout when keyboard appears
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initViews();
        setupClickListeners();
        setupRoleSelection();
        setupFocusListeners();
    }

    private void initViews() {
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etNic = findViewById(R.id.et_nic);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        cardEvOwner = findViewById(R.id.card_ev_owner);
        cardStationOperator = findViewById(R.id.card_station_operator);
        scrollView = findViewById(R.id.nestedScrollView);
        tvEvOwner = findViewById(R.id.tv_ev_owner);
        tvStationOperator = findViewById(R.id.tv_station_operator);
    }

    private void setupFocusListeners() {
        // Set up focus change listeners to scroll to focused field
        View[] fields = {etFirstName, etLastName, etNic, etEmail, etPhone, etPassword, etConfirmPassword};

        for (View field : fields) {
            field.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    // Scroll to the focused field with a slight delay
                    scrollView.postDelayed(() -> scrollView.smoothScrollTo(0, v.getBottom()), 100);
                }
            });
        }
    }

    private void setupRoleSelection() {
        // Set initial selection
        selectRole("EV_OWNER");

        cardEvOwner.setOnClickListener(v -> {
            selectRole("EV_OWNER");
            hideKeyboard();
        });

        cardStationOperator.setOnClickListener(v -> {
            selectRole("STATION_OPERATOR");
            hideKeyboard();
        });
    }

    private void selectRole(String role) {
        selectedRole = role;

        if (role.equals("EV_OWNER")) {
            cardEvOwner.setCardBackgroundColor(getColor(R.color.accent));
            cardEvOwner.setCardElevation(4f);

            tvEvOwner.setTextColor(getColor(R.color.white));
            tvStationOperator.setTextColor(getColor(R.color.accent));


            cardStationOperator.setCardBackgroundColor(getColor(R.color.white));
            cardStationOperator.setCardElevation(2f);
        } else {

            tvEvOwner.setTextColor(getColor(R.color.accent));
            tvStationOperator.setTextColor(getColor(R.color.white));


            cardStationOperator.setCardBackgroundColor(getColor(R.color.accent));
            cardStationOperator.setCardElevation(4f);

            cardEvOwner.setCardBackgroundColor(getColor(R.color.white));
            cardEvOwner.setCardElevation(2f);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> {
            hideKeyboard();
            performRegistration();
        });

        tvLogin.setOnClickListener(v -> {
            hideKeyboard();
            finish();
        });

        // Hide keyboard when clicking outside fields
        scrollView.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return false;
        });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void performRegistration() {
        String firstName = Objects.requireNonNull(etFirstName.getText()).toString().trim();
        String lastName = Objects.requireNonNull(etLastName.getText()).toString().trim();
        String nic = Objects.requireNonNull(etNic.getText()).toString().trim();
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String phone = Objects.requireNonNull(etPhone.getText()).toString().trim();
        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(etConfirmPassword.getText()).toString().trim();

        // Validate inputs
        if (firstName.isEmpty()) {
            etFirstName.setError("First name is required");
            scrollView.smoothScrollTo(0, etFirstName.getTop());
            return;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("Last name is required");
            scrollView.smoothScrollTo(0, etLastName.getTop());
            return;
        }

        if (nic.isEmpty()) {
            etNic.setError("NIC is required");
            scrollView.smoothScrollTo(0, etNic.getTop());
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            scrollView.smoothScrollTo(0, etEmail.getTop());
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            scrollView.smoothScrollTo(0, etPhone.getTop());
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            scrollView.smoothScrollTo(0, etPassword.getTop());
            return;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Please confirm your password");
            scrollView.smoothScrollTo(0, etConfirmPassword.getTop());
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            scrollView.smoothScrollTo(0, etConfirmPassword.getTop());
            return;
        }

        // Perform registration logic here
        Toast.makeText(this, "Registering as " + selectedRole, Toast.LENGTH_SHORT).show();
    }
}