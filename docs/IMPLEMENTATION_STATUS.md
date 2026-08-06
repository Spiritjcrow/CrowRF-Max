# Implementation status

Status labels: planned, implemented, compile-validated, simulator-tested, emulator-tested, physical-device-tested, multi-device-tested, production-tested.

| Subsystem | Status | Evidence / limitation |
|---|---|---|
| Product architecture | implemented | Hybrid Compose + Unity + Termux boundary documented |
| Privacy and reality boundary | implemented | Identifier handling and RF uncertainty rules documented |
| Protocol schema | implemented | Initial observation schema committed; validation tests pending |
| Android project | planned | Requires repository scaffold and Android SDK build |
| Capability profiler | planned | Per-install probe; current attached device not available through ADB |
| Wi-Fi/BLE/IMU adapters | planned | Real APIs only; no synthetic production data |
| Room persistence | planned | Schema and migrations pending |
| Compose 2D viewer | planned | Pending Android scaffold |
| Unity 6 renderer | planned | Requires Unity editor/export validation |
| Termux worker | planned | Termux and Termux:API unavailable in this build environment |
| Backend sync | planned | Local-only operation precedes cloud sync |
| Selma integration | planned | Separate consented orchestration bridge |
| CI artifacts | planned | Workflow added after build scaffolds exist |
| Physical device testing | planned | Current device is user-declared owned; connection/ADB unavailable |
