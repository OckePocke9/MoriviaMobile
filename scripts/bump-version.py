#!/usr/bin/env python3
"""Raise the Morivia version for Android and iOS.

Usage: python3 scripts/bump-version.py patch|minor|major

gradle.properties holds the version. This script raises it, increases the build number by one
and writes the same values to iosApp/Configuration/Version.xcconfig. Update CHANGELOG.md afterwards.
"""
from pathlib import Path
import re
import sys

kind = sys.argv[1] if len(sys.argv) == 2 else ""
if kind not in ("patch", "minor", "major"):
    raise SystemExit("Usage: python3 scripts/bump-version.py patch|minor|major")

root = Path(__file__).resolve().parents[1]
properties = root / "gradle.properties"
text = properties.read_text()

name = re.search(r"^morivia\.versionName=(\d+)\.(\d+)\.(\d+)$", text, re.M)
code = re.search(r"^morivia\.versionCode=(\d+)$", text, re.M)
if not name or not code:
    raise SystemExit("gradle.properties must contain morivia.versionName and morivia.versionCode.")

major, minor, patch = map(int, name.groups())
if kind == "major":
    major, minor, patch = major + 1, 0, 0
elif kind == "minor":
    minor, patch = minor + 1, 0
else:
    patch += 1
version = f"{major}.{minor}.{patch}"
build = int(code[1]) + 1

text = re.sub(r"^morivia\.versionName=.*$", f"morivia.versionName={version}", text, flags=re.M)
text = re.sub(r"^morivia\.versionCode=.*$", f"morivia.versionCode={build}", text, flags=re.M)
properties.write_text(text)

(root / "iosApp/Configuration/Version.xcconfig").write_text(
    "// Written by scripts/bump-version.py from gradle.properties. Do not edit by hand.\n"
    f"MARKETING_VERSION={version}\n"
    f"CURRENT_PROJECT_VERSION={build}\n"
)

# README: the version under the logo and a new "What's new" section for this version.
readme = root / "README.md"
text = readme.read_text()
text, count = re.subn(r"<!-- version -->[^<]*<!-- /version -->", f"<!-- version -->{version}<!-- /version -->", text)
if count != 1:
    raise SystemExit("README.md must contain one <!-- version -->X.Y.Z<!-- /version --> marker.")
section = re.search(r"^## What's new in .*?(?=^See the \[changelog\])", text, re.M | re.S)
if not section:
    raise SystemExit("README.md must contain a '## What's new in X.Y.Z' section followed by the changelog link.")
text = text[:section.start()] + f"## What's new in {version}\n\n- TODO: describe this update.\n\n" + text[section.end():]
readme.write_text(text)

print(f"Morivia {version} (build {build}).")
print(f"Before committing, describe the update in README.md ('What's new in {version}') and in a '## {version}' section of CHANGELOG.md.")
