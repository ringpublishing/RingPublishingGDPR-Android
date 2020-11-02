package com.ringpublishing.gdpr.demo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.ringpublishing.gdpr.RingPublishingGDPRActivity;
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
public class MainActivity extends AppCompatActivity
{

    private static final String TAG = MainActivity.class.getSimpleName();

    ActivityMainBinding activityMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Example how welcome or advanced settings consent view can be displayed from application.
        // Can be called for example from application settings or menu.
        activityMainBinding.mainButton.setOnClickListener(v -> {
            final Intent startSettingScreenIntent = RingPublishingGDPRActivity.createShowSettingsScreenIntent(this);
            startActivity(startSettingScreenIntent);
        });
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
}
