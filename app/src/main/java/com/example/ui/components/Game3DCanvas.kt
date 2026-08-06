package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.game.engine.Camera3D
import com.example.game.engine.Vector3
import com.example.game.model.*
import com.example.game.state.ParticleEffect

@Composable
fun Game3DCanvas(
    camera: Camera3D,
    terrainMap: TerrainMap,
    units: List<UnitEntity>,
    buildings: List<BuildingEntity>,
    resourceNodes: List<ResourceNode>,
    particles: List<ParticleEffect>,
    selectedUnitIds: Set<Long>,
    selectedBuildingId: Long?,
    selectedResourceNodeId: Long?,
    onMapTap: (Vector3) -> Unit,
    onBoxSelect: (Vector3, Vector3) -> Unit,
    onCameraUpdate: (Float, Float, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var dragStartOffset by remember { mutableStateOf<Offset?>(null) }
    var currentDragOffset by remember { mutableStateOf<Offset?>(null) }

    // Continuous rotation and pulse animations for selection rings & targeting indicators
    val infiniteTransition = rememberInfiniteTransition(label = "SelectionPulse")
    val animPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "RingRotate"
    )
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "AlphaPulse"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, rotation ->
                    if (zoom != 1.0f || rotation != 0f || pan != Offset.Zero) {
                        val panX = -pan.x * 0.04f / camera.zoom
                        val panZ = pan.y * 0.04f / camera.zoom
                        val zoomDelta = (zoom - 1.0f) * 0.8f
                        onCameraUpdate(panX, panZ, zoomDelta, rotation)
                    }
                }
            }
            .pointerInput(Unit) {
                // Drag selection box & single tap detection
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: continue
                        if (change.changedToDown()) {
                            dragStartOffset = change.position
                            currentDragOffset = change.position
                        } else if (change.pressed && dragStartOffset != null) {
                            currentDragOffset = change.position
                        } else if (change.changedToUp() && dragStartOffset != null) {
                            val start = dragStartOffset!!
                            val end = change.position
                            val dist = (end - start).getDistance()

                            if (dist < 20f) {
                                // Tap event! Convert screen tap to 3D world ground position
                                val worldPos = camera.screenToWorldGround(
                                    end.x,
                                    end.y,
                                    size.width.toFloat(),
                                    size.height.toFloat()
                                )
                                onMapTap(worldPos)
                            } else if (dist >= 20f) {
                                // Box selection
                                val p1 = camera.screenToWorldGround(start.x, start.y, size.width.toFloat(), size.height.toFloat())
                                val p2 = camera.screenToWorldGround(end.x, end.y, size.width.toFloat(), size.height.toFloat())
                                val minX = minOf(p1.x, p2.x)
                                val maxX = maxOf(p1.x, p2.x)
                                val minZ = minOf(p1.z, p2.z)
                                val maxZ = maxOf(p1.z, p2.z)
                                onBoxSelect(Vector3(minX, 0f, minZ), Vector3(maxX, 0f, maxZ))
                            }
                            dragStartOffset = null
                            currentDragOffset = null
                        }
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height

        // Calculate live enclosed units during active drag selection box
        val isDraggingBox = dragStartOffset != null && currentDragOffset != null && (currentDragOffset!! - dragStartOffset!!).getDistance() > 15f
        val previewSelectedIds = if (isDraggingBox) {
            val p1 = camera.screenToWorldGround(dragStartOffset!!.x, dragStartOffset!!.y, w, h)
            val p2 = camera.screenToWorldGround(currentDragOffset!!.x, currentDragOffset!!.y, w, h)
            val minX = minOf(p1.x, p2.x)
            val maxX = maxOf(p1.x, p2.x)
            val minZ = minOf(p1.z, p2.z)
            val maxZ = maxOf(p1.z, p2.z)

            units.filter {
                it.owner == Owner.PLAYER &&
                        it.position.x in minX..maxX &&
                        it.position.z in minZ..maxZ
            }.map { it.id }.toSet()
        } else {
            emptySet()
        }

        // 1. Draw 3D Terrain Mesh Grid
        drawTerrainGrid(camera, terrainMap, w, h)

        // 2. Draw 3D Resource Nodes
        resourceNodes.forEach { node ->
            drawResourceNode3D(camera, node, node.id == selectedResourceNodeId, w, h)
        }

        // 3. Draw 3D Buildings
        buildings.forEach { bld ->
            drawBuilding3D(camera, bld, bld.id == selectedBuildingId, w, h)
        }

        // 4. Draw 3D Units, Selection Indicators & Target Vectors
        units.forEach { unit ->
            val isSelected = selectedUnitIds.contains(unit.id)
            val isPreviewSelected = previewSelectedIds.contains(unit.id)
            drawUnit3D(
                camera = camera,
                unit = unit,
                isSelected = isSelected,
                isPreviewSelected = isPreviewSelected,
                animPulse = animPulse,
                alphaPulse = alphaPulse,
                screenWidth = w,
                screenHeight = h
            )
        }

        // 5. Draw 3D Particle Effects (Lightning, Meteors, Healing Spring)
        particles.forEach { p ->
            drawParticle3D(camera, p, w, h)
        }

        // 6. Draw Tactical Selection Box Overlay (when dragging across screen)
        if (isDraggingBox && dragStartOffset != null && currentDragOffset != null) {
            drawSelectionBoxOverlay(
                start = dragStartOffset!!,
                end = currentDragOffset!!,
                enclosedCount = previewSelectedIds.size,
                animPulse = animPulse
            )
        }
    }
}

private fun DrawScope.drawTerrainGrid(
    camera: Camera3D,
    terrainMap: TerrainMap,
    screenWidth: Float,
    screenHeight: Float
) {
    val tileSize = 2
    for (x in 0 until terrainMap.width step tileSize) {
        for (z in 0 until terrainMap.height step tileSize) {
            val fog = terrainMap.fog[x][z]
            val elevation = terrainMap.grid[x][z].elevation

            val p1 = camera.project(Vector3(x.toFloat(), elevation, z.toFloat()), screenWidth, screenHeight)
            val p2 = camera.project(Vector3((x + tileSize).toFloat(), elevation, z.toFloat()), screenWidth, screenHeight)
            val p3 = camera.project(Vector3((x + tileSize).toFloat(), elevation, (z + tileSize).toFloat()), screenWidth, screenHeight)
            val p4 = camera.project(Vector3(x.toFloat(), elevation, (z + tileSize).toFloat()), screenWidth, screenHeight)

            if (p1.isVisible || p3.isVisible) {
                val path = Path().apply {
                    moveTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                    lineTo(p4.x, p4.y)
                    close()
                }

                val baseColor = when (terrainMap.grid[x][z].type) {
                    TerrainType.GRASS -> Color(0xFF388E3C)
                    TerrainType.DESERT_SAND -> Color(0xFFD7CCC8)
                    TerrainType.SNOW -> Color(0xFFECEFF1)
                    TerrainType.WATER -> Color(0xFF1976D2)
                    TerrainType.ROCK -> Color(0xFF607D8B)
                }

                val finalColor = when (fog) {
                    FogState.VISIBLE -> baseColor
                    FogState.FOGGED -> baseColor.copy(alpha = 0.55f)
                    FogState.UNEXPLORED -> Color(0xFF1A1A1A)
                }

                drawPath(path, finalColor)
                drawPath(path, Color(0x22000000), style = Stroke(width = 1f))
            }
        }
    }
}

private fun DrawScope.drawResourceNode3D(
    camera: Camera3D,
    node: ResourceNode,
    isSelected: Boolean,
    screenWidth: Float,
    screenHeight: Float
) {
    val sp = camera.project(node.position, screenWidth, screenHeight)
    if (!sp.isVisible) return

    val scale = 14f * camera.zoom

    // Draw Gold / Wood / Berry 3D Geometry
    when (node.type) {
        ResourceNodeType.GOLD_MINE -> {
            drawCircle(Color(0xFFFFD700), radius = scale * 1.2f, center = Offset(sp.x, sp.y))
            drawCircle(Color(0xFFFFA000), radius = scale * 0.7f, center = Offset(sp.x - scale * 0.3f, sp.y - scale * 0.3f))
        }
        ResourceNodeType.FOREST_TREE -> {
            val topP = camera.project(node.position + Vector3(0f, 3f, 0f), screenWidth, screenHeight)
            drawLine(Color(0xFF5D4037), Offset(sp.x, sp.y), Offset(topP.x, topP.y), strokeWidth = scale * 0.4f)
            drawCircle(Color(0xFF1B5E20), radius = scale * 1.3f, center = Offset(topP.x, topP.y))
        }
        ResourceNodeType.BERRY_BUSH -> {
            drawCircle(Color(0xFF8E24AA), radius = scale, center = Offset(sp.x, sp.y))
        }
        ResourceNodeType.RELIC -> {
            drawCircle(Color(0xFF00E5FF), radius = scale * 1.1f, center = Offset(sp.x, sp.y))
        }
        else -> {
            drawCircle(Color(0xFF8D6E63), radius = scale, center = Offset(sp.x, sp.y))
        }
    }

    if (isSelected) {
        drawCircle(Color.Cyan, radius = scale * 1.8f, center = Offset(sp.x, sp.y), style = Stroke(width = 3f))
    }
}

private fun DrawScope.drawBuilding3D(
    camera: Camera3D,
    bld: BuildingEntity,
    isSelected: Boolean,
    screenWidth: Float,
    screenHeight: Float
) {
    val baseP = camera.project(bld.position, screenWidth, screenHeight)
    val topP = camera.project(bld.position + Vector3(0f, 4f, 0f), screenWidth, screenHeight)
    if (!baseP.isVisible) return

    val sizeScale = 22f * camera.zoom
    val color = if (bld.owner == Owner.PLAYER) Color(0xFF1E88E5) else Color(0xFFE53935)

    // Shadow
    drawOval(Color(0x55000000), topLeft = Offset(baseP.x - sizeScale * 1.4f, baseP.y - sizeScale * 0.6f), size = Size(sizeScale * 2.8f, sizeScale * 1.2f))

    // Building Base Cube
    val path = Path().apply {
        moveTo(baseP.x - sizeScale, baseP.y)
        lineTo(baseP.x + sizeScale, baseP.y)
        lineTo(topP.x + sizeScale * 0.8f, topP.y)
        lineTo(topP.x - sizeScale * 0.8f, topP.y)
        close()
    }
    drawPath(path, color)
    drawPath(path, Color.White, style = Stroke(width = 2f))

    if (isSelected) {
        drawCircle(Color.Yellow, radius = sizeScale * 1.8f, center = Offset(baseP.x, baseP.y), style = Stroke(width = 3.5f))
    }

    // Health bar above structure
    val hpPercent = bld.hpPercentage()
    val barWidth = sizeScale * 2.2f
    val barHeight = 8f
    val barTop = topP.y - 20f

    drawRect(Color.DarkGray, topLeft = Offset(topP.x - barWidth / 2f, barTop), size = Size(barWidth, barHeight))
    drawRect(if (bld.owner == Owner.PLAYER) Color.Green else Color.Red, topLeft = Offset(topP.x - barWidth / 2f, barTop), size = Size(barWidth * hpPercent, barHeight))
}

private fun DrawScope.drawUnit3D(
    camera: Camera3D,
    unit: UnitEntity,
    isSelected: Boolean,
    isPreviewSelected: Boolean,
    animPulse: Float,
    alphaPulse: Float,
    screenWidth: Float,
    screenHeight: Float
) {
    val baseP = camera.project(unit.position, screenWidth, screenHeight)
    val headP = camera.project(unit.position + Vector3(0f, 2.2f, 0f), screenWidth, screenHeight)
    if (!baseP.isVisible) return

    val unitRadius = if (unit.type.isMythUnit) 16f * camera.zoom else 10f * camera.zoom
    val color = when (unit.owner) {
        Owner.PLAYER -> Color(0xFF2196F3)
        Owner.AI_ENEMY -> Color(0xFFF44336)
        Owner.NEUTRAL -> Color.Gray
    }

    // 1. DUAL SELECTION RING SYSTEM UNDER UNIT FEET
    if (isSelected || isPreviewSelected) {
        val ringColor = when {
            isPreviewSelected -> Color(0xFFFDE047) // Bright Yellow for box select preview
            unit.owner == Owner.PLAYER && unit.type.isHero -> Color(0xFFFFD700)
            unit.owner == Owner.PLAYER -> Color(0xFF10B981) // Emerald Green for active selection
            else -> Color(0xFFEF4444)
        }

        // Inner Solid Glow Ring
        drawOval(
            ringColor.copy(alpha = alphaPulse * 0.5f),
            topLeft = Offset(baseP.x - unitRadius * 1.5f, baseP.y - unitRadius * 0.75f),
            size = Size(unitRadius * 3.0f, unitRadius * 1.5f),
            style = Stroke(width = 2f)
        )

        // Outer Rotating Dashed Ring
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), animPulse)
        drawOval(
            ringColor.copy(alpha = alphaPulse),
            topLeft = Offset(baseP.x - unitRadius * 1.9f, baseP.y - unitRadius * 0.95f),
            size = Size(unitRadius * 3.8f, unitRadius * 1.9f),
            style = Stroke(width = 3f, pathEffect = dashEffect)
        )
    }

    // 2. COMMAND TARGETING VECTOR LINE & RETICLE
    if (isSelected && unit.targetPosition != null) {
        val targetP = camera.project(unit.targetPosition!!, screenWidth, screenHeight)
        val targetColor = when (unit.state) {
            UnitState.ATTACKING -> Color(0xFFEF4444) // Red vector for attack command
            UnitState.GATHERING -> Color(0xFFF59E0B) // Amber vector for resource gather
            else -> Color(0xFF10B981)               // Green vector for move command
        }

        // Dashed Command Movement / Attack Line
        val pathDash = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), animPulse)
        drawLine(
            color = targetColor.copy(alpha = 0.85f),
            start = Offset(baseP.x, baseP.y),
            end = Offset(targetP.x, targetP.y),
            strokeWidth = 2.5f,
            pathEffect = pathDash
        )

        // Ground Target Reticle Ring at Target Position
        drawOval(
            color = targetColor.copy(alpha = alphaPulse),
            topLeft = Offset(targetP.x - 16f, targetP.y - 8f),
            size = Size(32f, 16f),
            style = Stroke(width = 2.5f)
        )
        // Crosshair reticle ticks
        drawLine(targetColor, Offset(targetP.x - 22f, targetP.y), Offset(targetP.x - 12f, targetP.y), strokeWidth = 2f)
        drawLine(targetColor, Offset(targetP.x + 12f, targetP.y), Offset(targetP.x + 22f, targetP.y), strokeWidth = 2f)
        drawLine(targetColor, Offset(targetP.x, targetP.y - 12f), Offset(targetP.x, targetP.y - 6f), strokeWidth = 2f)
        drawLine(targetColor, Offset(targetP.x, targetP.y + 6f), Offset(targetP.x, targetP.y + 12f), strokeWidth = 2f)
    }

    // Unit Ground Shadow
    drawOval(
        Color(0x66000000),
        topLeft = Offset(baseP.x - unitRadius * 1.2f, baseP.y - unitRadius * 0.5f),
        size = Size(unitRadius * 2.4f, unitRadius * 1.0f)
    )

    // Unit 3D Body Line & Head Circle
    drawLine(color, Offset(baseP.x, baseP.y), Offset(headP.x, headP.y), strokeWidth = unitRadius * 0.9f)
    drawCircle(if (unit.type.isHero) Color(0xFFFFD700) else color, radius = unitRadius * 0.8f, center = Offset(headP.x, headP.y))

    // Special Myth Horns / Wings / Symbol
    if (unit.type == UnitType.MINOTAUR) {
        drawLine(Color.White, Offset(headP.x - 8f, headP.y - 4f), Offset(headP.x - 14f, headP.y - 12f), strokeWidth = 3f)
        drawLine(Color.White, Offset(headP.x + 8f, headP.y - 4f), Offset(headP.x + 14f, headP.y - 12f), strokeWidth = 3f)
    } else if (unit.type == UnitType.VALKYRIE) {
        drawCircle(Color.Cyan, radius = unitRadius * 1.1f, center = Offset(headP.x, headP.y), style = Stroke(width = 2f))
    }

    // Unit Health Bar
    val hpPct = unit.hpPercentage()
    val barW = unitRadius * 2.5f
    val barH = 5f
    drawRect(Color.DarkGray, topLeft = Offset(headP.x - barW / 2f, headP.y - 14f), size = Size(barW, barH))
    drawRect(if (unit.owner == Owner.PLAYER) Color.Green else Color.Red, topLeft = Offset(headP.x - barW / 2f, headP.y - 14f), size = Size(barW * hpPct, barH))
}

private fun DrawScope.drawSelectionBoxOverlay(
    start: Offset,
    end: Offset,
    enclosedCount: Int,
    animPulse: Float
) {
    val left = minOf(start.x, end.x)
    val top = minOf(start.y, end.y)
    val right = maxOf(start.x, end.x)
    val bottom = maxOf(start.y, end.y)
    val boxWidth = right - left
    val boxHeight = bottom - top

    if (boxWidth < 5f || boxHeight < 5f) return

    val boxColor = Color(0xFF00FF88)
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), animPulse)

    // 1. Semi-transparent gradient fill
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                boxColor.copy(alpha = 0.15f),
                boxColor.copy(alpha = 0.05f)
            ),
            startY = top,
            endY = bottom
        ),
        topLeft = Offset(left, top),
        size = Size(boxWidth, boxHeight)
    )

    // 2. Dashed tactical border
    drawRect(
        color = boxColor.copy(alpha = 0.85f),
        topLeft = Offset(left, top),
        size = Size(boxWidth, boxHeight),
        style = Stroke(width = 2.dp.toPx(), pathEffect = dashEffect)
    )

    // 3. Tactical RTS Corner Brackets (L-shaped notches at 4 corners)
    val bracketLen = minOf(20.dp.toPx(), minOf(boxWidth, boxHeight) * 0.3f)
    val bracketStroke = 3.dp.toPx()

    // Top-Left Corner
    drawLine(boxColor, Offset(left, top), Offset(left + bracketLen, top), strokeWidth = bracketStroke)
    drawLine(boxColor, Offset(left, top), Offset(left, top + bracketLen), strokeWidth = bracketStroke)

    // Top-Right Corner
    drawLine(boxColor, Offset(right, top), Offset(right - bracketLen, top), strokeWidth = bracketStroke)
    drawLine(boxColor, Offset(right, top), Offset(right, top + bracketLen), strokeWidth = bracketStroke)

    // Bottom-Left Corner
    drawLine(boxColor, Offset(left, bottom), Offset(left + bracketLen, bottom), strokeWidth = bracketStroke)
    drawLine(boxColor, Offset(left, bottom), Offset(left, bottom - bracketLen), strokeWidth = bracketStroke)

    // Bottom-Right Corner
    drawLine(boxColor, Offset(right, bottom), Offset(right - bracketLen, bottom), strokeWidth = bracketStroke)
    drawLine(boxColor, Offset(right, bottom), Offset(right, bottom - bracketLen), strokeWidth = bracketStroke)

    // 4. Header Badge showing enclosed unit count
    val badgeTop = (top - 24.dp.toPx()).coerceAtLeast(10f)
    val badgeWidth = 140.dp.toPx()
    val badgeHeight = 20.dp.toPx()

    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(left, badgeTop),
        size = Size(badgeWidth, badgeHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
    )
    drawRoundRect(
        color = boxColor,
        topLeft = Offset(left, badgeTop),
        size = Size(badgeWidth, badgeHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx()),
        style = Stroke(width = 1.dp.toPx())
    )
}

private val voiceTextPaint = android.graphics.Paint().apply {
    color = android.graphics.Color.WHITE
    textSize = 30f
    isAntiAlias = true
    typeface = android.graphics.Typeface.DEFAULT_BOLD
    textAlign = android.graphics.Paint.Align.CENTER
}

private fun DrawScope.drawParticle3D(
    camera: Camera3D,
    particle: ParticleEffect,
    screenWidth: Float,
    screenHeight: Float
) {
    val sp = camera.project(particle.pos, screenWidth, screenHeight)
    if (!sp.isVisible) return

    if (particle.type.startsWith("VOICE:")) {
        val voiceText = particle.type.removePrefix("VOICE:")
        val textWidth = voiceTextPaint.measureText(voiceText)
        val boxWidth = textWidth + 32f
        val boxHeight = 42f
        val bubbleX = sp.x - boxWidth / 2f
        val bubbleY = sp.y - 52f

        // Draw speech bubble background
        drawRoundRect(
            color = Color(0xEE0F172A),
            topLeft = Offset(bubbleX, bubbleY),
            size = Size(boxWidth, boxHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = Color(0xFFFFD700),
            topLeft = Offset(bubbleX, bubbleY),
            size = Size(boxWidth, boxHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
            style = Stroke(width = 2f)
        )

        // Draw voice phrase text
        drawContext.canvas.nativeCanvas.drawText(
            voiceText,
            sp.x,
            bubbleY + 29f,
            voiceTextPaint
        )
        return
    }

    when (particle.type) {
        "LIGHTNING" -> {
            val topP = camera.project(particle.pos + Vector3(0f, 15f, 0f), screenWidth, screenHeight)
            drawLine(Color(0xFFFFF59D), Offset(topP.x, topP.y), Offset(sp.x, sp.y), strokeWidth = 8f)
            drawCircle(Color.White, radius = 25f, center = Offset(sp.x, sp.y))
        }
        "METEOR" -> {
            drawCircle(Color(0xFFFF3D00), radius = 30f, center = Offset(sp.x, sp.y))
            drawCircle(Color(0xFFFFC107), radius = 18f, center = Offset(sp.x, sp.y))
        }
        "HEAL" -> {
            drawCircle(Color(0xFF69F0AE), radius = 45f, center = Offset(sp.x, sp.y), style = Stroke(width = 4f))
        }
        else -> {
            drawCircle(Color.LightGray.copy(alpha = 0.5f), radius = 20f, center = Offset(sp.x, sp.y))
        }
    }
}
