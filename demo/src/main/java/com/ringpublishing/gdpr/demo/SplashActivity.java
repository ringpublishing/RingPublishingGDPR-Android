package com.ringpublishing.gdpr.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ringpublishing.gdpr.RingPublishingGDPR;
import com.ringpublishing.gdpr.demo.databinding.ActivitySplashBinding;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

/**
 * Example Splash activity used common by apps to initialize application components and fetch content.
 * Here we decide that consent view should be displayed or application should continue initialize external libraries.
 *
 * Start activity should be used with startActivityForResult to make sure handle consent screen complete.
 * In onActivityResult we are sure that application can continue initialization
 */
public class SplashActivity extends AppCompatActivity
{

    private final String TAG = SplashActivity.class.getCanonicalName();

    private final int REQUEST_CODE_OPEN_CONSENT = 123;

    ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "SplachActivity onCreate");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);

        // This condition decide that sdk should open RingPublishingGDPRActivity
        // shouldShowConsentForm() will be true on first application launch, or on next launches when consents are already outdated
        if (RingPublishingGDPR.getInstance().shouldShowConsentForm())
        {
            final Intent startWelcomeScreenIntent = RingPublishingGDPR.getInstance().createShowWelcomeScreenIntent(this);
            // Option to open advanced settings view
            //RingPublishingGDPR.getInstance().createShowSettingsScreenIntent(this);
            Log.i(TAG, "SplachActivity onCreate startWelcomeScreenIntent");
            startActivityForResult(startWelcomeScreenIntent, REQUEST_CODE_OPEN_CONSENT);
        }
        else
        {
            Log.i(TAG, "SplachActivity onCreate showAppContent");
            showAppContent();
        }
    }

    private void showAppContent()
    {
        // Here you can check if user agreed for all available vendors
        if(RingPublishingGDPR.getInstance().areVendorConsentsGiven())
        {
            // If you app uses SDK's from vendors, which are not part of the official TCF 2.0 vendor list
            // you can use this flag to check if you can enable / initialize them as user agreed for all vendors
            initializeSplashExternalLibraries();
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();

    }

    private void initializeSplashExternalLibraries()
    {
        // Here you can initialize external libraries initialized in your Splash screen,
        // because consent screen was already visited by user.
        Toast.makeText(this, R.string.toast_libraries_splash, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_CONSENT)
        {
            Log.i(TAG, "SplashActivity onActivityResult showAppContent");
            showAppContent();
        }
    }

}
