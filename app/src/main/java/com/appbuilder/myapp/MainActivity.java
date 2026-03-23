package com.appbuilder.myapp;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private ValueCallback<Uri[]> filePathCallback;
    private static final int FILE_REQ = 100;
    private static final String UPDATE_URL = "https://raw.githubusercontent.com/clearpixelstudio/my-android-app/main/update.json";
    private boolean doubleBackPressed = false;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary, getTheme()));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary, getTheme()));
        // ── Root layout ──────────────────────────────────────
        RelativeLayout root = new RelativeLayout(this);
        root.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
        // SwipeRefresh + WebView
        swipeRefresh = new SwipeRefreshLayout(this);
        RelativeLayout.LayoutParams srp = new RelativeLayout.LayoutParams(-1, -1);
        swipeRefresh.setLayoutParams(srp);
        swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary, getTheme()));
        webView = new WebView(this);
        webView.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
        swipeRefresh.addView(webView);
        // Progress bar
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        RelativeLayout.LayoutParams pbp = new RelativeLayout.LayoutParams(-1, 5);
        pbp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        progressBar.setLayoutParams(pbp);
        progressBar.setMax(100);
        progressBar.getProgressDrawable().setColorFilter(
            getResources().getColor(R.color.colorPrimary, getTheme()),
            android.graphics.PorterDuff.Mode.SRC_IN);
        root.addView(swipeRefresh);
        root.addView(progressBar);
        setContentView(root);
        // ── WebSettings ──────────────────────────────────────
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setAllowFileAccessFromFileURLs(true);
        ws.setAllowUniversalAccessFromFileURLs(true);
        ws.setLoadsImagesAutomatically(true);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);
        ws.setMediaPlaybackRequiresUserGesture(false);
        ws.setBuiltInZoomControls(false);
        ws.setUseWideViewPort(true);
        ws.setLoadWithOverviewMode(true);
        ws.setGeolocationEnabled(true);
        // ── Swipe to refresh ─────────────────────────────────
        swipeRefresh.setOnRefreshListener(() -> {
            webView.reload();
            swipeRefresh.setRefreshing(false);
        });
        // ── WebViewClient ────────────────────────────────────
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest req) {
                String url = req.getUrl().toString();
                if (url.startsWith("file://") || url.startsWith("http") || url.startsWith("about:")) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception e) {}
                return true;
            }
            @Override public void onPageStarted(WebView v, String url, Bitmap f) {
                progressBar.setVisibility(View.VISIBLE); progressBar.setProgress(10);
            }
            @Override public void onPageFinished(WebView v, String url) {
                progressBar.setProgress(100); progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
            }
            @Override public void onReceivedError(WebView v, WebResourceRequest req, WebResourceError err) {
                if (!isConnected()) {
                    v.loadData("<html><body style='background:#0a0c10;color:#cdd9e5;font-family:sans-serif;display:flex;flex-direction:column;align-items:center;justify-content:center;min-height:100vh;margin:0;text-align:center;padding:24px'><div style='font-size:3em;margin-bottom:16px'>📶</div><h2 style='color:#f85149'>No Internet</h2><p style='color:#768390'>Check your connection and try again.</p><button onclick='location.reload()' style='margin-top:16px;background:#2ea043;color:#fff;border:none;padding:12px 24px;border-radius:8px;font-size:1em;cursor:pointer'>Retry</button></body></html>", "text/html", "UTF-8");
                }
            }
        });
        // ── WebChromeClient ───────────────────────────────────
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView v, int p) {
                progressBar.setProgress(p); progressBar.setVisibility(p>=100?View.GONE:View.VISIBLE);
            }
            @Override public void onGeolocationPermissionsShowPrompt(String o, GeolocationPermissions.Callback cb) { cb.invoke(o, true, false); }
            @Override public void onPermissionRequest(PermissionRequest req) { req.grant(req.getResources()); }
            @Override public boolean onShowFileChooser(WebView wv, ValueCallback<Uri[]> cb, FileChooserParams p) {
                filePathCallback = cb;
                try { startActivityForResult(p.createIntent(), FILE_REQ); return true; }
                catch (Exception e) { filePathCallback = null; return false; }
            }
        });
        // ── Update checker ────────────────────────────────────
        new Thread(() -> checkForUpdate()).start();
        webView.loadUrl("file:///android_asset/index.html");
    }
    private void checkForUpdate() {
        try {
            URL url = new URL(UPDATE_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000); con.setReadTimeout(5000);
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder(); String line;
            while ((line = br.readLine()) != null) sb.append(line);
            String json = sb.toString();
            // Parse version_code from JSON
            int latest = 1;
            String name = ""; String updateUrl2 = "https://github.com/clearpixelstudio/my-android-app/releases";
            boolean force = false;
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"latest_version_code\"\\s*:\\s*(\\d+)").matcher(json);
            if (m.find()) latest = Integer.parseInt(m.group(1));
            java.util.regex.Matcher mn = java.util.regex.Pattern.compile("\"latest_version_name\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
            if (mn.find()) name = mn.group(1);
            java.util.regex.Matcher mu = java.util.regex.Pattern.compile("\"update_url\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
            if (mu.find()) updateUrl2 = mu.group(1);
            final int lat = latest; final String vn = name; final String uu = updateUrl2; final boolean frc = force;
            if (lat > 1) {
                new Handler(Looper.getMainLooper()).post(() -> showUpdateDialog(vn, uu, frc));
            }
        } catch (Exception e) { /* silently ignore */ }
    }
    private void showUpdateDialog(String vn, String updateUrl2, boolean force) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Update Available" + (vn.isEmpty() ? "" : " (v" + vn + ")"));
        b.setMessage("A new version is available! Please update for the latest features and fixes.");
        b.setPositiveButton("Update Now", (d, w) -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl2)));
        });
        if (!force) b.setNegativeButton("Later", null);
        b.setCancelable(!force);
        b.show();
    }
    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }
    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) { webView.goBack(); return; }
        if (doubleBackPressed) { super.onBackPressed(); return; }
        doubleBackPressed = true;
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackPressed = false, 2000);
    }
    @Override protected void onActivityResult(int req, int res, Intent data) {
        if (req == FILE_REQ && filePathCallback != null) {
            filePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(res, data));
            filePathCallback = null;
        }
        super.onActivityResult(req, res, data);
    }
    @Override protected void onPause()   { super.onPause();   if (webView!=null) webView.onPause();  }
    @Override protected void onResume()  { super.onResume();  if (webView!=null) webView.onResume(); }
    @Override protected void onDestroy() { if (webView!=null) { webView.destroy(); webView=null; } super.onDestroy(); }
}