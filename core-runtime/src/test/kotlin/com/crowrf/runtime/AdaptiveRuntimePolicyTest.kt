package com.crowrf.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveRuntimePolicyTest {
    private val policy = AdaptiveRuntimePolicy()

    @Test
    fun selectsFullModeOnlyWhenCapabilitiesAndPermissionsExist() {
        val decision = policy.decide(snapshot())
        assertEquals(RuntimeMode.FULL_AR_RF_DEPTH_UWB, decision.mode)
        assertEquals('S', decision.hardwareRank)
    }

    @Test
    fun severeThermalPressureImmediatelyFallsBack() {
        val decision = policy.decide(
            snapshot().copy(thermalStatus = ThermalStatus.SEVERE),
            RuntimeMode.FULL_AR_RF_DEPTH_UWB
        )
        assertEquals(RuntimeMode.RF_2D_HEATMAP, decision.mode)
        assertTrue("thermal pressure" in decision.reasons)
    }

    @Test
    fun absentRfPermissionDoesNotPretendRfIsAvailable() {
        val decision = policy.decide(
            snapshot().copy(
                permissions = snapshot().permissions.copy(
                    nearbyWifi = false,
                    nearbyBluetooth = false
                )
            )
        )
        assertEquals(RuntimeMode.RECORDED_MAP_VIEWER, decision.mode)
    }

    @Test
    fun unhealthyProbeCannotImmediatelyUpgradeRenderer() {
        val decision = policy.decide(
            snapshot().copy(
                quality = snapshot().quality.copy(
                    arTrackingConfidence = 0.55,
                    rendererFps = 24.0
                )
            ),
            RuntimeMode.RF_2D_HEATMAP
        )
        assertEquals(RuntimeMode.RF_2D_HEATMAP, decision.mode)
        assertTrue("upgrade hysteresis" in decision.reasons)
    }

    private fun snapshot() = CapabilitySnapshot(
        apiLevel = 36,
        ramMb = 8192,
        freeStorageMb = 4096,
        batteryPercent = 80,
        charging = false,
        thermalStatus = ThermalStatus.NOMINAL,
        wifi = true,
        ble = true,
        imu = true,
        camera = true,
        arCore = true,
        depth = true,
        uwb = true,
        permissions = PermissionSnapshot(
            nearbyWifi = true,
            nearbyBluetooth = true,
            camera = true,
            location = true
        ),
        quality = QualitySnapshot(
            arTrackingConfidence = 0.9,
            rfStability = 0.8,
            rendererFps = 60.0,
            imuDrift = 0.1,
            memoryPressure = 0.2
        )
    )
}
