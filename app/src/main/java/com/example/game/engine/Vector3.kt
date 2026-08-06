package com.example.game.engine

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 3D Vector math for software 3D projection, terrain geometry, and camera transforms.
 */
data class Vector3(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
) {
    operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3) = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float) = Vector3(x * scalar, y * scalar, z * scalar)
    operator fun div(scalar: Float) = Vector3(x / scalar, y / scalar, z / scalar)

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun normalized(): Vector3 {
        val len = length()
        return if (len > 0.0001f) this / len else Vector3()
    }

    fun distanceTo(other: Vector3): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    /**
     * Rotates vector around Y axis (vertical elevation in world).
     */
    fun rotateY(angleRad: Float): Vector3 {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Vector3(
            x = x * cosA + z * sinA,
            y = y,
            z = -x * sinA + z * cosA
        )
    }

    /**
     * Rotates vector around X axis (pitch / isometric angle).
     */
    fun rotateX(angleRad: Float): Vector3 {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Vector3(
            x = x,
            y = y * cosA - z * sinA,
            z = y * sinA + z * cosA
        )
    }
}

/**
 * Screen projected Point with depth Z for painter order sorting.
 */
data class ScreenPoint(
    val x: Float,
    val y: Float,
    val depth: Float,
    val isVisible: Boolean
)

/**
 * Isometric / Tactical 3D Camera State.
 */
data class Camera3D(
    var targetX: Float = 25f,
    var targetY: Float = 25f,
    var zoom: Float = 1.0f, // 0.5f (zoomed out) to 2.2f (close up)
    var yawDegrees: Float = 45f, // Camera rotation around Y axis
    var pitchDegrees: Float = 55f // Camera tilt angle (isometric RTS feel)
) {
    /**
     * Projects a 3D world coordinate into 2D screen coordinates relative to screen center.
     */
    fun project(
        worldPos: Vector3,
        screenWidth: Float,
        screenHeight: Float
    ): ScreenPoint {
        // Relativize to camera focus point
        val dx = worldPos.x - targetX
        val dy = worldPos.y - targetY // Elevation in world
        val dz = worldPos.z - targetY // Z ground depth in world

        // Convert camera angles to radians
        val yawRad = Math.toRadians(yawDegrees.toDouble()).toFloat()
        val pitchRad = Math.toRadians(pitchDegrees.toDouble()).toFloat()

        // 1. Rotate around Y (yaw)
        val rx = dx * cos(yawRad) - dz * sin(yawRad)
        val rz = dx * sin(yawRad) + dz * cos(yawRad)

        // 2. Tilt down (pitch)
        val ry = dy * cos(pitchRad) - rz * sin(pitchRad)
        val depth = dy * sin(pitchRad) + rz * cos(pitchRad)

        // 3. Perspective / Isometric Projection scaling
        val baseScale = 38f * zoom
        val screenX = (screenWidth / 2f) + (rx * baseScale)
        val screenY = (screenHeight / 2f) - (ry * baseScale)

        val isVisible = screenX in -200f..(screenWidth + 200f) && screenY in -200f..(screenHeight + 200f)

        return ScreenPoint(screenX, screenY, depth, isVisible)
    }

    /**
     * Unprojects a 2D screen coordinate back onto the ground plane (y=0 world elevation).
     */
    fun screenToWorldGround(
        screenX: Float,
        screenY: Float,
        screenWidth: Float,
        screenHeight: Float
    ): Vector3 {
        val baseScale = 38f * zoom
        val relX = (screenX - screenWidth / 2f) / baseScale
        val relY = -(screenY - screenHeight / 2f) / baseScale

        val yawRad = Math.toRadians(yawDegrees.toDouble()).toFloat()
        val pitchRad = Math.toRadians(pitchDegrees.toDouble()).toFloat()

        // Inverse pitch tilt
        val rz = -relY / sin(pitchRad)

        // Inverse yaw
        val worldX = targetX + (relX * cos(yawRad) + rz * sin(yawRad))
        val worldZ = targetY + (-relX * sin(yawRad) + rz * cos(yawRad))

        return Vector3(worldX, 0f, worldZ)
    }
}
