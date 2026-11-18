# Verification status: Morivia 1.4.0 (build 7)

## Completed static checks

- Shared version is 1.4.0, code 7; the version script wrote the same iOS Version.xcconfig values.
- 31 explicit app-language choices, excluding device language.
- Swedish and Finnish resource XML parses and positional format arguments match the English resources. Each locale currently translates 452 strings; the normal English fallback remains for untranslated strings.
- Android manifest, adaptive/legacy launcher XML and iOS icon catalog references parse and resolve. Existing launcher activity names and the app IDs remain unchanged.
- All 14 generated transparent icon variants have their visible artwork centred within one pixel. Android launcher previews and the common picker use identical generated images and gradients from ThemeColors.kt.
- All 48 locally reviewed Kotlin files parse without new syntax errors relative to the 1.3.0 source. Three old-grammar parse findings in pre-existing Kotlin constructs were compared with their unchanged baseline; this is not a Kotlin compiler check.
- Android signing configuration still uses the existing development keystore. No key or app ID was rotated.

## Added automated tests (not executed yet)

- Profile page callbacks preserve Switch Profile, Account and Settings destinations.
- Settings root does not duplicate profile/account actions.
- Every preset theme resolves to a matching launcher palette and unique component name.

## Pending before an installable release

The GitHub Actions release run did not start its version job because hosted runners are unavailable to this account. No compiler, Android host tests, lint or iOS build ran. The local fallback could fetch source and SDK tools, but Java/Gradle could not reach its build dependencies in this environment.

Run `.github/workflows/morivia-release.yml` after runners are available. It must pass tests and lint, build Android and iOS, verify APK identity/version/signature, then publish both together. Version 1.4.0 has NOT been published and there is no installable 1.4.0 APK/IPA yet. Fix any compilation/test failures without bumping again while this version is unpublished.

For private GitHub releases the updater opens the authenticated GitHub browser when anonymous API access fails. Stable opens `/releases/latest`; Beta opens `/releases`. Do not bundle GitHub credentials or make the repository public as an update workaround.
