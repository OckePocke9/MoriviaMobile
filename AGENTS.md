# Morivia project rules

Morivia is one Kotlin Multiplatform app for Android (phones and tablets) and iOS (iPhone and iPad). Shared code lives in `composeApp/src/commonMain`; platform code in `androidMain`/`androidFull`/`androidPlaystore` and `iosMain`/`iosFull`/`iosAppStore`; the iOS app shell in `iosApp/`. Keep both platforms building. Desktop is not a target.

## Release process for every app change

1. Choose the bump: patch for fixes and small text or design changes (1.1.0 → 1.1.1), minor for new features (1.1.0 → 1.2.0), major for a redesign or a large change to the app (1.1.0 → 2.0.0).
2. Run `python3 scripts/bump-version.py patch|minor|major`. It raises `morivia.versionName` and `morivia.versionCode` in `gradle.properties` and writes the same values to `iosApp/Configuration/Version.xcconfig`. Never edit the version by hand.
3. Write the README for the new version: the script sets the version under the logo and starts `## What's new in X.Y.Z`; replace its TODO line with a short, user-facing summary, and update any other README section the change affects. Add a `## X.Y.Z` section to `CHANGELOG.md` with every change. The release workflow refuses to build until both are written.
4. Commit and push to `main`. `.github/workflows/morivia-release.yml` tests and builds the Android APK and the unsigned iOS IPA and publishes the release `Morivia X.Y.Z` (tag `vX.Y.Z`, marked Latest) with both files, README.md, CHANGELOG.md, LICENSE and SHA256SUMS.txt.
5. Never overwrite or delete a published version. If the build fails, fix it and push again; nothing is published until both platforms build.

## Branding

- The app is Morivia everywhere: app name, launcher and home screen icons, in-app logo, README and release names.
- Nuvio account sign-in keeps Nuvio's own name and logo, because it is Nuvio's service. Texts that refer to Nuvio's services (Nuvio Cloud, the Nuvio web panel, Nuvio supporter membership) keep the Nuvio name.
- Internal technical names stay unchanged unless there is a reason to change them: the `com.nuvio.app` Kotlin package, the `nuvio://` link scheme, `nuvio.*` Gradle properties, `NUVIO_*` configuration keys and the Nuvio streaming engine. Renaming them would break builds, links or sync with Nuvio's services.
- The Android app ID is `com.morivia.app`; the iOS bundle ID is `com.morivia.app` (debug builds: `com.morivia.app.debug`).
- The launcher icon has a black background. In-app and repository logos have transparent backgrounds.
- Keep the original authors' copyright, the GPL-3.0 license and third-party notices.

## Secrets

Do not commit service credentials, API keys or production signing keys. Optional runtime settings go in `local.properties` (ignored by Git) or the repository secret `MORIVIA_LOCAL_PROPERTIES_BASE64`. The keystore in `.ci/` is a shared development key for personal builds only.
