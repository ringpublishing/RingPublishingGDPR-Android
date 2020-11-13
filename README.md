![RingPublishing](https://github.com/ringpublishing/RingPublishingGDPR-Android/raw/master/ringpublishing_logo.jpg)

# RingPublishingGDPR

Module which collects and saves user's consent in accordance with the standard TCF2.0

## Documentation

The documentation can be found at
[https://developer.ringpublishing.com/Money/docs/GDPRConsentManager/index.html](https://developer.ringpublishing.com/Money/docs/GDPRConsentManager/index.html).

Integration tutorial:

[https://developer.ringpublishing.com/Money/docs/GDPRConsentManager/howto/integrate-using-android-sdk.html](https://developer.ringpublishing.com/Money/docs/GDPRConsentManager/howto/integrate-using-android-sdk.html).

How consents are stored:

[https://developer.ringpublishing.com/Money/docs/GDPRConsentManager/topics/consent-storage-using-mobile-sdk.html](https://developer.ringpublishing.com/Money/docs/GDPRConsentManager/topics/consent-storage-using-mobile-sdk.html).

Reference guide:

[https://developer.ringpublishing.com/Money/docs/GDPRConsentManager/reference/gdpr-android-sdk.html](https://developer.ringpublishing.com/Money/docs/GDPRConsentManager/reference/gdpr-android-sdk.html).

## Requirements

- Android API >= 21
- AndroidX

## Permissions

List permissions used in module:
```ruby
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Installation from GitHub Packages

1. Add to your main build.gradle script section to GitHub repository
```ruby
maven {
    name = "RingPublishingGDPR-Android"
    url = uri("https://maven.pkg.github.com/ringpublishing/RingPublishingGDPR-Android")
    credentials {
        username = "your github username"
        password = "you github access token"
    }
}
```

2. Add to your application project module dependencies section
```ruby
implementation("com.ringpublishing:gdpr:1.1.0")
```
3. Add to your application AndroidManifest.xml in <application> section entry
```ruby
<activity android:name="com.ringpublishing.gdpr.RingPublishingGDPRActivity" android:theme="@style/AppTheme.NoActionBar" />
```

## Installation from GitHub source code
1. Checkout code from GitHub
2. Add RingPublishingGDPR like module to your project

In settings.gradle add:
```ruby
include ':yourApplication', ':RingPublishingGDPR'
```
In your application build.gradle add dependency
```ruby
implementation project(path: ':RingPublishingGDPR'))
```
3. Add to your application AndroidManifest in <application> section entry
```ruby
<activity android:name="com.ringpublishing.gdpr.RingPublishingGDPRActivity" android:theme="@style/AppTheme.NoActionBar" />
```


## Usage

Sync project and start looks usage in demo project. Start look in class DemoApplication.java

Start by importing `RingPublishingGDPR`:

```ruby
import com.ringpublishing.gdpr.RingPublishingGDPR;
```

then you have access to module instance:

```ruby
RingPublishingGDPR.getInstance()
```

For detailed example see example project in `demo` directory and start with class DemoApplication, SplashActivity and MainActivity or check our documentation.
