# Morivia project rules

Morivia is an Android app for phones and tablets, built with Kotlin Multiplatform and Compose Multiplatform. Shared code lives in `composeApp/src/commonMain`; Android code in `androidMain`/`androidFull`/`androidPlaystore`; the app module in `androidApp/`. Morivia is Android only since 2026-11-19 (the owner's decision): iOS and desktop are not targets, and the iOS app, its code and MPVKit were removed.

## Release process for every app change

1. Versions go up by 0.1 for every release: 0.1, 0.2 … 0.9, 1.0, 1.1 … 1.9, 2.0 (the owner's decision on 2026-11-19; the releases published as 1.0.0–1.7.1 were renamed v0.1–v1.5).
2. Run `.\scripts\bump-version.ps1` (or `python3 scripts/bump-version.py`, which does the same). It raises `morivia.versionName` by 0.1 and `morivia.versionCode` by one in `gradle.properties` and starts the README section below. Never edit the version by hand.
3. Write the README for every new version, every time: the script sets the version near the top and starts `## What's new in X.Y`; replace its TODO line with a short, user-facing summary, and update every other README section the release changes (What Morivia does, Player, Get Morivia and so on), so the README always describes the latest release. Add a `## X.Y` section to `CHANGELOG.md` with every change.
4. Commit and push to `main`.
5. Publish from the PC with `.\scripts\publish-release.ps1`. It refuses to run until the README and changelog are written and `main` is pushed, runs the Android host tests, builds and checks the signed APK, and publishes the release `Morivia Android vX.Y` (tag `vX.Y` on the pushed commit, marked Latest) with the APK, README.md, CHANGELOG.md, LICENSE and SHA256SUMS.txt. This public repository is Morivia's official repository, and the in-app updater reads its latest release. Releases from 1.6 are signed with the private release key (kept outside Git, set up in `local.properties` as `NUVIO_RELEASE_*`); the script refuses an APK signed with another key.
6. Never overwrite or delete a published version. If the build or tests fail, nothing is published: fix it and run the script again.

**GitHub Actions is turned off for this repository and must stay off** (the owner's decision on 2026-11-17, after the free Actions minutes ran out). Do not enable Actions or add automatic triggers (push, pull_request, schedule, release) to workflows. The files in `.github/workflows/` only run when started by hand, which needs Actions turned on again by the owner.

## Branding

- Owner's locked reference (2026-11-19): keep Nuvio sign-in exactly as local build 24. Do not redesign it or let Morivia theme, app logo or AMOLED settings affect it. Restore accidental changes to that baseline. A local source backup is in `logs/locked-nuvio-sign-in-build-24/`; AuthScreen.kt SHA-256 is `5E39C3B05B2A67912EF069B3836446E1D2B7E9146AC7BB7F9971CC994113E4AE`.
- During current local testing, retain version name 1.6 and increment only versionCode (24, 25, 26...). Do not push or publish until the owner explicitly asks. Install directly through ADB over wifi; do not use desktop control or Android Studio UI.

- The app is Morivia everywhere: app name, launcher and home screen icons, in-app logo, README and release names.
- Nuvio account sign-in keeps Nuvio's own name and logo, because it is Nuvio's service. Texts that refer to Nuvio's services (Nuvio Cloud, the Nuvio web panel, Nuvio supporter membership) keep the Nuvio name.
- Internal technical names stay unchanged unless there is a reason to change them: the `com.nuvio.app` Kotlin package, the `nuvio://` link scheme, `nuvio.*` Gradle properties, `NUVIO_*` configuration keys and the Nuvio streaming engine. Renaming them would break builds, links or sync with Nuvio's services.
- The Android app ID is `com.morivia.app`.
- The launcher icon has a black background. In-app and repository logos have transparent backgrounds.
- Keep the original authors' copyright, the GPL-3.0 license and third-party notices.

## Secrets

Do not commit service credentials, API keys or production signing keys. Optional runtime settings go in `local.properties` (ignored by Git) or the repository secret `MORIVIA_LOCAL_PROPERTIES_BASE64`. The keystore in `.ci/` is a shared development key for personal builds only.
