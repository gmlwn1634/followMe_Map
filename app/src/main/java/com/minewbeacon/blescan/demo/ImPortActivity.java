package com.minewbeacon.blescan.demo;


import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.yuliwuli.blescan.demo.databinding.ActivityImPortBinding;


public class ImPortActivity extends AppCompatActivity {

    private ActivityImPortBinding binding;
    private WebView webView;
    private static final String APP_SCHEME = "iamporttest://";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImPortBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.webView.setWebViewClient(new InicisWebViewClient(this));
        WebSettings settings = binding.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        binding.webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                CustomDialog customDialog = new CustomDialog(ImPortActivity.this, new CustomDialogClickListener() {
                    @Override
                    public void onPositiveClick() {
                        return;
                    }
                }, message, "");
                customDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                customDialog.setCancelable(false);
                customDialog.show();
                result.cancel();
                return true;

            }

        });


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(binding.webView, true);
        }

        Intent intent = getIntent();
        Uri intentData = intent.getData();

        if (intentData == null) {
            binding.webView.loadUrl(GlobalVar.URL + GlobalVar.URL_IAMPORT + LoginActivity.patientId);
        } else {
            String url = intentData.toString();
            if (url.startsWith(APP_SCHEME)) {
                String redirectURL = url.substring(APP_SCHEME.length() + 3);
                binding.webView.loadUrl(redirectURL);
            }

        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String url = intent.getDataString();
        if (url.startsWith(APP_SCHEME)) {
            String redirectURL = url.substring(APP_SCHEME.length() + 3);
            binding.webView.loadUrl(redirectURL);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PaymentActivity paymentActivity = (PaymentActivity) PaymentActivity.paymentActivity;
        paymentActivity.finish();

    }
}