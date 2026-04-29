package com.example.myapp;
import android.app.Activity; import android.content.Intent; import android.os.Bundle;
import android.os.Handler; import android.os.Looper;
public class SplashActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b); setContentView(R.layout.activity_splash);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class)); finish();
        }, 2000L);
    }
}