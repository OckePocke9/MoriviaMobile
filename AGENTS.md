# Morivia Android project rules

This project is Android-only. Keep shared Kotlin code required by Android; do not reintroduce iOS, Apple, desktop, or their release tooling.

## Required release process for every app change

- First release: 1.0.0, titled `Morivia Android 1.0.0`, tag `v1.0.0`.
- Patch: bug fixes, crashes, small text/design adjustments (1.0.0 -> 1.0.1).
- Minor: new backward-compatible features (1.0.0 -> 1.1.0).
- Major: total redesign or major changes to the app experience (1.0.0 -> 2.0.0).
- Choose the appropriate bump before committing app changes. Run `python3 scripts/bump-version.py patch|minor|major` and increment the Android versionCode exactly once per release.
- Update CHANGELOG.md with the actual changes. Build, validate, and publish the matching release after every completed app update. Never overwrite an existing version or release.
- The release title must be `Morivia Android X.Y.Z`, marked Latest, with the matching APK, README.md and CHANGELOG.md as assets.
- Preserve the original Nuvio logo and provider identity on Nuvio account sign-in. Use Morivia branding elsewhere, while preserving legal attribution and technical integration contracts.
- Launcher icon has black backing. In-app and repository logos have transparent backgrounds.
- Do not commit service credentials or production signing secrets. The checked-in development keystore is only for personal/development builds.
