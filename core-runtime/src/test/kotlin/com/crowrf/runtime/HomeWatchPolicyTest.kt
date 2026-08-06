package com.crowrf.runtime

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class HomeWatchPolicyTest {
    private val policy = HomeWatchPolicy()
    private val consent = HomeWatchConsent(true, true)

    @Test
    fun refusesMonitoringWithoutVisibleIndicator() {
        val result = policy.evaluate(event(serviceIndicatorVisible = false))
        assertEquals(AlertSeverity.NONE, result.severity)
        assertEquals("Home Watch inactive", result.title)
    }

    @Test
    fun householdMatchDoesNotAlert() {
        val result = policy.evaluate(event(householdMatchProbability = 0.94))
        assertEquals(SpeakerAssessment.HOUSEHOLD, result.assessment)
        assertEquals(AlertSeverity.NONE, result.severity)
    }

    @Test
    fun unknownVoiceUsesNeutralLanguage() {
        val result = policy.evaluate(event(householdMatchProbability = 0.20))
        assertEquals(SpeakerAssessment.UNRECOGNIZED, result.assessment)
        assertEquals("Unrecognized human voice detected", result.title)
        assertFalse(result.retainAudioExcerpt)
    }

    @Test
    fun corroborationAndRecurrenceRaiseUrgencyWithoutClaimingIntrusion() {
        val result = policy.evaluate(
            event(
                householdMatchProbability = 0.20,
                repeatedWithinFiveMinutes = 3,
                corroboratingOwnedSensorEvents = 1
            )
        )
        assertEquals(AlertSeverity.URGENT, result.severity)
        assertEquals("Unrecognized human voice detected", result.title)
    }

    @Test
    fun uncertainMatchRemainsUncertain() {
        val result = policy.evaluate(event(householdMatchProbability = 0.75))
        assertEquals(SpeakerAssessment.UNCERTAIN, result.assessment)
        assertEquals("Human voice detected; identity uncertain", result.title)
    }

    private fun event(
        householdMatchProbability: Double? = 0.2,
        repeatedWithinFiveMinutes: Int = 0,
        corroboratingOwnedSensorEvents: Int = 0,
        serviceIndicatorVisible: Boolean = true
    ) = VoiceEvent(
        eventId = "event-1",
        capturedAt = Instant.parse("2026-08-06T12:00:00Z"),
        humanVoiceProbability = 0.95,
        householdMatchProbability = householdMatchProbability,
        durationMillis = 1500,
        repeatedWithinFiveMinutes = repeatedWithinFiveMinutes,
        corroboratingOwnedSensorEvents = corroboratingOwnedSensorEvents,
        serviceIndicatorVisible = serviceIndicatorVisible,
        consent = consent
    )
}
