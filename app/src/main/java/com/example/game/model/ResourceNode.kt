package com.example.game.model

import com.example.game.engine.Vector3

enum class ResourceNodeType(
    val displayName: String,
    val iconSymbol: String,
    val resourceType: GatherResourceType,
    val initialAmount: Int,
    val colorHex: String
) {
    GOLD_MINE("Gold Mine", "⛏️", GatherResourceType.GOLD, 2500, "#FFD700"),
    FOREST_TREE("Pine Forest", "🌲", GatherResourceType.WOOD, 400, "#2E7D32"),
    BERRY_BUSH("Berry Bush", "🍇", GatherResourceType.FOOD, 350, "#8E24AA"),
    HUNT_ANIMAL("Wild Boar", "🐗", GatherResourceType.FOOD, 500, "#795548"),
    RELIC("Sacred Relic", "🏆", GatherResourceType.FAVOR, 9999, "#00BCD4")
}

data class ResourceNode(
    val id: Long,
    val type: ResourceNodeType,
    val position: Vector3,
    var remainingAmount: Int = type.initialAmount
) {
    fun isDepleted(): Boolean = remainingAmount <= 0
}
