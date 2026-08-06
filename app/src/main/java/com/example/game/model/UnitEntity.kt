package com.example.game.model

import com.example.game.engine.Vector3

enum class UnitType(
    val displayName: String,
    val iconSymbol: String,
    val isVillager: Boolean = false,
    val isHero: Boolean = false,
    val isMythUnit: Boolean = false,
    val maxHp: Int,
    val attack: Int,
    val range: Float,
    val speed: Float,
    val populationCost: Int,
    val foodCost: Int,
    val woodCost: Int,
    val goldCost: Int,
    val favorCost: Int,
    val specialAbility: String? = null
) {
    VILLAGER("Villager", "👨‍🌾", isVillager = true, maxHp = 80, attack = 6, range = 1.2f, speed = 2.2f, populationCost = 1, foodCost = 50, woodCost = 0, goldCost = 0, favorCost = 0),
    DWARVEN_MINER("Dwarven Miner", "⛏️", isVillager = true, maxHp = 90, attack = 8, range = 1.2f, speed = 2.0f, populationCost = 1, foodCost = 60, woodCost = 0, goldCost = 0, favorCost = 0),

    // Human Soldiers
    HOPLITE("Hoplite", "🛡️", maxHp = 120, attack = 12, range = 1.3f, speed = 2.0f, populationCost = 2, foodCost = 60, woodCost = 0, goldCost = 40, favorCost = 0),
    SPEARMAN("Spearman", "🗡️", maxHp = 100, attack = 10, range = 1.4f, speed = 2.4f, populationCost = 2, foodCost = 50, woodCost = 30, goldCost = 0, favorCost = 0),
    TOXOTES_ARCHER("Toxotes Archer", "🏹", maxHp = 75, attack = 14, range = 7.0f, speed = 2.1f, populationCost = 2, foodCost = 40, woodCost = 50, goldCost = 0, favorCost = 0),
    PRODROMOS_CAVALRY("Prodromos Cavalry", "🐴", maxHp = 150, attack = 16, range = 1.5f, speed = 3.5f, populationCost = 3, foodCost = 70, woodCost = 0, goldCost = 60, favorCost = 0),
    THROWING_AXEMAN("Throwing Axeman", "🪓", maxHp = 110, attack = 13, range = 4.5f, speed = 2.2f, populationCost = 2, foodCost = 50, woodCost = 40, goldCost = 0, favorCost = 0),

    // Heroes
    HERCULES("Hercules", "🦁", isHero = true, maxHp = 350, attack = 28, range = 1.5f, speed = 2.4f, populationCost = 3, foodCost = 150, woodCost = 0, goldCost = 100, favorCost = 10, specialAbility = "Heroic Leap"),
    PHARAOH("Pharaoh", "👑", isHero = true, maxHp = 220, attack = 20, range = 8.0f, speed = 2.2f, populationCost = 1, foodCost = 0, woodCost = 0, goldCost = 0, favorCost = 0, specialAbility = "Empower Building"),
    HERSIR("Hersir", "⚡", isHero = true, maxHp = 200, attack = 18, range = 1.5f, speed = 2.3f, populationCost = 2, foodCost = 80, woodCost = 0, goldCost = 40, favorCost = 5, specialAbility = "Gain Favor in Combat"),

    // Myth Units
    MINOTAUR("Minotaur", "🐂", isMythUnit = true, maxHp = 320, attack = 24, range = 1.5f, speed = 2.3f, populationCost = 4, foodCost = 150, woodCost = 0, goldCost = 0, favorCost = 20, specialAbility = "Bull Horn Charge"),
    CYCLOPS("Cyclops", "👁️", isMythUnit = true, maxHp = 400, attack = 30, range = 1.5f, speed = 1.8f, populationCost = 5, foodCost = 200, woodCost = 0, goldCost = 0, favorCost = 25, specialAbility = "Unit Toss"),
    VALKYRIE("Valkyrie", "🦅", isMythUnit = true, maxHp = 260, attack = 16, range = 2.0f, speed = 3.2f, populationCost = 3, foodCost = 120, woodCost = 0, goldCost = 0, favorCost = 18, specialAbility = "Heal Allied Units"),
    SPHINX("Sphinx", "🐾", isMythUnit = true, maxHp = 300, attack = 22, range = 1.5f, speed = 2.6f, populationCost = 4, foodCost = 140, woodCost = 0, goldCost = 0, favorCost = 22, specialAbility = "Sandstorm Whirlwind"),
    TITAN("Colossal Titan", "🗿", isMythUnit = true, maxHp = 1200, attack = 80, range = 2.5f, speed = 1.5f, populationCost = 10, foodCost = 800, woodCost = 0, goldCost = 800, favorCost = 50, specialAbility = "Earthquake Stomp")
}

enum class UnitState {
    IDLE,
    MOVING,
    ATTACKING,
    GATHERING,
    BUILDING
}

enum class GatherResourceType {
    FOOD,
    WOOD,
    GOLD,
    FAVOR
}

data class UnitEntity(
    val id: Long,
    val type: UnitType,
    val owner: Owner,
    var position: Vector3,
    var targetPosition: Vector3? = null,
    var targetEntityId: Long? = null,
    var currentHp: Int = type.maxHp,
    var state: UnitState = UnitState.IDLE,
    var gatherType: GatherResourceType? = null,
    var carriedResourceAmount: Int = 0,
    var rotationDegrees: Float = 0f,
    var lastAttackTimeMs: Long = 0L,
    var specialCooldownMs: Long = 0L
) {
    fun isDead(): Boolean = currentHp <= 0

    fun hpPercentage(): Float = (currentHp.toFloat() / type.maxHp.toFloat()).coerceIn(0f, 1f)
}
