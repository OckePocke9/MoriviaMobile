# Development signing

This keystore uses the standard Android debug credentials and is public. It signs debug builds and release builds made by anyone without Morivia's release key.

Official Morivia releases from 1.6 are signed with a private release key that is kept outside Git. Its path and password go in `local.properties` as `MORIVIA_RELEASE_STORE_FILE`, `MORIVIA_RELEASE_STORE_PASSWORD`, `MORIVIA_RELEASE_KEY_ALIAS` and `MORIVIA_RELEASE_KEY_PASSWORD`. Releases 0.1-1.5 were signed with this development key.
