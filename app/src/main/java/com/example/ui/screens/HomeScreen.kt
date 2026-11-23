package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.R
import com.example.ads.OfflineSafeAdBanner
import com.example.audio.LocalSoundManager
import com.example.data.AdSettings
import com.example.game.PixelArtAtlas
import com.example.game.RoadCatalog
import com.example.game.RoadThemeProfile
import com.example.game.RunnerCatalog
import com.example.game.RunnerCharacterProfile
import com.example.ui.AppDestination
import com.example.ui.GameUiState
import com.example.ui.GameViewModel
import com.example.ui.components.drawRoadSceneryBackground
import com.example.ui.theme.ArcadeBg
import com.example.ui.theme.ArcadeBorder
import com.example.ui.theme.ArcadeSurface
import com.example.ui.theme.ArcadeSurfaceElevated
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoltAmber
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.math.roundToInt

private enum class ShowcaseTab {
    CHARACTERS, ROADS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: GameUiState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val sound = LocalSoundManager.current

    var activeTab by rememberSaveable { mutableStateOf(ShowcaseTab.CHARACTERS) }
    var charIndex by rememberSaveable {
        mutableIntStateOf(
            RunnerCatalog.characters.indexOfFirst { it.id == uiState.equippedCharacterId }.coerceAtLeast(0)
        )
    }
    var roadIndex by rememberSaveable {
        mutableIntStateOf(
            RoadCatalog.roads.indexOfFirst { it.id == uiState.equippedRoadId }.coerceAtLeast(0)
        )
    }
    var showAdsSheet by remember { mutableStateOf(false) }

    // Continuous 12fps sprite stride & road scrolling clock
    var animTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(95L)
            animTick++
        }
    }

    val selectedChar = RunnerCatalog.characters[charIndex.coerceIn(0, RunnerCatalog.characters.lastIndex)]
    val selectedRoad = RoadCatalog.roads[roadIndex.coerceIn(0, RoadCatalog.roads.lastIndex)]

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        ArcadeBg,
                        selectedRoad.skyTintColor.copy(alpha = 0.55f),
                        ArcadeBg
                    )
                )
            )
    ) {
        // Scrollable Central Lobby (fits both Portrait & Landscape cleanly)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Clean Top Header Bar (Status, Wallet, Ads/Studio Shortcuts)
                HomeTopBar(
                    coins = uiState.coins,
                    highScore = uiState.highScore,
                    isOnline = uiState.effectiveOnline,
                    offlineOnlyMode = uiState.adSettings.offlineOnlyMode,
                    soundEnabled = uiState.physicsConfig.soundEnabled,
                    onToggleOfflineMode = { viewModel.toggleOfflineOnlyMode() },
                    onToggleSound = { viewModel.toggleSound() },
                    onOpenAds = {
                        sound.playMenuNavigate()
                        showAdsSheet = true
                    },
                    onOpenDesign = { viewModel.navigateTo(AppDestination.DESIGN) }
                )

                // Feedback message banner if any
                uiState.statusBannerMessage?.let { msg ->
                    Surface(
                        color = NeonCyan.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NeonCyan.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeonCyan,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "OK",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ArcadeSurfaceElevated)
                                    .clickable { viewModel.clearStatusMessage() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // 2. Segmented Tab: RUNNERS vs ROADS
                ShowcaseTabSwitcher(
                    activeTab = activeTab,
                    charUnlockedCount = uiState.unlockedCharacterIds.size,
                    charTotalCount = RunnerCatalog.characters.size,
                    roadUnlockedCount = uiState.unlockedRoadIds.size,
                    roadTotalCount = RoadCatalog.roads.size,
                    onSelectTab = { tab ->
                        if (activeTab != tab) {
                            sound.playMenuNavigate()
                            activeTab = tab
                        }
                    }
                )

                // 3. Single Giant Showcase Card (One Character OR One Road at a time!)
                if (activeTab == ShowcaseTab.CHARACTERS) {
                    SingleCharacterShowcaseCard(
                        character = selectedChar,
                        equippedRoad = uiState.equippedRoad,
                        index = charIndex,
                        total = RunnerCatalog.characters.size,
                        animTick = animTick,
                        unlocked = uiState.unlockedCharacterIds.contains(selectedChar.id),
                        equipped = uiState.equippedCharacterId == selectedChar.id,
                        userCoins = uiState.coins,
                        onPrev = {
                            sound.playButtonClick()
                            charIndex = if (charIndex - 1 < 0) RunnerCatalog.characters.lastIndex else charIndex - 1
                        },
                        onNext = {
                            sound.playButtonClick()
                            charIndex = (charIndex + 1) % RunnerCatalog.characters.size
                        },
                        onSelectDot = { idx ->
                            sound.playButtonClick()
                            charIndex = idx
                        },
                        onBuyOrEquip = { viewModel.buyOrEquipCharacter(selectedChar.id) }
                    )
                } else {
                    SingleRoadShowcaseCard(
                        road = selectedRoad,
                        equippedCharacter = uiState.equippedCharacter,
                        index = roadIndex,
                        total = RoadCatalog.roads.size,
                        animTick = animTick,
                        unlocked = uiState.unlockedRoadIds.contains(selectedRoad.id),
                        equipped = uiState.equippedRoadId == selectedRoad.id,
                        userCoins = uiState.coins,
                        onPrev = {
                            sound.playButtonClick()
                            roadIndex = if (roadIndex - 1 < 0) RoadCatalog.roads.lastIndex else roadIndex - 1
                        },
                        onNext = {
                            sound.playButtonClick()
                            roadIndex = (roadIndex + 1) % RoadCatalog.roads.size
                        },
                        onSelectDot = { idx ->
                            sound.playButtonClick()
                            roadIndex = idx
                        },
                        onBuyOrEquip = { viewModel.buyOrEquipRoad(selectedRoad.id) }
                    )
                }

                // 4. Primary Action Row: PLAY NOW + UNLIMITED LEVELS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.launchLevel(uiState.activeLevelNumber, resetRunScore = false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = uiState.equippedCharacter.primaryColor,
                            contentColor = ArcadeBg
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1.6f)
                            .height(54.dp)
                            .testTag("home_play_button")
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "PLAY LV ${uiState.activeLevelNumber}",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppDestination.STAGES) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("home_stages_button")
                    ) {
                        Icon(Icons.Filled.Flag, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("LEVELS", style = MaterialTheme.typography.labelLarge)
                    }
                }

                // 5. Offline Bonus Vault Banner (Always works 100% without internet!)
                OfflineBonusSupplyBanner(
                    bonusReady = uiState.isOfflineBonusReady(),
                    remainingMs = uiState.offlineBonusRemainingMs(),
                    offlineOnlyMode = uiState.adSettings.offlineOnlyMode,
                    onClaimBonus = { viewModel.claimOfflineBonusCoins() },
                    onOpenVault = {
                        sound.playMenuNavigate()
                        showAdsSheet = true
                    }
                )

                // 6. All Home Sections Quick Hub (Runners, Roads, Levels, Ads & Coins, Studio)
                HomeSectionsQuickBar(
                    activeTab = activeTab,
                    onSelectRunners = {
                        if (activeTab != ShowcaseTab.CHARACTERS) sound.playMenuNavigate() else sound.playButtonClick()
                        activeTab = ShowcaseTab.CHARACTERS
                    },
                    onSelectRoads = {
                        if (activeTab != ShowcaseTab.ROADS) sound.playMenuNavigate() else sound.playButtonClick()
                        activeTab = ShowcaseTab.ROADS
                    },
                    onOpenStages = { viewModel.navigateTo(AppDestination.STAGES) },
                    onOpenAds = {
                        sound.playMenuNavigate()
                        showAdsSheet = true
                    },
                    onOpenStudio = { viewModel.navigateTo(AppDestination.DESIGN) }
                )

                Spacer(Modifier.height(4.dp))
            }
        }

        // Offline-safe AdMob Banner at bottom of Home ONLY when online & ads enabled & not in Pure Offline Mode
        Box(modifier = Modifier.navigationBarsPadding()) {
            OfflineSafeAdBanner(
                adUnitId = uiState.adSettings.bannerUnitId,
                adsEnabled = uiState.adSettings.adsEnabled,
                isOnline = uiState.effectiveOnline,
                offlineOnlyMode = uiState.adSettings.offlineOnlyMode
            )
        }
    }

    // Offline Vault & Optional Online Ads Bottom Sheet
    if (showAdsSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                sound.playMenuNavigate()
                showAdsSheet = false
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = ArcadeSurface,
            contentColor = TextPrimary
        ) {
            AdsAndBonusCoinsSheet(
                adSettings = uiState.adSettings,
                isOnline = uiState.effectiveOnline,
                rewardedReady = uiState.rewardedAdReady,
                offlineBonusReady = uiState.isOfflineBonusReady(),
                offlineBonusRemainingMs = uiState.offlineBonusRemainingMs(),
                onClaimOfflineBonus = { viewModel.claimOfflineBonusCoins() },
                onWatchRewardedAd = { viewModel.watchRewardedAdForCoins(activity) },
                onSaveAdSettings = { viewModel.saveAdSettings(it) },
                onClose = {
                    sound.playButtonClick()
                    showAdsSheet = false
                }
            )
        }
    }
}

@Composable
private fun HomeTopBar(
    coins: Int,
    highScore: Int,
    isOnline: Boolean,
    offlineOnlyMode: Boolean,
    soundEnabled: Boolean,
    onToggleOfflineMode: () -> Unit,
    onToggleSound: () -> Unit,
    onOpenAds: () -> Unit,
    onOpenDesign: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: App Emblem + Title on Left, 48dp Audio / Vault / Studio buttons on Right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0B1026))
                        .border(1.5.dp, NeonCyan.copy(alpha = 0.78f), RoundedCornerShape(12.dp))
                        .testTag("home_app_emblem")
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_art),
                        contentDescription = "Volt Dash Icon",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Text(
                    text = "VOLT DASH",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    maxLines = 1
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ArcadeSurface)
                        .border(
                            1.dp,
                            if (soundEnabled) NeonCyan.copy(alpha = 0.45f) else ArcadeBorder,
                            CircleShape
                        )
                        .testTag("home_sound_toggle")
                ) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = if (soundEnabled) "Mute Sound Effects" else "Unmute Sound Effects",
                        tint = if (soundEnabled) NeonCyan else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onOpenAds,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ArcadeSurface)
                        .border(1.dp, MintEmerald.copy(alpha = 0.45f), CircleShape)
                        .testTag("home_ads_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.AttachMoney,
                        contentDescription = "Ads and Bonus Coins",
                        tint = MintEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onOpenDesign,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ArcadeSurface)
                        .border(1.dp, ArcadeBorder, CircleShape)
                        .testTag("home_design_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Architecture,
                        contentDescription = "Design and Physics Studio",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Row 2: Interactive Offline Mode Status Pill + Coin Wallet Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(ArcadeSurface)
                    .border(
                        width = 1.dp,
                        color = if (isOnline) MintEmerald.copy(alpha = 0.5f) else NeonCyan.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable(onClick = onToggleOfflineMode)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("home_offline_mode_pill")
            ) {
                Icon(
                    imageVector = if (isOnline) Icons.Filled.Wifi else Icons.Filled.CloudOff,
                    contentDescription = "Toggle Pure Offline Mode",
                    tint = if (isOnline) MintEmerald else NeonCyan,
                    modifier = Modifier.size(14.dp)
                )
                val statusLabel = when {
                    offlineOnlyMode -> "PURE OFFLINE • BEST $highScore"
                    isOnline -> "ONLINE • BEST $highScore"
                    else -> "OFFLINE READY • BEST $highScore"
                }
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOnline) MintEmerald else NeonCyan,
                    maxLines = 1
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(ArcadeSurfaceElevated)
                    .border(1.5.dp, VoltAmber.copy(alpha = 0.75f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("home_coins_badge")
            ) {
                Icon(
                    imageVector = Icons.Filled.MonetizationOn,
                    contentDescription = "Coins",
                    tint = VoltAmber,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "$coins C",
                    style = MaterialTheme.typography.labelLarge,
                    color = VoltAmber
                )
            }
        }
    }
}

@Composable
private fun ShowcaseTabSwitcher(
    activeTab: ShowcaseTab,
    charUnlockedCount: Int,
    charTotalCount: Int,
    roadUnlockedCount: Int,
    roadTotalCount: Int,
    onSelectTab: (ShowcaseTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ArcadeSurface)
            .border(1.dp, ArcadeBorder, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val charSelected = activeTab == ShowcaseTab.CHARACTERS
        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (charSelected) NeonCyan.copy(alpha = 0.18f) else Color.Transparent)
                .border(
                    width = if (charSelected) 1.dp else 0.dp,
                    color = if (charSelected) NeonCyan.copy(alpha = 0.65f) else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable { onSelectTab(ShowcaseTab.CHARACTERS) }
                .testTag("tab_showcase_characters"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                    contentDescription = null,
                    tint = if (charSelected) NeonCyan else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "RUNNERS ($charUnlockedCount/$charTotalCount)",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (charSelected) NeonCyan else TextSecondary
                )
            }
        }

        val roadSelected = activeTab == ShowcaseTab.ROADS
        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (roadSelected) NeonViolet.copy(alpha = 0.22f) else Color.Transparent)
                .border(
                    width = if (roadSelected) 1.dp else 0.dp,
                    color = if (roadSelected) NeonViolet.copy(alpha = 0.65f) else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable { onSelectTab(ShowcaseTab.ROADS) }
                .testTag("tab_showcase_roads"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Landscape,
                    contentDescription = null,
                    tint = if (roadSelected) NeonViolet else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "ROADS ($roadUnlockedCount/$roadTotalCount)",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (roadSelected) NeonViolet else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun OfflineBonusSupplyBanner(
    bonusReady: Boolean,
    remainingMs: Long,
    offlineOnlyMode: Boolean,
    onClaimBonus: () -> Unit,
    onOpenVault: () -> Unit
) {
    val remMins = ((remainingMs / 60_000L) + 1L).coerceAtLeast(1L)
    val accent = if (bonusReady) VoltAmber else NeonCyan

    Surface(
        color = ArcadeSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .testTag("offline_bonus_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accent.copy(alpha = 0.16f))
                        .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudOff,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = if (bonusReady) "OFFLINE BONUS COINS READY" else "100% OFFLINE VAULT ACTIVE",
                        style = MaterialTheme.typography.labelMedium,
                        color = accent
                    )
                    Text(
                        text = when {
                            bonusReady -> "Claim +50 Coins • Zero internet needed"
                            offlineOnlyMode -> "Pure Offline Mode ON • Next +50C in ${remMins}m"
                            else -> "All modes playable offline • Next +50C in ${remMins}m"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = if (bonusReady) onClaimBonus else onOpenVault,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (bonusReady) VoltAmber else ArcadeSurfaceElevated,
                    contentColor = if (bonusReady) ArcadeBg else TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("claim_offline_bonus_button")
            ) {
                Text(
                    text = if (bonusReady) "+50 C" else "VAULT",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun HomeSectionsQuickBar(
    activeTab: ShowcaseTab,
    onSelectRunners: () -> Unit,
    onSelectRoads: () -> Unit,
    onOpenStages: () -> Unit,
    onOpenAds: () -> Unit,
    onOpenStudio: () -> Unit
) {
    Surface(
        color = ArcadeSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ArcadeBorder, RoundedCornerShape(16.dp))
            .testTag("home_all_sections_bar")
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "GAME HUB SECTIONS",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SectionShortcutTile(
                    label = "RUNNERS",
                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                    accent = NeonCyan,
                    selected = activeTab == ShowcaseTab.CHARACTERS,
                    onClick = onSelectRunners,
                    modifier = Modifier.weight(1f).testTag("hub_section_runners")
                )
                SectionShortcutTile(
                    label = "ROADS",
                    icon = Icons.Filled.Landscape,
                    accent = NeonViolet,
                    selected = activeTab == ShowcaseTab.ROADS,
                    onClick = onSelectRoads,
                    modifier = Modifier.weight(1f).testTag("hub_section_roads")
                )
                SectionShortcutTile(
                    label = "LEVELS",
                    icon = Icons.Filled.Flag,
                    accent = VoltAmber,
                    selected = false,
                    onClick = onOpenStages,
                    modifier = Modifier.weight(1f).testTag("hub_section_levels")
                )
                SectionShortcutTile(
                    label = "ADS",
                    icon = Icons.Filled.AttachMoney,
                    accent = MintEmerald,
                    selected = false,
                    onClick = onOpenAds,
                    modifier = Modifier.weight(1f).testTag("hub_section_ads")
                )
                SectionShortcutTile(
                    label = "STUDIO",
                    icon = Icons.Filled.Architecture,
                    accent = TextPrimary,
                    selected = false,
                    onClick = onOpenStudio,
                    modifier = Modifier.weight(1f).testTag("hub_section_studio")
                )
            }
        }
    }
}

@Composable
private fun SectionShortcutTile(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) accent.copy(alpha = 0.16f) else ArcadeSurfaceElevated)
            .border(
                width = 1.dp,
                color = if (selected) accent.copy(alpha = 0.7f) else ArcadeBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = accent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) accent else TextSecondary,
            maxLines = 1
        )
    }
}

@Composable
private fun SingleCharacterShowcaseCard(
    character: RunnerCharacterProfile,
    equippedRoad: RoadThemeProfile,
    index: Int,
    total: Int,
    animTick: Int,
    unlocked: Boolean,
    equipped: Boolean,
    userCoins: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSelectDot: (Int) -> Unit,
    onBuyOrEquip: () -> Unit
) {
    val sprites = remember(character.id) { PixelArtAtlas.characterById(character.id).sprites }
    val roadTiles = remember(equippedRoad.id) { PixelArtAtlas.roadTilesById(equippedRoad.id) }
    val canAfford = userCoins >= character.priceCoins

    Surface(
        color = ArcadeSurface,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = if (equipped) character.primaryColor else character.primaryColor.copy(alpha = 0.45f),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Role Badge + Index
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(character.primaryColor.copy(alpha = 0.16f))
                        .border(1.dp, character.primaryColor.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = character.roleTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = character.primaryColor
                    )
                }

                Text(
                    text = "${index + 1} / $total",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }

            // Giant Single Character Stage with Left / Right Carousel Arrows
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onPrev,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ArcadeSurfaceElevated)
                        .border(1.dp, ArcadeBorder, CircleShape)
                        .testTag("char_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Character",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Huge Center Stage Pedestal
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(215.dp)
                        .padding(horizontal = 10.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    ArcadeBg,
                                    character.primaryColor.copy(alpha = 0.15f),
                                    equippedRoad.roadDeepColor.copy(alpha = 0.55f)
                                )
                            )
                        )
                        .border(1.dp, character.primaryColor.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = character.id,
                        transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.88f)).togetherWith(fadeOut()) },
                        label = "hero_showcase_anim"
                    ) { targetId ->
                        val activeSprites = remember(targetId) { PixelArtAtlas.characterById(targetId).sprites }
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val tilePx = 32.dp.toPx().roundToInt()
                            val floorY = (size.height - tilePx).roundToInt()
                            val scrollOffset = ((animTick * 6) % tilePx)
                            var tx = -scrollOffset
                            while (tx < size.width) {
                                drawImage(
                                    image = roadTiles.topTile.right,
                                    dstOffset = IntOffset(tx, floorY),
                                    dstSize = IntSize(tilePx, tilePx),
                                    filterQuality = FilterQuality.None
                                )
                                tx += tilePx
                            }

                            // Glowing halo behind runner
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(
                                        character.primaryColor.copy(alpha = 0.35f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width / 2f, floorY - 64.dp.toPx()),
                                    radius = 86.dp.toPx()
                                ),
                                center = Offset(size.width / 2f, floorY - 64.dp.toPx()),
                                radius = 86.dp.toPx()
                            )

                            // Huge crisp 132dp pixel sprite
                            val heroSize = 132.dp.toPx().roundToInt()
                            val hx = ((size.width - heroSize) / 2f).roundToInt()
                            val hy = floorY - heroSize + 4
                            val bmp = activeSprites.runFrames[animTick % activeSprites.runFrames.size].right
                            drawImage(
                                image = bmp,
                                dstOffset = IntOffset(hx, hy),
                                dstSize = IntSize(heroSize, heroSize),
                                filterQuality = FilterQuality.None
                            )
                        }
                    }

                    if (!unlocked) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ArcadeBg.copy(alpha = 0.85f))
                                .border(1.dp, VoltAmber.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = VoltAmber, modifier = Modifier.size(12.dp))
                                Text("${character.priceCoins} C", style = MaterialTheme.typography.labelSmall, color = VoltAmber)
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ArcadeSurfaceElevated)
                        .border(1.dp, ArcadeBorder, CircleShape)
                        .testTag("char_next_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Character",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Carousel Dots
            CarouselDots(count = total, selected = index, accent = character.primaryColor, onSelect = onSelectDot)

            // Character Details Below Stage
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = character.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ArcadeSurfaceElevated)
                        .border(1.dp, character.primaryColor.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = character.perkLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MintEmerald,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Stat Comparison Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBarMini("SPEED", character.speedMultiplier / 1.15f, character.primaryColor, Modifier.weight(1f))
                StatBarMini("JUMP", character.jumpMultiplier / 1.14f, NeonViolet, Modifier.weight(1f))
                StatBarMini("COINS", character.bonusCoinMultiplier / 1.50f, VoltAmber, Modifier.weight(1f))
            }

            // Big Buy / Equip Button Directly Below Showcase
            ShowcaseBuyEquipButton(
                unlocked = unlocked,
                equipped = equipped,
                canAfford = canAfford,
                priceCoins = character.priceCoins,
                accentColor = character.primaryColor,
                testTag = "char_action_button",
                onClick = onBuyOrEquip
            )
        }
    }
}

@Composable
private fun SingleRoadShowcaseCard(
    road: RoadThemeProfile,
    equippedCharacter: RunnerCharacterProfile,
    index: Int,
    total: Int,
    animTick: Int,
    unlocked: Boolean,
    equipped: Boolean,
    userCoins: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSelectDot: (Int) -> Unit,
    onBuyOrEquip: () -> Unit
) {
    val roadTiles = remember(road.id) { PixelArtAtlas.roadTilesById(road.id) }
    val heroSprites = remember(equippedCharacter.id) { PixelArtAtlas.characterById(equippedCharacter.id).sprites }
    val canAfford = userCoins >= road.priceCoins

    Surface(
        color = ArcadeSurface,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = if (equipped) road.roadTopColor else road.roadTopColor.copy(alpha = 0.45f),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(road.roadTopColor.copy(alpha = 0.16f))
                        .border(1.dp, road.roadTopColor.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = road.themeTag,
                        style = MaterialTheme.typography.labelSmall,
                        color = road.roadTopColor
                    )
                }

                Text(
                    text = "${index + 1} / $total",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onPrev,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ArcadeSurfaceElevated)
                        .border(1.dp, ArcadeBorder, CircleShape)
                        .testTag("road_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Road",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Giant Live Animated Road Diorama
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(215.dp)
                        .padding(horizontal = 10.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, road.roadTopColor.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val tilePx = 30.dp.toPx().roundToInt()
                        val roadY = (size.height - tilePx * 2).roundToInt()

                        // Distinct world background scenery (Volcanoes / Bio-Canopy / Pyramids / Aurora / Cosmos)
                        drawRoadSceneryBackground(
                            road = road,
                            scrollDistancePx = animTick * 7f,
                            worldTime = animTick * 0.095f,
                            scale = 1.15f,
                            groundBaselinePx = roadY.toFloat()
                        )

                        // Floating road-specific platform rail
                        val railY = (size.height * 0.42f).roundToInt()
                        val railStartX = (size.width * 0.46f).roundToInt()
                        for (r in 0..2) {
                            drawImage(
                                image = roadTiles.railTile.right,
                                dstOffset = IntOffset(railStartX + r * tilePx, railY),
                                dstSize = IntSize(tilePx, tilePx),
                                filterQuality = FilterQuality.None
                            )
                        }
                        val shardBmp = PixelArtAtlas.shardFrames[animTick % PixelArtAtlas.shardFrames.size].right
                        drawImage(
                            image = shardBmp,
                            dstOffset = IntOffset(railStartX + tilePx, railY - tilePx + 4),
                            dstSize = IntSize(tilePx, tilePx),
                            filterQuality = FilterQuality.None
                        )

                        // Sculpted road surface floor + sub-surface bricks unique to this road
                        val roadScroll = ((animTick * 7) % tilePx)
                        var rx = -roadScroll
                        while (rx < size.width) {
                            drawImage(
                                image = roadTiles.topTile.right,
                                dstOffset = IntOffset(rx, roadY),
                                dstSize = IntSize(tilePx, tilePx),
                                filterQuality = FilterQuality.None
                            )
                            drawImage(
                                image = roadTiles.subTile.right,
                                dstOffset = IntOffset(rx, roadY + tilePx),
                                dstSize = IntSize(tilePx, tilePx),
                                filterQuality = FilterQuality.None
                            )
                            rx += tilePx
                        }

                        // Active runner preview sprinting on this road!
                        val runnerSize = 68.dp.toPx().roundToInt()
                        val heroBmp = heroSprites.runFrames[animTick % heroSprites.runFrames.size].right
                        drawImage(
                            image = heroBmp,
                            dstOffset = IntOffset((size.width * 0.22f).roundToInt(), roadY - runnerSize + 4),
                            dstSize = IntSize(runnerSize, runnerSize),
                            filterQuality = FilterQuality.None
                        )
                    }

                    if (!unlocked) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ArcadeBg.copy(alpha = 0.85f))
                                .border(1.dp, VoltAmber.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = VoltAmber, modifier = Modifier.size(12.dp))
                                Text("${road.priceCoins} C", style = MaterialTheme.typography.labelSmall, color = VoltAmber)
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ArcadeSurfaceElevated)
                        .border(1.dp, ArcadeBorder, CircleShape)
                        .testTag("road_next_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Road",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            CarouselDots(count = total, selected = index, accent = road.roadTopColor, onSelect = onSelectDot)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = road.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = road.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "LEVEL STYLE: ${road.levelStyleBadge.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = VoltAmber,
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ArcadeSurfaceElevated)
                        .border(1.dp, road.roadTopColor.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = road.perkLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = road.roadTopColor,
                        textAlign = TextAlign.Center
                    )
                }
            }

            ShowcaseBuyEquipButton(
                unlocked = unlocked,
                equipped = equipped,
                canAfford = canAfford,
                priceCoins = road.priceCoins,
                accentColor = road.roadTopColor,
                testTag = "road_action_button",
                onClick = onBuyOrEquip
            )
        }
    }
}

@Composable
private fun CarouselDots(
    count: Int,
    selected: Int,
    accent: Color,
    onSelect: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until count) {
            val isSel = i == selected
            Box(
                modifier = Modifier
                    .size(width = if (isSel) 20.dp else 8.dp, height = 8.dp)
                    .clip(CircleShape)
                    .background(if (isSel) accent else ArcadeBorder)
                    .clickable { onSelect(i) }
            )
        }
    }
}

@Composable
private fun StatBarMini(
    label: String,
    fillFraction: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ArcadeSurfaceElevated)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(ArcadeBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fillFraction.coerceIn(0.2f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun ShowcaseBuyEquipButton(
    unlocked: Boolean,
    equipped: Boolean,
    canAfford: Boolean,
    priceCoins: Int,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    when {
        equipped -> {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor.copy(alpha = 0.2f),
                    contentColor = accentColor
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(1.5.dp, accentColor, RoundedCornerShape(14.dp))
                    .testTag(testTag)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("EQUIPPED & READY", style = MaterialTheme.typography.labelLarge)
            }
        }
        unlocked -> {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = ArcadeBg
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag(testTag)
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("EQUIP NOW", style = MaterialTheme.typography.labelLarge)
            }
        }
        else -> {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) VoltAmber else ArcadeSurfaceElevated,
                    contentColor = if (canAfford) ArcadeBg else TextMuted
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag(testTag)
            ) {
                Icon(
                    imageVector = if (canAfford) Icons.Filled.MonetizationOn else Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                val label = if (canAfford) {
                    "BUY NOW • $priceCoins COINS"
                } else {
                    "NEED $priceCoins COINS TO BUY"
                }
                Text(label, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun AdsAndBonusCoinsSheet(
    adSettings: AdSettings,
    isOnline: Boolean,
    rewardedReady: Boolean,
    offlineBonusReady: Boolean,
    offlineBonusRemainingMs: Long,
    onClaimOfflineBonus: () -> Unit,
    onWatchRewardedAd: () -> Unit,
    onSaveAdSettings: (AdSettings) -> Unit,
    onClose: () -> Unit
) {
    var bannerId by remember(adSettings.bannerUnitId) { mutableStateOf(adSettings.bannerUnitId) }
    var rewardedId by remember(adSettings.rewardedUnitId) { mutableStateOf(adSettings.rewardedUnitId) }
    var interstitialId by remember(adSettings.interstitialUnitId) { mutableStateOf(adSettings.interstitialUnitId) }
    val remMins = ((offlineBonusRemainingMs / 60_000L) + 1L).coerceAtLeast(1L)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "OFFLINE VAULT & OPTIONAL ADS",
            style = MaterialTheme.typography.titleSmall,
            color = NeonCyan
        )
        Text(
            text = "Volt Dash runs 100% offline: all 8 runners, 6 road worlds, unlimited procedural levels, sound effects & local saves work with zero internet.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        // Section 1: Always-available Offline Bonus Coins
        Button(
            onClick = onClaimOfflineBonus,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (offlineBonusReady) VoltAmber else ArcadeSurfaceElevated,
                contentColor = if (offlineBonusReady) ArcadeBg else TextSecondary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("sheet_claim_offline_bonus_button")
        ) {
            Icon(Icons.Filled.MonetizationOn, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (offlineBonusReady) {
                    "CLAIM OFFLINE BONUS (+50 COINS)"
                } else {
                    "OFFLINE BONUS IN ${remMins}M (PLAY LEVELS TO EARN)"
                },
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Pure Offline Mode Toggle (Disables all AdMob network calls)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ArcadeSurfaceElevated)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pure Offline Mode (Zero Data / No Ads)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Blocks all ad network calls even when Wi-Fi is on.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            Switch(
                checked = adSettings.offlineOnlyMode,
                onCheckedChange = { onSaveAdSettings(adSettings.copy(offlineOnlyMode = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ArcadeBg,
                    checkedTrackColor = NeonCyan
                ),
                modifier = Modifier.testTag("pure_offline_mode_switch")
            )
        }

        // Section 2: Optional Online Rewarded Ads
        Button(
            onClick = onWatchRewardedAd,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isOnline && rewardedReady && adSettings.adsEnabled && !adSettings.offlineOnlyMode) {
                    MintEmerald
                } else {
                    ArcadeSurfaceElevated
                },
                contentColor = if (isOnline && rewardedReady && adSettings.adsEnabled && !adSettings.offlineOnlyMode) {
                    ArcadeBg
                } else {
                    TextSecondary
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("watch_rewarded_ad_button")
        ) {
            Icon(Icons.Filled.MonetizationOn, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            val label = when {
                adSettings.offlineOnlyMode -> "PURE OFFLINE MODE ACTIVE"
                !adSettings.adsEnabled -> "ADS PAUSED (ENABLE BELOW)"
                !isOnline -> "NO INTERNET • OFFLINE MODE ACTIVE"
                rewardedReady -> "WATCH AD (+75 BONUS COINS)"
                else -> "LOADING REWARDED AD..."
            }
            Text(label, style = MaterialTheme.typography.labelLarge)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Enable AdMob Ads (Only When Online)",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Switch(
                checked = adSettings.adsEnabled,
                onCheckedChange = { onSaveAdSettings(adSettings.copy(adsEnabled = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ArcadeBg,
                    checkedTrackColor = MintEmerald
                )
            )
        }

        Surface(
            color = ArcadeSurfaceElevated,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ArcadeBorder, RoundedCornerShape(10.dp))
        ) {
            Text(
                text = "AdMob App ID: ${BuildConfig.ADMOB_APP_ID}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        AdUnitField("Banner Ad Unit ID", bannerId) { bannerId = it }
        AdUnitField("Rewarded Ad Unit ID", rewardedId) { rewardedId = it }
        AdUnitField("Interstitial Ad Unit ID", interstitialId) { interstitialId = it }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    bannerId = BuildConfig.ADMOB_BANNER_ID
                    rewardedId = BuildConfig.ADMOB_REWARDED_ID
                    interstitialId = BuildConfig.ADMOB_INTERSTITIAL_ID
                    onSaveAdSettings(
                        adSettings.copy(
                            bannerUnitId = BuildConfig.ADMOB_BANNER_ID,
                            rewardedUnitId = BuildConfig.ADMOB_REWARDED_ID,
                            interstitialUnitId = BuildConfig.ADMOB_INTERSTITIAL_ID
                        )
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("reset_ad_ids_button")
            ) {
                Text("DEFAULTS", style = MaterialTheme.typography.labelLarge)
            }
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier
                    .weight(0.9f)
                    .height(48.dp)
            ) {
                Text("CLOSE", style = MaterialTheme.typography.labelLarge)
            }
            Button(
                onClick = {
                    onSaveAdSettings(
                        adSettings.copy(
                            bannerUnitId = bannerId,
                            rewardedUnitId = rewardedId,
                            interstitialUnitId = interstitialId
                        )
                    )
                    onClose()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = ArcadeBg),
                modifier = Modifier
                    .weight(1.3f)
                    .height(48.dp)
                    .testTag("save_ad_ids_button")
            ) {
                Text("SAVE IDS", style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun AdUnitField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine = true,
        textStyle = MaterialTheme.typography.labelMedium.copy(color = TextPrimary),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = ArcadeBorder,
            focusedLabelColor = NeonCyan,
            unfocusedLabelColor = TextSecondary
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
