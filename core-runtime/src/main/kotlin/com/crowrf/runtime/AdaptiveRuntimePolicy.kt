package com.crowrf.runtime

/**
 * Pure deterministic policy. Android adapters collect facts; this class only decides.
 * It never enables hardware, requests permissions, or invents missing measurements.
 */
class AdaptiveRuntimePolicy {
    fun decide(snapshot: CapabilitySnapshot, previous: RuntimeMode? = null): RuntimeDecision {
        val reasons = mutableListOf<String>()
        val hardwareMaximum = hardwareMaximum(snapshot)
        var selected = hardwareMaximum

        if (snapshot.thermalStatus >= ThermalStatus.SEVERE) {
            selected = minOf(selected, RuntimeMode.RF_2D_HEATMAP)
            reasons += "thermal pressure"
        } else if (snapshot.thermalStatus == ThermalStatus.MODERATE) {
            selected = minOf(selected, RuntimeMode.RF_3D_FIELD)
            reasons += "moderate thermal pressure"
        }

        if (!snapshot.charging && snapshot.batteryPercent < 10) {
            selected = minOf(selected, RuntimeMode.RECORDED_MAP_VIEWER)
            reasons += "critical battery"
        } else if (!snapshot.charging && snapshot.batteryPercent < 20) {
            selected = minOf(selected, RuntimeMode.RF_2D_HEATMAP)
            reasons += "low battery"
        }

        if (snapshot.quality.memoryPressure >= 0.85) {
            selected = minOf(selected, RuntimeMode.RF_2D_HEATMAP)
            reasons += "memory pressure"
        }

        if (selected >= RuntimeMode.AR_LITE_RF &&
            snapshot.quality.arTrackingConfidence in 0.0..0.34
        ) {
            selected = if (hasRf(snapshot)) RuntimeMode.RF_3D_FIELD else RuntimeMode.RECORDED_MAP_VIEWER
            reasons += "AR tracking quality"
        }

        if (selected >= RuntimeMode.AR_LITE_RF &&
            snapshot.quality.rendererFps in 0.1..19.9
        ) {
            selected = RuntimeMode.RF_2D_HEATMAP
            reasons += "renderer performance"
        }

        // Upgrade hysteresis: one probe may downgrade immediately, but recovery must
        // reach healthy thresholds before returning to a high-cost renderer.
        if (previous != null && selected > previous && !healthyForUpgrade(snapshot)) {
            selected = previous
            reasons += "upgrade hysteresis"
        }

        if (reasons.isEmpty()) reasons += "best supported mode"
        return RuntimeDecision(
            mode = selected,
            hardwareRank = rank(hardwareMaximum),
            runtimeRank = rank(selected),
            reasons = reasons,
            reevaluateAfterMillis = when {
                snapshot.thermalStatus >= ThermalStatus.MODERATE -> 5_000
                !snapshot.charging && snapshot.batteryPercent < 20 -> 30_000
                else -> 60_000
            }
        )
    }

    private fun hardwareMaximum(s: CapabilitySnapshot): RuntimeMode = when {
        s.arCore && s.depth && s.uwb && hasRf(s) && s.permissions.camera ->
            RuntimeMode.FULL_AR_RF_DEPTH_UWB
        s.arCore && hasRf(s) && s.permissions.camera -> RuntimeMode.FULL_AR_RF
        s.camera && hasRf(s) && s.permissions.camera -> RuntimeMode.AR_LITE_RF
        hasRf(s) && s.imu -> RuntimeMode.RF_3D_FIELD
        hasRf(s) -> RuntimeMode.RF_2D_HEATMAP
        s.freeStorageMb > 64 -> RuntimeMode.RECORDED_MAP_VIEWER
        else -> RuntimeMode.LOCAL_ONLY
    }

    private fun hasRf(s: CapabilitySnapshot): Boolean =
        (s.wifi && s.permissions.nearbyWifi) ||
            (s.ble && s.permissions.nearbyBluetooth)

    private fun healthyForUpgrade(s: CapabilitySnapshot): Boolean =
        s.thermalStatus <= ThermalStatus.LIGHT &&
            (s.charging || s.batteryPercent >= 30) &&
            s.quality.memoryPressure < 0.70 &&
            (!s.arCore || s.quality.arTrackingConfidence >= 0.65) &&
            (s.quality.rendererFps == 0.0 || s.quality.rendererFps >= 28.0)

    private fun rank(mode: RuntimeMode): Char = when (mode) {
        RuntimeMode.FULL_AR_RF_DEPTH_UWB -> 'S'
        RuntimeMode.FULL_AR_RF -> 'A'
        RuntimeMode.AR_LITE_RF -> 'B'
        RuntimeMode.RF_3D_FIELD, RuntimeMode.RF_2D_HEATMAP -> 'C'
        RuntimeMode.RECORDED_MAP_VIEWER, RuntimeMode.LOCAL_ONLY -> 'D'
    }
}
