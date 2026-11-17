# Working on Morivia

Develop on `main`. Morivia targets Android (phones and tablets) and iOS (iPhone and iPad) from the same Kotlin code, so check that a change works on both.

For every completed app update:

1. Run `.\scripts\bump-version.ps1 patch`, `minor` or `major`.
2. Describe the update in README.md (*What's new in X.Y.Z*) and in a new section of `CHANGELOG.md`.
3. Commit and push to `main`.
4. Run `.\scripts\publish-release.ps1`. It tests and builds the Android app and publishes the release. Add an iOS IPA built on a Mac with `-IpaPath`; iOS cannot be built on Windows.

GitHub Actions is turned off for this repository and stays off, so nothing builds automatically on GitHub.

Follow [AGENTS.md](../AGENTS.md) at the repository root. Do not commit service credentials or production signing keys.
