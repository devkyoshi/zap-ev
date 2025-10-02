package com.ead.zap.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
// import com.ead.zap.MainActivity;


import com.ead.zap.MainActivity;
import com.ead.zap.R;
import com.ead.zap.ui.owner.EVOwnerMain;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignUp, tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvSignUp = findViewById(R.id.tv_signup);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to SignUp activity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Forgot Password activity
                Toast.makeText(LoginActivity.this, "Forgot Password clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mock authentication logic
        Intent intent;
        if (email.equals("stationoperator@mail.com")) {
            // Navigate to Station Operator main activity
            intent = new Intent(LoginActivity.this, com.ead.zap.ui.operator.StationOperatorMain.class);
            Toast.makeText(this, "Welcome Station Operator!", Toast.LENGTH_SHORT).show();
        } else if (email.equals("evowner@mail.com")) {
            // Navigate to EV Owner main activity
            intent = new Intent(LoginActivity.this, EVOwnerMain.class);
            Toast.makeText(this, "Welcome EV Owner!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Invalid credentials. Use stationoperator@mail.com or evowner@mail.com", Toast.LENGTH_LONG).show();
            return;
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}