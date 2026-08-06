package com.example.game.model

import com.example.game.engine.Vector3

enum class BuildingType(
    val displayName: String,
    val iconSymbol: String,
    val maxHp: Int,
    val foodCost: Int,
    val woodCost: Int,
    val goldCost: Int,
    val favorCost: Int,
    val populationProvided: Int = 0,
    val trainables: List<UnitType> = emptyList(),
    val sizeRadius: Float = 2.0f
) {
    TOWN_CENTER("Town Center", "🏛️", maxHp = 1200, foodCost = 0, woodCost = 300, goldCost = 150, favorCost = 0, populationProvided = 15, trainables = listOf(UnitType.VILLAGER, UnitType.HERCULES, UnitType.PHARAOH, UnitType.HERSIR)),
    HOUSE("House", "🏠", maxHp = 250, foodCost = 0, woodCost = 50, goldCost = 0, favorCost = 0, populationProvided = 5),
    FARM("Farm", "🌾", maxHp = 180, foodCost = 0, woodCost = 75, goldCost = 0, favorCost = 0),
    GRANARY("Granary / Dropoff", "🏺", maxHp = 300, foodCost = 0, woodCost = 50, goldCost = 0, favorCost = 0),
    STOREHOUSE("Storehouse", "📦", maxHp = 300, foodCost = 0, woodCost = 50, goldCost = 0, favorCost = 0),
    BARRACKS("Barracks", "⚔️", maxHp = 600, foodCost = 0, woodCost = 120, goldCost = 0, favorCost = 0, trainables = listOf(UnitType.HOPLITE, UnitType.SPEARMAN, UnitType.TOXOTES_ARCHER, UnitType.PRODROMOS_CAVALRY, UnitType.THROWING_AXEMAN)),
    TEMPLE("Temple", "⛩️", maxHp = 800, foodCost = 0, woodCost = 100, goldCost = 100, favorCost = 0, trainables = listOf(UnitType.MINOTAUR, UnitType.CYCLOPS, UnitType.VALKYRIE, UnitType.SPHINX, UnitType.TITAN)),
    ARMORY("Armory", "🛡️", maxHp = 700, foodCost = 0, woodCost = 150, goldCost = 50, favorCost = 0),
    WONDER("Wonder of Gods", "✨", maxHp = 3000, foodCost = 1000, woodCost = 1000, goldCost = 1000, favorCost = 500)
}

data class TrainingQueueItem(
    val unitType: UnitType,
    val totalTimeMs: Long = 5000L,
    var progressMs: Long = 0L
)

data class BuildingEntity(
    val id: Long,
    val type: BuildingType,
    val owner: Owner,
    val position: Vector3,
    var currentHp: Int = type.maxHp,
    var constructionProgress: Float = 1.0f, // 1.0f = fully built
    val trainingQueue: MutableList<TrainingQueueItem> = mutableListOf(),
    var isEmpowered: Boolean = false // Pharaoh Egyptian empowerment
) {
    fun isFinished(): Boolean = constructionProgress >= 1.0f
    fun isDead(): Boolean = currentHp <= 0
    fun hpPercentage(): Float = (currentHp.toFloat() / type.maxHp.toFloat()).coerceIn(0f, 1f)
}
