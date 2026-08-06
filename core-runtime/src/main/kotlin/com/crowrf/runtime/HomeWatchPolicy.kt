package com.crowrf.runtime

import java.time.Instant

enum class SpeakerAssessment { HOUSEHOLD, UNRECOGNIZED, UNCERTAIN }
enum class AlertSeverity { NONE, INFO, ATTENTION, URGENT }

data class HomeWatchConsent(
    val ownerConfirmed: Boolean,
    val householdMemberConfirmed: Boolean,
    val visibleIndicatorRequired: Boolean = true,
    val localProcessingRequired: Boolean = true,
    val neutralLabelsRequired: Boolean = true
) {
    val valid: Boolean
        get() = ownerConfirmed && householdMemberConfirmed &&
            visibleIndicatorRequired && localProcessingRequired && neutralLabelsRequired
}

data class VoiceEvent(
    val eventId: String,
    val capturedAt: Instant,
    val humanVoiceProbability: Double,
    val householdMatchProbability: Double?,
    val durationMillis: Long,
    val repeatedWithinFiveMinutes: Int,
    val corroboratingOwnedSensorEvents: Int,
    val serviceIndicatorVisible: Boolean,
    val consent: HomeWatchConsent
)

data class HomeWatchDecision(
    val assessment: SpeakerAssessment,
    val severity: AlertSeverity,
    val title: String,
    val retainAudioExcerpt: Boolean,
    val reasons: List<String>
)

class HomeWatchPolicy(
    private val humanVoiceThreshold: Double = 0.80,
    private val householdThreshold: Double = 0.82,
    private val uncertainBand: Double = 0.12
) {
    fun evaluate(event: VoiceEvent): HomeWatchDecision {
        require(event.humanVoiceProbability in 0.0..1.0)
        require(event.householdMatchProbability == null ||
            event.householdMatchProbability in 0.0..1.0)

        if (!event.consent.valid || !event.serviceIndicatorVisible) {
            return HomeWatchDecision(
                SpeakerAssessment.UNCERTAIN,
                AlertSeverity.NONE,
                "Home Watch inactive",
                retainAudioExcerpt = false,
                reasons = listOf("consent or visible service indicator missing")
            )
        }

        if (event.humanVoiceProbability < humanVoiceThreshold) {
            return HomeWatchDecision(
                SpeakerAssessment.UNCERTAIN,
                AlertSeverity.NONE,
                "No confident human voice event",
                retainAudioExcerpt = false,
                reasons = listOf("voice activity below threshold")
            )
        }

        val match = event.householdMatchProbability
        val assessment = when {
            match == null -> SpeakerAssessment.UNCERTAIN
            match >= householdThreshold -> SpeakerAssessment.HOUSEHOLD
            match >= householdThreshold - uncertainBand -> SpeakerAssessment.UNCERTAIN
            else -> SpeakerAssessment.UNRECOGNIZED
        }

        if (assessment == SpeakerAssessment.HOUSEHOLD) {
            return HomeWatchDecision(
                assessment,
                AlertSeverity.NONE,
                "Household voice detected; no alert sent",
                retainAudioExcerpt = false,
                reasons = listOf("consented household profile matched")
            )
        }

        val recurring = event.repeatedWithinFiveMinutes >= 2
        val corroborated = event.corroboratingOwnedSensorEvents > 0
        val severity = when {
            assessment == SpeakerAssessment.UNRECOGNIZED && recurring && corroborated ->
                AlertSeverity.URGENT
            assessment == SpeakerAssessment.UNRECOGNIZED && (recurring || corroborated) ->
                AlertSeverity.ATTENTION
            assessment == SpeakerAssessment.UNRECOGNIZED -> AlertSeverity.INFO
            else -> AlertSeverity.INFO
        }

        val title = when (assessment) {
            SpeakerAssessment.UNRECOGNIZED -> "Unrecognized human voice detected"
            SpeakerAssessment.UNCERTAIN -> "Human voice detected; identity uncertain"
            SpeakerAssessment.HOUSEHOLD -> error("handled above")
        }

        return HomeWatchDecision(
            assessment,
            severity,
            title,
            retainAudioExcerpt = false,
            reasons = buildList {
                add("on-device human voice threshold met")
                if (recurring) add("event repeated")
                if (corroborated) add("corroborating owned sensor event")
                add("audio excerpt disabled by default")
            }
        )
    }
}
