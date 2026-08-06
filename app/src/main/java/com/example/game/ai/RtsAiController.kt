package com.example.game.ai

import com.example.game.engine.SoundEffects
import com.example.game.engine.Vector3
import com.example.game.model.*
import kotlin.random.Random

/**
 * Autonomous RTS AI Opponent for Age of Mythology.
 * Handles economic gathering, base expansion, aging up, army training, and wave attacks.
 */
class RtsAiController(
    val aiFaction: Faction = Faction.EGYPTIAN,
    val aiGod: MajorGod = MajorGod.RA,
    val difficultyMultiplier: Float = 1.0f
) {
    var aiResources = Resources(food = 300, wood = 300, gold = 200, favor = 50)
    var aiAge = Age.ARCHAIC
    var lastAiTickMs = 0L
    var lastAttackWaveMs = 0L

    fun tick(
        currentTimeMs: Long,
        units: MutableList<UnitEntity>,
        buildings: MutableList<BuildingEntity>,
        resourceNodes: MutableList<ResourceNode>,
        idGenerator: () -> Long,
        onAiCastGodPower: (GodPower, Vector3) -> Unit
    ) {
        if (currentTimeMs - lastAiTickMs < 2000L) return // AI decision tick every 2 seconds
        lastAiTickMs = currentTimeMs

        val aiUnits = units.filter { it.owner == Owner.AI_ENEMY }
        val aiBuildings = buildings.filter { it.owner == Owner.AI_ENEMY }
        val playerUnits = units.filter { it.owner == Owner.PLAYER }
        val playerBuildings = buildings.filter { it.owner == Owner.PLAYER }

        // 1. Resource Generation for AI
        val villagerCount = aiUnits.count { it.type.isVillager }
        val foodIncome = (villagerCount * 12 * difficultyMultiplier).toInt()
        val woodIncome = (villagerCount * 10 * difficultyMultiplier).toInt()
        val goldIncome = (villagerCount * 10 * difficultyMultiplier).toInt()
        val favorIncome = (5 * difficultyMultiplier).toInt()
        aiResources = aiResources.plus(Resources(foodIncome, woodIncome, goldIncome, favorIncome))

        // 2. Spawn Villagers at AI Town Center if population allows
        val townCenter = aiBuildings.find { it.type == BuildingType.TOWN_CENTER && !it.isDead() }
        if (townCenter != null && villagerCount < 12 * difficultyMultiplier.toInt()) {
            val vCost = Resources(food = 50, wood = 0, gold = 0)
            if (aiResources.hasEnough(vCost)) {
                aiResources = aiResources.minus(vCost)
                val spawnPos = townCenter.position + Vector3(
                    (Random.nextFloat() - 0.5f) * 4f,
                    0f,
                    (Random.nextFloat() - 0.5f) * 4f
                )
                units.add(
                    UnitEntity(
                        id = idGenerator(),
                        type = UnitType.VILLAGER,
                        owner = Owner.AI_ENEMY,
                        position = spawnPos
                    )
                )
            }
        }

        // 3. AI Base Building Construction
        val aiBaseCenter = townCenter?.position ?: Vector3(40f, 0f, 40f)

        // Need Barracks
        val hasBarracks = aiBuildings.any { it.type == BuildingType.BARRACKS }
        if (!hasBarracks && aiResources.hasEnough(Resources(0, 120, 0))) {
            aiResources = aiResources.minus(Resources(0, 120, 0))
            buildings.add(
                BuildingEntity(
                    id = idGenerator(),
                    type = BuildingType.BARRACKS,
                    owner = Owner.AI_ENEMY,
                    position = aiBaseCenter + Vector3(-6f, 0f, 4f)
                )
            )
        }

        // Need Temple
        val hasTemple = aiBuildings.any { it.type == BuildingType.TEMPLE }
        if (!hasTemple && aiResources.hasEnough(Resources(0, 100, 100))) {
            aiResources = aiResources.minus(Resources(0, 100, 100))
            buildings.add(
                BuildingEntity(
                    id = idGenerator(),
                    type = BuildingType.TEMPLE,
                    owner = Owner.AI_ENEMY,
                    position = aiBaseCenter + Vector3(6f, 0f, -4f)
                )
            )
        }

        // 4. Aging Up AI
        if (aiAge == Age.ARCHAIC && aiResources.food >= 400) {
            aiResources = aiResources.minus(Resources(400, 0, 0))
            aiAge = Age.CLASSICAL
        } else if (aiAge == Age.CLASSICAL && aiResources.food >= 800 && aiResources.gold >= 500) {
            aiResources = aiResources.minus(Resources(800, 0, 500))
            aiAge = Age.HEROIC
        } else if (aiAge == Age.HEROIC && aiResources.food >= 1000 && aiResources.gold >= 1000) {
            aiResources = aiResources.minus(Resources(1000, 0, 1000))
            aiAge = Age.MYTHIC
        }

        // 5. Train AI Soldiers & Myth Units
        val militaryUnits = aiUnits.filter { !it.type.isVillager }
        val barracks = aiBuildings.find { it.type == BuildingType.BARRACKS }
        val temple = aiBuildings.find { it.type == BuildingType.TEMPLE }

        if (barracks != null && militaryUnits.size < 20 * difficultyMultiplier) {
            val solType = if (Random.nextBoolean()) UnitType.HOPLITE else UnitType.TOXOTES_ARCHER
            val cost = Resources(solType.foodCost, solType.woodCost, solType.goldCost)
            if (aiResources.hasEnough(cost)) {
                aiResources = aiResources.minus(cost)
                units.add(
                    UnitEntity(
                        id = idGenerator(),
                        type = solType,
                        owner = Owner.AI_ENEMY,
                        position = barracks.position + Vector3(2f, 0f, 2f)
                    )
                )
            }
        }

        if (temple != null && militaryUnits.size < 25 * difficultyMultiplier && aiAge.level >= 2) {
            val mythType = when (aiFaction) {
                Faction.GREEK -> UnitType.MINOTAUR
                Faction.EGYPTIAN -> UnitType.SPHINX
                Faction.NORSE -> UnitType.VALKYRIE
                Faction.ATLANTEAN -> UnitType.TITAN
            }
            val cost = Resources(mythType.foodCost, mythType.woodCost, mythType.goldCost, mythType.favorCost)
            if (aiResources.hasEnough(cost)) {
                aiResources = aiResources.minus(cost)
                units.add(
                    UnitEntity(
                        id = idGenerator(),
                        type = mythType,
                        owner = Owner.AI_ENEMY,
                        position = temple.position + Vector3(-2f, 0f, -2f)
                    )
                )
            }
        }

        // 6. Launch Attack Wave every 35 seconds or if army >= 8 units
        if (militaryUnits.size >= 6 && currentTimeMs - lastAttackWaveMs > 30000L) {
            lastAttackWaveMs = currentTimeMs
            val target = playerBuildings.firstOrNull()?.position ?: playerUnits.firstOrNull()?.position ?: Vector3(10f, 0f, 10f)

            militaryUnits.forEach { unit ->
                unit.targetPosition = target + Vector3((Random.nextFloat() - 0.5f) * 6f, 0f, (Random.nextFloat() - 0.5f) * 6f)
                unit.state = UnitState.MOVING
            }

            // AI Casts God Power during attack!
            val godPowerToCast = when (aiAge) {
                Age.ARCHAIC -> GodPower.LIGHTNING_BOLT
                Age.CLASSICAL -> GodPower.SHOCKWAVE
                Age.HEROIC -> GodPower.METEOR
                Age.MYTHIC -> GodPower.TORNADO
            }
            onAiCastGodPower(godPowerToCast, target)
        }
    }
}
