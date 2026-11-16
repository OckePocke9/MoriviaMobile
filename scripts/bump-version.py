#!/usr/bin/env python3
from pathlib import Path
import re,sys
kind=sys.argv[1] if len(sys.argv)==2 else ""
if kind not in ("patch","minor","major"): raise SystemExit("Usage: python3 scripts/bump-version.py patch|minor|major")
p=Path(__file__).resolve().parents[1]/"gradle.properties"
s=p.read_text()
old=re.search(r"^morivia.versionName=(\d+)\.(\d+)\.(\d+)$",s,re.M)
a,b,c=map(int,old.groups())
a,b,c=(a+1,0,0) if kind=="major" else (a,b+1,0) if kind=="minor" else (a,b,c+1)
version=f"{a}.{b}.{c}"
s=s[:old.start()]+f"morivia.versionName={version}"+s[old.end():]
s=re.sub(r"^morivia.versionCode=(\d+)$",lambda m:f"morivia.versionCode={int(m[1])+1}",s,flags=re.M)
p.write_text(s)
previous=old[0].split("=")[1]
r=p.parent/"README.md"
text=r.read_text().replace(f"## Version {previous}",f"## Version {version}").replace(f"Morivia Android {previous}",f"Morivia Android {version}").replace(f"Morivia-Android-{previous}-",f"Morivia-Android-{version}-")
r.write_text(text)
print(f"Morivia Android {version}. Update CHANGELOG.md before committing.")
