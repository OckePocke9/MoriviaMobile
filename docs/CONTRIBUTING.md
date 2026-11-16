# Working on Morivia Android

Use main for Morivia development. This project targets Android only.

For every completed app update:

1. Choose patch for fixes/small adjustments, minor for compatible new features, or major for large redesigns.
2. Run python3 scripts/bump-version.py patch, minor or major.
3. Update CHANGELOG.md and validate the build.
4. Commit the changes. The release workflow builds and publishes the matching APK and documentation.

Follow AGENTS.md at repository root. Preserve Nuvio's provider identity on Nuvio account sign-in, legal attribution and functional service contracts. Do not commit service credentials or production signing secrets.
