package com.example.projet_tp.ui.auth;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projet_tp.R;
import com.example.projet_tp.ui.main.HomeActivity;
import com.example.projet_tp.utils.ReservationManager;
import com.example.projet_tp.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000;
    private SessionManager sessionManager;
    private ReservationManager reservationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);
        reservationManager = new ReservationManager(this);

        reservationManager.cleanExpiredReservations();

        startAnimations();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkLoginStatus();
            finish();
        }, SPLASH_DELAY);
    }

    private void startAnimations() {
        View logoCard = findViewById(R.id.logoCard);
        View appTitle = findViewById(R.id.appTitle);

        if (logoCard != null) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoCard, "scaleX", 0f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoCard, "scaleY", 0f, 1f);
            ObjectAnimator rotation = ObjectAnimator.ofFloat(logoCard, "rotation", -180f, 0f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(logoCard, "alpha", 0f, 1f);

            AnimatorSet logoSet = new AnimatorSet();
            logoSet.playTogether(scaleX, scaleY, rotation, alpha);
            logoSet.setDuration(1000);
            logoSet.setInterpolator(new OvershootInterpolator());
            logoSet.start();
        }

        if (appTitle != null) {
            appTitle.setAlpha(0f);
            appTitle.setTranslationY(30f);
            appTitle.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .setStartDelay(500)
                    .start();
        }
    }

    private void checkLoginStatus() {
        Intent intent;
        boolean isLoggedIn = sessionManager.isLoggedIn();

        if (isLoggedIn) {
            intent = new Intent(SplashActivity.this, HomeActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
    }
}