#!/usr/bin/env bash
# Validates the first line of a commit message against the Conventional Commits standard
# described in docs/contributing.md. Used by lefthook (commit-msg hook): scripts/check-commit-msg.sh <message-file>
set -euo pipefail

msg_file="${1:?usage: check-commit-msg.sh <commit-message-file>}"
first_line="$(head -n 1 "$msg_file")"

# Messages created by git itself are allowed.
case "$first_line" in
  Merge\ *|Revert\ \"*|fixup!\ *|squash!\ *) exit 0 ;;
esac

pattern='^(feat|fix|refactor|perf|test|docs|style|build|ci|chore|revert)(\([a-z0-9_./-]+\))?!?: .{1,72}$'

if [[ ! "$first_line" =~ $pattern ]] || [[ "$first_line" == *. ]] || [[ ${#first_line} -gt 100 ]]; then
  cat >&2 <<MSG

✖ Invalid commit message:
    $first_line

Expected:  <type>(<scope>): <subject>

  type     feat | fix | refactor | perf | test | docs | style | build | ci | chore | revert
  scope    optional, lowercase: core, phone_auth, maintenance, billing, app-admin, quality, docs, deps, ci …
  subject  imperative, lowercase start, no trailing period, max 72 characters
  !        add after the scope for a breaking change:  feat(core)!: rename Resource.Progress

Examples:
  feat(phone_auth): add GetManagerContactUC
  fix(core): clear the session when the refresh token is missing
  docs: describe the release flow

Details: docs/contributing.md
MSG
  exit 1
fi
