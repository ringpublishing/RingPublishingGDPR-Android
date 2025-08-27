package com.ringpublishing.gdpr.demo;

import android.app.Application;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.ringpublishing.gdpr.BuildConfig;
import com.ringpublishing.gdpr.LogListener;
import com.ringpublishing.gdpr.RingPublishingGDPR;
import com.ringpublishing.gdpr.RingPublishingGDPRError;
import com.ringpublishing.gdpr.RingPublishingGDPRListener;
import com.ringpublishing.gdpr.RingPublishingGDPRUIConfig;

public class DemoApplication extends Application {

    private final RingPublishingGDPR ringPublishingGDPR = RingPublishingGDPR.getInstance();

    @Override
    public void onCreate() {
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
        if (TextUtils.isEmpty(appTenantId) || TextUtils.isEmpty(appBrandingName)) {
            Toast.makeText(this, R.string.demo_configuration_warning, Toast.LENGTH_LONG).show();
        }

        // Optional listener that informs application about saving or updating consents.
        // You can set one listener, if you need to clear it set null value
        ringPublishingGDPR.setRingPublishingGDPRListener(new RingPublishingGDPRListener() {
            @Override
            public void onConsentsUpdated() {
                // Here you can check if user agreed on all available vendors
                if (RingPublishingGDPR.getInstance().areVendorConsentsGiven()) {
                    // If you app uses SDK's from vendors, which are not part of the official TCF 2.0 vendor list
                    // you can use this flag to check if you can enable / initialize them as user agreed on all vendors
                    DemoApplication.this.initializeApplicationExternalLibraries();
                }
            }

            @Override
            public void onError(RingPublishingGDPRError error, String detailMessage) {
                if (error == RingPublishingGDPRError.CLOSE_FORM_WITH_ERROR) {
                    Log.e("GDPR_ERROR", String.format("Cmp form closed by Error: %s", detailMessage));
                } else {
                    Log.e("GDPR_ERROR", String.format("Error: %s message: %s", error.name(), detailMessage));
                }
            }
        });

        // Create UI configuration for consent screen
        final RingPublishingGDPRUIConfig ringPublishingGDPRUIConfig = new RingPublishingGDPRUIConfig(yourAppTypeFace, yourAppThemeColor);

        // Initialize once immediately inside application object before initialize external libraries.
        // To see where initialize external libraries see SplashActivity
        // See more in this method javadoc
        ringPublishingGDPR.initialize(this, appTenantId, appBrandingName, ringPublishingGDPRUIConfig);

        // If you want, you can also use alternative initialization method with additional parameter: forcedGDPRApplies
        // ringPublishingGDPR.initialize(this, appTenantId, appBrandingName, ringPublishingGDPRUIConfig, true);
        // ringPublishingGDPR.initialize(this, appTenantId, appBrandingName, ringPublishingGDPRUIConfig, false);


        if (BuildConfig.DEBUG) {
            RingPublishingGDPR.getInstance().enableDebugLogs(true);
            addOptionalCustomLogger();
        }
    }

    private static void addOptionalCustomLogger() {
        RingPublishingGDPR.getInstance().addLogListener(new LogListener() {
            @Override
            public void debug(String message) {
                //Your local or remote log handler
            }

            @Override
            public void info(String message) {
                //Your local or remote log handler
            }

            @Override
            public void warn(String message) {
                //Your local or remote log handler
            }

            @Override
            public void error(String message) {
                //Your local or remote log handler
            }
        });
    }

    private void initializeApplicationExternalLibraries() {
        // Here we can initialize or update initialization all application libraries
        Toast.makeText(DemoApplication.this, R.string.toast_libraries_application, Toast.LENGTH_SHORT).show();
    }
}
