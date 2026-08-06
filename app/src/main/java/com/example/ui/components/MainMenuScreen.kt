package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.engine.SoundEffects
import com.example.game.model.Faction
import com.example.game.model.MajorGod
import com.example.game.state.CampaignData
import com.example.game.state.CampaignMission

@Composable
fun MainMenuScreen(
    onStartSkirmish: (Faction, MajorGod, Faction) -> Unit,
    onStartCampaignMission: (CampaignMission) -> Unit,
    onStartSandbox: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("MAIN") } // MAIN, FACTION_PICK, CAMPAIGN_PICK, GUIDE
    var selectedFaction by remember { mutableStateOf(Faction.GREEK) }
    var selectedGod by remember { mutableStateOf(MajorGod.ZEUS) }
    var enemyFaction by remember { mutableStateOf(Faction.EGYPTIAN) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF311010)
                    )
                )
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Epic Title Banner
            Text(
                text = "⚡ AGE OF MYTHOLOGY 3D ⚡",
                color = Color(0xFFFFD700),
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = "AUTONOMOUS MOBILE RTS EXPERIENCE",
                color = Color(0xFFF59E0B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (selectedTab) {
                "MAIN" -> {
                    MainMenuOptions(
                        onSkirmishClick = { selectedTab = "FACTION_PICK" },
                        onCampaignClick = { selectedTab = "CAMPAIGN_PICK" },
                        onSandboxClick = onStartSandbox,
                        onGuideClick = { selectedTab = "GUIDE" }
                    )
                }
                "FACTION_PICK" -> {
                    FactionSetupScreen(
                        selectedFaction = selectedFaction,
                        selectedGod = selectedGod,
                        enemyFaction = enemyFaction,
                        onFactionSelect = { f ->
                            selectedFaction = f
                            selectedGod = MajorGod.values().first { it.faction == f }
                        },
                        onGodSelect = { g -> selectedGod = g },
                        onEnemyFactionSelect = { ef -> enemyFaction = ef },
                        onStartGame = {
                            SoundEffects.playVictory()
                            onStartSkirmish(selectedFaction, selectedGod, enemyFaction)
                        },
                        onBack = { selectedTab = "MAIN" }
                    )
                }
                "CAMPAIGN_PICK" -> {
                    CampaignSelectScreen(
                        onMissionSelect = { mission ->
                            SoundEffects.playVictory()
                            onStartCampaignMission(mission)
                        },
                        onBack = { selectedTab = "MAIN" }
                    )
                }
                "GUIDE" -> {
                    HowToPlayGuide(onBack = { selectedTab = "MAIN" })
                }
            }
        }
    }
}

@Composable
private fun MainMenuOptions(
    onSkirmishClick: () -> Unit,
    onCampaignClick: () -> Unit,
    onSandboxClick: () -> Unit,
    onGuideClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(max = 320.dp)
    ) {
        MenuButton(
            title = "⚔️ SKIRMISH VS AI",
            subtitle = "Custom random battle with gods & titans",
            onClick = onSkirmishClick,
            color = Color(0xFFD97706)
        )

        MenuButton(
            title = "📜 CAMPAIGN MODE",
            subtitle = "Fall of the Trident story saga",
            onClick = onCampaignClick,
            color = Color(0xFF2563EB)
        )

        MenuButton(
            title = "⚡ GOD POWER SANDBOX",
            subtitle = "Unlimited favor & divine powers",
            onClick = onSandboxClick,
            color = Color(0xFF059669)
        )

        MenuButton(
            title = "📖 TOUCH CONTROLS & GUIDE",
            subtitle = "Camera gestures, multi-select & combat",
            onClick = onGuideClick,
            color = Color(0xFF4B5563)
        )
    }
}

@Composable
private fun MenuButton(title: String, subtitle: String, onClick: () -> Unit, color: Color) {
    Button(
        onClick = {
            SoundEffects.playUnitSelect()
            onClick()
        },
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(subtitle, color = Color.LightGray, fontSize = 10.sp)
        }
    }
}

@Composable
private fun FactionSetupScreen(
    selectedFaction: Faction,
    selectedGod: MajorGod,
    enemyFaction: Faction,
    onFactionSelect: (Faction) -> Unit,
    onGodSelect: (MajorGod) -> Unit,
    onEnemyFactionSelect: (Faction) -> Unit,
    onStartGame: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text("SELECT YOUR PANTHEON", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Faction.values().forEach { f ->
                    val isSel = f == selectedFaction
                    Surface(
                        color = if (isSel) Color(0xFFB45309) else Color(0xFF1F2937),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onFactionSelect(f) }
                            .border(1.dp, if (isSel) Color.Yellow else Color.Gray, RoundedCornerShape(12.dp))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                            Text(f.iconSymbol, fontSize = 20.sp)
                            Text(f.displayName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text("CHOOSE MAJOR GOD", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            val gods = MajorGod.values().filter { it.faction == selectedFaction }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                gods.forEach { g ->
                    val isSel = g == selectedGod
                    Surface(
                        color = if (isSel) Color(0xFF2563EB) else Color(0xFF1F2937),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onGodSelect(g) }
                            .border(1.dp, if (isSel) Color.Cyan else Color.Gray, RoundedCornerShape(12.dp))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                            Text(g.godName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(g.title, color = Color.LightGray, fontSize = 9.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }

        item {
            Surface(
                color = Color(0x99111827),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("God Passive Bonus:", color = Color(0xFFF59E0B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(selectedGod.passiveBonus, color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Starting God Power: ${selectedGod.startingGodPower.displayName} (${selectedGod.startingGodPower.iconSymbol})", color = Color.Cyan, fontSize = 12.sp)
                }
            }
        }

        item {
            Text("ENEMY AI PANTHEON", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Faction.values().forEach { ef ->
                    val isSel = ef == enemyFaction
                    Surface(
                        color = if (isSel) Color(0xFF991B1B) else Color(0xFF1F2937),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onEnemyFactionSelect(ef) }
                            .border(1.dp, if (isSel) Color.Red else Color.Gray, RoundedCornerShape(12.dp))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                            Text(ef.iconSymbol, fontSize = 20.sp)
                            Text(ef.displayName, color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                    Text("Back", color = Color.White)
                }
                Button(
                    onClick = onStartGame,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.weight(2f)
                ) {
                    Text("⚔️ ENTER BATTLE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun CampaignSelectScreen(
    onMissionSelect: (CampaignMission) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("FALL OF THE TRIDENT SAGAS", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            items(CampaignData.missions) { mission ->
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMissionSelect(mission) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📜 Mission ${mission.id}: ", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(mission.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(mission.subtitle, color = Color.Cyan, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(mission.objectiveDesc, color = Color.LightGray, fontSize = 11.sp)
                    }
                }
            }
        }

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Back to Main Menu", color = Color.White)
        }
    }
}

@Composable
private fun HowToPlayGuide(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("MOBILE RTS TOUCH CONTROLS GUIDE", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            item {
                GuideCard("👆 3D Camera Controls", "• Drag 1 Finger: Pan map camera\n• Pinch 2 Fingers: Zoom in & out\n• Rotate 2 Fingers: Rotate 3D tactical angle")
            }
            item {
                GuideCard("🟩 Unit Selection & Commands", "• Tap Unit / Building: Select single object\n• Drag Box Ring: Select multiple soldiers\n• Tap Map Terrain: Move army or harvest trees/gold")
            }
            item {
                GuideCard("⚡ Divine God Powers", "• Tap God Power icon in top-right\n• Tap target area on 3D battlefield\n• Rain Lightning, Meteors, Healing, or Tornadoes!")
            }
            item {
                GuideCard("🏛️ Economy & Aging Up", "• Villagers gather Food, Wood, Gold & Favor\n• Build Houses to raise Population limit\n• Advance Ages at Town Center for Myth Units!")
            }
        }

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Back", color = Color.White)
        }
    }
}

@Composable
private fun GuideCard(title: String, desc: String) {
    Surface(
        color = Color(0xFF1F2937),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, color = Color.White, fontSize = 12.sp)
        }
    }
}
