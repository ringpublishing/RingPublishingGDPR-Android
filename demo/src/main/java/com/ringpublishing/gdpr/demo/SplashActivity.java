package com.ringpublishing.gdpr.demo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ringpublishing.gdpr.ConsentFormListener;
import com.ringpublishing.gdpr.RingPublishingGDPR;
import com.ringpublishing.gdpr.demo.databinding.ActivitySplashBinding;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

/**
 * Example Splash activity used common by apps to initialize application components and fetch content.
 * Here we decide that consent view should be displayed or application should continue initialize external libraries.
 * <p>
 * Start activity should be used with startActivityForResult to make sure handle consent screen complete.
 * In onActivityResult we are sure that application can continue initialization
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity
{

    private final String TAG = SplashActivity.class.getCanonicalName();

    private final int REQUEST_CODE_OPEN_CONSENT = 123;

    ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);

        Log.i(TAG, "In onCreate call shouldShowConsentForm and in ConsentFormListener to check that consent screen should be displayed");
        RingPublishingGDPR.getInstance().shouldShowConsentForm(new ConsentFormListener()
        {
            @Override
            public void onReadyToShowForm()
            {
                final Intent startWelcomeScreenIntent = RingPublishingGDPR.getInstance().createShowWelcomeScreenIntent(SplashActivity.this);
                // Option to open advanced settings view
                //RingPublishingGDPR.getInstance().createShowSettingsScreenIntent(this);
                Log.i(TAG, "RingPublishingGDPR is ready to show consent screen on application start. Consent screen should be displayed.");
                startActivityForResult(startWelcomeScreenIntent, REQUEST_CODE_OPEN_CONSENT);
            }

            @Override
            public void onConsentsUpToDate()
            {
                Log.i(TAG, "Consents was checked and they are up to date. Consent screen is not need to display. Now application content can be shown");
                showAppContent();
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        RingPublishingGDPR.getInstance().removeConsentFormListener();
        super.onDestroy();
    }

    private void showAppContent()
    {
        // Here you can check if user agreed on all available vendors
        if (RingPublishingGDPR.getInstance().areVendorConsentsGiven())
        {
            // If you app uses SDK's from vendors, which are not part of the official TCF 2.0 vendor list
            // you can use this flag to check if you can enable / initialize them as user agreed on all vendors
            initializeSplashExternalLibraries();
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void initializeSplashExternalLibraries()
    {
        // Here you can initialize external libraries initialized in your Splash screen,
        // because consent screen has been already presented to user.
        Toast.makeText(this, R.string.toast_libraries_splash, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_CONSENT)
        {
            Log.i(TAG, "In onActivityResult consent form is accepted or closed by user. Now application content can be shown");
            showAppContent();
        }
    }

}
