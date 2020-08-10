package com.ana.fireecams;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.Settings;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private String deviceId,pushToken,kdId,ref,gaid;
    private String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webView);


        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        url = "http://fircamernw.club?utm_source=fircamernw_aosapp&device_id="+deviceId+"&push-token="+pushToken+"&kd_id="+kdId+"&ref="+ref+"&gaid="+gaid;
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.loadUrl(url);

    }
}