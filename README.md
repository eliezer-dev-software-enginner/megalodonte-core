# Megalodonte Core

Platform-neutral contracts shared by JavaFX and Android. This module owns observable state, disposable subscriptions, semantic theme tokens, scaling policy, screen lifecycle, route matching and backend capability metadata. It contains no JavaFX or Android imports.

```kotlin
implementation("megalodonte:megalodonte-core:1.0.0-beta")
```

The project uses a Java 25 toolchain but emits Java 17 bytecode. Its checks reject platform imports and bytecode above Java 17. The JPMS module `megalodonte.core` exports the neutral packages. Hosts must depend on this artifact instead of recompiling the same classes into their own JARs.

## Public contracts

| Package | Main types | Responsibility |
| --- | --- | --- |
| `megalodonte.base.state` | `State`, `ReadableState`, `Subscription`, `ForEachState` | Mutable and read-only state, observation and disposal |
| `megalodonte.base.theme` | `ThemeInterface`, `ThemeManager`, `ThemeColors`, `ThemeTypography`, `ThemeSpacing`, `ThemePadding`, `ThemeBorder`, `ThemePlatform` | Semantic design tokens and host hooks |
| `megalodonte.base.scale` | `ScaleProvider` | Host-supplied display scaling |
| `megalodonte.contracts` | `ScreenLifecycle`, `RouteMatcher`, `BackendContract`, `Capability` | Lifecycle, navigation matching and backend capability negotiation |

The host installs `ScaleProvider.setDetector` and `ThemePlatform.install` during bootstrap. Scene and font loading belong to the host; Android does not have a JavaFX `Scene`. `ThemeManager.applyFontFamily(Object)` is transitional, so consumers built against older signatures must be recompiled.

```java
State<String> state = State.of("ready");
try (Subscription binding = state.observe(this::update)) {
    state.set("running");
}
```

`State` calls observers immediately, suppresses equal updates and assumes the host's UI-thread policy. Its `volatile` value does not make listener mutation thread-safe. `ReadableState.map` subscribes only while observed and recomputes on `get`; use `ComputedState` from the reactivity module for a cached derived value. Keep mapper functions pure.

The capability contract describes categories of backend support, not the complete component API. See the sibling Android project's documentation for the current coverage matrix.

```sh
./gradlew check publishToMavenLocal
```

In the Megalodonte workspace, check out this independent repository under `framework/libraries/megalodonte-core`.
