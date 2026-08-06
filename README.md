# sniffa-common

Small shared Java library for Sniffa Studio's backend services (`sniffa-discord`, `sniffa-backend`,
and future ones) — utilities that would otherwise get copy-pasted between projects.

## What's in here

- `studio.sniffa.common.config.ConfigLoader` — loads config from a local properties file (if
  present) with environment variables as a fallback for every key. Each service still defines its
  own typed config class (e.g. `BotConfig`, `BackendConfig`) with named getters; it just delegates
  the actual parsing to this instead of re-implementing it.
- `studio.sniffa.common.security.SecureCompare` — constant-time string comparison for auth checks
  (tokens, passwords), so equality checks don't leak timing information.

## Using it locally

Not published anywhere yet. Consuming projects pull it in via a Gradle composite build:

```kotlin
// settings.gradle.kts
includeBuild("../sniffa-common")
```

```kotlin
// build.gradle.kts
dependencies {
    implementation("studio.sniffa:sniffa-common")
}
```
