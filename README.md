# CrowRF Max

CrowRF Max is an offline-first Android spatial RF observation and visualization system for user-authorized devices and environments.

## Product shape

- **Kotlin + Jetpack Compose** is the control, permissions, sensing, persistence, calibration, playback, privacy, and fallback UI layer.
- **Unity 6** is an optional high-capability AR and volumetric renderer delivered through an Android library boundary.
- **Termux** is an optional authenticated local worker for builds, analysis, conversions, and repository tasks.
- A versioned JSON protocol keeps observations and derived estimates portable across all three runtimes.

The app never represents RSSI alone as exact distance, direction, identity, wavelength measurement, or physical geometry. Raw observations, derived RF fields, AR geometry, confidence, uncertainty, and provenance remain separate.

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), [docs/SECURITY_AND_PRIVACY.md](docs/SECURITY_AND_PRIVACY.md), and [docs/IMPLEMENTATION_STATUS.md](docs/IMPLEMENTATION_STATUS.md).
