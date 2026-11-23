package com.example.ui

import android.app.Activity
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdMobManager
import com.example.audio.SoundManager
import com.example.data.AdSettings
import com.example.data.PhysicsConfig
import com.example.data.PlayerProgression
import com.example.data.TuningPreferencesRepository
import com.example.game.HapticCue
import com.example.game.LevelDefinitions
import com.example.game.LiveInputState
import com.example.game.PlatformerEngine
import com.example.game.RoadCatalog
import com.example.game.RoadThemeProfile
import com.example.game.RunnerCatalog
import com.example.game.RunnerCharacterProfile
import com.example.game.SoundEffect
import com.example.game.StageDefinition
import com.example.game.WorldSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppDestination {
    SPLASH, HOME, PLAY, DESIGN, STAGES
}

enum class DesignSubTab(val label: String) {
    ASSETS("Assets"),
    PHYSICS("Physics"),
    LEVELS("Levels"),
    CONTROLS("Controls")
}

data class GameUiState(
    val destination: AppDestination = AppDestination.SPLASH,
    val activeLevelNumber: Int = 1,
    val activeStage: StageDefinition = LevelDefinitions.getStageForLevel(1),
    val isPaused: Boolean = false,
    val physicsConfig: PhysicsConfig = PhysicsConfig.ArcadeTight,
    val snapshot: WorldSnapshot = WorldSnapshot(),
    val cumulativeScore: Int = 0,
    val highScore: Int = 0,
    val highestLevel: Int = 1,
    val coins: Int = 0,
    val unlockedCharacterIds: Set<String> = setOf("volt"),
    val equippedCharacterId: String = "volt",
    val unlockedRoadIds: Set<String> = setOf("neon_highway"),
    val equippedRoadId: String = "neon_highway",
    val bestTimesByLevel: Map<Int, Long> = emptyMap(),
    val bestShardsByLevel: Map<Int, Int> = emptyMap(),
    val designSubTab: DesignSubTab = DesignSubTab.ASSETS,
    val selectedSpriteId: String = "hero_run",
    val selectedBlueprintStageIndex: Int = 0,
    val adSettings: AdSettings = AdSettings(),
    val isOnline: Boolean = false,
    val rewardedAdReady: Boolean = false,
    val lastOfflineBonusClaimMs: Long = 0L,
    val statusBannerMessage: String? = null
) {
    val totalLiveScore: Int get() = cumulativeScore + snapshot.stageScore
    val effectiveOnline: Boolean get() = isOnline && !adSettings.offlineOnlyMode
    val equippedCharacter: RunnerCharacterProfile
        get() = RunnerCatalog.byId(equippedCharacterId)
    val equippedRoad: RoadThemeProfile
        get() = RoadCatalog.byId(equippedRoadId)

    fun isOfflineBonusReady(nowMs: Long = System.currentTimeMillis()): Boolean =
        lastOfflineBonusClaimMs == 0L || (nowMs - lastOfflineBonusClaimMs) >= GameViewModel.OFFLINE_BONUS_COOLDOWN_MS

    fun offlineBonusRemainingMs(nowMs: Long = System.currentTimeMillis()): Long =
        if (lastOfflineBonusClaimMs == 0L) 0L
        else (GameViewModel.OFFLINE_BONUS_COOLDOWN_MS - (nowMs - lastOfflineBonusClaimMs)).coerceAtLeast(0L)
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TuningPreferencesRepository(application.applicationContext)
    val adMobManager = AdMobManager(application.applicationContext)
    val soundManager: SoundManager = SoundManager.getInstance(application.applicationContext)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val liveInput = LiveInputState()

    val engine: PlatformerEngine = PlatformerEngine(
        initialStage = LevelDefinitions.getStageForLevel(1),
        config = PhysicsConfig.ArcadeTight,
        activeCharacter = RunnerCatalog.characters.first(),
        activeRoad = RoadCatalog.roads.first(),
        soundManager = soundManager
    )

    var frameTick by mutableLongStateOf(0L)
        private set

    var hudElapsedMs by mutableLongStateOf(0L)
        private set

    private var lastHudTimerSyncMs = -100L
    private var lastCreditedStageCoins = 0
    private var levelsClearedSinceInterstitial = 0
    private var progressionReady = false
    private var userInteractedWithLevel = false

    init {
        viewModelScope.launch {
            repository.physicsConfigFlow.collect { savedConfig ->
                engine.config = savedConfig
                soundManager.setEnabled(savedConfig.soundEnabled)
                _uiState.update { it.copy(physicsConfig = savedConfig) }
            }
        }
        viewModelScope.launch {
            repository.progressionFlow.collect { prog ->
                progressionReady = true
                engine.activeCharacter = RunnerCatalog.byId(prog.equippedCharacterId)
                engine.activeRoad = RoadCatalog.byId(prog.equippedRoadId)
                if (!userInteractedWithLevel) {
                    val restoredLevel = prog.activeLevelNumber.coerceAtLeast(1)
                    val restoredHighest = maxOf(prog.highestLevel, restoredLevel)
                    val restoredStage = LevelDefinitions.getStageForLevel(restoredLevel, prog.equippedRoadId)
                    if (engine.stage.levelNumber != restoredStage.levelNumber || engine.activeRoad.id != prog.equippedRoadId) {
                        engine.loadStage(restoredStage)
                    }
                    _uiState.update {
                        it.copy(
                            activeLevelNumber = restoredLevel,
                            activeStage = restoredStage,
                            cumulativeScore = prog.cumulativeScore,
                            highScore = maxOf(it.highScore, prog.highScore),
                            highestLevel = maxOf(it.highestLevel, restoredHighest),
                            coins = prog.coins,
                            unlockedCharacterIds = prog.unlockedCharacterIds,
                            equippedCharacterId = prog.equippedCharacterId,
                            unlockedRoadIds = prog.unlockedRoadIds,
                            equippedRoadId = prog.equippedRoadId,
                            bestTimesByLevel = if (prog.bestTimesByLevel.isNotEmpty()) prog.bestTimesByLevel else it.bestTimesByLevel,
                            bestShardsByLevel = if (prog.bestShardsByLevel.isNotEmpty()) prog.bestShardsByLevel else it.bestShardsByLevel,
                            lastOfflineBonusClaimMs = maxOf(it.lastOfflineBonusClaimMs, prog.lastOfflineBonusClaimMs),
                            snapshot = engine.snapshot()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            highScore = maxOf(it.highScore, prog.highScore),
                            highestLevel = maxOf(it.highestLevel, prog.highestLevel),
                            bestTimesByLevel = if (prog.bestTimesByLevel.isNotEmpty()) prog.bestTimesByLevel else it.bestTimesByLevel,
                            bestShardsByLevel = if (prog.bestShardsByLevel.isNotEmpty()) prog.bestShardsByLevel else it.bestShardsByLevel,
                            lastOfflineBonusClaimMs = maxOf(it.lastOfflineBonusClaimMs, prog.lastOfflineBonusClaimMs)
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            repository.adSettingsFlow.collect { settings ->
                _uiState.update { it.copy(adSettings = settings) }
                if (_uiState.value.destination != AppDestination.SPLASH) {
                    adMobManager.initializeAndPreload(
                        rewardedUnitId = settings.rewardedUnitId,
                        interstitialUnitId = settings.interstitialUnitId,
                        adsEnabled = settings.adsEnabled,
                        offlineOnlyMode = settings.offlineOnlyMode
                    )
                }
            }
        }
        viewModelScope.launch {
            adMobManager.isOnline.collect { online ->
                _uiState.update { it.copy(isOnline = online) }
                val state = _uiState.value
                val ads = state.adSettings
                if (online && !ads.offlineOnlyMode && state.destination != AppDestination.SPLASH) {
                    adMobManager.initializeAndPreload(
                        rewardedUnitId = ads.rewardedUnitId,
                        interstitialUnitId = ads.interstitialUnitId,
                        adsEnabled = ads.adsEnabled,
                        offlineOnlyMode = ads.offlineOnlyMode
                    )
                }
            }
        }
        viewModelScope.launch {
            adMobManager.rewardedReady.collect { ready ->
                _uiState.update { it.copy(rewardedAdReady = ready) }
            }
        }
        _uiState.update { it.copy(snapshot = engine.snapshot()) }
    }

    override fun onCleared() {
        super.onCleared()
        adMobManager.release()
    }

    fun navigateTo(dest: AppDestination) {
        val current = _uiState.value
        if (current.destination != dest) {
            soundManager.playMenuNavigate()
        }
        // If leaving PLAY after clearing a stage, advance resume pointer to the next unlocked level
        if (current.destination == AppDestination.PLAY && dest != AppDestination.PLAY && current.snapshot.stageCleared) {
            userInteractedWithLevel = true
            val nextLv = current.activeLevelNumber + 1
            val carriedScore = current.cumulativeScore + current.snapshot.stageScore
            val nextStage = LevelDefinitions.getStageForLevel(nextLv, current.equippedRoadId)
            lastCreditedStageCoins = 0
            engine.loadStage(nextStage)
            _uiState.update { state ->
                state.copy(
                    destination = dest,
                    activeLevelNumber = nextLv,
                    activeStage = nextStage,
                    highestLevel = maxOf(state.highestLevel, nextLv),
                    cumulativeScore = carriedScore,
                    highScore = maxOf(state.highScore, carriedScore),
                    isPaused = false,
                    snapshot = engine.snapshot(),
                    statusBannerMessage = null
                )
            }
            persistCurrentProgression()
        } else {
            _uiState.update { state ->
                state.copy(
                    destination = dest,
                    isPaused = if (dest != AppDestination.PLAY) state.isPaused else false,
                    statusBannerMessage = null
                )
            }
            if (current.destination == AppDestination.PLAY && dest != AppDestination.PLAY) {
                persistCurrentProgression()
            }
        }
    }

    fun clearStatusMessage() {
        soundManager.playButtonClick()
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    fun selectDesignSubTab(tab: DesignSubTab) {
        if (_uiState.value.designSubTab != tab) {
            soundManager.playMenuNavigate()
        }
        _uiState.update { it.copy(designSubTab = tab) }
    }

    fun selectSprite(spriteId: String) {
        soundManager.playButtonClick()
        _uiState.update { it.copy(selectedSpriteId = spriteId) }
    }

    fun selectBlueprintStage(index: Int) {
        soundManager.playButtonClick()
        val safeIdx = index.coerceIn(0, LevelDefinitions.stages.lastIndex)
        _uiState.update { it.copy(selectedBlueprintStageIndex = safeIdx) }
    }

    fun enterHomeFromSplash() {
        soundManager.playMenuNavigate()
        _uiState.update { it.copy(destination = AppDestination.HOME) }
        val ads = _uiState.value.adSettings
        if (_uiState.value.effectiveOnline && ads.adsEnabled) {
            adMobManager.initializeAndPreload(
                rewardedUnitId = ads.rewardedUnitId,
                interstitialUnitId = ads.interstitialUnitId,
                adsEnabled = ads.adsEnabled,
                offlineOnlyMode = ads.offlineOnlyMode
            )
        }
    }

    fun startGameFromTitle() {
        soundManager.playMenuNavigate()
        if (progressionReady) {
            launchLevel(_uiState.value.activeLevelNumber, resetRunScore = false)
        } else {
            viewModelScope.launch {
                val prog = repository.progressionFlow.first()
                progressionReady = true
                if (!userInteractedWithLevel) {
                    val restoredLevel = prog.activeLevelNumber.coerceAtLeast(1)
                    val restoredStage = LevelDefinitions.getStageForLevel(restoredLevel, prog.equippedRoadId)
                    engine.activeCharacter = RunnerCatalog.byId(prog.equippedCharacterId)
                    engine.activeRoad = RoadCatalog.byId(prog.equippedRoadId)
                    engine.loadStage(restoredStage)
                    _uiState.update {
                        it.copy(
                            activeLevelNumber = restoredLevel,
                            activeStage = restoredStage,
                            highestLevel = maxOf(it.highestLevel, prog.highestLevel, restoredLevel),
                            cumulativeScore = prog.cumulativeScore,
                            highScore = maxOf(it.highScore, prog.highScore),
                            coins = prog.coins,
                            unlockedCharacterIds = prog.unlockedCharacterIds,
                            equippedCharacterId = prog.equippedCharacterId,
                            unlockedRoadIds = prog.unlockedRoadIds,
                            equippedRoadId = prog.equippedRoadId
                        )
                    }
                }
                launchLevel(_uiState.value.activeLevelNumber, resetRunScore = false)
            }
        }
    }

    fun clearLiveInput() {
        liveInput.moveDir = 0f
        liveInput.jumpHeld = false
        liveInput.jumpPressLatch = false
        liveInput.dashPressLatch = false
    }

    fun buyOrEquipCharacter(characterId: String) {
        val spec = RunnerCatalog.byId(characterId)
        val current = _uiState.value
        val alreadyUnlocked = current.unlockedCharacterIds.contains(spec.id)

        if (alreadyUnlocked) {
            engine.activeCharacter = spec
            _uiState.update {
                it.copy(
                    equippedCharacterId = spec.id,
                    statusBannerMessage = "${spec.name} equipped!"
                )
            }
            soundManager.playButtonClick()
            persistCurrentProgression()
        } else if (current.coins >= spec.priceCoins) {
            val newCoins = current.coins - spec.priceCoins
            val newUnlocked = current.unlockedCharacterIds + spec.id
            engine.activeCharacter = spec
            _uiState.update {
                it.copy(
                    coins = newCoins,
                    unlockedCharacterIds = newUnlocked,
                    equippedCharacterId = spec.id,
                    statusBannerMessage = "Unlocked & equipped ${spec.name}!"
                )
            }
            soundManager.playUnlockSuccess()
            persistCurrentProgression()
        } else {
            val needed = spec.priceCoins - current.coins
            soundManager.playActionDenied()
            _uiState.update {
                it.copy(statusBannerMessage = "Play levels or claim Offline Bonus to earn $needed more Coins for ${spec.name}!")
            }
        }
    }

    fun buyOrEquipRoad(roadId: String) {
        val road = RoadCatalog.byId(roadId)
        val current = _uiState.value
        val alreadyUnlocked = current.unlockedRoadIds.contains(road.id)

        if (alreadyUnlocked) {
            engine.activeRoad = road
            _uiState.update {
                it.copy(
                    equippedRoadId = road.id,
                    activeStage = LevelDefinitions.getStageForLevel(current.activeLevelNumber, road.id),
                    statusBannerMessage = "${road.name} track & level equipped!"
                )
            }
            soundManager.playButtonClick()
            persistCurrentProgression()
        } else if (current.coins >= road.priceCoins) {
            val newCoins = current.coins - road.priceCoins
            val newUnlocked = current.unlockedRoadIds + road.id
            engine.activeRoad = road
            _uiState.update {
                it.copy(
                    coins = newCoins,
                    unlockedRoadIds = newUnlocked,
                    equippedRoadId = road.id,
                    activeStage = LevelDefinitions.getStageForLevel(current.activeLevelNumber, road.id),
                    statusBannerMessage = "Unlocked & equipped ${road.name}!"
                )
            }
            soundManager.playUnlockSuccess()
            persistCurrentProgression()
        } else {
            val needed = road.priceCoins - current.coins
            soundManager.playActionDenied()
            _uiState.update {
                it.copy(statusBannerMessage = "Need $needed more Coins for ${road.name}!")
            }
        }
    }

    fun claimOfflineBonusCoins(nowMs: Long = System.currentTimeMillis()) {
        val current = _uiState.value
        if (current.isOfflineBonusReady(nowMs)) {
            val newCoins = current.coins + OFFLINE_BONUS_COINS
            _uiState.update {
                it.copy(
                    coins = newCoins,
                    lastOfflineBonusClaimMs = nowMs,
                    statusBannerMessage = "+$OFFLINE_BONUS_COINS Offline Bonus Coins claimed! Works 100% without internet."
                )
            }
            soundManager.playUnlockSuccess()
            persistCurrentProgression()
        } else {
            val remMs = current.offlineBonusRemainingMs(nowMs)
            val mins = ((remMs / 60_000L) + 1L).coerceAtLeast(1L)
            soundManager.playActionDenied()
            _uiState.update {
                it.copy(statusBannerMessage = "Offline Bonus recharges in ${mins}m — play any level to earn coins now!")
            }
        }
    }

    fun toggleOfflineOnlyMode() {
        val current = _uiState.value.adSettings
        val next = current.copy(offlineOnlyMode = !current.offlineOnlyMode)
        saveAdSettings(next)
    }

    fun saveAdSettings(settings: AdSettings) {
        soundManager.playButtonClick()
        _uiState.update {
            it.copy(
                adSettings = settings,
                statusBannerMessage = when {
                    settings.offlineOnlyMode -> "Pure Offline Mode ON — Zero internet or ads used"
                    settings.adsEnabled -> "AdMob settings saved (used only when online)"
                    else -> "Ads disabled — 100% ad-free offline mode"
                }
            )
        }
        viewModelScope.launch {
            repository.saveAdSettings(settings)
        }
        adMobManager.initializeAndPreload(
            rewardedUnitId = settings.rewardedUnitId,
            interstitialUnitId = settings.interstitialUnitId,
            adsEnabled = settings.adsEnabled,
            offlineOnlyMode = settings.offlineOnlyMode
        )
    }

    fun watchRewardedAdForCoins(activity: Activity?) {
        val state = _uiState.value
        if (state.adSettings.offlineOnlyMode) {
            soundManager.playActionDenied()
            _uiState.update { it.copy(statusBannerMessage = "Pure Offline Mode is ON — claim Offline Bonus Coins or play levels!") }
            return
        }
        if (!state.adSettings.adsEnabled) {
            soundManager.playActionDenied()
            _uiState.update { it.copy(statusBannerMessage = "Ads paused — claim Offline Bonus Coins or play levels!") }
            return
        }
        if (!state.effectiveOnline) {
            soundManager.playActionDenied()
            _uiState.update { it.copy(statusBannerMessage = "No internet — game works 100% offline! Claim Offline Bonus or play levels.") }
            return
        }
        soundManager.playButtonClick()
        adMobManager.showRewardedAd(
            activity = activity,
            rewardedUnitId = state.adSettings.rewardedUnitId,
            onEarnedCoins = { bonus ->
                soundManager.playUnlockSuccess()
                _uiState.update { cur ->
                    cur.copy(
                        coins = cur.coins + bonus,
                        statusBannerMessage = "+$bonus Bonus Coins awarded!"
                    )
                }
                persistCurrentProgression()
            },
            onClosedOrUnavailable = {
                adMobManager.preloadRewarded(state.adSettings.rewardedUnitId)
            }
        )
    }

    fun launchLevel(levelNumber: Int, resetRunScore: Boolean = true) {
        userInteractedWithLevel = true
        clearLiveInput()
        soundManager.playMenuNavigate()
        val safeLevel = levelNumber.coerceAtLeast(1)
        val roadId = _uiState.value.equippedRoadId
        val stageDef = LevelDefinitions.getStageForLevel(safeLevel, roadId)
        lastCreditedStageCoins = 0
        engine.activeCharacter = _uiState.value.equippedCharacter
        engine.activeRoad = _uiState.value.equippedRoad
        engine.loadStage(stageDef)
        lastHudTimerSyncMs = -100L
        hudElapsedMs = 0L

        _uiState.update { state ->
            val newCumulative = if (resetRunScore) 0 else state.cumulativeScore
            val newHighest = maxOf(state.highestLevel, safeLevel)
            state.copy(
                destination = AppDestination.PLAY,
                activeLevelNumber = safeLevel,
                activeStage = stageDef,
                isPaused = false,
                cumulativeScore = newCumulative,
                highestLevel = newHighest,
                snapshot = engine.snapshot()
            )
        }
        persistCurrentProgression()
        frameTick++
    }

    fun restartActiveStage() {
        userInteractedWithLevel = true
        clearLiveInput()
        soundManager.playButtonClick()
        val currentLevel = _uiState.value.activeLevelNumber
        val roadId = _uiState.value.equippedRoadId
        val stageDef = LevelDefinitions.getStageForLevel(currentLevel, roadId)
        lastCreditedStageCoins = 0
        engine.activeCharacter = _uiState.value.equippedCharacter
        engine.activeRoad = _uiState.value.equippedRoad
        engine.loadStage(stageDef)
        lastHudTimerSyncMs = -100L
        hudElapsedMs = 0L
        _uiState.update {
            it.copy(
                activeStage = stageDef,
                isPaused = false,
                snapshot = engine.snapshot()
            )
        }
        persistCurrentProgression()
        frameTick++
    }

    fun continueFromOutCheckpoint() {
        clearLiveInput()
        soundManager.playButtonClick()
        engine.continueFromCheckpoint()
        _uiState.update {
            it.copy(
                isPaused = false,
                snapshot = engine.snapshot()
            )
        }
        frameTick++
    }

    fun nextLevel(activity: Activity? = null) {
        clearLiveInput()
        soundManager.playMenuNavigate()
        val state = _uiState.value
        val carriedScore = state.cumulativeScore + state.snapshot.stageScore
        val nextLv = state.activeLevelNumber + 1
        val stageDef = LevelDefinitions.getStageForLevel(nextLv, state.equippedRoadId)
        lastCreditedStageCoins = 0
        engine.activeCharacter = state.equippedCharacter
        engine.activeRoad = state.equippedRoad
        engine.loadStage(stageDef)
        lastHudTimerSyncMs = -100L
        hudElapsedMs = 0L

        val newHighScore = maxOf(state.highScore, carriedScore)
        val newHighestLevel = maxOf(state.highestLevel, nextLv)

        _uiState.update {
            it.copy(
                destination = AppDestination.PLAY,
                activeLevelNumber = nextLv,
                activeStage = stageDef,
                isPaused = false,
                cumulativeScore = carriedScore,
                highScore = newHighScore,
                highestLevel = newHighestLevel,
                snapshot = engine.snapshot()
            )
        }
        persistCurrentProgression()
        frameTick++

        // Optional interstitial every 3rd cleared level ONLY if online & ads enabled & not in Pure Offline Mode
        levelsClearedSinceInterstitial++
        if (state.adSettings.adsEnabled && !state.adSettings.offlineOnlyMode && state.effectiveOnline && levelsClearedSinceInterstitial >= 3) {
            levelsClearedSinceInterstitial = 0
            adMobManager.maybeShowInterstitial(activity, state.adSettings.interstitialUnitId)
        }
    }

    fun togglePause() {
        if (_uiState.value.snapshot.stageCleared || _uiState.value.snapshot.isPlayerOut) return
        clearLiveInput()
        soundManager.playButtonClick()
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }

    fun setPaused(paused: Boolean) {
        if (paused) clearLiveInput()
        soundManager.playButtonClick()
        _uiState.update { it.copy(isPaused = paused) }
    }

    fun updatePhysicsConfig(newConfig: PhysicsConfig) {
        engine.config = newConfig
        soundManager.setEnabled(newConfig.soundEnabled)
        _uiState.update { it.copy(physicsConfig = newConfig) }
        viewModelScope.launch {
            repository.savePhysicsConfig(newConfig)
        }
    }

    fun toggleSound() {
        val current = _uiState.value.physicsConfig
        val nextEnabled = !current.soundEnabled
        updatePhysicsConfig(current.copy(soundEnabled = nextEnabled))
        if (nextEnabled) {
            soundManager.playUnlockSuccess()
        }
    }

    fun toggleHitboxesInPlay() {
        val current = _uiState.value.physicsConfig
        updatePhysicsConfig(current.copy(showHitboxes = !current.showHitboxes))
    }

    fun onAppBackgrounded() {
        clearLiveInput()
        val state = _uiState.value
        if (state.destination == AppDestination.PLAY && !state.isPaused && !state.snapshot.isPlayerOut && !state.snapshot.stageCleared) {
            _uiState.update { it.copy(isPaused = true) }
        }
        if (state.snapshot.stageCleared) {
            val nextLv = state.activeLevelNumber + 1
            val carried = state.cumulativeScore + state.snapshot.stageScore
            persistCurrentProgression(overrideActiveLevel = nextLv, overrideCumulativeScore = carried)
        } else {
            persistCurrentProgression()
        }
    }

    private fun persistCurrentProgression(
        overrideActiveLevel: Int? = null,
        overrideCumulativeScore: Int? = null
    ) {
        val cur = _uiState.value
        val activeLv = (overrideActiveLevel ?: cur.activeLevelNumber).coerceAtLeast(1)
        val prog = PlayerProgression(
            highScore = cur.highScore,
            highestLevel = maxOf(cur.highestLevel, activeLv),
            activeLevelNumber = activeLv,
            cumulativeScore = (overrideCumulativeScore ?: cur.cumulativeScore).coerceAtLeast(0),
            coins = cur.coins,
            unlockedCharacterIds = cur.unlockedCharacterIds,
            equippedCharacterId = cur.equippedCharacterId,
            unlockedRoadIds = cur.unlockedRoadIds,
            equippedRoadId = cur.equippedRoadId,
            bestTimesByLevel = cur.bestTimesByLevel,
            bestShardsByLevel = cur.bestShardsByLevel,
            lastOfflineBonusClaimMs = cur.lastOfflineBonusClaimMs
        )
        viewModelScope.launch {
            repository.saveProgression(prog)
        }
    }

    fun stepSimulation(
        dt: Float,
        keyMoveDir: Float,
        keyJumpHeld: Boolean,
        keyDashPressed: Boolean
    ): HapticCue? {
        val state = _uiState.value
        val shouldPause = state.isPaused || state.destination != AppDestination.PLAY
        if (!shouldPause) {
            if (keyMoveDir != 0f || keyJumpHeld || keyDashPressed) {
                val combinedMove = if (liveInput.moveDir != 0f) liveInput.moveDir else keyMoveDir
                engine.setControls(
                    moveDir = combinedMove,
                    isJumpHeld = liveInput.jumpHeld || keyJumpHeld,
                    isDashPressed = keyDashPressed
                )
            } else {
                engine.applyLiveInput(liveInput)
            }
        }
        engine.step(dt, paused = shouldPause)
        frameTick++

        val snap = engine.snapshot()
        if (snap.elapsedMs - lastHudTimerSyncMs >= 80L || snap.stageCleared || snap.isPlayerOut) {
            lastHudTimerSyncMs = snap.elapsedMs
            hudElapsedMs = snap.elapsedMs
        }

        val prev = state.snapshot
        val discreteChanged = snap.stageScore != prev.stageScore ||
            snap.stageCoins != prev.stageCoins ||
            snap.shardsCollected != prev.shardsCollected ||
            snap.combo != prev.combo ||
            snap.canDash != prev.canDash ||
            snap.deaths != prev.deaths ||
            snap.isPlayerOut != prev.isPlayerOut ||
            snap.stageCleared != prev.stageCleared

        if (discreteChanged) {
            val deltaCoins = (snap.stageCoins - lastCreditedStageCoins).coerceAtLeast(0)
            lastCreditedStageCoins = snap.stageCoins
            val newTotalCoins = state.coins + deltaCoins
            val liveTotal = state.cumulativeScore + snap.stageScore
            val newHighScore = maxOf(state.highScore, liveTotal)

            _uiState.update { current ->
                var updatedBestTimes = current.bestTimesByLevel
                var updatedBestShards = current.bestShardsByLevel
                var updatedHighest = current.highestLevel
                if (snap.stageCleared && !prev.stageCleared) {
                    val lv = current.activeLevelNumber
                    updatedHighest = maxOf(updatedHighest, lv + 1)
                    val prevBest = current.bestTimesByLevel[lv]
                    if (prevBest == null || snap.elapsedMs < prevBest) {
                        updatedBestTimes = updatedBestTimes + (lv to snap.elapsedMs)
                    }
                    val prevShards = current.bestShardsByLevel[lv] ?: 0
                    if (snap.shardsCollected > prevShards) {
                        updatedBestShards = updatedBestShards + (lv to snap.shardsCollected)
                    }
                }
                current.copy(
                    snapshot = snap.copy(elapsedMs = hudElapsedMs),
                    coins = newTotalCoins,
                    highScore = newHighScore,
                    highestLevel = updatedHighest,
                    bestTimesByLevel = updatedBestTimes,
                    bestShardsByLevel = updatedBestShards
                )
            }

            if (snap.stageCleared && !prev.stageCleared) {
                persistCurrentProgression(
                    overrideActiveLevel = state.activeLevelNumber + 1,
                    overrideCumulativeScore = liveTotal
                )
            } else if (deltaCoins > 0 || newHighScore > state.highScore || (snap.isPlayerOut && !prev.isPlayerOut)) {
                persistCurrentProgression()
            }
        }

        val cue = engine.consumeHapticCue()
        return if (state.physicsConfig.hapticsEnabled) cue else null
    }

    companion object {
        const val OFFLINE_BONUS_COINS = 50
        const val OFFLINE_BONUS_COOLDOWN_MS = 2 * 60 * 60 * 1000L
    }
}
