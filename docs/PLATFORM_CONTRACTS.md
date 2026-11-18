# Platform contracts and artifact naming

Megalodonte keeps platform-independent behavior in `megalodonte-core`. Desktop,
Android, Roku, and future hosts implement these contracts without leaking JavaFX,
Android SDK, or another host API into the core.

## Direction

- Preserve public behavior already implemented by the desktop libraries and make
  those classes explicitly implement neutral contracts when their signatures match.
- Adapt Android implementations to the same contracts while retaining Android's
  lifecycle, UI-thread, and native-widget behavior.
- Put instance behavior in interfaces. Static factories such as `Show.when(...)`
  remain platform APIs because Java does not override static interface methods.
- Keep shared values and algorithms in neutral artifacts: state, subscriptions,
  lifecycle, route matching, capability negotiation, theme tokens, and portable
  reactivity.
- Keep rendering, native navigation, windows, widgets, and platform lifecycle in
  suffixed artifacts.

This direction extends beyond reactivity. Components share semantic props and
component contracts; routers share route matching and navigation results; themes
share tokens while host application remains platform-specific. The same boundary
applies to future libraries.

## Naming

Neutral artifacts do not receive a platform suffix. Current examples are
`megalodonte-core` and `megalodonte-theme`. The Android base compiles the
platform-neutral reactive source subset directly from `megalodonte-reactivity`;
there is no intermediate portable reactivity artifact.

Platform artifacts end with their platform:

- Desktop: `megalodonte-base-desktop`, `megalodonte-components-desktop`,
  `megalodonte-reactivity-desktop`, and `megalodonte-router-desktop`.
- Android: `megalodonte-base-android` and `megalodonte-bridge-android` today;
  components, router, and other libraries use the same `-android` suffix when
  extracted into independent artifacts.

Package names may remain source-compatible across platforms. Artifact and JPMS names
must reveal the platform so a dependency graph cannot silently select JavaFX for an
Android application or an Android adapter for desktop.

Neutral artifacts compile with a Java 17 toolchain so they can participate in
Android builds using AGP 8.12 and Gradle 8.13. Desktop applications can still
use Java 25 independently; the neutral bytecode boundary does not require the
Android build daemon to run on Java 25.

## Initial shared contracts

- `ReadableState<T>` and `State<T>`: shared observable values.
- `ForEachState<T, C>`: observable list mapped to platform components.
- `ConditionalContent<C>`: condition plus true/false composition branches.
- `ComponentContract<C>` and `RefContract<T>`: component binding through a
  platform-neutral reference.
- `ScreenComponentContract<C>`: screen composition and lifecycle without a
  native toolkit type.
- `ScreenLifecycle`: platform-neutral mount and destroy lifecycle.

Component contracts deliberately do not expose JavaFX `Node`, Android `View`,
bridge identifiers or native children. Those details remain in each platform
implementation. The packages `megalodonte.base.components` and
`megalodonte.components` are separate layers: the first owns component and
screen abstractions, while the second owns concrete controls and layouts.
- `RouteMatcher<R>`: deterministic path matching.
- `BackendContract`: explicit platform capabilities.

Contract tests belong in core. Each platform also tests its adapter against the
contract; compiling against the interface alone does not prove native behavior.
