# Working on Morivia

Develop on `main`. Morivia targets Android (phones and tablets) and iOS (iPhone and iPad) from the same Kotlin code, so check that a change works on both.

For every completed app update:

1. Run `python3 scripts/bump-version.py patch`, `minor` or `major`.
2. Add the new version's section to `CHANGELOG.md`.
3. Build Android locally (see the README). iOS builds need macOS; GitHub Actions builds them on every release.
4. Commit and push. The release workflow tests both apps and publishes the release.

Pull requests to `main` get an Android debug build from `.github/workflows/pr-full-debug-build.yml`.

Follow [AGENTS.md](../AGENTS.md) at the repository root. Do not commit service credentials or production signing keys.
