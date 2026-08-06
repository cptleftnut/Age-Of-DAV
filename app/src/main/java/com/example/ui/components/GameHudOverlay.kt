package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.game.engine.Vector3
import com.example.game.model.*
import com.example.game.state.GameUiState
import com.example.game.assets.iconRes
import kotlinx.coroutines.delay

@Composable
fun GameHudOverlay(
    uiState: GameUiState,
    units: List<UnitEntity>,
    buildings: List<BuildingEntity>,
    resourceNodes: List<ResourceNode>,
    onCastGodPower: (GodPower) -> Unit,
    onTrainUnit: (Long, UnitType) -> Unit,
    onCancelQueueItem: (Long, Int) -> Unit = { _, _ -> },
    onBuildStructure: (BuildingType) -> Unit,
    onAgeUp: () -> Unit,
    onMiniMapJump: (Vector3) -> Unit,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBuildMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. TOP RESOURCE BAR
        TopResourceBar(
            resources = uiState.playerResources,
            age = uiState.currentAge,
            population = uiState.population,
            maxPopulation = uiState.maxPopulation,
            gameTimeSeconds = uiState.gameTimeSeconds,
            logMessage = uiState.logMessage,
            onPauseClick = onPauseClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )

        // 2. GOD POWERS BAR (Top Right)
        GodPowersBar(
            powers = uiState.availableGodPowers,
            activeCastingPower = uiState.activeCastingGodPower,
            onCastGodPower = onCastGodPower,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 70.dp, end = 12.dp)
        )

        // 3. INTERACTIVE MINI-MAP (Bottom Left)
        MiniMapRadar(
            units = units,
            buildings = buildings,
            resourceNodes = resourceNodes,
            cameraTargetX = uiState.camera.targetX,
            cameraTargetZ = uiState.camera.targetY,
            onMiniMapJump = onMiniMapJump,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .size(130.dp)
        )

        // 4. ACTION & SELECTION COMMAND DECK (Bottom Right / Center)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
        ) {
            val selectedUnits = units.filter { uiState.selectedUnitIds.contains(it.id) }
            val selectedBuilding = buildings.find { it.id == uiState.selectedBuildingId }

            when {
                selectedUnits.isNotEmpty() -> {
                    UnitSelectionDeck(
                        selectedUnits = selectedUnits,
                        showBuildMenu = showBuildMenu,
                        onToggleBuildMenu = { showBuildMenu = !showBuildMenu },
                        onBuildStructure = { bType ->
                            onBuildStructure(bType)
                            showBuildMenu = false
                        }
                    )
                }
                selectedBuilding != null -> {
                    BuildingSelectionDeck(
                        building = selectedBuilding,
                        currentAge = uiState.currentAge,
                        playerResources = uiState.playerResources,
                        population = uiState.population,
                        maxPopulation = uiState.maxPopulation,
                        onTrainUnit = { uType -> onTrainUnit(selectedBuilding.id, uType) },
                        onCancelQueueItem = { idx -> onCancelQueueItem(selectedBuilding.id, idx) },
                        onAgeUp = onAgeUp
                    )
                }
                else -> {
                    // Default Idle Command Deck
                    IdleCommandDeck(
                        onToggleBuildMenu = { showBuildMenu = !showBuildMenu },
                        onAgeUp = onAgeUp,
                        showBuildMenu = showBuildMenu,
                        onBuildStructure = { bType ->
                            onBuildStructure(bType)
                            showBuildMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopResourceBar(
    resources: Resources,
    age: Age,
    population: Int,
    maxPopulation: Int,
    gameTimeSeconds: Int,
    logMessage: String,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedTime = remember(gameTimeSeconds) {
        val minutes = gameTimeSeconds / 60
        val seconds = gameTimeSeconds % 60
        String.format("%02d:%02d", minutes, seconds)
    }

    Surface(
        modifier = modifier,
        color = Color(0xEE0F172A),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFB45309),
                    Color(0xFFFFD700),
                    Color(0xFFB45309)
                )
            )
        ),
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Age Banner & Timer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color(0xFFB45309),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFD700))
                    ) {
                        Text(
                            text = "🏛️ ${age.ageName}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = Color(0xCC1F2937),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0x66FFFFFF))
                    ) {
                        Text(
                            text = "⏱️ $formattedTime",
                            color = Color(0xFFE2E8F0),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }

                // Resources Counter Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false).padding(horizontal = 6.dp)
                ) {
                    item {
                        AnimatedResourceChip("🥩", "Food", resources.food, Color(0xFFEF4444))
                    }
                    item {
                        AnimatedResourceChip("🌲", "Wood", resources.wood, Color(0xFF10B981))
                    }
                    item {
                        AnimatedResourceChip("⛏️", "Gold", resources.gold, Color(0xFFF59E0B))
                    }
                    item {
                        AnimatedResourceChip("⚡", "Favor", resources.favor, Color(0xFF3B82F6))
                    }
                    item {
                        PopulationChip(population, maxPopulation)
                    }
                }

                // Pause Button
                IconButton(
                    onClick = onPauseClick,
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color(0xCC374151), CircleShape)
                        .border(1.dp, Color(0xFFF59E0B), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause Game",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Realtime Game Ticker Log
            if (logMessage.isNotBlank()) {
                Text(
                    text = logMessage,
                    color = Color(0xFFFDE047),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AnimatedResourceChip(
    symbol: String,
    label: String,
    value: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateIntAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 400),
        label = "ResourceAnim_$label"
    )

    var prevValue by remember { mutableStateOf(value) }
    var isIncreased by remember { mutableStateOf(false) }
    var isDecreased by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (value > prevValue) {
            isIncreased = true
            isDecreased = false
        } else if (value < prevValue) {
            isDecreased = true
            isIncreased = false
        }
        prevValue = value
        delay(600)
        isIncreased = false
        isDecreased = false
    }

    val scale by animateFloatAsState(
        targetValue = if (isIncreased || isDecreased) 1.12f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "ResourceScale_$label"
    )

    val flashColor = when {
        isIncreased -> Color(0xFF22C55E)
        isDecreased -> Color(0xFFEF4444)
        else -> accentColor
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xDD111827),
        border = BorderStroke(
            width = if (isIncreased || isDecreased) 1.5.dp else 1.dp,
            color = flashColor.copy(alpha = if (isIncreased || isDecreased) 0.9f else 0.4f)
        ),
        modifier = modifier
            .scale(scale)
            .padding(vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(20.dp)
                    .background(accentColor.copy(alpha = 0.25f), CircleShape)
                    .border(1.dp, accentColor.copy(alpha = 0.7f), CircleShape)
            ) {
                Text(text = symbol, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = label.uppercase(),
                    fontSize = 7.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = animatedValue.toString(),
                    color = if (isIncreased) Color(0xFF4ADE80) else if (isDecreased) Color(0xFFF87171) else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun PopulationChip(
    population: Int,
    maxPopulation: Int,
    accentColor: Color = Color(0xFF8B5CF6)
) {
    val animatedPop by animateIntAsState(targetValue = population, animationSpec = tween(400), label = "Pop")
    val animatedMax by animateIntAsState(targetValue = maxPopulation, animationSpec = tween(400), label = "MaxPop")

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xDD111827),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(20.dp)
                    .background(accentColor.copy(alpha = 0.25f), CircleShape)
                    .border(1.dp, accentColor.copy(alpha = 0.7f), CircleShape)
            ) {
                Text(text = "👥", fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "POP",
                    fontSize = 7.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$animatedPop/$animatedMax",
                    color = if (animatedPop >= animatedMax && animatedMax > 0) Color(0xFFEF4444) else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun GodPowersBar(
    powers: List<GodPower>,
    activeCastingPower: GodPower?,
    onCastGodPower: (GodPower) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "GOD POWERS",
            color = Color(0xFFF59E0B),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        powers.forEach { power ->
            val isSelected = activeCastingPower == power
            Surface(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .clickable { onCastGodPower(power) }
                    .border(
                        width = if (isSelected) 3.dp else 1.5.dp,
                        color = if (isSelected) Color.Yellow else Color(0xFFFFD700),
                        shape = CircleShape
                    ),
                color = if (isSelected) Color(0xFFB45309) else Color(0xCC1F2937)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(power.iconRes),
                        contentDescription = power.displayName,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniMapRadar(
    units: List<UnitEntity>,
    buildings: List<BuildingEntity>,
    resourceNodes: List<ResourceNode>,
    cameraTargetX: Float,
    cameraTargetZ: Float,
    onMiniMapJump: (Vector3) -> Unit,
    modifier: Modifier = Modifier
) {
    val mapSize = 50f
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "RadarSweepAngle"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF020617))
                )
            )
            .border(
                border = BorderStroke(
                    width = 2.5.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFD97706),
                            Color(0xFFFFD700),
                            Color(0xFFB45309),
                            Color(0xFFFFD700),
                            Color(0xFFD97706)
                        )
                    )
                ),
                shape = CircleShape
            )
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val targetX = ((offset.x / size.width) * mapSize).coerceIn(0f, mapSize)
                    val targetZ = ((offset.y / size.height) * mapSize).coerceIn(0f, mapSize)
                    onMiniMapJump(Vector3(targetX, 0f, targetZ))
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val targetX = ((change.position.x / size.width) * mapSize).coerceIn(0f, mapSize)
                    val targetZ = ((change.position.y / size.height) * mapSize).coerceIn(0f, mapSize)
                    onMiniMapJump(Vector3(targetX, 0f, targetZ))
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val scaleX = w / mapSize
            val scaleY = h / mapSize
            val centerOffset = Offset(w / 2f, h / 2f)

            // 1. Grid & Compass Radial Rings
            drawCircle(
                color = Color(0x3338BDF8),
                radius = w * 0.45f,
                style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
            )
            drawCircle(
                color = Color(0x2238BDF8),
                radius = w * 0.25f,
                style = Stroke(width = 1f)
            )

            // Axis Crosshairs
            drawLine(Color(0x22FFFFFF), Offset(w / 2f, 0f), Offset(w / 2f, h), strokeWidth = 1f)
            drawLine(Color(0x22FFFFFF), Offset(0f, h / 2f), Offset(w, h / 2f), strokeWidth = 1f)

            // 2. Rotating Tactical Radar Sweep Beam
            val sweepRad = Math.toRadians(sweepAngle.toDouble())
            val endX = centerOffset.x + (w * 0.5f) * kotlin.math.cos(sweepRad).toFloat()
            val endY = centerOffset.y + (h * 0.5f) * kotlin.math.sin(sweepRad).toFloat()
            drawLine(
                color = Color(0x6610B981),
                start = centerOffset,
                end = Offset(endX, endY),
                strokeWidth = 2f
            )

            // 3. Resource Nodes
            resourceNodes.forEach { node ->
                val px = node.position.x * scaleX
                val py = node.position.z * scaleY
                when (node.type) {
                    ResourceNodeType.GOLD_MINE -> {
                        drawCircle(Color(0xFFFFD700), radius = 3.5f, center = Offset(px, py))
                        drawCircle(Color.White, radius = 1.5f, center = Offset(px, py))
                    }
                    ResourceNodeType.FOREST_TREE -> {
                        drawCircle(Color(0xFF22C55E), radius = 2.5f, center = Offset(px, py))
                    }
                    ResourceNodeType.BERRY_BUSH, ResourceNodeType.HUNT_ANIMAL -> {
                        drawCircle(Color(0xFFF59E0B), radius = 2.5f, center = Offset(px, py))
                    }
                    ResourceNodeType.RELIC -> {
                        drawCircle(Color(0xFF00BCD4), radius = 3.5f, center = Offset(px, py))
                        drawCircle(Color.White, radius = 1.5f, center = Offset(px, py))
                    }
                }
            }

            // 4. Buildings
            buildings.forEach { bld ->
                val px = bld.position.x * scaleX
                val py = bld.position.z * scaleY
                val isPlayer = bld.owner == Owner.PLAYER
                val bColor = if (isPlayer) Color(0xFF3B82F6) else Color(0xFFEF4444)
                val bSize = if (bld.type == BuildingType.TOWN_CENTER) 8f else 6f

                drawRect(
                    color = bColor,
                    topLeft = Offset(px - bSize / 2f, py - bSize / 2f),
                    size = Size(bSize, bSize)
                )
                if (bld.type == BuildingType.TOWN_CENTER) {
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(px - 1.5f, py - 1.5f),
                        size = Size(3f, 3f)
                    )
                }
            }

            // 5. Real-Time Unit Positions
            units.forEach { unit ->
                val px = unit.position.x * scaleX
                val py = unit.position.z * scaleY
                val isPlayer = unit.owner == Owner.PLAYER

                when {
                    unit.type.isHero || unit.type.isMythUnit -> {
                        // Myth / Hero Unit Highlighted Icon
                        val heroColor = if (isPlayer) Color(0xFFFFD700) else Color(0xFFFF4444)
                        drawCircle(heroColor, radius = 4.5f, center = Offset(px, py))
                        drawCircle(Color.White, radius = 2f, center = Offset(px, py))
                    }
                    else -> {
                        // Standard Unit Dot
                        val unitColor = if (isPlayer) Color(0xFF06B6D4) else Color(0xFFDC2626)
                        drawCircle(unitColor, radius = 2.5f, center = Offset(px, py))
                    }
                }
            }

            // 6. Camera Viewport Field-of-View Rect
            val camPx = cameraTargetX * scaleX
            val camPy = cameraTargetZ * scaleY
            val boxW = 28f
            val boxH = 20f

            drawRect(
                color = Color(0xFFFFD700),
                topLeft = Offset(camPx - boxW / 2f, camPy - boxH / 2f),
                size = Size(boxW, boxH),
                style = Stroke(width = 1.8f)
            )

            // Center Camera Reticle Dot
            drawCircle(Color(0xFFFFD700), radius = 2f, center = Offset(camPx, camPy))

            // 7. Cardinal Direction Compass Marker (N)
            drawContext.canvas.nativeCanvas.drawText(
                "N",
                w / 2f,
                14f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.rgb(255, 215, 0)
                    textSize = 18f
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
private fun UnitSelectionDeck(
    selectedUnits: List<UnitEntity>,
    showBuildMenu: Boolean,
    onToggleBuildMenu: () -> Unit,
    onBuildStructure: (BuildingType) -> Unit
) {
    val firstUnit = selectedUnits.first()

    Surface(
        color = Color(0xEE1F2937),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF3B82F6)),
        modifier = Modifier.widthIn(max = 320.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(firstUnit.type.iconRes),
                        contentDescription = firstUnit.type.displayName,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (selectedUnits.size > 1) "${selectedUnits.size} Units Selected" else firstUnit.type.displayName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "HP: ${firstUnit.currentHp}/${firstUnit.type.maxHp} | ATK: ${firstUnit.type.attack}",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (firstUnit.type.isVillager) {
                    Button(
                        onClick = onToggleBuildMenu,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🏗️ Build Structure", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { /* Attack Stance */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("⚔️ Attack", fontSize = 12.sp, color = Color.White)
                    }
                }
            }

            // Build Sub-menu
            AnimatedVisibility(visible = showBuildMenu) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    items(BuildingType.values()) { bType ->
                        Surface(
                            color = Color(0xFF374151),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { onBuildStructure(bType) }
                                .padding(4.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(6.dp)
                            ) {
                                Image(
                                    painter = painterResource(bType.iconRes),
                                    contentDescription = bType.displayName,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(bType.displayName, color = Color.White, fontSize = 10.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(painter = painterResource(R.drawable.ic_resource_wood), contentDescription = "Wood", modifier = Modifier.size(9.dp))
                                    Text("${bType.woodCost}", color = Color.Yellow, fontSize = 9.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Image(painter = painterResource(R.drawable.ic_resource_gold), contentDescription = "Gold", modifier = Modifier.size(9.dp))
                                    Text("${bType.goldCost}", color = Color.Yellow, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BuildingSelectionDeck(
    building: BuildingEntity,
    currentAge: Age,
    playerResources: Resources,
    population: Int,
    maxPopulation: Int,
    onTrainUnit: (UnitType) -> Unit,
    onCancelQueueItem: (Int) -> Unit,
    onAgeUp: () -> Unit
) {
    if (building.type == BuildingType.BARRACKS) {
        BarracksPanel(
            building = building,
            playerResources = playerResources,
            population = population,
            maxPopulation = maxPopulation,
            onTrainUnit = onTrainUnit,
            onCancelQueueItem = onCancelQueueItem
        )
    } else {
        Surface(
            color = Color(0xEE1F2937),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B)),
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(building.type.iconRes),
                        contentDescription = building.type.displayName,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(building.type.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("HP: ${building.currentHp}/${building.type.maxHp}", color = Color.LightGray, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Active Training Queue Bar if present
                if (building.trainingQueue.isNotEmpty()) {
                    val activeItem = building.trainingQueue.first()
                    val targetProgress = (activeItem.progressMs.toFloat() / activeItem.totalTimeMs.toFloat()).coerceIn(0f, 1f)
                    val animatedProgress by animateFloatAsState(
                        targetValue = targetProgress,
                        animationSpec = tween(durationMillis = 100),
                        label = "BuildingTrainProgress"
                    )

                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text(
                                text = "Training: ${activeItem.unitType.displayName} (${(targetProgress * 100).toInt()}%)",
                                color = Color.Cyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF3B82F6),
                                trackColor = Color(0xFF334155)
                            )
                        }
                    }
                }

                // Trainable Units
                if (building.type.trainables.isNotEmpty()) {
                    Text("Train Units:", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        items(building.type.trainables) { uType ->
                            Button(
                                onClick = { onTrainUnit(uType) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                    painter = painterResource(uType.iconRes),
                                    contentDescription = uType.displayName,
                                    modifier = Modifier.size(14.dp)
                                )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text(uType.displayName, fontSize = 11.sp, color = Color.White)
                                        Text("🥩${uType.foodCost} ⛏️${uType.goldCost}", fontSize = 9.sp, color = Color.Yellow)
                                    }
                                }
                            }
                        }
                    }
                }

                // Age Up Button at Town Center
                if (building.type == BuildingType.TOWN_CENTER && currentAge.level < 4) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = onAgeUp,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("⚡ ADVANCE AGE (${Age.values()[currentAge.level].ageName})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun IdleCommandDeck(
    onToggleBuildMenu: () -> Unit,
    onAgeUp: () -> Unit,
    showBuildMenu: Boolean,
    onBuildStructure: (BuildingType) -> Unit
) {
    Surface(
        color = Color(0xDD1F2937),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Button(
                onClick = onToggleBuildMenu,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
            ) {
                Text("🏗️ Build Menu", color = Color.White, fontWeight = FontWeight.Bold)
            }

            AnimatedVisibility(visible = showBuildMenu) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    items(BuildingType.values()) { bType ->
                        Surface(
                            color = Color(0xFF374151),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { onBuildStructure(bType) }
                                .padding(4.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(6.dp)
                            ) {
                                Image(
                                    painter = painterResource(bType.iconRes),
                                    contentDescription = bType.displayName,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(bType.displayName, color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
