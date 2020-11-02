package com.ringpublishing.gdpr.demo;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.webkit.WebView;
import android.widget.Toast;

import com.ringpublishing.gdpr.BuildConfig;
import com.ringpublishing.gdpr.RingPublishingGDPR;
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
        2. Add to your application section in AndroidManifest.xml entry:
           <activity android:name="com.ringpublishing.gdpr.RingPublishingGDPRActivity" android:theme="@style/AppTheme.NoActionBar" />
           Example is in demo file AndroidManifest.xml
        3. Add to your launch Activity (SplashActivity) condition on start with ask shouldShowConsentForm() like in demo SplashActivity
        4. Move your libraries initialization according to example in SplashActivity.
           (When libraries have feature to observe consent preferences and after preference change they can initialize again with proper consent status,
           then they can be initialized just on application start like you had before.
           Otherwise you must move initialization libraries to section like showAppContent() in demo SplashActivity)
         */

        final Typeface yourAppTypeFace = Typeface.DEFAULT;
        final int yourAppThemeColor = Color.YELLOW;
        final String appTenantId = "1746213"; // Fill here your application tenantId. Obtain from Ring Publishing. Example: "1234"
        final String appBrandingName = "app_onet_android"; // Fill here application brandName. Obtain from Ring Publishing. Example: "myAppName"

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
    }

}
