# Morivia project rules

Morivia is one Kotlin Multiplatform app for Android (phones and tablets) and iOS (iPhone and iPad). Shared code lives in `composeApp/src/commonMain`; platform code in `androidMain`/`androidFull`/`androidPlaystore` and `iosMain`/`iosFull`/`iosAppStore`; the iOS app shell in `iosApp/`. Keep both platforms building. Desktop is not a target.

## Release process for every app change

1. Choose the bump: patch for fixes and small text or design changes (1.1.0 → 1.1.1), minor for new features (1.1.0 → 1.2.0), major for a redesign or a large change to the app (1.1.0 → 2.0.0).
2. Run `.\scripts\bump-version.ps1 patch|minor|major` (or `python3 scripts/bump-version.py`, which does the same). It raises `morivia.versionName` and `morivia.versionCode` in `gradle.properties`, writes the same values to `iosApp/Configuration/Version.xcconfig` and starts the README section below. Never edit the version by hand.
3. Write the README for the new version: the script sets the version under the logo and starts `## What's new in X.Y.Z`; replace its TODO line with a short, user-facing summary, and update any other README section the change affects. Add a `## X.Y.Z` section to `CHANGELOG.md` with every change.
4. Commit and push to `main`.
5. Publish from the PC with `.\scripts\publish-release.ps1`. It refuses to run until the README and changelog are written and `main` is pushed, runs the Android host tests, builds and checks the signed APK, and publishes the release `Morivia X.Y.Z` (tag `vX.Y.Z` on the pushed commit, marked Latest) with the APK, README.md, CHANGELOG.md, LICENSE and SHA256SUMS.txt. Add an unsigned iOS IPA built on a Mac with `-IpaPath`; without one the release has the Android app only. It then copies the APK and release notes to the public repository `OckePocke9/MoriviaUpdates` (`scripts/publish-update-mirror.ps1`), because the in-app updater cannot read this private repository. Only the APK, checksums and notes go there, never source code.
6. Never overwrite or delete a published version. If the build or tests fail, nothing is published: fix it and run the script again.

**GitHub Actions is turned off for this repository and must stay off** (the owner's decision on 2026-11-17, after the free Actions minutes ran out). Do not enable Actions or add automatic triggers (push, pull_request, schedule, release) to workflows. The files in `.github/workflows/` only run when started by hand, which needs Actions turned on again by the owner.

## Branding

- The app is Morivia everywhere: app name, launcher and home screen icons, in-app logo, README and release names.
- Nuvio account sign-in keeps Nuvio's own name and logo, because it is Nuvio's service. Texts that refer to Nuvio's services (Nuvio Cloud, the Nuvio web panel, Nuvio supporter membership) keep the Nuvio name.
- Internal technical names stay unchanged unless there is a reason to change them: the `com.nuvio.app` Kotlin package, the `nuvio://` link scheme, `nuvio.*` Gradle properties, `NUVIO_*` configuration keys and the Nuvio streaming engine. Renaming them would break builds, links or sync with Nuvio's services.
- The Android app ID is `com.morivia.app`; the iOS bundle ID is `com.morivia.app` (debug builds: `com.morivia.app.debug`).
- The launcher icon has a black background. In-app and repository logos have transparent backgrounds.
- Keep the original authors' copyright, the GPL-3.0 license and third-party notices.

## Secrets

Do not commit service credentials, API keys or production signing keys. Optional runtime settings go in `local.properties` (ignored by Git) or the repository secret `MORIVIA_LOCAL_PROPERTIES_BASE64`. The keystore in `.ci/` is a shared development key for personal builds only.
