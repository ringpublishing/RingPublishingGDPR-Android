package com.ringpublishing.gdpr.demo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ringpublishing.gdpr.RingPublishingGDPR;
import com.ringpublishing.gdpr.RingPublishingGDPRListener;
import com.ringpublishing.gdpr.demo.databinding.ActivityMainBinding;

import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.preference.PreferenceManager;

/**
 * Example MainActivity of app content.
 * Launcher SplashActivity will check consents, then open consent screen or open this activity.
 * On this activity user have already external libraries initialized.
 * Here you not need do nothing, just for example we print consents and give posibility to open settings consent screen.
 */
public class MainActivity extends AppCompatActivity implements RingPublishingGDPRListener
{

    private static final String TAG = MainActivity.class.getCanonicalName();

    ActivityMainBinding activityMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Example how welcome or advanced settings consent view can be displayed from application.
        // Can be called for example from application settings or menu.
        activityMainBinding.mainButton.setOnClickListener(v -> {
            final Intent startSettingScreenIntent = RingPublishingGDPR.getInstance().createShowSettingsScreenIntent(this);
            startActivity(startSettingScreenIntent);
        });

        // Optional example: You can add consentsUpdate listener where you want
        // Remember to remove listener when object is destroying
        RingPublishingGDPR.getInstance().addRingPublishingGDPRListeners(this);
    }

    @Override
    protected void onDestroy()
    {
        RingPublishingGDPR.getInstance().removeRingPublishingGDPRListeners(this);
        super.onDestroy();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // Optional example how to print saved consents during testing implementation.
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final Map<String, ?> all = preferences.getAll();
        for (String preference : all.keySet())
        {
            Log.i(TAG, "Preference key=" + preference + " value= " + all.get(preference));
        }
    }

    @Override
    public void onBackPressed()
    {
        finish(); // Just for this example to not open Splash Screen when back from MainActivity
        super.onBackPressed();
    }

    @Override
    public void onConsentsUpdated()
    {
        // Here you can check if user agreed on all available vendors
        if (RingPublishingGDPR.getInstance().areVendorConsentsGiven())
        {
            // If you app uses SDK's from vendors, which are not part of the official TCF 2.0 vendor list
            // you can use this flag to check if you can enable / initialize them as user agreed on all vendors
            initializeLocalExternalLibraries();
        }
    }

    private void initializeLocalExternalLibraries()
    {
        // Initialize your app SDK's that should be initialized on this concrete class
        Toast.makeText(this, R.string.toast_libraries_main, Toast.LENGTH_SHORT).show();
    }
}
