package com.ead.zap.ui.operator;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ead.zap.R;
import com.ead.zap.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class StationOperatorMain extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_station_operator_main);

            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_operator);
            if (bottomNavigationView != null) {
                bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
            }

            // Load the default fragment
            if (savedInstanceState == null) {
                loadFragment(new OperatorQRScannerFragment());
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();
                if (itemId == R.id.navigation_scanner) {
                    selectedFragment = new OperatorQRScannerFragment();
                } else if (itemId == R.id.navigation_history) {
                    selectedFragment = new OperatorHistoryFragment();
                } else if (itemId == R.id.navigation_profile) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                }

                return true;
            };

    private void loadFragment(Fragment fragment) {
        try {
            if (fragment != null && !isFinishing()) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container_operator, fragment);
                transaction.commitAllowingStateLoss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}