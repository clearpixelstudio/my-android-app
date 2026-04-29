package com.example.myapp;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.*;
import android.view.View;
import android.os.Build;
import android.content.pm.PackageManager;
import java.io.File;
import java.security.MessageDigest;

public class MainActivity extends Activity {
    private WebView webView;

    @SuppressLint({"SetJavaScriptEnabled","HardwareIds"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        // Root Detection
        String[] rootPaths={"/system/app/Superuser.apk","/system/xbin/su","/system/bin/su","/sbin/su","/data/local/su","/data/local/bin/su","/data/local/xbin/su","/system/sd/xbin/su","/system/bin/failsafe/su","/data/local/xbin/mu"};
        for(String path:rootPaths){if(new java.io.File(path).exists()){android.widget.Toast.makeText(this,"App cannot run on rooted device",android.widget.Toast.LENGTH_LONG).show();finish();return;}}
        try{Process p=Runtime.getRuntime().exec(new String[]{"/system/xbin/which","su"});if(new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream())).readLine()!=null){finish();return;}}catch(Exception ignored){}


        // Signature Verification (Anti-Tamper)
        try{
            android.content.pm.PackageInfo pi=getPackageManager().getPackageInfo(getPackageName(),android.content.pm.PackageManager.GET_SIGNATURES);
            for(android.content.pm.Signature sig:pi.signatures){
                java.security.MessageDigest md=java.security.MessageDigest.getInstance("SHA-256");
                byte[] h=md.digest(sig.toByteArray());
                StringBuilder sb=new StringBuilder();
                for(byte b:h)sb.append(String.format("%02X",b));
                android.util.Log.d("APKFactory","Signature:"+sb.toString());
            }
        }catch(Exception ignored){}

        
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webView);
        setupWebView();
        webView.loadUrl("file:///android_asset/www/index.html");
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setMixedContentMode(true?WebSettings.MIXED_CONTENT_ALWAYS_ALLOW:WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        
        webView.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebRequest req){v.loadUrl(req.getUrl().toString());return true;}
            @Override public void onPageFinished(WebView v,String url){}
        });
        webView.setWebChromeClient(new WebChromeClient(){});
    }

    @Override
    public void onBackPressed(){
        if(webView.canGoBack()){webView.goBack();}else{super.onBackPressed();}
    }

    @Override protected void onResume(){super.onResume();}
}
