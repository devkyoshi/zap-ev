package com.ead.zap.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;
import com.ead.zap.R;
import com.ead.zap.ui.auth.LoginActivity;
import com.ead.zap.utils.PreferenceManager;
import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private OnboardingAdapter onboardingAdapter;
    private LinearLayout layoutDots;
    private Button btnSkip;
    private Button btnNext;
    private PreferenceManager preferenceManager;
    private final List<OnboardingItem> onboardingItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        preferenceManager = new PreferenceManager(this);

        // If not first time, skip to login
        if (!preferenceManager.isFirstTimeLaunch()) {
            launchLoginScreen();
            finish();
        }

        layoutDots = findViewById(R.id.layoutDots);
        btnSkip = findViewById(R.id.btnSkip);
        btnNext = findViewById(R.id.btnNext);

        // Setup onboarding items
        setupOnboardingItems();

        ViewPager2 onboardingViewPager = findViewById(R.id.viewPagerOnboarding);
        onboardingAdapter = new OnboardingAdapter(onboardingItems);
        onboardingViewPager.setAdapter(onboardingAdapter);

        setupOnboardingIndicators();
        setCurrentOnboardingIndicator(0);

        onboardingViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentOnboardingIndicator(position);

                if (position == onboardingItems.size() - 1) {
                    btnNext.setText(R.string.get_started);
                    btnSkip.setVisibility(View.GONE);
                } else {
                    btnNext.setText(R.string.next);
                    btnSkip.setVisibility(View.VISIBLE);
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            int currentPosition = onboardingViewPager.getCurrentItem();
            if (currentPosition < onboardingItems.size() - 1) {
                onboardingViewPager.setCurrentItem(currentPosition + 1);
            } else {
                preferenceManager.setFirstTimeLaunch(false);
                launchLoginScreen();
            }
        });

        btnSkip.setOnClickListener(v -> {
            preferenceManager.setFirstTimeLaunch(false);
            launchLoginScreen();
        });
    }

    private void setupOnboardingItems() {
        // Adding placeholder images - replace with actual drawable resources
        onboardingItems.add(new OnboardingItem(
                R.drawable.onboarding_1,
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_desc_1)
        ));

        onboardingItems.add(new OnboardingItem(
                R.drawable.onboarding_2,
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_desc_2)
        ));

        onboardingItems.add(new OnboardingItem(
                R.drawable.onboarding_3,
                getString(R.string.onboarding_title_3),
                getString(R.string.onboarding_desc_3)
        ));
    }

    private void setupOnboardingIndicators() {
        View[] indicators = new View[onboardingAdapter.getItemCount()];

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                16, // width in px
                16  // height in px
        );
        layoutParams.setMargins(8, 0, 8, 0);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new View(this);
            indicators[i].setBackground(ContextCompat.getDrawable(
                    this, R.drawable.onboarding_indicator_inactive
            ));
            indicators[i].setLayoutParams(layoutParams);
            layoutDots.addView(indicators[i]);
        }
    }

    private void setCurrentOnboardingIndicator(int index) {
        int childCount = layoutDots.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = layoutDots.getChildAt(i);
            view.setBackground(ContextCompat.getDrawable(
                    getApplicationContext(),
                    i == index ? R.drawable.onboarding_indicator_active : R.drawable.onboarding_indicator_inactive
            ));
        }
    }

    private void launchLoginScreen() {
        startActivity(new Intent(OnboardingActivity.this, LoginActivity.class));
        finish();
    }
}
