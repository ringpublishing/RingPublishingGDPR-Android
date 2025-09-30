1.9.0 Release notes (2025-09-30)
=============================================================

### Changes

* compile / target sdk = 36
* gradle-9.0.0
* updated some libraries versions
* fixed some deprecations and code warnings
1.8.3 Release notes (2025-06-29)
=============================================================

### Changes

* Handled dark mode for loading / error view
1.8.1 Release notes (2024-09-23)
=============================================================

### Changes

* Android 15 support

1.8.1 Release notes (2023-12-29)
=============================================================

### Changes

* Update Android libraries to latest versions

1.8.0 Release notes (2023-10-25)
=============================================================

### Changes

* Added support for TCF2.2:
    - replaced 'SharedPreferences' field: 'IABTCF_UseNonStandardStacks' with 'IABTCF_UseNonStandardTexts'

1.7.3 Release notes (2023-09-06)
=============================================================

### Changes

* Gradle 8
* Android 14
1.7.2 Release notes (2023-06-16)
=============================================================

### Changes

* Add to 'RingPublishingGDPRError' new type of error 'CLOSE_FORM_WITH_ERROR' that inform about closed form.
* Inform about close for errors in 'RingPublishingGDPRListener' in method 'onError()'

1.7.1 Release notes (2023-06-15)
=============================================================

### Changes

* Remove error log about missing consentFormListener

1.7.0 Release notes (2023-05-26)
=============================================================

### Changes

* New option to add or remove additional custom logger in class 'RingPublishingGDPR' by method 'addLogListener()'
* New parameter 'detailMessage' on method 'void onError(RingPublishingGDPRError error, String detailMessage)' in 'RingPublishingGDPRListener'
* Parameter 'detailMessage' is used for get more information about errors during loading and display consent form

1.6.1 Release notes (2022-11-03)
=============================================================

### Changes

* Update dependencies for common library
* Update target and compile sdk version
1.6.0 Release notes (2021-11-12)
=============================================================

### Changes

* Removed addRingPublishingGDPRListener and removeRingPublishingGDPRListener and replace by setRingPublishingGDPRListener
* New method in RingPublishingGDPRListener - onError - method for log case when library cannot show correct form or cannot create correct configuration.

1.5.2 Release notes (2021-10-26)
=============================================================

### Fixed

* Memory leaks
* Crash FormView create

1.5.1 Release notes (2021-07-06)
=============================================================

### Features

* Support for html <select> tag

1.5.0 Release notes (2021-06-30)
=============================================================

### Features

* Added support for optional parameter in module initializer: 'forcedGDPRApplies' in new initialization method.
    - If you want to ignore geo-ip based detection for 'gdprApplies', pass as 'forcedGDPRApplies' param either true of false. This parameter is optional.
* Added public module method: 'isGDPRApplies()'. This contains information if GDPR applies in current context.
    - This property at module initialization (and before) has value saved from last app session.
    - This property will be populated with fresh value somewhere between:
        - after module initialization
        - before module calls one of  methods with consents status, either 'shouldShowConsentForm()' or 'onConsentsUpdated()'
1.4.1 Release notes (2021-05-24)
=============================================================

### Fixes

* Synchronize state of web view cookie between sessions

1.4.0 Release notes (2021-05-19)
=============================================================

### Features

* Added support for backend configuration (based on geo-ip) for 'gdprApplies' (IABTCF_gdprApplies) flag. This flag can no longer be passed as parameter to the SDK initializer.
* Interaction with module on app start is now always asynchronous (where previously during first app launch it was synchronous).

### Changes

* Removed 'gdprApplies' property from 'RingPublishingGDPR.getInstance().initialize()' method
* Changed 'shouldShowConsentForm()' method from 'RingPublishingGDPR' interface. Now method have new argument 'ConsentFormListener'

### Fixes

* Fixed crash related to getFormView() and FormViewController


1.3.5 Release notes (2021-02-25)
=============================================================

### Fixed

* Crash in RingPublishingGDPRActivity when class try add view to layout, but this view still have old parent.
* From now RingPublishingGDPRActivity have singleTask launch mode. In layout activity_ring_publishing_gdpr unused LinearLayout is removed.

1.3.4 Release notes (2021-02-18)
=============================================================

### Changes

* Automatic Release process check

1.3.3 Release notes (2021-02-27)
=============================================================

### Changes

* Automatic Release process

1.3.3 Release notes (2021-02-27)
=============================================================

### Changes

* Automatic Release process

1.3.2 Release notes (2021-01-13)
=============================================================

### Fixed

* Fix for removing parent from old instance view when system created new RingPublishingGDPRActivity.

1.3.2 Release notes (2021-01-13)
=============================================================

### Fixed

* Fix for removing parent from old instance view when system created new RingPublishingGDPRActivity.

1.3.2 Release notes (2021-01-13)
=============================================================

### Fixed

* Fix for removing parent from old instance view when system created new RingPublishingGDPRActivity.

1.3.1 Release notes (2021-01-04)
=============================================================

### Fixed

* Fix for removing parent from view when system created new RingPublishingGDPRActivity before destroying old one.
* Fix timeout and close form on long requests
* Prevent NullPointerException when ringPublishingGDPRActivityCallback is unregistered already

1.3.0 Release notes (2020-12-18)
=============================================================

### Features

* New listener 'RingPublishingGDPRListener' than observe consents update in method 'onConsentsUpdated()'

1.2.0 Release notes (2020-11-20)
=============================================================

### Features

* New value 'RingPublishing_PublicConsents' stored in user defaults (should be used for other purposes than Ad Server (public))
* Public interface has been extended about additional method "areVendorConsentsGiven" which can be used by other Ring Publishing modules, e.g. Ad Server.
* Removed from demo AndroidMainfest.xml <activity android:name="com.ringpublishing.gdpr.RingPublishingGDPRActivity" android:theme="@style/GDPRStyle" /> and integrated it to RingPublishingGDPR module

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
