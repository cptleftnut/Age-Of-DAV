package com.example.game.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.ai.RtsAiController
import com.example.game.engine.Camera3D
import com.example.game.engine.SoundEffects
import com.example.game.engine.Vector3
import com.example.game.engine.VoiceLineAction
import com.example.game.engine.VoiceLineManager
import com.example.game.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

enum class GameScreenState {
    MAIN_MENU,
    FACTION_SELECT,
    SKIRMISH_GAME,
    CAMPAIGN_SELECT,
    CAMPAIGN_GAME,
    GOD_POWER_SANDBOX,
    VICTORY,
    DEFEAT
}

data class ActiveGodPowerCast(
    val power: GodPower,
    val targetPos: Vector3,
    val startTimeMs: Long,
    val isPlayer: Boolean
)

data class ParticleEffect(
    val id: Long,
    val pos: Vector3,
    val type: String, // "LIGHTNING", "METEOR", "HEAL", "EXPLOSION", "DUST"
    val startTimeMs: Long,
    val durationMs: Long = 1000L
)

data class GameMatchStats(
    val enemyUnitsKilled: Int = 0,
    val playerUnitsLost: Int = 0,
    val playerUnitsTrained: Int = 0,
    val buildingsDestroyed: Int = 0,
    val godPowersCast: Int = 0,
    val totalFoodGathered: Int = 0,
    val totalWoodGathered: Int = 0,
    val totalGoldGathered: Int = 0,
    val totalFavorGathered: Int = 0,
    val maxAgeReached: Age = Age.ARCHAIC
) {
    val totalResourcesGathered: Int get() = totalFoodGathered + totalWoodGathered + totalGoldGathered + totalFavorGathered
}

data class GameUiState(
    val screenState: GameScreenState = GameScreenState.MAIN_MENU,
    val currentMission: CampaignMission? = null,
    val playerFaction: Faction = Faction.GREEK,
    val playerMajorGod: MajorGod = MajorGod.ZEUS,
    val currentAge: Age = Age.ARCHAIC,
    val playerResources: Resources = Resources(food = 350, wood = 300, gold = 200, favor = 50),
    val population: Int = 10,
    val maxPopulation: Int = 20,
    val selectedUnitIds: Set<Long> = emptySet(),
    val selectedBuildingId: Long? = null,
    val selectedResourceNodeId: Long? = null,
    val activeCastingGodPower: GodPower? = null,
    val availableGodPowers: List<GodPower> = listOf(GodPower.LIGHTNING_BOLT, GodPower.HEALING_SPRING),
    val godPowerCooldowns: Map<GodPower, Long> = emptyMap(),
    val isPaused: Boolean = false,
    val showAgeUpDialog: Boolean = false,
    val gameTimeSeconds: Int = 0,
    val camera: Camera3D = Camera3D(),
    val logMessage: String = "Welcome to Age of Mythology 3D! Tap to command units.",
    val matchStats: GameMatchStats = GameMatchStats()
)

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _units = MutableStateFlow<List<UnitEntity>>(emptyList())
    val units: StateFlow<List<UnitEntity>> = _units.asStateFlow()

    private val _buildings = MutableStateFlow<List<BuildingEntity>>(emptyList())
    val buildings: StateFlow<List<BuildingEntity>> = _buildings.asStateFlow()

    private val _resourceNodes = MutableStateFlow<List<ResourceNode>>(emptyList())
    val resourceNodes: StateFlow<List<ResourceNode>> = _resourceNodes.asStateFlow()

    private val _activeCasts = MutableStateFlow<List<ActiveGodPowerCast>>(emptyList())
    val activeCasts: StateFlow<List<ActiveGodPowerCast>> = _activeCasts.asStateFlow()

    private val _particles = MutableStateFlow<List<ParticleEffect>>(emptyList())
    val particles: StateFlow<List<ParticleEffect>> = _particles.asStateFlow()

    var terrainMap = TerrainMap(50, 50)
    private var aiController = RtsAiController()
    private var gameLoopJob: Job? = null
    private var entityIdCounter = 100L

    private fun nextId(): Long = ++entityIdCounter

    fun startSkirmish(
        faction: Faction = Faction.GREEK,
        god: MajorGod = MajorGod.ZEUS,
        enemyFaction: Faction = Faction.EGYPTIAN
    ) {
        _uiState.update {
            it.copy(
                screenState = GameScreenState.SKIRMISH_GAME,
                playerFaction = faction,
                playerMajorGod = god,
                currentAge = Age.ARCHAIC,
                playerResources = Resources(food = 400, wood = 400, gold = 250, favor = 50),
                availableGodPowers = listOf(god.startingGodPower, GodPower.HEALING_SPRING),
                logMessage = "Skirmish battle started! Gather resources & build your empire."
            )
        }
        aiController = RtsAiController(aiFaction = enemyFaction)
        initMapEntities()
        startGameLoop()
    }

    fun startCampaign(mission: CampaignMission) {
        _uiState.update {
            it.copy(
                screenState = GameScreenState.CAMPAIGN_GAME,
                currentMission = mission,
                playerFaction = mission.playerFaction,
                playerMajorGod = mission.playerGod,
                currentAge = Age.ARCHAIC,
                playerResources = Resources(food = 500, wood = 500, gold = 300, favor = 100),
                availableGodPowers = listOf(mission.playerGod.startingGodPower, GodPower.LIGHTNING_BOLT, GodPower.RESTORATION),
                logMessage = mission.introDialogue
            )
        }
        aiController = RtsAiController(aiFaction = mission.enemyFaction)
        initMapEntities()
        startGameLoop()
    }

    fun startSandbox() {
        _uiState.update {
            it.copy(
                screenState = GameScreenState.GOD_POWER_SANDBOX,
                playerFaction = Faction.GREEK,
                playerMajorGod = MajorGod.ZEUS,
                currentAge = Age.MYTHIC,
                playerResources = Resources(food = 9999, wood = 9999, gold = 9999, favor = 9999),
                availableGodPowers = GodPower.values().toList(),
                logMessage = "God Power Sandbox! Unlimited divine powers ready to unleash!"
            )
        }
        aiController = RtsAiController(difficultyMultiplier = 1.5f)
        initMapEntities()
        startGameLoop()
    }

    private fun initMapEntities() {
        terrainMap = TerrainMap(50, 50)
        val unitList = mutableListOf<UnitEntity>()
        val buildingList = mutableListOf<BuildingEntity>()
        val nodeList = mutableListOf<ResourceNode>()

        // 1. Player Base
        val playerTcPos = Vector3(12f, 0f, 12f)
        buildingList.add(
            BuildingEntity(
                id = nextId(),
                type = BuildingType.TOWN_CENTER,
                owner = Owner.PLAYER,
                position = playerTcPos
            )
        )
        // Player Villagers
        repeat(4) { i ->
            unitList.add(
                UnitEntity(
                    id = nextId(),
                    type = UnitType.VILLAGER,
                    owner = Owner.PLAYER,
                    position = playerTcPos + Vector3(2f + i * 1.5f, 0f, 2f)
                )
            )
        }
        // Player Soldiers
        unitList.add(UnitEntity(id = nextId(), type = UnitType.HOPLITE, owner = Owner.PLAYER, position = playerTcPos + Vector3(-3f, 0f, 2f)))
        unitList.add(UnitEntity(id = nextId(), type = UnitType.MINOTAUR, owner = Owner.PLAYER, position = playerTcPos + Vector3(-4f, 0f, -2f)))

        // 2. Enemy Base
        val enemyTcPos = Vector3(38f, 0f, 38f)
        buildingList.add(
            BuildingEntity(
                id = nextId(),
                type = BuildingType.TOWN_CENTER,
                owner = Owner.AI_ENEMY,
                position = enemyTcPos
            )
        )
        repeat(4) { i ->
            unitList.add(
                UnitEntity(
                    id = nextId(),
                    type = UnitType.VILLAGER,
                    owner = Owner.AI_ENEMY,
                    position = enemyTcPos + Vector3(-2f - i * 1.5f, 0f, -2f)
                )
            )
        }
        unitList.add(UnitEntity(id = nextId(), type = UnitType.SPEARMAN, owner = Owner.AI_ENEMY, position = enemyTcPos + Vector3(3f, 0f, -2f)))
        unitList.add(UnitEntity(id = nextId(), type = UnitType.SPHINX, owner = Owner.AI_ENEMY, position = enemyTcPos + Vector3(4f, 0f, 2f)))

        // 3. Resource Nodes across map
        // Gold Mines near player and enemy
        nodeList.add(ResourceNode(nextId(), ResourceNodeType.GOLD_MINE, Vector3(18f, 0f, 10f)))
        nodeList.add(ResourceNode(nextId(), ResourceNodeType.GOLD_MINE, Vector3(32f, 0f, 40f)))
        nodeList.add(ResourceNode(nextId(), ResourceNodeType.GOLD_MINE, Vector3(25f, 0f, 25f)))

        // Forests
        repeat(8) { i ->
            nodeList.add(ResourceNode(nextId(), ResourceNodeType.FOREST_TREE, Vector3(8f + i * 1.5f, 0f, 18f)))
            nodeList.add(ResourceNode(nextId(), ResourceNodeType.FOREST_TREE, Vector3(35f + i * 1.5f, 0f, 30f)))
        }

        // Berry Bushes & Boars
        nodeList.add(ResourceNode(nextId(), ResourceNodeType.BERRY_BUSH, Vector3(10f, 0f, 6f)))
        nodeList.add(ResourceNode(nextId(), ResourceNodeType.HUNT_ANIMAL, Vector3(28f, 0f, 18f)))
        nodeList.add(ResourceNode(nextId(), ResourceNodeType.RELIC, Vector3(25f, 0f, 35f)))

        _units.value = unitList
        _buildings.value = buildingList
        _resourceNodes.value = nodeList

        // Set camera focus to player TC
        _uiState.update {
            it.copy(
                camera = Camera3D(targetX = playerTcPos.x, targetY = playerTcPos.z, zoom = 1.0f),
                gameTimeSeconds = 0,
                matchStats = GameMatchStats(maxAgeReached = it.currentAge)
            )
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastSecTick = System.currentTimeMillis()
            while (true) {
                val now = System.currentTimeMillis()

                if (!_uiState.value.isPaused) {
                    updateGameLogic(now)

                    if (now - lastSecTick >= 1000L) {
                        lastSecTick = now
                        _uiState.update { it.copy(gameTimeSeconds = it.gameTimeSeconds + 1) }
                    }
                }

                delay(50L) // 20 FPS simulation tick for smooth high-efficiency mobile loop
            }
        }
    }

    private fun updateGameLogic(currentTimeMs: Long) {
        val currentUnits = _units.value.toMutableList()
        val currentBuildings = _buildings.value.toMutableList()
        val currentNodes = _resourceNodes.value.toMutableList()

        // 1. Process Unit Movements, Combat & Resource Harvesting
        for (unit in currentUnits) {
            if (unit.isDead()) continue

            // Movement
            unit.targetPosition?.let { target ->
                val dist = unit.position.distanceTo(target)
                if (dist > 0.5f) {
                    val dir = (target - unit.position).normalized()
                    val moveDist = unit.type.speed * 0.15f
                    unit.position = unit.position + (dir * moveDist)
                    unit.rotationDegrees = Math.toDegrees(kotlin.math.atan2(-dir.x.toDouble(), dir.z.toDouble())).toFloat()
                    unit.state = UnitState.MOVING
                } else {
                    unit.targetPosition = null
                    if (unit.state == UnitState.MOVING) unit.state = UnitState.IDLE
                }
            }

            // Auto Attack Target
            unit.targetEntityId?.let { targetId ->
                val enemyUnit = currentUnits.find { it.id == targetId && !it.isDead() }
                val enemyBuilding = currentBuildings.find { it.id == targetId && !it.isDead() }

                val targetPos = enemyUnit?.position ?: enemyBuilding?.position
                if (targetPos != null) {
                    val dist = unit.position.distanceTo(targetPos)
                    if (dist <= unit.type.range) {
                        unit.state = UnitState.ATTACKING
                        if (currentTimeMs - unit.lastAttackTimeMs >= 1200L) {
                            unit.lastAttackTimeMs = currentTimeMs
                            SoundEffects.playAttack()

                            if (enemyUnit != null) {
                                enemyUnit.currentHp -= unit.type.attack
                                if (enemyUnit.isDead()) {
                                    unit.targetEntityId = null
                                    unit.state = UnitState.IDLE
                                    if (unit.owner == Owner.PLAYER) {
                                        _uiState.update { s -> s.copy(matchStats = s.matchStats.copy(enemyUnitsKilled = s.matchStats.enemyUnitsKilled + 1)) }
                                    } else if (enemyUnit.owner == Owner.PLAYER) {
                                        _uiState.update { s -> s.copy(matchStats = s.matchStats.copy(playerUnitsLost = s.matchStats.playerUnitsLost + 1)) }
                                    }
                                }
                            } else if (enemyBuilding != null) {
                                enemyBuilding.currentHp -= unit.type.attack
                                if (enemyBuilding.isDead()) {
                                    unit.targetEntityId = null
                                    unit.state = UnitState.IDLE
                                    if (unit.owner == Owner.PLAYER) {
                                        _uiState.update { s -> s.copy(matchStats = s.matchStats.copy(buildingsDestroyed = s.matchStats.buildingsDestroyed + 1)) }
                                    }
                                }
                            }
                        }
                    } else if (unit.targetPosition == null) {
                        // Move closer
                        unit.targetPosition = targetPos
                    }
                } else {
                    unit.targetEntityId = null
                }
            }

            // Villager Gathering
            if (unit.type.isVillager && unit.state == UnitState.GATHERING) {
                unit.targetEntityId?.let { nodeId ->
                    val node = currentNodes.find { it.id == nodeId && !it.isDepleted() }
                    if (node != null && unit.position.distanceTo(node.position) <= 2.0f) {
                        if (currentTimeMs - unit.lastAttackTimeMs >= 1000L) {
                            unit.lastAttackTimeMs = currentTimeMs
                            node.remainingAmount -= 5
                            unit.carriedResourceAmount += 5

                            if (unit.carriedResourceAmount >= 20) {
                                // Add to player resources
                                if (unit.owner == Owner.PLAYER) {
                                    _uiState.update { state ->
                                        val gained = when (node.type.resourceType) {
                                            GatherResourceType.FOOD -> Resources(food = 20)
                                            GatherResourceType.WOOD -> Resources(wood = 20)
                                            GatherResourceType.GOLD -> Resources(gold = 20)
                                            GatherResourceType.FAVOR -> Resources(favor = 10)
                                        }
                                        val stats = when (node.type.resourceType) {
                                            GatherResourceType.FOOD -> state.matchStats.copy(totalFoodGathered = state.matchStats.totalFoodGathered + 20)
                                            GatherResourceType.WOOD -> state.matchStats.copy(totalWoodGathered = state.matchStats.totalWoodGathered + 20)
                                            GatherResourceType.GOLD -> state.matchStats.copy(totalGoldGathered = state.matchStats.totalGoldGathered + 20)
                                            GatherResourceType.FAVOR -> state.matchStats.copy(totalFavorGathered = state.matchStats.totalFavorGathered + 10)
                                        }
                                        state.copy(
                                            playerResources = state.playerResources.plus(gained),
                                            matchStats = stats
                                        )
                                    }
                                }
                                unit.carriedResourceAmount = 0
                            }
                        }
                    }
                }
            }
        }

        // 2. Process Building Training Queues
        for (bld in currentBuildings) {
            if (bld.isDead() || bld.trainingQueue.isEmpty()) continue
            val item = bld.trainingQueue.first()
            item.progressMs += 50L
            if (item.progressMs >= item.totalTimeMs) {
                bld.trainingQueue.removeAt(0)
                SoundEffects.playBuildingComplete()
                if (bld.owner == Owner.PLAYER) {
                    _uiState.update { s -> s.copy(matchStats = s.matchStats.copy(playerUnitsTrained = s.matchStats.playerUnitsTrained + 1)) }
                }

                val spawnPos = bld.position + Vector3(3f, 0f, 3f)
                currentUnits.add(
                    UnitEntity(
                        id = nextId(),
                        type = item.unitType,
                        owner = bld.owner,
                        position = spawnPos
                    )
                )
            }
        }

        // 3. AI Tick
        aiController.tick(
            currentTimeMs = currentTimeMs,
            units = currentUnits,
            buildings = currentBuildings,
            resourceNodes = currentNodes,
            idGenerator = { nextId() },
            onAiCastGodPower = { power, target -> castGodPowerEffect(power, target, isPlayer = false) }
        )

        // 4. Reveal Fog of War around player units & buildings
        terrainMap.resetVisibleToFogged()
        currentUnits.filter { it.owner == Owner.PLAYER }.forEach {
            terrainMap.revealArea(it.position.x, it.position.z, 8f)
        }
        currentBuildings.filter { it.owner == Owner.PLAYER }.forEach {
            terrainMap.revealArea(it.position.x, it.position.z, 12f)
        }

        // 5. Check Victory / Defeat Conditions
        val playerTcAlive = currentBuildings.any { it.owner == Owner.PLAYER && it.type == BuildingType.TOWN_CENTER && !it.isDead() }
        val enemyTcAlive = currentBuildings.any { it.owner == Owner.AI_ENEMY && it.type == BuildingType.TOWN_CENTER && !it.isDead() }

        if (!playerTcAlive && _uiState.value.screenState != GameScreenState.DEFEAT && _uiState.value.screenState != GameScreenState.GOD_POWER_SANDBOX) {
            SoundEffects.playDefeat()
            _uiState.update { it.copy(screenState = GameScreenState.DEFEAT, logMessage = "DEFEAT! Your Town Center has been destroyed.") }
        } else if (!enemyTcAlive && _uiState.value.screenState != GameScreenState.VICTORY && _uiState.value.screenState != GameScreenState.GOD_POWER_SANDBOX) {
            SoundEffects.playVictory()
            _uiState.update { it.copy(screenState = GameScreenState.VICTORY, logMessage = "VICTORY! Enemy forces routed!") }
        }

        _units.value = currentUnits.filter { !it.isDead() }
        _buildings.value = currentBuildings.filter { !it.isDead() }
        _resourceNodes.value = currentNodes.filter { !it.isDepleted() }
    }

    // Touch Controls & Player Command Handlers
    fun onSelectEntitiesInBox(boxMin: Vector3, boxMax: Vector3) {
        val selectedIds = _units.value.filter {
            it.owner == Owner.PLAYER &&
                    it.position.x in boxMin.x..boxMax.x &&
                    it.position.z in boxMin.z..boxMax.z
        }.map { it.id }.toSet()

        if (selectedIds.isNotEmpty()) {
            val faction = _uiState.value.playerFaction
            val voiceData = VoiceLineManager.triggerVoiceLine(faction, VoiceLineAction.SELECT)
            
            val leadUnit = _units.value.find { it.id in selectedIds }
            leadUnit?.let { u ->
                addParticle(u.position + Vector3(0f, 2.5f, 0f), "VOICE:${voiceData.phrase}", 1800L)
            }

            _uiState.update { 
                it.copy(
                    selectedUnitIds = selectedIds, 
                    selectedBuildingId = null, 
                    selectedResourceNodeId = null,
                    logMessage = "[${faction.displayName} Army]: ${voiceData.displayMessage}"
                ) 
            }
        }
    }

    fun onTapMapCoordinate(worldPos: Vector3) {
        val state = _uiState.value

        // 1. If currently casting a God Power
        state.activeCastingGodPower?.let { power ->
            castGodPower(power, worldPos)
            _uiState.update { it.copy(activeCastingGodPower = null) }
            return
        }

        // 2. Check if tapped an enemy unit / building or resource node
        val tappedEnemyUnit = _units.value.find { it.owner == Owner.AI_ENEMY && it.position.distanceTo(worldPos) < 2.0f }
        val tappedEnemyBuilding = _buildings.value.find { it.owner == Owner.AI_ENEMY && it.position.distanceTo(worldPos) < 3.0f }
        val tappedResource = _resourceNodes.value.find { it.position.distanceTo(worldPos) < 2.0f }
        val tappedPlayerUnit = _units.value.find { it.owner == Owner.PLAYER && it.position.distanceTo(worldPos) < 2.0f }
        val tappedPlayerBuilding = _buildings.value.find { it.owner == Owner.PLAYER && it.position.distanceTo(worldPos) < 3.0f }

        if (state.selectedUnitIds.isNotEmpty()) {
            // Determine command type & trigger voice line
            val faction = state.playerFaction
            val actionType = when {
                tappedEnemyUnit != null || tappedEnemyBuilding != null -> VoiceLineAction.ATTACK
                tappedResource != null -> VoiceLineAction.GATHER
                else -> VoiceLineAction.MOVE
            }
            val voiceData = VoiceLineManager.triggerVoiceLine(faction, actionType)

            val currentUnits = _units.value.toMutableList()
            val selectedCount = state.selectedUnitIds.size
            val cols = kotlin.math.ceil(kotlin.math.sqrt(selectedCount.toDouble())).toInt().coerceAtLeast(1)

            state.selectedUnitIds.forEachIndexed { index, unitId ->
                val u = currentUnits.find { it.id == unitId } ?: return@forEachIndexed
                if (index == 0) {
                    addParticle(u.position + Vector3(0f, 2.5f, 0f), "VOICE:${voiceData.phrase}", 1800L)
                }
                if (tappedEnemyUnit != null) {
                    u.targetEntityId = tappedEnemyUnit.id
                    u.targetPosition = tappedEnemyUnit.position
                    u.state = UnitState.ATTACKING
                } else if (tappedEnemyBuilding != null) {
                    u.targetEntityId = tappedEnemyBuilding.id
                    u.targetPosition = tappedEnemyBuilding.position
                    u.state = UnitState.ATTACKING
                } else if (tappedResource != null && u.type.isVillager) {
                    u.targetEntityId = tappedResource.id
                    u.targetPosition = tappedResource.position
                    u.state = UnitState.GATHERING
                } else {
                    val row = index / cols
                    val col = index % cols
                    val offsetX = (col - (cols - 1) / 2f) * 1.6f
                    val offsetZ = (row - (cols - 1) / 2f) * 1.6f
                    u.targetPosition = worldPos + Vector3(offsetX, 0f, offsetZ)
                    u.targetEntityId = null
                    u.state = UnitState.MOVING
                }
            }
            _units.value = currentUnits
            _uiState.update { 
                it.copy(logMessage = "[${faction.displayName} Order]: ${voiceData.displayMessage}")
            }
        } else {
            // Select object
            when {
                tappedPlayerUnit != null -> {
                    val faction = state.playerFaction
                    val voiceData = VoiceLineManager.triggerVoiceLine(faction, VoiceLineAction.SELECT)
                    addParticle(tappedPlayerUnit.position + Vector3(0f, 2.5f, 0f), "VOICE:${voiceData.phrase}", 1800L)
                    _uiState.update { 
                        it.copy(
                            selectedUnitIds = setOf(tappedPlayerUnit.id), 
                            selectedBuildingId = null, 
                            selectedResourceNodeId = null,
                            logMessage = "[${tappedPlayerUnit.type.displayName}]: ${voiceData.displayMessage}"
                        ) 
                    }
                }
                tappedPlayerBuilding != null -> {
                    SoundEffects.playUnitSelect()
                    _uiState.update { it.copy(selectedBuildingId = tappedPlayerBuilding.id, selectedUnitIds = emptySet(), selectedResourceNodeId = null) }
                }
                tappedResource != null -> {
                    _uiState.update { it.copy(selectedResourceNodeId = tappedResource.id, selectedUnitIds = emptySet(), selectedBuildingId = null) }
                }
                else -> {
                    // Deselect
                    _uiState.update { it.copy(selectedUnitIds = emptySet(), selectedBuildingId = null, selectedResourceNodeId = null) }
                }
            }
        }
    }

    fun selectGodPowerToCast(power: GodPower) {
        _uiState.update { it.copy(activeCastingGodPower = power, logMessage = "TAP MAP TARGET TO CAST ${power.displayName.uppercase()}!") }
    }

    private fun castGodPower(power: GodPower, targetPos: Vector3) {
        SoundEffects.playGodPowerCast()
        castGodPowerEffect(power, targetPos, isPlayer = true)
        _uiState.update {
            it.copy(
                logMessage = "DIVINE GOD POWER: ${power.displayName} CAST AT MAP LOCATION!",
                playerResources = it.playerResources.copy(favor = (it.playerResources.favor - 20).coerceAtLeast(0)),
                matchStats = it.matchStats.copy(godPowersCast = it.matchStats.godPowersCast + 1)
            )
        }
    }

    private fun castGodPowerEffect(power: GodPower, targetPos: Vector3, isPlayer: Boolean) {
        val currentUnits = _units.value.toMutableList()
        val currentBuildings = _buildings.value.toMutableList()
        val targetOwner = if (isPlayer) Owner.AI_ENEMY else Owner.PLAYER

        when (power) {
            GodPower.LIGHTNING_BOLT -> {
                // Strike strongest enemy in target radius
                currentUnits.filter { it.owner == targetOwner && it.position.distanceTo(targetPos) < 6f }.forEach {
                    it.currentHp -= 300
                }
                addParticle(targetPos, "LIGHTNING", 1500L)
            }
            GodPower.METEOR -> {
                repeat(5) {
                    val offset = Vector3((Random.nextFloat() - 0.5f) * 8f, 0f, (Random.nextFloat() - 0.5f) * 8f)
                    currentUnits.filter { it.owner == targetOwner && it.position.distanceTo(targetPos + offset) < 5f }.forEach {
                        it.currentHp -= 180
                    }
                    currentBuildings.filter { it.owner == targetOwner && it.position.distanceTo(targetPos + offset) < 5f }.forEach {
                        it.currentHp -= 350
                    }
                    addParticle(targetPos + offset, "METEOR", 2000L)
                }
            }
            GodPower.HEALING_SPRING -> {
                currentUnits.filter { it.owner == (if (isPlayer) Owner.PLAYER else Owner.AI_ENEMY) && it.position.distanceTo(targetPos) < 10f }.forEach {
                    it.currentHp = (it.currentHp + 200).coerceAtMost(it.type.maxHp)
                }
                addParticle(targetPos, "HEAL", 3000L)
            }
            GodPower.RESTORATION -> {
                currentUnits.filter { it.owner == Owner.PLAYER }.forEach {
                    it.currentHp = it.type.maxHp
                }
                addParticle(targetPos, "HEAL", 2000L)
            }
            GodPower.TORNADO -> {
                currentBuildings.filter { it.owner == targetOwner && it.position.distanceTo(targetPos) < 8f }.forEach {
                    it.currentHp -= 600
                }
                addParticle(targetPos, "EXPLOSION", 3000L)
            }
            else -> {
                addParticle(targetPos, "DUST", 1000L)
            }
        }

        _units.value = currentUnits
        _buildings.value = currentBuildings
    }

    private fun addParticle(pos: Vector3, type: String, durationMs: Long) {
        val pList = _particles.value.toMutableList()
        pList.add(ParticleEffect(nextId(), pos, type, System.currentTimeMillis(), durationMs))
        _particles.value = pList
    }

    fun trainUnitInBuilding(buildingId: Long, unitType: UnitType) {
        val currentBuildings = _buildings.value.toMutableList()
        val bld = currentBuildings.find { it.id == buildingId } ?: return

        val cost = Resources(unitType.foodCost, unitType.woodCost, unitType.goldCost, unitType.favorCost)
        if (_uiState.value.playerResources.hasEnough(cost)) {
            _uiState.update { it.copy(playerResources = it.playerResources.minus(cost)) }
            bld.trainingQueue.add(TrainingQueueItem(unitType))
            SoundEffects.playUnitCommand()
            _buildings.value = currentBuildings
        }
    }

    fun cancelTrainingQueueItem(buildingId: Long, index: Int) {
        val currentBuildings = _buildings.value.toMutableList()
        val bld = currentBuildings.find { it.id == buildingId } ?: return
        if (index in 0 until bld.trainingQueue.size) {
            val removedItem = bld.trainingQueue.removeAt(index)
            val uType = removedItem.unitType
            val refund = Resources(uType.foodCost, uType.woodCost, uType.goldCost, uType.favorCost)
            _uiState.update { it.copy(playerResources = it.playerResources.plus(refund)) }
            _buildings.value = currentBuildings
        }
    }

    fun buildStructure(buildingType: BuildingType, nearPos: Vector3) {
        val cost = Resources(buildingType.foodCost, buildingType.woodCost, buildingType.goldCost, buildingType.favorCost)
        if (_uiState.value.playerResources.hasEnough(cost)) {
            _uiState.update { it.copy(playerResources = it.playerResources.minus(cost)) }
            val currentBuildings = _buildings.value.toMutableList()
            currentBuildings.add(
                BuildingEntity(
                    id = nextId(),
                    type = buildingType,
                    owner = Owner.PLAYER,
                    position = nearPos + Vector3(4f, 0f, 4f)
                )
            )
            _buildings.value = currentBuildings
            SoundEffects.playBuildingComplete()
        }
    }

    fun triggerAgeUp() {
        val currentLevel = _uiState.value.currentAge.level
        if (currentLevel < 4) {
            val nextAge = Age.values()[currentLevel]
            val cost = Resources(nextAge.foodCost, 0, nextAge.goldCost)
            if (_uiState.value.playerResources.hasEnough(cost)) {
                _uiState.update {
                    it.copy(
                        playerResources = it.playerResources.minus(cost),
                        currentAge = nextAge,
                        availableGodPowers = it.availableGodPowers + GodPower.METEOR,
                        logMessage = "AGE ADVANCED TO ${nextAge.ageName.uppercase()}! New myth units unlocked!",
                        matchStats = it.matchStats.copy(maxAgeReached = nextAge)
                    )
                }
                SoundEffects.playAgeUp()
            }
        }
    }

    fun restartCurrentMatch() {
        val state = _uiState.value
        when {
            state.currentMission != null -> startCampaign(state.currentMission)
            state.screenState == GameScreenState.GOD_POWER_SANDBOX -> startSandbox()
            else -> startSkirmish(state.playerFaction, state.playerMajorGod)
        }
    }

    fun updateCamera(panDx: Float, panDz: Float, zoomDelta: Float, rotateDeltaDegrees: Float) {
        _uiState.update { state ->
            val cam = state.camera
            cam.targetX = (cam.targetX + panDx).coerceIn(5f, 45f)
            cam.targetY = (cam.targetY + panDz).coerceIn(5f, 45f)
            cam.zoom = (cam.zoom + zoomDelta).coerceIn(0.6f, 2.2f)
            cam.yawDegrees = (cam.yawDegrees + rotateDeltaDegrees) % 360f
            state.copy(camera = cam)
        }
    }

    fun returnToMainMenu() {
        gameLoopJob?.cancel()
        _uiState.update { GameUiState(screenState = GameScreenState.MAIN_MENU) }
    }
}
