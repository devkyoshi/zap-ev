package com.ead.zap.ui.owner;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ead.zap.R;
import com.ead.zap.ui.auth.LoginActivity;

public class ReactivationInfoActivity extends AppCompatActivity {

    private TextView tvTitle, tvMessage, tvContactInfo, tvEmailInfo, tvPhoneInfo;
    private Button btnContactSupport, btnCallSupport, btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reactivation_info);

        setupToolbar();
        initViews();
        setupClickListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Account Reactivation");
        }
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvMessage = findViewById(R.id.tv_message);
        tvContactInfo = findViewById(R.id.tv_contact_info);
        tvEmailInfo = findViewById(R.id.tv_email_info);
        tvPhoneInfo = findViewById(R.id.tv_phone_info);
        btnContactSupport = findViewById(R.id.btn_contact_support);
        btnCallSupport = findViewById(R.id.btn_call_support);
        btnBackToLogin = findViewById(R.id.btn_back_to_login);

        // Set the content
        tvTitle.setText("Account Deactivated");
        tvMessage.setText("Your account has been deactivated and cannot be used to access the application at this time.");
        tvContactInfo.setText("To reactivate your account, please contact our backoffice team:");
        tvEmailInfo.setText("support@zap-ev.com");
        tvPhoneInfo.setText("+94 11 234 5678");
    }

    private void setupClickListeners() {
        btnContactSupport.setOnClickListener(v -> {
            // Open email client
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:support@zap-ev.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Account Reactivation Request");
            emailIntent.putExtra(Intent.EXTRA_TEXT, 
                "Dear ZAP EV Support Team,\n\n" +
                "I would like to request reactivation of my account.\n\n" +
                "Account Details:\n" +
                "- Email: [Your Email]\n" +
                "- NIC: [Your NIC]\n" +
                "- Phone: [Your Phone]\n\n" +
                "Please let me know if you need any additional information.\n\n" +
                "Thank you,\n" +
                "[Your Name]");
            
            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(emailIntent, "Send Email"));
            }
        });

        btnCallSupport.setOnClickListener(v -> {
            // Open dialer
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:+94112345678"));
            
            if (callIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(callIntent);
            }
        });

        btnBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Handle back button
        if (getSupportActionBar() != null) {
            findViewById(android.R.id.home).setOnClickListener(v -> onBackPressed());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        // Navigate back to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}