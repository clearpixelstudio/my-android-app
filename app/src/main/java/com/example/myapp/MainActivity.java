package com.example.myapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private SwipeRefreshLayout swipeRefresh;
    private FrameLayout offlinePage;
    private ProgressBar loadingBar;
    private boolean backPressedOnce = false;
    private int pageLoadCount = 0;
    
    
    private static final String APP_URL = "file:///android_asset/index.html";
    private static final int VERSION_CODE = 1;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        

        // Root layout
        RelativeLayout root = new RelativeLayout(this);
        root.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        

        // Loading progress bar
        loadingBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        loadingBar.setMax(100);
        loadingBar.setProgress(0);
        int primaryColor = ContextCompat.getColor(this, R.color.colorPrimary);
        loadingBar.getProgressDrawable().setColorFilter(
            new PorterDuffColorFilter(primaryColor, PorterDuff.Mode.SRC_IN));
        RelativeLayout.LayoutParams pbParams = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 8);
        pbParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        root.addView(loadingBar, pbParams);

        
        // SwipeRefreshLayout wraps the WebView
        swipeRefresh = new SwipeRefreshLayout(this);
        swipeRefresh.setColorSchemeColors(primaryColor);
        RelativeLayout.LayoutParams srParams = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        
        root.addView(swipeRefresh, srParams);

        webView = new WebView(this);
        swipeRefresh.addView(webView, new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        swipeRefresh.setOnRefreshListener(() -> {
            webView.reload();
        });
        

        // Offline page
        offlinePage = buildOfflinePage();
        RelativeLayout.LayoutParams opParams = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        
        root.addView(offlinePage, opParams);
        offlinePage.setVisibility(View.GONE);

        setContentView(root);

        // WebView settings
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMediaPlaybackRequiresUserGesture(false);
        
        settings.setSupportZoom(false);

        

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                loadingBar.setProgress(progress);
                loadingBar.setVisibility(progress < 100 ? View.VISIBLE : View.GONE);
                if (progress == 100 && swipeRefresh != null) swipeRefresh.setRefreshing(false);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://")) {
                    return false; // Load in WebView
                }
                // External app (mailto, tel, etc.)
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception e) { /* ignore */ }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                offlinePage.setVisibility(View.GONE);
                loadingBar.setVisibility(View.GONE);
                pageLoadCount++;

                

                

                
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String url) {
                super.onReceivedError(view, errorCode, description, url);
                if (!isNetworkAvailable()) {
                    offlinePage.setVisibility(View.VISIBLE);
                }
            }
        });

        

        webView.loadUrl(APP_URL);

        
    }

    private FrameLayout buildOfflinePage() {
        FrameLayout page = new FrameLayout(this);
        page.setBackgroundColor(Color.WHITE);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);
        ll.setPadding(48, 48, 48, 48);

        TextView emoji = new TextView(this);
        emoji.setText("📡");
        emoji.setTextSize(48f);
        emoji.setGravity(Gravity.CENTER);
        ll.addView(emoji);

        TextView title = new TextView(this);
        title.setText(R.string.no_internet_title);
        title.setTextSize(20f);
        title.setTextColor(Color.parseColor("#333333"));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 24, 0, 8);
        ll.addView(title);

        TextView msg = new TextView(this);
        msg.setText(R.string.no_internet_msg);
        msg.setTextSize(15f);
        msg.setTextColor(Color.parseColor("#666666"));
        msg.setGravity(Gravity.CENTER);
        msg.setPadding(0, 0, 0, 32);
        ll.addView(msg);

        Button retryBtn = new Button(this);
        retryBtn.setText(R.string.retry);
        retryBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        retryBtn.setTextColor(Color.WHITE);
        retryBtn.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                offlinePage.setVisibility(View.GONE);
                webView.reload();
            } else {
                Toast.makeText(this, R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
            }
        });
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.gravity = Gravity.CENTER_HORIZONTAL;
        ll.addView(retryBtn, btnParams);

        FrameLayout.LayoutParams llParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        llParams.gravity = Gravity.CENTER;
        page.addView(ll, llParams);
        return page;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnectedOrConnecting();
    }

    

    

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
            return;
        }
        
        if (backPressedOnce) {
            super.onBackPressed();
            return;
        }
        backPressedOnce = true;
        Toast.makeText(this, R.string.press_back_exit, Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> backPressedOnce = false, 2000);
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        
    }

    @Override
    protected void onPause() {
        
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}