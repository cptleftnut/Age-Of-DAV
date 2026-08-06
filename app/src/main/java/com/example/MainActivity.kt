package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.engine.VoiceLineManager
import com.example.game.state.GameScreenState
import com.example.game.state.GameViewModel
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VoiceLineManager.init(this)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AgeOfMythologyApp()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        VoiceLineManager.shutdown()
    }
}

@Composable
fun AgeOfMythologyApp(gameViewModel: GameViewModel = viewModel()) {
    val uiState by gameViewModel.uiState.collectAsState()
    val units by gameViewModel.units.collectAsState()
    val buildings by gameViewModel.buildings.collectAsState()
    val resourceNodes by gameViewModel.resourceNodes.collectAsState()
    val particles by gameViewModel.particles.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when (uiState.screenState) {
            GameScreenState.MAIN_MENU -> {
                MainMenuScreen(
                    onStartSkirmish = { faction, god, enemyFaction ->
                        gameViewModel.startSkirmish(faction, god, enemyFaction)
                    },
                    onStartCampaignMission = { mission ->
                        gameViewModel.startCampaign(mission)
                    },
                    onStartSandbox = {
                        gameViewModel.startSandbox()
                    }
                )
            }
            GameScreenState.SKIRMISH_GAME,
            GameScreenState.CAMPAIGN_GAME,
            GameScreenState.GOD_POWER_SANDBOX,
            GameScreenState.VICTORY,
            GameScreenState.DEFEAT -> {
                // Interactive 3D Canvas
                Game3DCanvas(
                    camera = uiState.camera,
                    terrainMap = gameViewModel.terrainMap,
                    units = units,
                    buildings = buildings,
                    resourceNodes = resourceNodes,
                    particles = particles,
                    selectedUnitIds = uiState.selectedUnitIds,
                    selectedBuildingId = uiState.selectedBuildingId,
                    selectedResourceNodeId = uiState.selectedResourceNodeId,
                    onMapTap = { pos -> gameViewModel.onTapMapCoordinate(pos) },
                    onBoxSelect = { min, max -> gameViewModel.onSelectEntitiesInBox(min, max) },
                    onCameraUpdate = { dx, dz, zoom, rot -> gameViewModel.updateCamera(dx, dz, zoom, rot) }
                )

                if (uiState.screenState != GameScreenState.VICTORY && uiState.screenState != GameScreenState.DEFEAT) {
                    // Mobile RTS HUD Overlay
                    GameHudOverlay(
                        uiState = uiState,
                        units = units,
                        buildings = buildings,
                        resourceNodes = resourceNodes,
                        onCastGodPower = { power -> gameViewModel.selectGodPowerToCast(power) },
                        onTrainUnit = { bldId, unitType -> gameViewModel.trainUnitInBuilding(bldId, unitType) },
                        onCancelQueueItem = { bldId, idx -> gameViewModel.cancelTrainingQueueItem(bldId, idx) },
                        onBuildStructure = { bldType -> gameViewModel.buildStructure(bldType, uiState.camera.screenToWorldGround(0f, 0f, 100f, 100f)) },
                        onAgeUp = { gameViewModel.triggerAgeUp() },
                        onMiniMapJump = { target -> gameViewModel.updateCamera(target.x - uiState.camera.targetX, target.z - uiState.camera.targetY, 0f, 0f) },
                        onPauseClick = { gameViewModel.onTapMapCoordinate(uiState.camera.screenToWorldGround(0f, 0f, 0f, 0f)) }
                    )

                    // Pause Menu Overlay
                    if (uiState.isPaused) {
                        PauseMenuDialog(
                            onResume = { gameViewModel.onTapMapCoordinate(uiState.camera.screenToWorldGround(0f, 0f, 0f, 0f)) },
                            onMainMenu = { gameViewModel.returnToMainMenu() }
                        )
                    }
                }

                // Victory Modal Screen Overlay
                if (uiState.screenState == GameScreenState.VICTORY) {
                    VictoryOverlay(
                        uiState = uiState,
                        onRestart = { gameViewModel.restartCurrentMatch() },
                        onMainMenu = { gameViewModel.returnToMainMenu() }
                    )
                }

                // Defeat Modal Screen Overlay
                if (uiState.screenState == GameScreenState.DEFEAT) {
                    DefeatOverlay(
                        uiState = uiState,
                        onRestart = { gameViewModel.restartCurrentMatch() },
                        onMainMenu = { gameViewModel.returnToMainMenu() }
                    )
                }
            }
            else -> {
                MainMenuScreen(
                    onStartSkirmish = { f, g, ef -> gameViewModel.startSkirmish(f, g, ef) },
                    onStartCampaignMission = { m -> gameViewModel.startCampaign(m) },
                    onStartSandbox = { gameViewModel.startSandbox() }
                )
            }
        }
    }
}
