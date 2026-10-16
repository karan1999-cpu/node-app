# Nodd

Swap contacts by QR. Version 1 is an Android app that needs no server:

- **My card**: your name, mobile, email, work and links, saved only on the phone. Switch each detail on or off for the QR.
- **Show my QR**: a standard vCard QR. Any phone camera, Google Lens or the iPhone camera offers *Add contact* with your details filled in; the other person taps Save.
- **Send as contact file**: the same card as a `.vcf` through WhatsApp, Quick Share or Bluetooth.
- **Scan**: scans someone else's contact QR with Google's code scanner and opens *Add contact* pre-filled.
- **Plan**: the concept document, bundled for offline reading.

## Install on a phone

Build the APK (see below), copy `app/build/outputs/apk/debug/app-debug.apk` to the phone (USB, Drive, WhatsApp to yourself), open it, and allow "Install unknown apps" for the app you opened it from. APKs aren't committed to git; share test builds through GitHub Releases instead.

Or with the phone plugged in and USB debugging on:

```
%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk
```

## Build

Open this folder in Android Studio, or run:

```
gradlew.bat assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/`. Gradle 8.14.3 can't run on JDK 25, so `gradle/gradle-daemon-jvm.properties` pins the build to JDK 21 (Android Studio's downloaded `~/.jdks/jbr-21` is found automatically).

On Windows, keep the project in a short path such as `E:\Projects\nodd-app`; very deep folders hit the 260-character path limit during the build.

## Editing the plan

The plan is `docs/nodd-concept.html`, the same file published as the web page. Edit it and rebuild: the `buildPlanPage` task in `app/build.gradle.kts` wraps it with the bundled fonts (`app/src/main/assets/fonts`, SIL Open Font License) and packs it into the APK.

## Before Google Play

- `applicationId` is `com.cybersync.nodd`. It can't change after the first upload, so confirm it first.
- Play needs a signed release bundle: create an upload key in Android Studio (*Build > Generate Signed App Bundle*), then upload `app-release.aab`.
- The debug APK is for testing only.
