package com.appbuilder.myapp;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(android.view.Gravity.CENTER);
        root.setBackgroundColor(getResources().getColor(R.color.colorSplash, getTheme()));
        ImageView icon = new ImageView(this);
        icon.setImageResource(R.mipmap.ic_launcher);
        icon.setAlpha(0f);
        int sz = dpToPx(96);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sz, sz);
        icon.setLayoutParams(lp);
        TextView name = new TextView(this);
        name.setText("App builder");
        name.setTextColor(getResources().getColor(R.color.white, getTheme()));
        name.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 24);
        name.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        name.setGravity(android.view.Gravity.CENTER);
        name.setAlpha(0f);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        nlp.topMargin = dpToPx(16);
        name.setLayoutParams(nlp);
        root.addView(icon);
        root.addView(name);
        setContentView(root);
        // Animate in
        ObjectAnimator iconFade = ObjectAnimator.ofFloat(icon, View.ALPHA, 0f, 1f);
        ObjectAnimator iconScale = ObjectAnimator.ofFloat(icon, View.SCALE_X, 0.6f, 1f);
        ObjectAnimator iconScaleY = ObjectAnimator.ofFloat(icon, View.SCALE_Y, 0.6f, 1f);
        ObjectAnimator nameFade = ObjectAnimator.ofFloat(name, View.ALPHA, 0f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(iconFade, iconScale, iconScaleY, nameFade);
        set.setDuration(600);
        set.setInterpolator(new DecelerateInterpolator());
        set.start();
        icon.postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        }, 1800);
    }
    private int dpToPx(int dp) {
        return (int)(dp * getResources().getDisplayMetrics().density);
    }
}