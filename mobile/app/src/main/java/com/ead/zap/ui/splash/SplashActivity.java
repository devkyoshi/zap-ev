package com.ead.zap.ui.splash;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.ead.zap.R;
import com.ead.zap.ui.onboarding.OnboardingActivity;
import com.ead.zap.utils.PreferenceManager;

import java.util.Random;

/**
 * SplashActivity - Visually appealing splash screen with loading animations
 * Features:
 * - Gradient background with electric theme
 * - Rotating electric ring animation
 * - Pulsing logo with glow effect
 * - Animated progress bar
 * - Floating electric particles
 * - Dynamic loading text updates
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3500; // 3.5 seconds
    private static final int PROGRESS_ANIMATION_DURATION = 3000;
    private static final int TEXT_UPDATE_INTERVAL = 800;

    private PreferenceManager preferenceManager;
    
    // UI Components
    private ImageView ivLogo;
    private ImageView ivElectricRing;
    private ImageView ivParticle1, ivParticle2, ivParticle3;
    private TextView tvAppName;
    private TextView tvTagline;
    private TextView tvLoadingText;
    private View progressBar;
    private View logoBackground;

    // Animation handlers
    private Handler textUpdateHandler;
    private Handler splashHandler;
    private Runnable textUpdateRunnable;

    // Loading text states
    private final String[] loadingTexts = {
            "Initializing app…",
            "Connecting to services…", 
            "Loading charging stations…",
            "Preparing interface…",
            "Almost ready…"
    };
    private int currentTextIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Install splash screen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        preferenceManager = new PreferenceManager(this);

        // Initialize UI components
        initializeViews();
        
        // Start animations
        startSplashAnimations();
        
        // Start loading text updates
        startLoadingTextUpdates();
        
        // Start progress bar animation
        startProgressBarAnimation();
        
        // Navigate to next screen after delay
        navigateToNextScreen();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        ivLogo = findViewById(R.id.ivLogo);
        ivElectricRing = findViewById(R.id.ivElectricRing);
        ivParticle1 = findViewById(R.id.ivParticle1);
        ivParticle2 = findViewById(R.id.ivParticle2);
        ivParticle3 = findViewById(R.id.ivParticle3);
        tvAppName = findViewById(R.id.tvAppName);
        tvTagline = findViewById(R.id.tvTagline);
        tvLoadingText = findViewById(R.id.tvLoadingText);
        progressBar = findViewById(R.id.progressBar);
        logoBackground = findViewById(R.id.logoBackground);
    }

    /**
     * Start all splash screen animations
     */
    private void startSplashAnimations() {
        // Logo pulse animation
        startLogoPulseAnimation();
        
        // Electric ring rotation
        startElectricRingAnimation();
        
        // Floating particles animation
        startParticlesAnimation();
        
        // App name slide-in animation
        startAppNameAnimation();
        
        // Tagline fade-in animation
        startTaglineAnimation();
        
        // Logo background glow animation
        startLogoBackgroundAnimation();
    }

    /**
     * Logo pulsing animation with scale effect
     */
    private void startLogoPulseAnimation() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivLogo, "scaleX", 0.8f, 1.1f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivLogo, "scaleY", 0.8f, 1.1f, 1.0f);
        
        scaleX.setDuration(2000);
        scaleY.setDuration(2000);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.setRepeatMode(ObjectAnimator.REVERSE);
        scaleY.setRepeatMode(ObjectAnimator.REVERSE);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        
        scaleX.start();
        scaleY.start();
    }

    /**
     * Electric ring continuous rotation animation
     */
    private void startElectricRingAnimation() {
        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_continuous);
        ivElectricRing.startAnimation(rotateAnimation);
    }

    /**
     * Floating particles animation with random movement
     */
    private void startParticlesAnimation() {
        startParticleAnimation(ivParticle1, 1500);
        startParticleAnimation(ivParticle2, 2000);
        startParticleAnimation(ivParticle3, 1800);
    }

    /**
     * Individual particle floating animation
     */
    private void startParticleAnimation(ImageView particle, int duration) {
        Random random = new Random();
        
        // Random vertical movement
        ObjectAnimator moveY = ObjectAnimator.ofFloat(particle, "translationY", 
                0, -30 - random.nextInt(20), 0);
        moveY.setDuration(duration);
        moveY.setRepeatCount(ObjectAnimator.INFINITE);
        moveY.setRepeatMode(ObjectAnimator.REVERSE);
        moveY.setInterpolator(new AccelerateDecelerateInterpolator());
        
        // Alpha animation
        ObjectAnimator alpha = ObjectAnimator.ofFloat(particle, "alpha", 0.3f, 0.9f, 0.3f);
        alpha.setDuration(duration);
        alpha.setRepeatCount(ObjectAnimator.INFINITE);
        alpha.setRepeatMode(ObjectAnimator.REVERSE);
        
        moveY.start();
        alpha.start();
    }

    /**
     * App name slide-in animation from bottom
     */
    private void startAppNameAnimation() {
        tvAppName.setTranslationY(100);
        tvAppName.setAlpha(0);
        
        tvAppName.animate()
                .translationY(0)
                .alpha(1.0f)
                .setDuration(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    /**
     * Tagline fade-in animation with delay
     */
    private void startTaglineAnimation() {
        tvTagline.setAlpha(0);
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            tvTagline.animate()
                    .alpha(0.9f)
                    .setDuration(800)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }, 500);
    }

    /**
     * Logo background glow effect
     */
    private void startLogoBackgroundAnimation() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoBackground, "scaleX", 1.0f, 1.1f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoBackground, "scaleY", 1.0f, 1.1f, 1.0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(logoBackground, "alpha", 0.8f, 1.0f, 0.8f);
        
        scaleX.setDuration(3000);
        scaleY.setDuration(3000);
        alpha.setDuration(3000);
        
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        alpha.setRepeatCount(ObjectAnimator.INFINITE);
        
        scaleX.setRepeatMode(ObjectAnimator.REVERSE);
        scaleY.setRepeatMode(ObjectAnimator.REVERSE);
        alpha.setRepeatMode(ObjectAnimator.REVERSE);
        
        scaleX.start();
        scaleY.start();
        alpha.start();
    }

    /**
     * Animated progress bar that fills smoothly
     */
    private void startProgressBarAnimation() {
        ValueAnimator progressAnimator = ValueAnimator.ofInt(0, 200);
        progressAnimator.setDuration(PROGRESS_ANIMATION_DURATION);
        progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        progressAnimator.addUpdateListener(animation -> {
            int width = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams params = progressBar.getLayoutParams();
            params.width = width;
            progressBar.setLayoutParams(params);
        });
        
        progressAnimator.start();
    }

    /**
     * Dynamic loading text updates
     */
    private void startLoadingTextUpdates() {
        textUpdateHandler = new Handler(Looper.getMainLooper());
        textUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentTextIndex < loadingTexts.length) {
                    // Fade out current text
                    tvLoadingText.animate()
                            .alpha(0.0f)
                            .setDuration(200)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    // Update text and fade in
                                    tvLoadingText.setText(loadingTexts[currentTextIndex]);
                                    tvLoadingText.animate()
                                            .alpha(0.8f)
                                            .setDuration(200)
                                            .setListener(null)
                                            .start();
                                    
                                    currentTextIndex++;
                                    if (currentTextIndex < loadingTexts.length) {
                                        textUpdateHandler.postDelayed(textUpdateRunnable, TEXT_UPDATE_INTERVAL);
                                    }
                                }
                            })
                            .start();
                } else {
                    // Show final loading text
                    tvLoadingText.animate()
                            .alpha(0.0f)
                            .setDuration(200)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    tvLoadingText.setText(getString(R.string.loading_text));
                                    tvLoadingText.animate()
                                            .alpha(0.8f)
                                            .setDuration(200)
                                            .setListener(null)
                                            .start();
                                }
                            })
                            .start();
                }
            }
        };
        
        // Start text updates after initial delay
        textUpdateHandler.postDelayed(textUpdateRunnable, TEXT_UPDATE_INTERVAL);
    }

    /**
     * Navigate to the next appropriate screen after splash duration
     */
    private void navigateToNextScreen() {
        splashHandler = new Handler(Looper.getMainLooper());
        splashHandler.postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, OnboardingActivity.class);
            startActivity(intent);
            
            // Add smooth transition animation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DURATION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up handlers to prevent memory leaks
        if (textUpdateHandler != null) {
            textUpdateHandler.removeCallbacks(textUpdateRunnable);
        }
        if (splashHandler != null) {
            splashHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onBackPressed() {
        // Disable back button on splash screen
        // Do nothing to prevent users from going back
    }
}