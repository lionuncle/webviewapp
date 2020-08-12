package com.ana.fireecams;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.kochava.base.Tracker;
import com.onesignal.OneSignal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static com.android.installreferrer.api.InstallReferrerClient.newBuilder;

public class MainActivity extends AppCompatActivity implements InstallReferrerStateListener {

    private static final String TAG = "INSTALL";
    private WebView webView;
    private String deviceId, pushToken, kdId;
    private static String ref = "utm_source=google-play&utm_medium=organic", gaid;
    private String url;
    InstallReferrerClient mReferrerClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getGaid();
        webView = findViewById(R.id.webView);

        Uri reveiverIntent = getIntent().getData();
        try {
            if (reveiverIntent != null) {
                String openUrl = new URL(reveiverIntent.toString()).toString();
                webView.loadUrl(openUrl);
                return;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Tracker.configure(new Tracker.Configuration(getApplicationContext())
                .setAppGuid("kofireecams-tksg")
        );


        mReferrerClient = newBuilder(this).build();
        mReferrerClient.startConnection(this);

        //One signal Your App ID: b837aa87-03d0-41f7-87f7-255870a99c3d

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        pushToken = "b837aa87-03d0-41f7-87f7-255870a99c3d";

        kdId = Tracker.getDeviceId();


        // OneSignal Initialization
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();


    }

    @Override
    public void onInstallReferrerSetupFinished(int responseCode) {
        switch (responseCode) {
            case InstallReferrerResponse.OK:
                try {
                    Log.v(TAG, "InstallReferrer conneceted");
                    ReferrerDetails response = mReferrerClient.getInstallReferrer();
                    //handleReferrer(response);
                    ref = response.getInstallReferrer();
                    ready();

                    mReferrerClient.endConnection();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                Log.w(TAG, "InstallReferrer not supported");
                break;
            case InstallReferrerResponse.SERVICE_UNAVAILABLE:
                Log.w(TAG, "Unable to connect to the service");
                break;

            default:
                Log.w(TAG, "responseCode not found.");
        }
    }

    private void getGaid() {
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                AdvertisingIdClient.Info idInfo = null;
                try {
                    idInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
                } catch (GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException | IOException e) {
                    e.printStackTrace();
                }
                String advertId = null;
                try {
                    advertId = idInfo != null ? idInfo.getId() : null;
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                return advertId;
            }

            @Override
            protected void onPostExecute(String advertId) {
                gaid = advertId;
                ready();
                //Toast.makeText(getApplicationContext(), advertId, Toast.LENGTH_SHORT).show();
            }

        };
        task.execute();
    }

    private void ready() {
        url = "http://fircamernw.club?utm_source=fircamernw_aosapp&device_id=" + deviceId + "&push-token=" + pushToken + "&kd_id=" + kdId + "&ref=" + ref + "&gaid=" + gaid;
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);
        settings.setDefaultTextEncodingName("utf-8");
        Uri reveiverIntent = getIntent().getData();

        if (reveiverIntent == null) {
            webView.loadUrl(url);
        }
    }

    @Override
    public void onInstallReferrerServiceDisconnected() {
        Log.d(TAG, "INstallation disconnected");
    }

    @Override
    public void onBackPressed() {
        if (webView.getUrl().equals("http://fircamernw.club/privacy-policy.html") || webView.getUrl().equals("http://fircamernw.club/terms.html")) {
            MainActivity.super.onBackPressed();
            return;
        }
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        MainActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        deleteCache(MainActivity.this);
        super.onDestroy();
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {}
    }
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}