package com.example.game.model

import kotlin.math.sin

enum class TerrainType(val colorHex: String, val water: Boolean = false) {
    GRASS("#4CAF50"),
    DESERT_SAND("#E0A96D"),
    SNOW("#ECEFF1"),
    WATER("#1E88E5", water = true),
    ROCK("#78909C")
}

enum class FogState {
    UNEXPLORED,
    FOGGED,
    VISIBLE
}

class TerrainMap(
    val width: Int = 50,
    val height: Int = 50,
    val defaultType: TerrainType = TerrainType.GRASS
) {
    val grid = Array(width) { x ->
        Array(height) { z ->
            val elevation = (sin(x * 0.2f) * 0.5f + sin(z * 0.2f) * 0.5f).coerceIn(0f, 2f)
            val isWater = x in 22..27 && z in 0..15 // River stretch
            val type = if (isWater) TerrainType.WATER else defaultType
            TerrainTile(x, z, elevation, type)
        }
    }

    val fog = Array(width) { Array(height) { FogState.UNEXPLORED } }

    fun getElevation(x: Float, z: Float): Float {
        val gx = x.toInt().coerceIn(0, width - 1)
        val gz = z.toInt().coerceIn(0, height - 1)
        return grid[gx][gz].elevation
    }

    fun revealArea(centerX: Float, centerZ: Float, radius: Float) {
        val minX = (centerX - radius).toInt().coerceIn(0, width - 1)
        val maxX = (centerX + radius).toInt().coerceIn(0, width - 1)
        val minZ = (centerZ - radius).toInt().coerceIn(0, height - 1)
        val maxZ = (centerZ + radius).toInt().coerceIn(0, height - 1)

        val radiusSq = radius * radius
        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                val dx = x - centerX
                val dz = z - centerZ
                if (dx * dx + dz * dz <= radiusSq) {
                    fog[x][z] = FogState.VISIBLE
                }
            }
        }
    }

    /**
     * Resets visible tiles back to fogged for real-time fog of war update loop.
     */
    fun resetVisibleToFogged() {
        for (x in 0 until width) {
            for (z in 0 until height) {
                if (fog[x][z] == FogState.VISIBLE) {
                    fog[x][z] = FogState.FOGGED
                }
            }
        }
    }
}

data class TerrainTile(
    val x: Int,
    val z: Int,
    val elevation: Float,
    val type: TerrainType
)
