package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.SoundEffects
import com.example.game.state.GameMatchStats
import com.example.game.state.GameUiState

/**
 * Production-Grade Modal Game-End Screen Overlay.
 * Displays when a base is destroyed or an objective is completed.
 * Features animated result banners, match statistics breakdown cards,
 * star ratings, and 'Play Again' / 'Main Menu' action controls.
 */
@Composable
fun VictoryOverlay(
    uiState: GameUiState? = null,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    GameEndModalOverlay(
        isVictory = true,
        uiState = uiState,
        onRestart = onRestart,
        onMainMenu = onMainMenu
    )
}

@Composable
fun DefeatOverlay(
    uiState: GameUiState? = null,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    GameEndModalOverlay(
        isVictory = false,
        uiState = uiState,
        onRestart = onRestart,
        onMainMenu = onMainMenu
    )
}

@Composable
fun GameEndModalOverlay(
    isVictory: Boolean,
    uiState: GameUiState?,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Overview, 1: Military, 2: Economy

    // Pulsing and scaling animations for banner badge
    val infiniteTransition = rememberInfiniteTransition(label = "EndGamePulse")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "BadgeScale"
    )

    val stats = uiState?.matchStats ?: GameMatchStats()
    val matchSeconds = uiState?.gameTimeSeconds ?: 0
    val minutes = matchSeconds / 60
    val seconds = matchSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    // Calculate star rating for victory
    val stars = when {
        !isVictory -> 1
        matchSeconds < 300 || stats.enemyUnitsKilled >= 10 -> 3
        else -> 2
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD9090D16))
            .padding(16.dp)
    ) {
        Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    colors = if (isVictory) listOf(Color(0xFFFFD700), Color(0xFFB45309))
                    else listOf(Color(0xFFEF4444), Color(0xFF991B1B))
                )
            ),
            shadowElevation = 16.dp,
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                // 1. HEADER EMBLEM & TITLE BANNER
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .scale(scalePulse)
                        .background(
                            brush = Brush.radialGradient(
                                colors = if (isVictory) listOf(Color(0xFFFFD700), Color(0xFF78350F))
                                else listOf(Color(0xFFEF4444), Color(0xFF450A0A))
                            ),
                            shape = CircleShape
                        )
                        .border(
                            2.dp,
                            if (isVictory) Color(0xFFFFF176) else Color(0xFFFCA5A5),
                            CircleShape
                        )
                ) {
                    Text(
                        text = if (isVictory) "🏆" else "💀",
                        fontSize = 38.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isVictory) "VICTORY OF THE GODS!" else "DEFEAT IN BATTLE",
                    color = if (isVictory) Color(0xFFFFD700) else Color(0xFFEF4444),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isVictory) {
                        uiState?.currentMission?.title?.let { "Completed: $it" }
                            ?: "Enemy Town Center Levels & Myth Army Vanquished!"
                    } else {
                        "Your Town Center was overrun by enemy forces."
                    },
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                // Star Rating Display
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { index ->
                        Text(
                            text = if (index < stars) "⭐" else "▪",
                            fontSize = 18.sp,
                            color = if (index < stars) Color(0xFFFFD700) else Color(0xFF475569)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. STATS BREAKDOWN TABS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("OVERVIEW", "MILITARY", "ECONOMY").forEachIndexed { idx, title ->
                        val isSelected = selectedTab == idx
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF334155) else Color.Transparent)
                                .clickable {
                                    selectedTab = idx
                                    SoundEffects.playUnitSelect()
                                }
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) Color(0xFFFFD700) else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. TAB CONTENT
                when (selectedTab) {
                    0 -> OverviewStatsTab(stats = stats, formattedTime = formattedTime)
                    1 -> MilitaryStatsTab(stats = stats)
                    2 -> EconomyStatsTab(stats = stats, maxAge = stats.maxAgeReached.ageName)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. ACTION BUTTONS
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            SoundEffects.playButtonClick()
                            onMainMenu()
                        },
                        border = BorderStroke(1.dp, Color(0xFF64748B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("MAIN MENU", color = Color(0xFFE2E8F0), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            SoundEffects.playButtonClick()
                            onRestart()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isVictory) Color(0xFF10B981) else Color(0xFF2563EB)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔄", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isVictory) "PLAY AGAIN" else "RETRY BATTLE",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewStatsTab(stats: GameMatchStats, formattedTime: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatMetricCard(
                icon = "⏱️",
                label = "Match Duration",
                value = formattedTime,
                valueColor = Color(0xFF60A5FA),
                modifier = Modifier.weight(1f)
            )
            StatMetricCard(
                icon = "⚔️",
                label = "Enemies Slain",
                value = "${stats.enemyUnitsKilled} Units",
                valueColor = Color(0xFFEF4444),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatMetricCard(
                icon = "🛡️",
                label = "Army Recruited",
                value = "${stats.playerUnitsTrained} Units",
                valueColor = Color(0xFF34D399),
                modifier = Modifier.weight(1f)
            )
            StatMetricCard(
                icon = "🏰",
                label = "Buildings Destroyed",
                value = "${stats.buildingsDestroyed}",
                valueColor = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatMetricCard(
                icon = "⚡",
                label = "God Powers Cast",
                value = "${stats.godPowersCast}",
                valueColor = Color(0xFFA855F7),
                modifier = Modifier.weight(1f)
            )
            StatMetricCard(
                icon = "🌾",
                label = "Resources Harvested",
                value = "${stats.totalResourcesGathered}",
                valueColor = Color(0xFFFDE047),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MilitaryStatsTab(stats: GameMatchStats) {
    val totalEngagements = stats.enemyUnitsKilled + stats.playerUnitsLost
    val killRatio = if (totalEngagements > 0) stats.enemyUnitsKilled.toFloat() / totalEngagements.toFloat() else 0.5f

    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "COMBAT EFFICIENCY & KILL / LOSS RATIO",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enemies Killed: ${stats.enemyUnitsKilled}", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Units Lost: ${stats.playerUnitsLost}", color = Color(0xFFF87171), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Kill / Loss Progress Bar
            LinearProgressIndicator(
                progress = { killRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF10B981),
                trackColor = Color(0xFFEF4444)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Total Army Recruited:", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                Text("${stats.playerUnitsTrained} Units", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Divine Intervention Casts:", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                Text("${stats.godPowersCast} Spells", color = Color(0xFFA855F7), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun EconomyStatsTab(stats: GameMatchStats, maxAge: String) {
    val total = stats.totalResourcesGathered.coerceAtLeast(1).toFloat()

    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("RESOURCES HARVESTED", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Highest Age: $maxAge", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            ResourceBarRow("🥩 Food", stats.totalFoodGathered, stats.totalFoodGathered / total, Color(0xFF34D399))
            Spacer(modifier = Modifier.height(6.dp))
            ResourceBarRow("🌲 Wood", stats.totalWoodGathered, stats.totalWoodGathered / total, Color(0xFF818CF8))
            Spacer(modifier = Modifier.height(6.dp))
            ResourceBarRow("⛏️ Gold", stats.totalGoldGathered, stats.totalGoldGathered / total, Color(0xFFFDE047))
            Spacer(modifier = Modifier.height(6.dp))
            ResourceBarRow("⚡ Favor", stats.totalFavorGathered, stats.totalFavorGathered / total, Color(0xFFA855F7))
        }
    }
}

@Composable
private fun ResourceBarRow(label: String, amount: Int, ratio: Float, color: Color) {
    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(label, color = Color.White, fontSize = 11.sp)
            Text("$amount", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { ratio.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF0F172A)
        )
    }
}

@Composable
private fun StatMetricCard(
    icon: String,
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(label, color = Color(0xFF94A3B8), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
            }
        }
    }
}

@Composable
fun PauseMenuDialog(
    onResume: () -> Unit,
    onMainMenu: () -> Unit
) {
    var isMuted by remember { mutableStateOf(SoundEffects.isMuted) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA000000))
    ) {
        Surface(
            color = Color(0xFF1F2937),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.padding(24.dp).widthIn(max = 300.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                Text("⏸️ GAME PAUSED", color = Color(0xFFFFD700), fontSize = 18.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Audio Sound Effects & Voices", color = Color.White, fontSize = 13.sp)
                    Switch(
                        checked = !isMuted,
                        onCheckedChange = {
                            isMuted = !it
                            com.example.game.engine.SoundEffects.isMuted = isMuted
                            com.example.game.engine.VoiceLineManager.isMuted = isMuted
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("RESUME BATTLE", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onMainMenu,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("EXIT TO MAIN MENU", color = Color.White)
                }
            }
        }
    }
}
