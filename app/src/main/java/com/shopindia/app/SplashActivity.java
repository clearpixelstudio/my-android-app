package com.shopindia.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.splash_logo);
        TextView appName = findViewById(R.id.splash_app_name);
        TextView tagline = findViewById(R.id.splash_tagline);

        // Logo: scale + fade in
        if (logo != null) {
            AnimationSet logoAnim = new AnimationSet(true);
            ScaleAnimation scale = new ScaleAnimation(
                0.6f, 1f, 0.6f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            );
            scale.setDuration(500);
            AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
            fadeIn.setDuration(500);
            logoAnim.addAnimation(scale);
            logoAnim.addAnimation(fadeIn);
            logoAnim.setFillAfter(true);
            logo.startAnimation(logoAnim);
        }

        // App name: fade in with slight delay
        if (appName != null) {
            AlphaAnimation nameFade = new AlphaAnimation(0f, 1f);
            nameFade.setStartOffset(300);
            nameFade.setDuration(400);
            nameFade.setFillAfter(true);
            appName.startAnimation(nameFade);
        }

        // Tagline: fade in last
        if (tagline != null) {
            AlphaAnimation tagFade = new AlphaAnimation(0f, 1f);
            tagFade.setStartOffset(550);
            tagFade.setDuration(400);
            tagFade.setFillAfter(true);
            tagline.startAnimation(tagFade);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 2000);
    }
}
