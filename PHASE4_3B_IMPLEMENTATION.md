# PHASE4_3B_IMPLEMENTATION.md

## Status

**IMPLEMENTED — CI and device regression validation pending.**

Phase 4.3B applies the owner-aware lifecycle contract approved in
`PHASE4_3A_PROVIDER_LIFECYCLE_DESIGN.md`.

This slice is limited to project-owned/authorized capture surfaces.

## Production changes

### `CaptureProviderRegistry`

The previous process-global mutable slot:

```text
set(provider)
get()
clear()
```

is replaced for owner lifecycle mutation by:

```text
register(provider) -> CaptureProviderRegistration
get()
```

Registry semantics:

- registrations are ordered by creation time;
- `get()` returns the newest live provider;
- closing a stale/non-current handle removes only that registration;
- closing the current handle restores the previous still-live provider;
- closing the same handle repeatedly is harmless;
- registry metadata is protected by one private lock;
- `provider.capture()` is never executed under that lock.

`clearAllForTests()` is internal and exists only to isolate unit tests. It does
not reset registration ids, so a previously issued stale handle cannot collide
with a later test registration id.

### `MainActivity`

The internal debug WebView now owns a
`CaptureProviderRegistration?`.

Registration order on page readiness:

```text
register new provider
        ↓
store new handle
        ↓
close previous handle
```

Teardown order:

```text
mark not ready
        ↓
close Activity-owned handle
        ↓
destroy WebView
```

`MainActivity` no longer performs a process-global registry `clear()`.

## Intentionally unchanged

- `CaptureProvider` contract
- `WebViewCaptureProvider`
- `CaptureBridge`
- `ScreenCaptureService`
- capture-source selection / no-fallback rule
- Flutter AAR/toolchain ownership
- MethodChannel contract
- target opaque Flutter/native/DEX artifacts remain excluded

## Tests added

`CaptureProviderRegistryTest` covers:

1. first registration selection;
2. newest registration selection;
3. stale-close safety;
4. restoration after current close;
5. empty state after all close;
6. idempotent close;
7. middle-registration removal;
8. newest-live restoration with three providers;
9. arbitrary stale-close ordering.

All tests use lightweight fake `CaptureProvider` instances; no third-party or
protected content is involved.

## Static verification before push

Required:

```text
git diff --check
git grep CaptureProviderRegistry.set
git grep CaptureProviderRegistry.clear
```

Expected owner mutation result:

- no production `CaptureProviderRegistry.set(...)`;
- no production `CaptureProviderRegistry.clear()`;
- `CaptureBridge` and `ScreenCaptureService` remain `get()` consumers.

Per project rules, do not run a local Android Gradle build. GitHub Actions is
the build authority.

## Validation gates

### CI gate

Must pass:

- `:app:compileDebugKotlin`
- `:app:testDebugUnitTest`
- `:app:assembleDebug`
- `:app:lintDebug`

Expected test count after this patch: previous 163 + 9 registry tests = **172**
tests, assuming no unrelated test inventory changes.

### Device gate after CI GREEN

Repeat existing project-owned smoke tests:

1. fresh ScreenPilot;
2. wait for internal WebView provider readiness;
3. internal capture still saves/returns the marked
   `SP-WEBVIEW-2026-08` content;
4. open Flutter test host;
5. `Capture via Bridge` -> `Success`;
6. dimensions/image render remain valid;
7. Back -> reopen Flutter -> capture again.

Phase 4.3B becomes COMPLETE only after CI and the device regression gate pass.

## Security boundary

This implementation does not acquire another application's View, does not add
cross-process capture, does not use Accessibility scraping, does not alter
MediaProjection policy, and does not bypass protected-content/security
controls.
