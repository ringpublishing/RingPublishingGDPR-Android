package com.ringpublishing.gdpr.demo;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.webkit.WebView;
import android.widget.Toast;

import com.ringpublishing.gdpr.BuildConfig;
import com.ringpublishing.gdpr.RingPublishingGDPR;
import com.ringpublishing.gdpr.RingPublishingGDPRListener;
import com.ringpublishing.gdpr.RingPublishingGDPRUIConfig;

import androidx.multidex.MultiDexApplication;

public class DemoApplication extends MultiDexApplication
{

    private final RingPublishingGDPR ringPublishingGDPR = RingPublishingGDPR.getInstance();

    @Override
    public void onCreate()
    {
        super.onCreate();
        // Optional for debug Demo application webview
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);

        /*
        Integration steps:

        1. Initialize in Application object RingPublishingGDPR sdk with configuration
        2. Add to your launch Activity (SplashActivity) condition on start with ask shouldShowConsentForm() like in demo SplashActivity
        3. Move your libraries initialization according to example in SplashActivity.
           (When libraries have feature to observe consent preferences and after preference change they can initialize again with proper consent status,
           then they can be initialized just on application start like you had before.
           Otherwise you must move initialization libraries to section like showAppContent() in demo SplashActivity)
         */

        final Typeface yourAppTypeFace = Typeface.DEFAULT;
        final int yourAppThemeColor = Color.YELLOW;
        final String appTenantId = ""; // Fill here your application tenantId. Obtain from Ring Publishing. Example: "1234"
        final String appBrandingName = ""; // Fill here application brandName. Obtain from Ring Publishing. Example: "myAppName"

        // Just for this example to set tenantId and brandingName before install demo application.
        if(TextUtils.isEmpty(appTenantId) || TextUtils.isEmpty(appBrandingName))
        {
            Toast.makeText(this, R.string.demo_configuration_warning, Toast.LENGTH_LONG).show();
        }

        // Create UI configuration for consent screen
        final RingPublishingGDPRUIConfig ringPublishingGDPRUIConfig = new RingPublishingGDPRUIConfig(yourAppTypeFace, yourAppThemeColor);

        // Initialize once immediately inside application object before initialize external libraries.
        // To see where initialize external libraries see SplashActivity
        // See more in this method javadoc
        ringPublishingGDPR.initialize(this, appTenantId, appBrandingName, ringPublishingGDPRUIConfig);

        // When (in you usage context) GDPR does not apply, use initialize method with extra parameter gdprApplies set to false
        // ringPublishingGDPR.initialize(false, this, appTenantId, appBrandingName, ringPublishingGDPRUIConfig);

        // Optional listener that informs application about saving or updating consents.
        // You can add more listeners on each place where you initialize SDK.
        // Remember call ringPublishingGDPR.removeRingPublishingGDPRListeners() on your object destroy
        ringPublishingGDPR.addRingPublishingGDPRListeners(() ->
        {
            // Here you can check if user agreed on all available vendors
            if(RingPublishingGDPR.getInstance().areVendorConsentsGiven())
            {
                // If you app uses SDK's from vendors, which are not part of the official TCF 2.0 vendor list
                // you can use this flag to check if you can enable / initialize them as user agreed on all vendors
                initializeApplicationExternalLibraries();
            }
        });
    }

    private void initializeApplicationExternalLibraries()
    {
        // Here we can initialize or update initialization all application libraries
        Toast.makeText(DemoApplication.this, R.string.toast_libraries_application, Toast.LENGTH_SHORT).show();
    }
}
