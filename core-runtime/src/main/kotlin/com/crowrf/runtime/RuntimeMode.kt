package com.crowrf.runtime

enum class RuntimeMode(val rank: Int) {
    LOCAL_ONLY(0),
    RECORDED_MAP_VIEWER(1),
    RF_2D_HEATMAP(2),
    RF_3D_FIELD(3),
    AR_LITE_RF(4),
    FULL_AR_RF(5),
    FULL_AR_RF_DEPTH_UWB(6)
}

data class CapabilitySnapshot(
    val apiLevel: Int,
    val ramMb: Int,
    val freeStorageMb: Long,
    val batteryPercent: Int,
    val charging: Boolean,
    val thermalStatus: ThermalStatus,
    val wifi: Boolean,
    val ble: Boolean,
    val imu: Boolean,
    val camera: Boolean,
    val arCore: Boolean,
    val depth: Boolean,
    val uwb: Boolean,
    val permissions: PermissionSnapshot,
    val quality: QualitySnapshot = QualitySnapshot()
)

data class PermissionSnapshot(
    val nearbyWifi: Boolean,
    val nearbyBluetooth: Boolean,
    val camera: Boolean,
    val location: Boolean
)

data class QualitySnapshot(
    val arTrackingConfidence: Double = 0.0,
    val rfStability: Double = 0.0,
    val rendererFps: Double = 0.0,
    val imuDrift: Double = 1.0,
    val memoryPressure: Double = 0.0
)

enum class ThermalStatus { NOMINAL, LIGHT, MODERATE, SEVERE, CRITICAL }

data class RuntimeDecision(
    val mode: RuntimeMode,
    val hardwareRank: Char,
    val runtimeRank: Char,
    val reasons: List<String>,
    val reevaluateAfterMillis: Long
)
