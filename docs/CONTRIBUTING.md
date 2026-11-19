# Working on Morivia

Develop on `main`. Morivia is an Android app for phones and tablets.

For every completed app update:

1. Run `.\scripts\bump-version.ps1`. Versions go up by 0.1: 0.9, 1.0, 1.1 and so on.
2. Describe the update in README.md (*What's new in X.Y*) and in a new section of `CHANGELOG.md`.
3. Commit and push to `main`.
4. Run `.\scripts\publish-release.ps1`. It tests and builds the Android app and publishes the release.

GitHub Actions is turned off for this repository and stays off, so nothing builds automatically on GitHub.

Follow [AGENTS.md](../AGENTS.md) at the repository root. Do not commit service credentials or production signing keys.
