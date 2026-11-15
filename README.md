<p align="center">
  <img src="docs/images/megalodonte-logo.png" width="180" alt="Megalodonte logo">
</p>

# Megalodonte Core

Platform-neutral Java library of contracts shared by JavaFX and Android hosts. It provides observable state, disposable subscriptions, semantic theme tokens, scaling policy, screen lifecycle, route matching and backend capability metadata. It contains no JavaFX or Android imports.

## Build and dependency

Building this repository requires JDK 25 and the included Gradle wrapper (Gradle 9.2.0). The library emits Java 17 bytecode. From the repository root, run:

```sh
./gradlew check publishToMavenLocal
```

The `check` task runs the contract test and verifies the platform import and bytecode boundaries. `publishToMavenLocal` installs the artifact in the local Maven repository. A Gradle consumer configured with `mavenLocal()` can then declare:

```kotlin
implementation("megalodonte:megalodonte-core:1.0.0-beta")
```

The JPMS module `megalodonte.core` exports the neutral packages. Hosts should depend on this artifact instead of recompiling the same classes into their own JARs.

## Public contracts

`SubscriptionScope.capture(builder)` groups subscriptions created synchronously
by a render without owning the `State` values. Close `captured.scope()` when the
tree is discarded; a failing builder is cleaned up automatically. Core state and
mapped state participate; custom state implementations can register their cleanup
with `SubscriptionScope.onClose(...)`. This is platform-neutral and opt-in: normal
subscriptions outside a capture retain their existing lifetime. Desktop live reload
uses it to release old or rejected trees. Work created asynchronously is not captured.

| Package | Main types | Responsibility |
| --- | --- | --- |
| `megalodonte.base.state` | `State`, `ReadableState`, `Subscription`, `SubscriptionScope`, `ForEachState` | Mutable and read-only state, observation and disposal |
| `megalodonte.base.theme` | `ThemeInterface`, `ThemeManager`, `ThemeColors`, `ThemeTypography`, `ThemeSpacing`, `ThemePadding`, `ThemeBorder`, `ThemePlatform` | Semantic design tokens and host hooks |
| `megalodonte.base.scale` | `ScaleProvider` | Host-supplied display scaling |
| `megalodonte.contracts` | `ScreenLifecycle`, `RouteMatcher`, `BackendContract`, `Capability` | Lifecycle, navigation matching and backend capability negotiation |

The host installs `ScaleProvider.setDetector` and `ThemePlatform.install` during bootstrap. Scene and font loading belong to the host; Android does not have a JavaFX `Scene`. `ThemeManager.applyFontFamily(Object)` is transitional, so consumers built against older signatures must be recompiled.

```java
import megalodonte.base.state.State;
import megalodonte.base.state.Subscription;

State<String> state = State.of("ready");
try (Subscription binding = state.observe(value -> System.out.println(value))) {
    state.set("running");
}
```

`State` calls observers immediately, suppresses equal updates and assumes the host's UI-thread policy. Its `volatile` value does not make listener mutation thread-safe. `ReadableState.map` subscribes only while observed and recomputes on `get`; use `ComputedState` from the reactivity module for a cached derived value. Keep mapper functions pure.

The capability contract describes categories of backend support, not the complete component API. Platform coverage depends on the host implementation and is not specified in this repository.

## Private runtime configuration

`megalodonte.config.RuntimeEnvironment.forApp("application-name")` reads process
environment variables first, followed by the user's private `.env` file. The file
is located at `%APPDATA%/Megalodonte/application-name/.env` on Windows and
`~/.config/megalodonte/application-name/.env` on Linux; `XDG_CONFIG_HOME` is
respected.

`forAppOrProject` also accepts the project-root `.env` during development when
the current directory contains `packaging.toml`. Use `require("KEY")` for required
values. Packaging never includes the private file or places credentials in `-D`
options.

## License

No license file is present in this repository. Licensing terms require confirmation from the project maintainer.
