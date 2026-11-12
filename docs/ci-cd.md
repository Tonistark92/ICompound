# CI/CD and local hooks

> Written from the workflow `ICompound-ci`. **As pasted, every line of that file is commented out, so GitHub ignores it.** To activate it, remove the leading `#` and save it as `.github/workflows/android-ci.yml`.

![From commit to release](docs/images/dev-workflow.png)

## 1. Two safety nets

| | Local hooks (lefthook) | CI (GitHub Actions) |
|---|---|---|
| When | `git commit`, `git push` | pull request, push to `develop` / `main`, tag `v*`, manual run |
| Checks | Spotless · commit message · Detekt · Konsist · unit tests · lint | the same plus debug build, instrumented tests, release build |
| Purpose | fast feedback before the code leaves your machine | the **source of truth**: a PR cannot merge unless it is green |
| Skippable | yes (`--no-verify`, emergencies only) | no (branch protection) |

Keep the lists in sync: whatever `pre-push` runs must also run in the `validate` job. Hook details: [contributing.md](contributing.md#5-local-hooks-lefthook).

## 2. The pipeline

**Triggers:** `pull_request` into `develop` / `main`; `push` to `develop` / `main`; tags `v*`; manual (`workflow_dispatch`). Permissions are read-only (`contents: read`). The `concurrency` group cancels an older run of the same branch when you push again, but never cancels a tag (release) run.

| Job | Runs on | Needs | Steps |
|---|---|---|---|
| **validate** (Validate Android Project) | every trigger | none | checkout · JDK · Gradle · **release version check** (tags only) · `spotlessCheck` · `detekt` · `:quality:konsist:test` · `test` · `lint` · `assembleDebug` · upload reports |
| **ui-tests** (Android UI Tests) | every trigger | `validate` | enable KVM · emulator API 35 (x86_64, google_apis, pixel_2) · `connectedDebugAndroidTest` |
| **release-production** (Build Production Release) | tags `v*` only | `validate`, `ui-tests` | environment `production` (approval gate) · decode keystore from a secret · `bundleRelease` · upload the AAB (30 days) |

### Release version check (tags only)

For a tag `v1.2.3` the job reads `version.properties` and fails unless:

1. the tag matches `v<MAJOR>.<MINOR>.<PATCH>`;
2. `VERSION_NAME` equals the tag without the `v`;
3. `VERSION_CODE` is a positive integer.

```properties
# version.properties
VERSION_NAME=1.2.3
VERSION_CODE=12
```

`VERSION_CODE` must increase with every Play Store upload.

### Reports

Test, lint and Detekt reports are uploaded as the `android-quality-reports` artifact (7 days), even when a step fails. Open them first when CI is red.

## 3. Secrets and build properties

The workflow exposes secrets as `ORG_GRADLE_PROJECT_<NAME>` environment variables. Gradle turns each one into the project property `<NAME>`, so the same name works in your local `~/.gradle/gradle.properties`.

| Name | Used for (meaning inferred from the name) | Local equivalent |
|---|---|---|
| `API_KEY` | backend API key | `~/.gradle/gradle.properties` |
| `SOCKET_PORT` | realtime socket port | same |
| `GOOGLE_CLIENT_ID` | Google sign-in client id | same |
| `IC_COMPOUND_DATASTORE_NAME` | name of the preferences DataStore file | same |
| `REMOTE_URL` | production base URL | same |
| `DEBUG_REMOTE_URL` | base URL for debug builds | same |

Release-only secrets (used by `release-production` only):

| Name | Purpose |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | the release keystore, base64 encoded |
| `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD` | signing credentials |

Read them in Gradle with `providers.gradleProperty("API_KEY")` and expose them as `buildConfigField`s. Rules: [security-and-release.md](security-and-release.md#2-build-secrets). Create/rotate secrets in *GitHub → Settings → Secrets and variables → Actions*; put the release ones in the `production` **environment** so only that job can read them.

> Certificate pins are not in this list. If the app reads them from build properties, add them to the release job (see section 6).

## 4. Release process

1. Make sure `develop` is green and contains everything for the release.
2. Open a PR `develop → main` titled `chore(release): v1.2.3`. In that PR bump `version.properties` (`VERSION_NAME`, `VERSION_CODE`).
3. Merge it after review and green checks.
4. Tag the merge commit on `main`:
   ```bash
   git checkout main && git pull
   git tag v1.2.3
   git push origin v1.2.3
   ```
5. The tag run executes `validate` (including the version check), `ui-tests`, then **waits for approval** on the `production` environment.
6. After approval it builds the signed AAB and uploads it as `production-aab-v1.2.3`.
7. **Today:** download the AAB and upload it in the Play Console. **Later:** fastlane does this (section 7).
8. Write release notes from the `feat` / `fix` commits since the previous tag.

Hotfix: branch `hotfix/<name>` from `main`, fix, PR into `main`, bump the patch version, tag, then back-merge `main` into `develop`.

## 5. Branch protection

Configure in *GitHub → Settings → Branches* for `main` and `develop`:

* Require a pull request, at least 1 approval, dismiss stale approvals on new commits.
* Require status checks: **Validate Android Project**, **Android UI Tests**, **PR title follows the commit standard**.
* Require branches to be up to date; allow **squash merge only**; no force pushes; no deletions.
* Restrict who can create `v*` tags (tag protection / rulesets).
* Require the `production` environment reviewers for releases.

## 6. Review of the current workflow: recommended changes

Ordered by importance.

1. **Scope secrets to the jobs that need them.** The `env:` block at the top of the file puts every secret in front of every step of every job, including third-party actions, and runs on PRs where secrets are empty for forks. Keep only non-secret placeholders for `validate` and `ui-tests` (they compile and test against a mock server), and give real values only to `release-production`:
   ```yaml
   validate:
     env:
       ORG_GRADLE_PROJECT_REMOTE_URL: https://example.invalid/
       ORG_GRADLE_PROJECT_DEBUG_REMOTE_URL: https://example.invalid/
       ORG_GRADLE_PROJECT_API_KEY: ci-placeholder
       # …same for the other properties
   release-production:
     env:
       ORG_GRADLE_PROJECT_API_KEY: ${{ secrets.API_KEY }}
       # …real values here only
   ```
2. **Two apps, one artifact path.** `bundleRelease` builds every app module and the upload step only reads `app/build/outputs/…`. With `app-admin` and `app-resident`, build and upload each one:
   ```yaml
   release-production:
     strategy:
       matrix:
         app: [app-admin, app-resident]
     steps:
       - run: ./gradlew :${{ matrix.app }}:bundleRelease
       - uses: actions/upload-artifact@v4
         with:
           name: ${{ matrix.app }}-aab-${{ github.ref_name }}
           path: ${{ matrix.app }}/build/outputs/bundle/release/*.aab
   ```
   Decide the versioning model: **one tag releases both apps** (shared `version.properties`, simplest while they ship together), or **per-app tags** (`admin-v1.2.3`, `resident-v1.2.3`, one version file per app, tag filter and version check adjusted).
3. **Upload the R8 mapping file** with the AAB. Without it production stack traces cannot be de-obfuscated:
   ```yaml
   path: |
     app-admin/build/outputs/bundle/release/*.aab
     app-admin/build/outputs/mapping/release/mapping.txt
   ```
4. **Restrict release tags to `main`.** Anyone who can push a tag can start a release from any commit. Add `fetch-depth: 0` to the checkout of the release job, then:
   ```yaml
   - name: Verify the tag points to a commit on main
     run: |
       git merge-base --is-ancestor "$GITHUB_SHA" origin/main \
         || { echo "::error::Release tags must be on main"; exit 1; }
   ```
5. **Pin every action the same way.** `checkout` and `setup-java` are pinned to commit SHAs; `setup-gradle`, `upload-artifact` and `android-emulator-runner` use tags. Pin all of them to SHAs and let Dependabot update them:
   ```yaml
   # .github/dependabot.yml
   version: 2
   updates:
     - package-ecosystem: github-actions
       directory: /
       schedule: { interval: weekly }
   ```
6. **Certificate pins in CI.** If release pinning reads `PIN_PRIMARY` / `PIN_BACKUP` from properties, add them to the release job only; a release built without them has no pinning.
7. **JDK 25.** Make sure the Gradle, Android Gradle Plugin and Kotlin versions you use support running on JDK 25; if you see toolchain errors, JDK 21 or 17 are the safe choices. Keep local and CI JDKs identical.
8. **Add `timeout-minutes`** to each job (for example 30 for `validate`, 45 for `ui-tests`) so a hung emulator cannot burn runner minutes.
9. **Do not run Konsist twice.** `test` already includes `:quality:konsist:test`. In the unit-test step use `./gradlew test -x :quality:konsist:test` and keep the dedicated Konsist step for a readable log.
10. **Speed up UI tests** (optional): cache the AVD snapshot, or run them on PRs into `main` and on a label, once the suite grows. Until then keep them on every PR, because the Definition of Done requires them.

## 7. Fastlane (planned)

Not implemented yet. Plan, so the current setup does not need rework:

* **Layout:** `Gemfile` (`gem "fastlane"`), `fastlane/Appfile`, `fastlane/Fastfile`, and later `fastlane/metadata/android/…` for store listings.
* **Lanes:**
  | Lane | Does |
  |---|---|
  | `test` | Spotless, Detekt, Konsist, unit tests, lint (the same commands as the hooks) |
  | `build_release` | signed `bundleRelease` for the chosen app |
  | `internal` | `upload_to_play_store(track: "internal", aab: …)` |
  | `promote` | internal → beta → production |
  | `changelog` | release notes from Conventional Commits (`feat`, `fix`, `perf`, `revert`) via `changelog_from_git_commits` |
* **CI change:** add `ruby/setup-ruby` with `bundler-cache: true`, then replace "Build signed production AAB" with `bundle exec fastlane build_release app:<app>` and add a final step `bundle exec fastlane internal`. Keep the artifact upload.
* **New secret** in the `production` environment: `PLAY_SERVICE_ACCOUNT_JSON` (base64 of the Google Play service-account key).
* **Versioning:** keep `version.properties` as the single source; the lane reads it, it does not bump it.
* **Commit standard** is already what makes the changelog lane work, which is why it is enforced from day one.

## 8. Troubleshooting

| Symptom | Likely cause |
|---|---|
| Workflow does not run | the file is still fully commented, or not in `.github/workflows/` |
| Version check fails on a tag | tag not `vX.Y.Z`, or `VERSION_NAME` in `version.properties` differs from the tag |
| `validate` fails on `spotlessCheck` only | run `./gradlew spotlessApply` and commit (the `pre-commit` hook normally does this) |
| Konsist fails in CI but passed locally | your branch is behind `develop`, or a rule changed; rebase and re-run `:quality:konsist:test` |
| `ui-tests` hangs | emulator boot problem; re-run, and add `timeout-minutes` |
| Release build works in debug but DTOs are empty | missing R8 keep rules ([testing-and-quality.md](testing-and-quality.md#5-proguard--r8-rules)) |
| Release job is skipped | not a tag, or the tag does not start with `v` |
