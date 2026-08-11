# PHASE4_3A_PROVIDER_LIFECYCLE_DESIGN.md

## Scope

Phase 4.3A is a **design-only** checkpoint for lifecycle-safe registration of **project-owned / authorized** capture surfaces.

It does not integrate opaque target Flutter assets, AOT binaries, DEX, native libraries, or third-party Views. It does not alter security/integrity controls.

No production source/build change is authorized by this document.

## Inputs

Current known-green implementation:

- `CaptureProviderRegistry` is a process-global singleton with `set(provider)`, `get()`, and `clear()`.
- the internal debug WebView is owned by `MainActivity`;
- `MainActivity.onPageFinished()` registers `WebViewCaptureProvider`;
- `MainActivity.onDestroy()` currently calls global `clear()` and then destroys the WebView;
- `CaptureBridge` and `ScreenCaptureService` are read-only consumers through `CaptureProviderRegistry.get()`;
- `WebViewCaptureProvider` holds its `WebView` via `WeakReference` and performs `webView.draw(Canvas)` on `Dispatchers.Main.immediate`;
- Phase 3 device runtime proved the bridge can capture the project-owned off-screen WebView while Flutter is foreground.

CI run #24 (`31464037838`, commit `f2f356e`) is GREEN:

- compile: PASS
- unit tests: 163 / 163 PASS
- `assembleDebug`: PASS
- `lintDebug`: PASS

The recurring KSP/AWT `ApplicationManager.getApplication() == null` exception remains nonfatal tooling noise because the requested Gradle tasks complete successfully.

## 1. Problem statement

The current registry has no registration identity.

With only one owner, this is acceptable:

```text
MainActivity debug WebView
        |
        +-- set(provider)
        |
        +-- onDestroy -> clear()
```

It becomes unsafe as soon as two **project-owned** surfaces may overlap:

```text
A registers
B registers and becomes current
A disposes later
A calls global clear()
B is accidentally removed
```

That is the **stale-dispose problem**.

The registry therefore needs lifecycle ownership before a second project-owned WebView / Flutter PlatformView is introduced.

## 2. Design goals

The lifecycle contract MUST provide:

1. exactly one provider selected as `current` at any instant;
2. identity for each registration;
3. idempotent owner disposal;
4. stale-dispose protection;
5. deterministic temporary override + restoration;
6. no change to `CaptureBridge` / `ScreenCaptureService` lookup semantics;
7. no implicit MediaProjection fallback;
8. no strong reference from `WebViewCaptureProvider` to a WebView;
9. thread-safe registry metadata updates;
10. no target/third-party View acquisition mechanism.

## 3. Recommended registry contract

### Public production-facing shape

Conceptual API:

```kotlin
interface CaptureProviderRegistration : AutoCloseable {
    override fun close()
}

object CaptureProviderRegistry {
    fun register(provider: CaptureProvider): CaptureProviderRegistration
    fun get(): CaptureProvider?
}
```

A narrow test/reset API MAY exist internally:

```kotlin
internal fun clearAllForTests()
```

The existing global mutation API:

```kotlin
set(provider)
clear()
```

should be removed or deprecated after all owner call sites migrate. Normal surface owners must never call a process-global `clear()`.

### Registration handle

`register(provider)` returns a handle containing an opaque unique token.

The owner stores this handle and closes **that handle only** when the surface is disposed.

The token is registry-internal. Callers do not need to compare or manufacture tokens.

## 4. Active-provider semantics: registration stack

Use registration order as a small lifecycle stack.

Conceptual state:

```text
oldest                                      newest/current
[A] -> [B] -> [C]
               ^
               get()
```

Rules:

- registering a provider appends a new entry;
- `get()` returns the newest still-live registration;
- closing a handle removes only its own entry;
- closing a non-current/stale registration MUST NOT affect the current entry;
- closing the current entry restores the previous still-live registration;
- closing a handle twice is a no-op;
- if the last live registration closes, `get()` returns `null`.

This is preferable to single-slot compare-and-clear because the existing internal debug provider can remain alive while a temporary project-owned Flutter PlatformView is foreground, then be restored automatically when the temporary surface is disposed.

### Required examples

#### Temporary override and restore

```text
register A        -> current A
register B        -> current B
close B           -> current A
close A           -> current null
```

#### Stale owner disposal

```text
register A        -> current A
register B        -> current B
close A           -> current B
close B           -> current null
```

#### Rebuild / replacement

```text
register A
register B1
register B2       -> current B2
close B1          -> current B2
close B2          -> current A
```

## 5. Thread-safety model

Registry mutation is tiny and infrequent. Prefer correctness over lock-free complexity.

Recommended implementation model:

- one private lock;
- one monotonically increasing registration id;
- one ordered in-memory list/map of `{id, provider}`;
- synchronize only `register`, handle `close`, `get`, and test reset;
- NEVER execute `provider.capture()` while holding the registry lock.

A lock protects ownership metadata only. Capture remains owned by the provider implementation.

An immutable-state `AtomicReference` design is also acceptable if it preserves the exact semantics above, but it is not required.

## 6. MainActivity migration design

Current internal debug WebView ownership remains in `MainActivity`.

Add one Activity-owned field conceptually:

```text
internalCaptureRegistration: CaptureProviderRegistration?
```

### Registration

After `onPageFinished`, fixed viewport re-measure/layout, and readiness checks:

1. create `WebViewCaptureProvider(wv)`;
2. `newHandle = CaptureProviderRegistry.register(provider)`;
3. swap the Activity field to `newHandle`;
4. close the previous handle **after** the new registration exists;
5. mark internal provider ready.

Register-new-before-close-old prevents a temporary `provider_unavailable` gap during a same-owner re-registration.

### Activity teardown

Required order:

1. mark internal provider not ready;
2. close the Activity's registration handle;
3. null the handle;
4. stop/loading cleanup;
5. destroy the WebView;
6. keep existing cached-FlutterEngine teardown policy unchanged.

Do **not** call process-global `CaptureProviderRegistry.clear()` from `MainActivity.onDestroy()`.

This makes configuration-change and background-surface disposal owner-local.

## 7. Future project-owned Flutter PlatformView lifecycle

This is a future implementation contract, not authorization to integrate third-party target content.

A project-owned PlatformView wrapper would own its own registration handle:

```text
create owned WebView
        |
wait until capture-ready / non-zero size
        |
register(WebViewCaptureProvider(webView))
        |
store registration handle
        |
PlatformView.dispose()
        |
close registration
        |
destroy WebView
```

If the internal debug provider A is still live:

```text
internal A current
Flutter-owned B registers -> B current
B disposes                -> A restored
```

If `MainActivity` is destroyed while B remains current:

```text
A closes                  -> B remains current
B later disposes          -> null
```

This is the core stale-dispose guarantee Phase 4.3 requires.

## 8. Flutter engine / reopen behavior

The registry is independent of Flutter engine ownership.

Keep the Phase-3 engine contract unchanged:

- cached engine id `screenpilot_capture_host`;
- `CaptureBridge` registered once when that engine is created;
- configuration-change recreation must not destroy the cached engine;
- `CaptureBridge` continues calling only `CaptureProviderRegistry.get()`.

A project-owned PlatformView may be recreated when Flutter UI is reopened. Each View instance gets a distinct registration handle. Old View disposal must not remove a newer View's provider.

## 9. Capture-in-flight boundary

Registration ownership controls **future provider lookup**. It does not promise to cancel a capture that already obtained a provider before disposal.

Required owner rule:

- close registration before destroying its WebView.

Current `WebViewCaptureProvider` already:

- weakly references the WebView;
- executes drawing on the main dispatcher;
- converts exceptions/unavailable state into `CaptureResult.Error`.

That is sufficient for the first lifecycle implementation. Do not add a complex capture-cancellation protocol in Phase 4.3B unless tests demonstrate a real failure.

If a later race is reproducible, a registration-bound active guard may be added as a separate small phase.

## 10. Consumer compatibility

`CaptureBridge` should require **no behavior change**:

```text
get() == null -> provider_unavailable
get() != null -> provider.capture()
```

`ScreenCaptureService` internal-provider mode should also require **no behavior change**.

Important invariant:

```text
INTERNAL_PROVIDER
    provider absent/fails
        -> fail/return null

NEVER
        -> silently fall back to MediaProjection
```

## 11. Unit-test matrix for Phase 4.3B

Add focused registry tests before runtime work.

Minimum cases:

| Test | Expected |
|---|---|
| register A | `get() === A` |
| register A then B | `get() === B` |
| close stale A while B active | `get() === B` |
| close B while A still live | `get() === A` |
| close A then close B | final `get() == null` |
| close same handle twice | no exception; state unchanged |
| A -> B1 -> B2, close B1 | current remains B2 |
| A -> B1 -> B2, close B2 | current becomes B1 if B1 live |
| close non-current entries in arbitrary order | newest live entry remains current |
| close all | `get() == null` |

Tests should use lightweight fake `CaptureProvider` instances and test registry identity/ordering. No real WebView is required for these tests.

## 12. Runtime regression gates for Phase 4.3B

Phase 4.3B implementation is not complete until:

### Gate A — CI/static
- compileDebugKotlin PASS
- all existing unit tests PASS
- new registry lifecycle tests PASS
- assembleDebug PASS
- lintDebug PASS
- `git diff --check` clean

### Gate B — existing Phase-2 runtime
Fresh app:

- internal debug page becomes ready;
- internal debug capture still works;
- marker remains `SP-WEBVIEW-2026-08`;
- output remains project-owned WebView content.

### Gate C — existing Phase-3 bridge
Fresh app:

- open Flutter test host;
- `Capture via Bridge`;
- status `Success`;
- image displays;
- dimensions remain expected;
- reopen Flutter and capture again.

### Gate D — lifecycle regression
With two **project-owned test providers/surfaces** in a controlled test harness:

- A active;
- B registers and becomes current;
- dispose A while B active -> B remains;
- dispose B after A already disposed -> provider unavailable;
- separate case: B disposes while A remains -> A restores.

Do not use third-party/protected content for this gate.

## 13. Non-goals / prohibited shortcuts

Phase 4.3A/4.3B do not:

- discover or reflect into another application's WebView;
- use Accessibility to scrape another application's UI;
- use cross-process View access;
- use MediaProjection as a workaround for an unavailable internal provider;
- bypass `FLAG_SECURE` or protected-content policy;
- preserve/recreate gate, license, signature, or identity spoofing;
- copy target Flutter assets, AOT/engine binaries, DEX, or native libraries.

## 14. Implementation slice

### Phase 4.3B — Owner-aware registry implementation

Entry condition:

- Phase 4.3A design reviewed and tracked;
- CI remains GREEN.

Allowed changes:

1. `CaptureProviderRegistry.kt`
2. `MainActivity.kt` owner-local registration handle migration
3. focused registry unit tests
4. phase docs

Expected untouched behavior:

- `CaptureProvider` contract
- `WebViewCaptureProvider` capture implementation
- `CaptureBridge` channel contract
- `ScreenCaptureService` capture-source semantics
- Flutter AAR/toolchain ownership

Exit condition:

- all unit/CI gates GREEN;
- existing Phase-2 and Phase-3 device smoke tests pass;
- stale-dispose behavior proven with project-owned test surfaces/providers.

## Decision

**Phase 4.3A COMPLETE — use owner-scoped registration handles with newest-live registration selection and automatic restoration of the previous live project-owned provider.**

Next implementation checkpoint: **Phase 4.3B — owner-aware registry implementation**, still limited to project-owned/authorized capture surfaces.
