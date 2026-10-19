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

| Package | Main types | Responsibility |
| --- | --- | --- |
| `megalodonte.base.state` | `State`, `ReadableState`, `Subscription`, `ForEachState` | Mutable and read-only state, observation and disposal |
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

## License

No license file is present in this repository. Licensing terms require confirmation from the project maintainer.
