# Release Process

## Unreleased UI changes

- Added palette-aware glass navigation and Settings panels without live blur; existing light/dark/system and Android dynamic-color choices remain in effect.
- Preserved the six-page pager and all connection handoffs while adding an automatic labeled tablet rail (600dp) and sidebar (1000dp). Fullscreen desktop sessions hide both phone and tablet chrome.
- Grouped Settings into localized Security & Privacy, Terminal & Input, Appearance & Language, Backup & Restore, and About & Updates sections. Wide windows use bounded two-column Settings content from 840dp.
- Glass fills combine palette tint and diagonal highlights in one cached translucent gradient with a gradient rim. A static colored backdrop is drawn once behind transparent navigation/destination containers, making translucency visible without live blur/refraction or repeated selected-state fills. Pager pages use retained graphics layers so horizontal motion repositions display lists rather than redrawing static screen content; animations remain enabled.
- Terminal swipes resolve relative to the gesture's starting page, preserving one-page navigation even after crossing the pager midpoint. Terminal selection, IME, fullscreen and connection handoffs retain their existing behavior.
- Retained inactive terminal pages no longer register the Activity hardware-key interceptor or accept terminal input; background sessions/output remain alive.
- Fork debug builds use `com.hension.havenx.glassdebug` for side-by-side UI previews; release and existing `.debug` installations and their data are not replaced.
- Verified implementation commit `6cc6f3f` through GitHub Actions Build and CI (APK, unit tests, lint). Actions-built `.glassdebug` preview exercised repeated six-page switching at actual 1080×2400/440dpi phone and 1600×2560/320dpi tablet viewports, plus light/dark glass and connection/theme dialogs. Restored emulator display overrides afterward; existing `.debug` and physical phone data were untouched.
- Empty-session emulator frame samples do not establish a speedup: warmed phone baseline/fix p50/p90/p95 were both 6/8/9ms (jank 1.85%/1.88%); tablet baseline/fix p50/p90 were 5/7ms, p95 8/10ms. Live SSH output, selection, remote fullscreen and connection handoffs require real session credentials and were not runtime-verified. No local compilation/tests/lint were run.

## 1. Bump version

Edit `app/build.gradle.kts`:
```kotlin
versionCode = <increment>
versionName = "<x.y.z>"
```

## 2. Commit, tag, push

```bash
git add app/build.gradle.kts <changed files>
git commit -m "Bump to v<x.y.z>"
git tag v<x.y.z>
git push origin main v<x.y.z>
```

The `v*` tag triggers the **Release** workflow on GitHub Actions which:
- Builds a signed APK and AAB (keystore password from GitHub secrets)
- Creates a GitHub release with the APK attached

## 3. Download Play Store bundle

```bash
mkdir -p releases/v<x.y.z>
gh release download v<x.y.z> --repo GlassOnTin/Haven --pattern '*.aab' -D releases/v<x.y.z>/
```

Upload the AAB to Google Play Console.

## 4. Update F-Droid merge request

The F-Droid metadata lives in a GitLab fork:
- Repo: `ianrosswilliams/fdroiddata`
- Branch: `haven-ssh-client`
- MR: `fdroid/fdroiddata!33920`

Edit `metadata/sh.haven.app.yml`:
- **Replace** the single build entry (only keep the latest version)
- Use the **full commit hash**, not the tag name
- Update `CurrentVersion` and `CurrentVersionCode`

```bash
# Get the commit hash for the tag
git rev-parse v<x.y.z>
```

```yaml
Builds:
  - versionName: <x.y.z>
    versionCode: <code>
    commit: <full commit hash>
    subdir: app
    gradle:
      - yes

CurrentVersion: <x.y.z>
CurrentVersionCode: <code>
```

### Rebase before pushing

The fork's branch must be on top of upstream master or GitLab won't merge it.
Clone upstream, create the branch from its master, and force-push to the fork:

```bash
cd /tmp
git clone --no-checkout --single-branch --branch master \
  "https://oauth2:${GITLAB_TOKEN}@gitlab.com/fdroid/fdroiddata.git" fdroiddata-rebase
cd fdroiddata-rebase
git remote add fork \
  "https://oauth2:${GITLAB_TOKEN}@gitlab.com/ianrosswilliams/fdroiddata.git"
git checkout -b haven-ssh-client master
# create/update metadata/sh.haven.app.yml here
git add metadata/sh.haven.app.yml
git commit -m "Add Haven SSH client v<x.y.z>"
git push fork haven-ssh-client --force
cd / && rm -rf /tmp/fdroiddata-rebase
```

### MR description template

The MR description must follow the F-Droid "App inclusion" template with Required/Strongly Recommended/Suggested checkboxes. See `.gitlab/merge_request_templates/App inclusion.md` in fdroiddata.

## 5. Verify

- [ ] GitHub release page has APK
- [ ] CI workflow passes (lint + tests)
- [ ] F-Droid MR pipeline passes
- [ ] Play Store bundle uploaded (if applicable)

## Signing

The release keystore `haven-release.jks` is in the repo root.
Passwords are stored in GitHub secrets:
- `KEYSTORE_PASSWORD`
- `KEY_PASSWORD`
- `KEY_ALIAS`

Local release builds require these as environment variables:
```bash
export KEYSTORE_PASSWORD=<password>
export KEY_PASSWORD=<password>
./gradlew :app:bundleRelease
```

## F-Droid details

- GitLab username: `ianrosswilliams`
- GitHub username: `GlassOnTin`
- F-Droid builds from source using the tagged commit
- `AutoUpdateMode: Version` + `UpdateCheckMode: Tags` means F-Droid will auto-detect new tags
