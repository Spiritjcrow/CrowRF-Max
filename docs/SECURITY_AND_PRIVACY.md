# Security and privacy

- Collection is limited to the user's owned device or an explicitly authorized lab scope.
- Nearby visibility does not authorize connection, interception, tracking, or identification.
- Wi-Fi and Bluetooth identifiers are hashed with an installation-scoped key before persistence.
- Screenshots, IP addresses, MAC addresses, account identifiers, tokens, and secrets are never committed.
- Cloud synchronization is opt-in; local sensing, recording, playback, export, and deletion work offline.
- The Termux bridge is loopback-only, authenticated, replay-resistant, deadline-bound, and operation-allowlisted.
- Raw observations are immutable. Estimates retain derivation version, confidence, uncertainty, calibration, and provenance.
- RSSI is displayed as signal strength or proximity trend, never reliable bearing or exact distance.
- RF-field estimates and measured physical geometry use visibly different layers.
- The user can stop collection, inspect permissions, export app-owned data, and delete it.
