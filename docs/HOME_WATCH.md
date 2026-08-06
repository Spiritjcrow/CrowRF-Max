# Home Watch mode

Home Watch is an explicitly armed, local-first environment event recorder for the user's owned home and devices.

## Non-negotiable controls

- Monitoring starts only from a visible user action while the app is in the foreground.
- Android runs microphone collection as a declared microphone foreground service with a persistent, non-dismissible notification and Stop action.
- Household voice profiles require the enrolled person's informed consent and can be deleted independently.
- The classifier emits household, unrecognized, or uncertain. It never emits "intruder" and never identifies a stranger.
- Continuous raw audio is not retained. On-device voice activity detection opens a short encrypted event buffer.
- Cloud audio upload, facial recognition, police contact, confrontation, and automatic door/device control are disabled.
- Remote alerts contain event metadata by default; an audio excerpt requires a separate opt-in.
- Every arm, disarm, permission change, model decision, alert, export, and deletion is audit logged.
- Monitoring automatically disarms after permission loss, service failure, consent revocation, or an expired schedule.

## Android lifecycle

Android restricts microphone foreground services because RECORD_AUDIO is a while-in-use permission. The user must open the app and arm Home Watch; it cannot silently begin microphone capture at boot or from an arbitrary background trigger.

Required permissions are requested only when the feature is enabled:

- RECORD_AUDIO
- POST_NOTIFICATIONS on supported versions
- FOREGROUND_SERVICE
- FOREGROUND_SERVICE_MICROPHONE

Camera is a separate feature and permission. It remains off in the initial Home Watch implementation.

## Processing pipeline

1. Audio frames remain in memory.
2. On-device voice activity detection identifies possible human vocal activity.
3. A consented local speaker-embedding model compares the event with enrolled household profiles.
4. Policy combines model confidence, duration, recurrence, schedule, and corroborating owned sensors.
5. An immutable metadata event is written to encrypted app storage.
6. Local notification is posted.
7. Optional encrypted remote push sends metadata to a user-selected device.
8. Retention removes event media on schedule while preserving a minimal audit tombstone.

Speaker matching is probabilistic. Low confidence is always labeled uncertain.

## Alert language

Allowed:

- "Unrecognized human voice detected"
- "Human voice detected; identity uncertain"
- "Household voice detected; no alert sent"

Disallowed:

- "Intruder identified"
- claims of identity, intent, threat, location, or direction unsupported by sensors

## Default retention

- Raw rolling buffer: memory only
- Alert excerpt: disabled
- Event metadata: 30 days
- Audit tombstone: 90 days
- Manual pin: explicit user action, with visible retention state
