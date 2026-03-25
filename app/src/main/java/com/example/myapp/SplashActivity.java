package com.example.myapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(ContextCompat.getColor(this, R.color.splashBackground));

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setAlpha(0f);

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.mipmap.ic_launcher);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(192, 192);
        iconParams.gravity = Gravity.CENTER_HORIZONTAL;
        iconParams.bottomMargin = 32;
        container.addView(icon, iconParams);

        TextView title = new TextView(this);
        title.setText(getString(R.string.app_name));
        title.setTextSize(24f);
        title.setTextColor(ContextCompat.getColor(this, R.color.colorOnPrimary));
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.gravity = Gravity.CENTER_HORIZONTAL;
        container.addView(title, titleParams);

        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        containerParams.gravity = Gravity.CENTER;
        root.addView(container, containerParams);
        setContentView(root);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(container, "alpha", 0f, 1f);
        fadeIn.setDuration(700);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(container, "scaleX", 0.85f, 1f);
        scaleX.setDuration(700);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(container, "scaleY", 0.85f, 1f);
        scaleY.setDuration(700);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(fadeIn, scaleX, scaleY);
        animSet.start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 1800);
    }
}