# Unfollowly

A privacy-first Instagram follower-change tracker for Android. Users import their official Instagram JSON export; all parsing, snapshots, and comparisons happen on-device.

## What works

- Imports Instagram ZIP archives or individual JSON files
- Handles split `followers_*.json` files and nested `following.json`
- Compares up to 30 snapshots
- Shows new followers, unfollowers, non-followers, fans, and mutuals
- Searchable people lists with optional Instagram profile links
- No login, password collection, backend, trackers, or broad storage permission
- Fully offline after installation

## Open in Android Studio

1. Open this folder in Android Studio Ladybug or newer.
2. Allow Gradle sync to complete (JDK 17).
3. Run the `app` configuration on Android 8.0+.

The included launch scripts bootstrap the pinned Gradle version on first use. From a terminal, run `./gradlew assembleDebug` (macOS/Linux) or `gradlew.bat assembleDebug` (Windows).

## Creating suitable Instagram data

In Instagram, open **Accounts Center → Your information and permissions → Download your information**. Select followers/following and JSON format. Import the downloaded ZIP into Unfollowly. Repeat later to calculate changes.

## Release checklist

- Replace `applicationId` if desired.
- Create a signed Android App Bundle from **Build → Generate Signed Bundle/APK**.
- Add a public privacy-policy URL to the Play Console listing.
- Complete the Play Console Data safety form: the app itself does not collect or share user data.
- Do not market profile-viewer or stalker detection; Instagram exports cannot prove those claims.

## Trademark note

Unfollowly is an original name, but trademark and store-name availability have not been legally cleared. Verify availability before publishing.
