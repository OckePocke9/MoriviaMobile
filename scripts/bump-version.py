#!/usr/bin/env python3
"""Raise the Morivia Android version.

Usage: python3 scripts/bump-version.py

Versions go up by 0.1: 0.1, 0.2 ... 0.9, 1.0, 1.1 ... 1.9, 2.0 and so on.
gradle.properties holds the version. This script raises it, increases the build number by one
and starts a new "What's new" section in README.md. Update README.md and CHANGELOG.md afterwards.
"""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
properties = root / "gradle.properties"
text = properties.read_text()

name = re.search(r"^morivia\.versionName=(\d+)\.(\d+)(?:\.\d+)?$", text, re.M)
code = re.search(r"^morivia\.versionCode=(\d+)$", text, re.M)
if not name or not code:
    raise SystemExit("gradle.properties must contain morivia.versionName and morivia.versionCode.")

# One step of 0.1; after X.9 comes the next whole number.
major, minor = int(name[1]), int(name[2]) + 1
if minor >= 10:
    major, minor = major + 1, 0
version = f"{major}.{minor}"
build = int(code[1]) + 1

text = re.sub(r"^morivia\.versionName=.*$", f"morivia.versionName={version}", text, flags=re.M)
text = re.sub(r"^morivia\.versionCode=.*$", f"morivia.versionCode={build}", text, flags=re.M)
properties.write_text(text)

# README: the version near the top and a new "What's new" section for this version.
readme = root / "README.md"
text = readme.read_text()
text, count = re.subn(r"<!-- version -->[^<]*<!-- /version -->", f"<!-- version -->{version}<!-- /version -->", text)
if count != 1:
    raise SystemExit("README.md must contain one <!-- version -->X.Y<!-- /version --> marker.")
section = re.search(r"^## What's new in .*?(?=^See the \[changelog\])", text, re.M | re.S)
if not section:
    raise SystemExit("README.md must contain a '## What's new in X.Y' section followed by the changelog link.")
text = text[:section.start()] + f"## What's new in {version}\n\n- TODO: describe this update.\n\n" + text[section.end():]
readme.write_text(text)

print(f"Morivia Android v{version} (build {build}).")
print(f"Before committing, describe the update in README.md ('What's new in {version}') and in a '## {version}' section of CHANGELOG.md.")
