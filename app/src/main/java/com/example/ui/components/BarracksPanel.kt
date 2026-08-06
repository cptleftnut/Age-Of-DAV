package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.SoundEffects
import com.example.game.model.BuildingEntity
import com.example.game.model.BuildingType
import com.example.game.model.Resources
import com.example.game.model.UnitType

/**
 * Bottom-aligned dedicated Barracks Command & Training Panel for RTS gameplay.
 * Displays infantry unit selection, stats breakdown, cost validation,
 * and animated real-time training progress bars with queue controls.
 */
@Composable
fun BarracksPanel(
    building: BuildingEntity,
    playerResources: Resources,
    population: Int,
    maxPopulation: Int,
    onTrainUnit: (UnitType) -> Unit,
    onCancelQueueItem: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val trainableUnits = remember(building.type) {
        if (building.type.trainables.isNotEmpty()) {
            building.type.trainables
        } else {
            listOf(
                UnitType.HOPLITE,
                UnitType.SPEARMAN,
                UnitType.TOXOTES_ARCHER,
                UnitType.PRODROMOS_CAVALRY,
                UnitType.THROWING_AXEMAN
            )
        }
    }

    var selectedUnitForDetails by remember { mutableStateOf<UnitType?>(trainableUnits.firstOrNull()) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp),
        color = Color(0xEE0F172A),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF59E0B),
                    Color(0xFFB45309)
                )
            )
        ),
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 1. BARRACKS HEADER & HEALTH BAR
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFB45309), CircleShape)
                            .border(1.5.dp, Color(0xFFFFD700), CircleShape)
                    ) {
                        Text(building.type.iconSymbol, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = building.type.displayName.uppercase(),
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val hpRatio = building.hpPercentage()
                            val hpColor = when {
                                hpRatio > 0.6f -> Color(0xFF10B981)
                                hpRatio > 0.3f -> Color(0xFFF59E0B)
                                else -> Color(0xFFEF4444)
                            }
                            Text(
                                text = "HP: ${building.currentHp}/${building.type.maxHp}",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // HP Progress Bar
                            LinearProgressIndicator(
                                progress = { hpRatio },
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = hpColor,
                                trackColor = Color(0xFF334155)
                            )
                        }
                    }
                }

                // Active Status Badge
                val isTraining = building.trainingQueue.isNotEmpty()
                Surface(
                    color = if (isTraining) Color(0x3310B981) else Color(0x3364748B),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isTraining) Color(0xFF10B981) else Color(0xFF64748B))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (isTraining) Color(0xFF10B981) else Color(0xFF94A3B8),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isTraining) "TRAINING ARMY" else "IDLE",
                            color = if (isTraining) Color(0xFF34D399) else Color(0xFFCBD5E1),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. ACTIVE TRAINING PROGRESS BAR & QUEUE
            if (building.trainingQueue.isNotEmpty()) {
                val activeItem = building.trainingQueue.first()
                val targetProgress = (activeItem.progressMs.toFloat() / activeItem.totalTimeMs.toFloat()).coerceIn(0f, 1f)
                val animatedProgress by animateFloatAsState(
                    targetValue = targetProgress,
                    animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
                    label = "TrainingProgress"
                )

                val remainingSec = ((activeItem.totalTimeMs - activeItem.progressMs).coerceAtLeast(0) / 1000f)

                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(activeItem.unitType.iconSymbol, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Training ${activeItem.unitType.displayName}...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = String.format("%.1fs", remainingSec),
                                color = Color(0xFF60A5FA),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Animated Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animatedProgress)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF2563EB),
                                                Color(0xFF60A5FA),
                                                Color(0xFF93C5FD)
                                            )
                                        )
                                    )
                            )
                        }

                        // Queued items row
                        if (building.trainingQueue.size > 1) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Queue (${building.trainingQueue.size}):",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                building.trainingQueue.drop(1).forEachIndexed { idx, qItem ->
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .background(Color(0xFF334155), CircleShape)
                                            .clickable { onCancelQueueItem(idx + 1) }
                                    ) {
                                        Text(qItem.unitType.iconSymbol, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. INFANTRY UNIT SELECTION CARDS
            Text(
                text = "SELECT UNIT TO TRAIN:",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(trainableUnits) { unitType ->
                    val cost = Resources(unitType.foodCost, unitType.woodCost, unitType.goldCost, unitType.favorCost)
                    val canAfford = playerResources.hasEnough(cost) && (population + unitType.populationCost <= maxPopulation)
                    val isSelected = selectedUnitForDetails == unitType

                    Surface(
                        color = when {
                            isSelected -> Color(0xFF1E3A8A)
                            canAfford -> Color(0xFF1E293B)
                            else -> Color(0xFF0F172A)
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = when {
                                isSelected -> Color(0xFF60A5FA)
                                canAfford -> Color(0xFF475569)
                                else -> Color(0xFF334155)
                            }
                        ),
                        modifier = Modifier
                            .width(110.dp)
                            .clickable {
                                selectedUnitForDetails = unitType
                                SoundEffects.playUnitSelect()
                            }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(unitType.iconSymbol, fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = unitType.displayName,
                                color = if (canAfford) Color.White else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Resource Costs summary
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (unitType.foodCost > 0) {
                                    Text("🥩${unitType.foodCost}", fontSize = 9.sp, color = if (playerResources.food >= unitType.foodCost) Color(0xFF34D399) else Color(0xFFF87171))
                                }
                                if (unitType.woodCost > 0) {
                                    Text("🌲${unitType.woodCost}", fontSize = 9.sp, color = if (playerResources.wood >= unitType.woodCost) Color(0xFF34D399) else Color(0xFFF87171))
                                }
                                if (unitType.goldCost > 0) {
                                    Text("⛏️${unitType.goldCost}", fontSize = 9.sp, color = if (playerResources.gold >= unitType.goldCost) Color(0xFFFDE047) else Color(0xFFF87171))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Train Button
                            Button(
                                onClick = {
                                    if (canAfford) {
                                        onTrainUnit(unitType)
                                    }
                                },
                                enabled = canAfford,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2563EB),
                                    disabledContainerColor = Color(0xFF334155)
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.fillMaxWidth().height(28.dp)
                            ) {
                                Text(
                                    text = if (canAfford) "+ TRAIN" else "LOCKED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (canAfford) Color.White else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }

            // 4. SELECTED UNIT DETAILS & STATS BREAKDOWN
            selectedUnitForDetails?.let { unit ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xAA1E293B),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(unit.iconSymbol, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${unit.displayName}:",
                                color = Color(0xFFF59E0B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "❤️${unit.maxHp} HP  ⚔️${unit.attack} ATK  🏹${unit.range}m  💨${unit.speed}spd",
                                color = Color(0xFFE2E8F0),
                                fontSize = 10.sp
                            )
                        }

                        Text(
                            text = "👥 ${unit.populationCost} Pop",
                            color = Color(0xFFA855F7),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
