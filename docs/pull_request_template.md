## What and why
<!-- What does this PR change, and why? Link the ticket / issue. -->

Closes #

## Type
<!-- Same as the PR title prefix: feat | fix | refactor | perf | test | docs | style | build | ci | chore -->

## How to test
<!-- Steps a reviewer can follow. Mention test accounts or flags if needed. -->

## Screenshots (required when a screen was added or changed)
<!-- Drag images in. Cover every state you built: initial, loading, content, empty, error, offline, validation errors. -->

| Screen / state | Light | Dark | English (LTR) | Arabic (RTL) / other languages |
|---|---|---|---|---|
|  |  |  |  |  |

- [ ] Not a UI change (no screenshots needed)

## Checklist

**Code and architecture**
- [ ] PR title follows the commit standard (`feat(scope): subject`)
- [ ] Follows the feature recipe (docs/adding-a-feature.md); naming and packages match docs/project-structure.md
- [ ] Koin module and navigation entry registered in every app that uses the feature
- [ ] No secrets, demo flags, `Log` / `println` / `printStackTrace`

**Tests and quality**
- [ ] Unit tests: mapper, request contract, use case, repository, ViewModel
- [ ] Instrumented test added (required for flows across screens or features, and for Android-framework behaviour), or reason given: <!-- why not -->
- [ ] Konsist, Detekt, Spotless and lint are green locally (pre-push) and in CI
- [ ] `consumer-rules.pro` updated; a minified (release) build was exercised

**UI**
- [ ] `@Preview` for every state, in light and dark, in every supported language (including RTL), and at large font scale
- [ ] Strings are in `strings.xml` (all languages), errors go through the shared `AppException` mapper
- [ ] Content descriptions / test tags added

**Docs**
- [ ] Docs updated if a base class, convention or pipeline step changed
