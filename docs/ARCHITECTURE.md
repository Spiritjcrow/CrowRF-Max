# Hybrid architecture

## Runtime layers

1. **Native Android shell**
   - Jetpack Compose adaptive UI
   - runtime permission policy
   - capability profiler
   - Wi-Fi, BLE, IMU, battery, thermal, network, and optional RTT/UWB adapters
   - Room persistence and app-owned exports
   - 2D heatmap and recorded-session viewer
2. **Unity renderer**
   - Unity 6, AR Foundation, and ARCore XR Plugin
   - optional geometry, depth, trajectory, markers, and volumetric RF rendering
   - packaged/exported as an Android library and started only when supported
3. **Termux worker**
   - deterministic allowlisted operations
   - loopback-only authenticated bridge
   - no general unauthenticated shell
4. **Shared protocol**
   - immutable observations
   - versioned derived products
   - capability, provenance, units, confidence, uncertainty, and errors on every record

## Device adaptation

Every installation performs its own local capability probe. A Google account device list is a deployment inventory, not a sensor-capability source and not authorization for collection.

Fallback order:

1. AR + depth + RF + optional ranging
2. AR + RF
3. RF + IMU 3D field
4. RF 2D heatmap
5. Recorded-map viewer
6. Local-only metadata viewer

Missing hardware or permission must degrade explicitly and must never crash the app.

## Android–Unity boundary

Kotlin owns Android permissions and sensor APIs. Unity consumes normalized protocol records through a narrow Android library/AAR interface. Unity never requests broader permissions or reads raw identifiers independently.

## Android–Termux boundary

Messages contain protocol version, request ID, task ID, operation, arguments, deadline, and authentication proof. Only allowlisted operations such as build, test, analyze-image, translate, render-dataset, and probe-capabilities are accepted. Servers bind to loopback by default.

## Selma boundary

Selma is a separate offline-first companion and orchestration product. CrowRF Max may expose consented summaries or task operations through the shared bridge. Matchmaking-site Selma remains a distinct deployment and receives no RF data by default.
