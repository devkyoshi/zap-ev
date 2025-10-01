package com.ead.zap.ui.owner;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import com.ead.zap.R;
import com.ead.zap.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class EVOwnerMain extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_owner_main);

            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            if (bottomNavigationView != null) {
                bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
            }

            // Load the default fragment or specified fragment
            if (savedInstanceState == null) {
                // Check if we should open the bookings tab directly
                if (getIntent().getBooleanExtra("open_bookings_tab", false)) {
                    loadFragment(new OwnerBookingsFragment());
                    bottomNavigationView.setSelectedItemId(R.id.navigation_bookings);
                } else {
                    loadFragment(new OwnerHomeFragment());
                }
            }
        } catch (Exception e) {
            // Handle any potential crashes during initialization
            e.printStackTrace();
            finish();
        }
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    selectedFragment = new OwnerHomeFragment();
                } else if (itemId == R.id.navigation_bookings) {
                    selectedFragment = new OwnerBookingsFragment();
                } else if (itemId == R.id.navigation_map) {
                    selectedFragment = new OwnerMapsFragment();
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
                transaction.replace(R.id.fragment_container, fragment);
                transaction.commitAllowingStateLoss(); // Use this to prevent IllegalStateException
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
