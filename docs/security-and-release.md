# Security and release

Icompound handles residents' personal data (names, phone numbers, bills, visitor passes). Treat security as part of every feature.

## 1. Rules

1. **No secrets in source.** API keys, secrets, base URLs and certificate pins come from `local.properties` (local) or CI secrets, through `buildConfigField`. Anything inside an APK can be extracted, so the long-term goal is per-user credentials issued by the backend, not shared app secrets.
2. **The token lives only in `TokenStore`** (encrypted KVault). Never in plain preferences, logs, `ViewState` or the stored profile JSON (`saveSession` stores the user with `token = null`).
3. **Logging only through `AppLogger`** (debug builds only). Never log tokens, passwords, OTP codes, phone numbers or other personal data. `MVIViewModel` logs state objects in debug, so keep secrets out of `ViewState` or mask them in `toString()`.
4. **HTTP logging** is `BODY` in debug only, with `Authorization` and `Cookie` redacted. Off in release.
5. **Certificate pinning** is on in release with a primary and a backup pin. Off in debug so proxies work.
6. **401 handling is centralised** in `TokenAuthenticator`. Features never refresh or clear tokens themselves. With no refresh token, a 401 means "session expired": the session is cleared and `SessionEvents` sends the user to login.
7. **Never show `exception.message`** to users; map `AppException` to string resources.
8. **Roles:** the UI hides what a role cannot do, but the **backend must enforce it**.
9. **Test data:** never paste real tokens or personal data into code, tests, issues or chats. Use throwaway values.

## 2. Build secrets

The build reads these properties (names as used by CI):

| Property | Purpose |
|---|---|
| `API_KEY` | backend API key |
| `SOCKET_PORT` | realtime socket port |
| `GOOGLE_CLIENT_ID` | Google sign-in client id |
| `IC_COMPOUND_DATASTORE_NAME` | preferences DataStore file name |
| `REMOTE_URL` | production base URL |
| `DEBUG_REMOTE_URL` | base URL for debug builds |
| `PIN_PRIMARY`, `PIN_BACKUP` | certificate pins, if pinning is configured from properties (not yet in the CI secret list) |

Locally put them in `~/.gradle/gradle.properties` (outside the repo, `NAME=value`). In CI they arrive as `ORG_GRADLE_PROJECT_<NAME>` environment variables; Gradle exposes both as project properties. Full table and CI handling: [ci-cd.md](ci-cd.md#3-secrets-and-build-properties).

```kotlin
fun secret(name: String): String = providers.gradleProperty(name).orElse("").get()

android.defaultConfig {
    buildConfigField("String", "API_KEY", "\"${secret("API_KEY")}\"")
    buildConfigField("String", "SOCKET_PORT", "\"${secret("SOCKET_PORT")}\"")
    buildConfigField("String", "GOOGLE_CLIENT_ID", "\"${secret("GOOGLE_CLIENT_ID")}\"")
}
android.buildTypes {
    debug   { buildConfigField("String", "BASE_URL", "\"${secret("DEBUG_REMOTE_URL")}\"") }
    release { buildConfigField("String", "BASE_URL", "\"${secret("REMOTE_URL")}\"") }
}
```

Rules: validation jobs in CI use non-secret placeholders; only the release job receives real values. Never print a property in a Gradle log.

## 3. Certificate pinning

Get a pin (intermediate CA or leaf, plus a **backup**):

```bash
openssl s_client -servername api.example.com -connect api.example.com:443 </dev/null 2>/dev/null \
 | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der \
 | openssl dgst -sha256 -binary | openssl enc -base64
```

* Always ship **two pins**; a rotated certificate with a single pin breaks every installed app until users update.
* Calendar reminder for certificate renewal; add the new pin in a release **before** the server switches.
* A pinning failure reaches the UI as `AppException.Network.UntrustedConnection`.

## 4. Network security config

`src/main/res/xml/network_security_config.xml` (release: HTTPS only):

```xml
<network-security-config>
    <base-config cleartextTrafficPermitted="false" />
</network-security-config>
```

`src/debug/res/xml/network_security_config.xml` (allow the local backend while developing):

```xml
<network-security-config>
    <base-config cleartextTrafficPermitted="false" />
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="false">10.0.2.2</domain>
    </domain-config>
</network-security-config>
```

Manifest: `android:networkSecurityConfig="@xml/network_security_config"`, `android:allowBackup="false"`.

## 5. Release checklist

- [ ] All tests, Konsist and lint are green on CI
- [ ] Release build is minified (R8) and every feature's `consumer-rules.pro` was verified on it
- [ ] Pins (primary + backup) are set for the production host; debug-only proxies are not trusted
- [ ] No demo or mock flags (for example a `DEMO_MODE`), no test endpoints, no `Log` / `println` / `printStackTrace`
- [ ] No secrets in the repo (`git grep` for keys), secrets come from CI
- [ ] `isDebuggable` off; HTTP logging off
- [ ] Cleartext traffic disabled in release; backup disabled
- [ ] `versionCode` / `versionName` bumped; changelog written
- [ ] Signing keys stored in CI secrets, not in the repo
- [ ] Tag `vX.Y.Z` is on `main` and matches `version.properties`; the release job passed the `production` approval
- [ ] R8 `mapping.txt` was saved with the AAB (needed to read production stack traces)
- [ ] Smoke test on the release build: log in, call a protected endpoint, force a 401, log out
