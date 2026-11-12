# Contributing

How work flows from an idea to a merged pull request: branches, commit standard, local hooks, and what every PR must contain.

![From commit to release: local hooks, pull request checks, merge, tag and release](images/dev-workflow.png)

## 1. One-time setup

1. Install JDK (the version CI uses; see [ci-cd.md](ci-cd.md)) and Android Studio.
2. Put the build secrets in `~/.gradle/gradle.properties` (outside the repo). CI passes the same names as `ORG_GRADLE_PROJECT_<NAME>`; locally you write `<NAME>=value`. Names and meaning: [ci-cd.md](ci-cd.md#3-secrets-and-build-properties).
3. Install [lefthook](https://lefthook.dev) and activate the hooks **once per clone**:
   ```bash
   brew install lefthook        # or: npm i -g lefthook / go install … (see lefthook docs)
   lefthook install
   ```
4. Check it works: `git commit --allow-empty -m "bad message"` must be rejected.

## 2. Branching

| Branch | Purpose | Created from | Merges into |
|---|---|---|---|
| `main` | released code, every release is tagged `vX.Y.Z` | | |
| `develop` | integration branch, always green | | `main` (release PR) |
| `feature/<scope>-<short-name>` | new feature or use case | `develop` | `develop` |
| `fix/<scope>-<short-name>` | bug fix | `develop` | `develop` |
| `hotfix/<short-name>` | urgent production fix | `main` | `main`, then back-merge into `develop` |
| `chore/…`, `docs/…`, `ci/…` | maintenance | `develop` | `develop` |

Examples: `feature/phone_auth-manager-contact`, `fix/core-session-clear`, `docs/ci-cd`.
CI runs on pull requests and pushes to `develop` and `main`. Protect both branches (see [ci-cd.md](ci-cd.md#5-branch-protection)).

## 3. Working on a feature

1. Branch from `develop`.
2. Follow [adding-a-feature.md](adding-a-feature.md). Commit small, logical steps (each commit compiles).
3. Push early and open a **draft PR** so CI starts running.
4. When the Definition of Done is complete (section 6), mark the PR ready for review.

## 4. Commit standard

We use **Conventional Commits**: `<type>(<scope>): <subject>`.

```
feat(phone_auth): add GetManagerContactUC

Fetch the compound manager's phone before login so the phone screen can offer a
"contact the manager" action. The call is sent without a bearer token.

Refs: #123
```

### Types

| Type | Use for | Appears in release notes |
|---|---|---|
| `feat` | a new user-visible capability | yes (Features) |
| `fix` | a bug fix | yes (Fixes) |
| `perf` | a performance improvement | yes |
| `refactor` | code change that neither fixes a bug nor adds a feature | no |
| `test` | adding or fixing tests only | no |
| `docs` | documentation only | no |
| `style` | formatting (Spotless) with no logic change | no |
| `build` | Gradle, dependencies, version bumps | no |
| `ci` | pipeline, hooks, scripts | no |
| `chore` | anything else that does not touch app behaviour | no |
| `revert` | reverting a previous commit | yes |

### Scope

Lowercase name of what you changed: a feature (`phone_auth`, `maintenance`, `billing`, `visitors`, `facilities`), `core`, an app (`app-admin`, `app-resident`), or `quality`, `docs`, `deps`, `ci`. Omit the scope only when the change really spans everything.

### Rules

1. **Header** (first line): `type(scope): subject`, at most 100 characters in total, subject at most 72.
2. **Subject**: imperative mood ("add", not "added" or "adds"), starts lowercase, **no trailing period**.
3. **Body** (optional): wrap at 72 characters, explain **why**, not what; the diff already shows what.
4. **Footer** (optional): `Refs: #123` / `Closes: #123`; `BREAKING CHANGE: <description>` for incompatible changes.
5. **Breaking change**: add `!` after the scope (`feat(core)!: rename Resource.Progress`) and a `BREAKING CHANGE:` footer. Breaking changes need a docs update in the same PR.
6. One logical change per commit. Do not mix a refactor with a feature.
7. Never commit secrets, generated files or formatting-only noise in unrelated files.

### Good and bad

| Bad | Good |
|---|---|
| `fixed stuff` | `fix(core): clear the session when the refresh token is missing` |
| `Added new use case.` | `feat(phone_auth): add CheckNumberUC` |
| `feat: everything for billing` | split into `feat(billing): add GetBillsUC`, `feat(billing): add bills screen` |
| `update` | `build(deps): bump retrofit to 2.12.0` |
| `WIP` | commit locally, squash before the PR is ready |

### Enforcement

* **Locally:** the `commit-msg` hook runs `scripts/check-commit-msg.sh` and rejects non-conforming messages.
* **In CI:** the `PR title` workflow checks the PR title. We **squash-merge**, so the PR title becomes the single commit on `develop`; keep it a valid Conventional Commit.
* **Releases:** release notes are generated from `feat` / `fix` / `perf` / `revert` commits (fastlane changelog, planned; see [ci-cd.md](ci-cd.md#7-fastlane-planned)).

## 5. Local hooks (lefthook)

Configuration lives in `lefthook.yml`.

| Hook | Runs | Goal |
|---|---|---|
| `pre-commit` | Spotless (`spotlessApply`, formatted files are re-staged) | fast; never blocks on style, it just fixes it |
| `commit-msg` | `scripts/check-commit-msg.sh` | commit standard |
| `pre-push` | Detekt, Konsist architecture tests, unit tests, Android Lint | the same checks CI runs, found before the push |

Rules:

* Hooks are a safety net, **CI is the source of truth**. Keep the `pre-push` list in sync with the `validate` job.
* Skipping is allowed only in emergencies: `git push --no-verify` or `LEFTHOOK=0 git push`. CI will still block the merge.
* If a hook is too slow, fix the cause (Gradle daemon, build cache); do not disable it.

## 6. Pull requests

### Title and size

* Title = a valid commit header (`feat(phone_auth): add manager contact use case`).
* Keep PRs reviewable: one feature, or one use case, or one fix. Split large work (for example: data layer PR, then presentation PR).
* Fill in the PR template (`.github/pull_request_template.md`).

### What every feature PR must contain

| Evidence | Required when | Details |
|---|---|---|
| **Unit tests** | always (mapper, request contract, use case, repository, ViewModel) | [testing-and-quality.md](testing-and-quality.md#1-unit-tests) |
| **Screenshots of every screen you added or changed** | any UI change | below |
| **`@Preview`s for all cases** | any composable added or changed | below |
| **Instrumented test** | the feature is a flow across screens or features, uses Android framework behaviour, or touches login, payment or navigation | [testing-and-quality.md](testing-and-quality.md#3-instrumented-tests) |
| **Konsist, Detekt, Spotless, lint green** | always | CI + pre-push |
| **ProGuard / R8 rules + a minified build run** | any new DTO or serialized model | `consumer-rules.pro` |
| **Docs updated** | a base class, convention or pipeline step changed | |

### Screenshots

Add them to the PR description (drag and drop). For **each screen and each state you built**:

* the **states**: initial, loading, content, empty, error, offline, validation errors, disabled;
* **light and dark** theme;
* **every supported language**, including right-to-left layouts;
* optionally a large font scale (1.5×) for screens with dense text.

Use the table in the PR template. Take them from the `@Preview` panel (copy / export image) or from an emulator. No UI change? Tick "Not a UI change".

### Previews

Previews are the cheapest way to prove every case works, and reviewers can open them in Android Studio. Rules:

* Every `*Screen` composable has previews for **all states** (use a `PreviewParameterProvider` over the state class) and for **light/dark × every supported language × large font**, using the shared preview annotation.
* Screens stay **stateless** (state in, actions out) so previews need no ViewModel or Koin.
* Konsist verifies that every `*Screen.kt` file contains a preview.

How to write them: [testing-and-quality.md](testing-and-quality.md#2-compose-previews-and-screenshots).

### Instrumented tests: when is "it time"?

Add at least one instrumented test (happy path + one failure path) when the PR:

* adds or changes a **flow across two or more screens** (phone number → OTP → home);
* connects **two features** (login then a protected call, booking then payment, visitor pass then QR);
* depends on **Android behaviour**: permissions, NFC, camera / QR, notifications, deep links, navigation back stack, process restore;
* fixes a bug that only reproduces on a device.

Pure domain/data changes and single stateless composables are covered by unit tests and previews.

### Review checklist (for reviewers)

- [ ] PR title and commits follow the standard; scope is right
- [ ] Structure follows [project-structure.md](project-structure.md): correct packages, names, visibility
- [ ] Correct base classes (`BaseUseCase` / `AuthenticatedUseCase`, `MVIViewModel`); no `try/catch` around network calls; no direct dispatchers
- [ ] DTO fields nullable, mapper handles nulls; request contract declares every key
- [ ] Koin module registered everywhere it is used; ViewModel is `viewModelOf`
- [ ] Screenshots match the previews and cover all states, themes and languages
- [ ] Tests are meaningful (they fail when the code is wrong)
- [ ] No secrets, demo flags, logging of personal data
- [ ] CI is green

## 7. Merging and releasing

* Merge with **squash** after review and green checks. Delete the branch.
* A release is a PR `develop → main`, then a tag `vX.Y.Z` on `main` that matches `version.properties`. Full steps: [ci-cd.md](ci-cd.md#4-release-process).
