# Changelog

## 1.4.1

- Show the Morivia icon, name and version only on the Profile tab, under the Settings card; remove the same footer from the Settings and About pages.

## 1.4.0

- Add a dedicated Profile tab with the active profile, existing Switch Profile and Account destinations, and a separate Settings card.
- Remove account/profile shortcuts from Settings and its search; keep Watch Party in a standard settings card.
- Replace the experimental Watch Party join warning with the host instruction and remove its duplicate.
- Show a larger Morivia name, centred theme icon and live version in the footer; remove the made-with line from this UI while retaining copyright and licence notices.
- Add Swedish and Finnish app-language options with 452 translated strings each and the existing English fallback for remaining strings (31 language choices plus device language).
- Centre the existing ribbon artwork and generate matching common, Android and iOS icon palettes for every preset theme; custom themes use the nearest compiled palette.
- Synchronise preset theme selection and the launcher icon while preserving all existing Android launcher component names.
- Wrap collection names and folder labels in management, editing and home collection rows.
- Open MoriviaMobile’s latest stable release from the private-repository updater fallback; retain normal APK download, permission and install handling.
- Raise both platforms to version 1.4.0, build 7, retaining their application IDs and signing identity.


## 1.3.0

- Subtitle speech sync: when a video has no embedded subtitle timing, AutoSync measures the speech in the playing audio and lines the selected addon subtitle up with it, correcting a constant delay and frame-rate drift (23.976, 24 and 25 fps). Works in ExoPlayer (pass-through voice-band audio processor) and libmpv on Android and iOS (FFmpeg `astats`/`ametadata` filter added after the file loads; libmpv keeps the old audio chain if it cannot start). Results are applied only when the match stands clearly above every other shift, after two minutes of played audio and again as more audio arrives. See docs/subtitle-autosync.md.
- The iOS player bridge can add and remove an audio filter (`addAudioFilter`, `removeAudioFilter`).
- Every movie and episode starts in the Fill resize mode (full screen without zoom); the resize button still cycles Fit, Fill and Zoom during playback.
- docs/subtitle-autosync.md: AutoSync is on by default and covers libmpv on Android and iOS.
- iOS: the foldable iPhone hinge and vertical bar code needs the iOS 27.1 SDK; `scripts/build-ios-ipa.sh` builds it only when Xcode has that SDK (condition `MORIVIA_IOS_27_1_SDK`), so the app also builds with Xcode 26 on GitHub's Intel Macs.
- Release workflow: the iOS job gives Gradle and Kotlin/Native 9 GB, because linking the release framework ran out of the 4 GB that Android builds use. 1.1.0 and 1.2.0 were never published because their iOS builds did not finish (no free Apple silicon runner, then this memory limit); 1.3.0 is the first release with both apps and contains all of their changes.

## 1.2.0

Brings in all 47 changes from Nuvio 1.1.0-beta.1 while keeping Morivia's branding and fixes:

- Library: remember the selected list and type; filter by genre, year and watched status.
- Addons: support Stremio player and library tracking addons, with a consent prompt; events are queued per addon and not sent for series watched reconciliation.
- Subtitles: outline width setting; the styled overlay keeps subtitle colours, positions, formatting and top placement.
- Player: fall back from the next episode's automatically selected source when it fails; on Android, bottom controls ignore the gesture navigation inset.
- TorBox: match alternative-hash cache answers.
- iOS: keep downloads out of iCloud and computer backups.
- Languages: add Simplified Chinese and Ukrainian (Android and iOS); complete Brazilian Portuguese; update Hebrew, Portuguese, French and Urdu; fix mixed right-to-left and left-to-right text in Search and Watch Party.
- New and updated translations use the Morivia name, except where they refer to Nuvio's own services.
- Nuvio's AltStore source (`store.json`) is not included: AltStore needs public downloads, and this repository is private.

## 1.1.0

- New: Morivia for iPhone and iPad. Every release now contains an unsigned iOS app (`Morivia-iOS-X.Y.Z.ipa`, iOS/iPadOS 16.1 or newer) next to the Android app for phones and tablets.
- The iOS app uses the Morivia name, icon and bundle ID `com.morivia.app`, with the same features as the full Android app.
- Android and iOS share one version: `scripts/bump-version.py` updates both.
- Releases are now named "Morivia X.Y.Z" and are built by one workflow that tests Android, builds both apps and publishes them together.
- Rewritten README with download and install steps for Android and iOS.
- Removed leftover workflows from the original project that closed new issues and pull requests with Nuvio messages, and one-time setup workflows.
- Issue template links now point to Morivia's releases and README instead of Nuvio's.
- `.gitignore` no longer hides AGENTS.md, the scripts folder or the docs folder.
- The debug log script uses Morivia's app ID by default.

## 1.0.2

- Create a persistent local default profile for first-time guests instead of leaving them at an empty profile picker.
- Keep MPV playback view updates on the main thread after native snapshot reads.
- Register and remove foreground lifecycle observers on the main thread, including cancellation during navigation.
- Restore HTTPS certificate and hostname validation for media requests and prevent Authorization headers from being reattached after redirects to another origin.
- Use MoriviaMobile for update checks and offer the GitHub releases page when private releases cannot be accessed in the app.
- Never offer an APK built only for an incompatible CPU architecture.
- Run Android host tests and lint before publishing releases and on pull requests to main.
- Use Java 21 in CI so Android 16/API 36 Robolectric tests can run.
- Update the legacy progress migration test to use a real content duration rather than a filtered one-second placeholder.
- Add Android instrumentation coverage for guest startup and production FileProvider subtitle sharing; run the POSIX-only FileProvider host test on Linux CI.

## 1.0.1

- Restore Nuvio Cloud configuration for email/password and Sign in with link.
- Use the official public publishable client key when no backend override is configured.
- Reject incomplete backend overrides during the build instead of producing a broken login.

## 1.0.0

- First Morivia Android release.
- Morivia app name, launcher icon, transparent in-app logo and repository branding.
- Original Nuvio account sign-in identity retained.
- Android-only source tree and build configuration; Apple and desktop files removed.
- Independent Android versioning and a consistent development signing identity.
- Semantic version policy for every future app update.
