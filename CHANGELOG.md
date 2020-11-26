1.2.0 Release notes (2020-11-20)
=============================================================

### Features

* New value 'RingPublishing_PublicConsents' stored in user defaults (should be used for other purposes than Ad Server (public))
* Public interface has been extended about two additional properties  "areVendorConsentsGiven" which can be used by other Ring Publishing modules, e.g. Ad Server.
* Remove from demo AndroidMainfest.xml  <activity android:name="com.ringpublishing.gdpr.RingPublishingGDPRActivity" android:theme="@style/GDPRStyle" /> move it to RingPublishingGDPR

1.1.0 Release notes (2020-11-13)
=============================================================

### Features

* RingPublishingGDPR 'initialize' method now accepts additional parameter: 'gdprApplies'
* Immidiatelly after module initialization, in User Defaults are stored two properties:
   - 'IABTCF_CmpSdkID' with official CMP SDK ID
   - 'IABTCF_gdprApplies' with passed to module boolean flag if GDPR should apply

### Changes

* Method: 'shouldShowConsentForm()' respects GDPR setting and stored consents. If 'gdprApplies' is false, this method will return false.
* Method: 'didAskUserForConsents()' removed from public interface

1.0.1 Release notes (2020-11-12)
=============================================================

### Features

* Release notes support
* Add to logs package to better filter logs
* Better handling connection errors

### Fixed

* Fix bug where user back to application from background


1.0.0 Release notes (2020-10-27)
=============================================================

First 'RingPublishingGDPR' module release

### Features

* Collects and saves user's consent in accordance with the standard TCF2.0
https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#in-app-details
* Has support for temporary Google’s Additional Consent
https://support.google.com/admanager/answer/9681920?hl=en
* Collects and saves 'internal' user's consent stored in UserDefaults with 'RingPublishing_' key prefixes
1.0.0 Release notes (2020-10-27)
