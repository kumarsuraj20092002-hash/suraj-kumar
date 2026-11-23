package com.example

import com.example.data.PhysicsConfig
import com.example.game.LevelDefinitions
import com.example.game.LiveInputState
import com.example.game.PlatformerEngine
import com.example.game.RoadCatalog
import com.example.game.RunnerCatalog
import com.example.game.SoundEffect
import com.example.game.SoundPlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlatformerEngineTest {

    @Test
    fun handcraftedAndUnlimitedProceduralLevelsAreValidAndPlayable() {
        assertEquals(3, LevelDefinitions.stages.size)
        for (level in 1..25) {
            val stage = LevelDefinitions.getStageForLevel(level)
            assertEquals(level, stage.levelNumber)
            assertEquals(12, stage.heightTiles)
            val expectedWidth = stage.rows.first().length
            assertTrue("Level $level too short", expectedWidth >= 60)
            stage.rows.forEach { row ->
                assertEquals("Row length mismatch in level $level", expectedWidth, row.length)
            }
            val joined = stage.rows.joinToString("")
            assertTrue("Level $level missing spawn P", joined.contains('P'))
            assertTrue("Level $level missing goal G", joined.contains('G'))
            assertTrue("Level $level missing shards C", joined.contains('C'))
        }
    }

    @Test
    fun liveInputLatchingAndScoreProgressionWorkWithoutDroppingTaps() {
        val engine = PlatformerEngine(LevelDefinitions.getStageForLevel(1), PhysicsConfig.ArcadeTight)
        val liveInput = LiveInputState()

        // Settle on floor
        repeat(15) {
            engine.applyLiveInput(liveInput)
            engine.step(0.016f, paused = false)
        }
        assertTrue(engine.player.onGround)

        // Run right and collect shards -> score & coins should rise
        liveInput.moveDir = 1f
        repeat(45) {
            engine.applyLiveInput(liveInput)
            engine.step(0.016f, paused = false)
        }
        assertTrue(engine.snapshot().shardsCollected >= 1)
        assertTrue(engine.snapshot().stageScore >= 200)
        assertTrue(engine.snapshot().stageCoins >= 5)

        // Single-frame latched jump tap + latched dash tap
        liveInput.jumpPressLatch = true
        liveInput.jumpHeld = false
        engine.applyLiveInput(liveInput)
        engine.step(0.016f, paused = false)
        assertTrue(engine.player.vy < 0f)

        liveInput.dashPressLatch = true
        engine.applyLiveInput(liveInput)
        engine.step(0.016f, paused = false)
        assertFalse(engine.player.canAirDash)
        assertTrue(engine.player.dashTimer > 0f)
    }

    @Test
    fun characterAndRoadCatalogsAndPlayerOutWorkOffline() {
        assertEquals(8, RunnerCatalog.characters.size)
        assertEquals(6, RoadCatalog.roads.size)
        assertEquals(0, RunnerCatalog.characters.first().priceCoins)
        assertEquals(0, RoadCatalog.roads.first().priceCoins)

        val engine = PlatformerEngine(
            initialStage = LevelDefinitions.getStageForLevel(1),
            config = PhysicsConfig.ArcadeTight,
            activeCharacter = RunnerCatalog.characters.last(),
            activeRoad = RoadCatalog.roads.last()
        )
        assertFalse(engine.snapshot().isPlayerOut)
        engine.triggerPlayerOut()
        assertTrue(engine.snapshot().isPlayerOut)
        assertEquals(1, engine.snapshot().deaths)

        engine.continueFromCheckpoint()
        assertFalse(engine.snapshot().isPlayerOut)
    }

    @Test
    fun soundManagerTriggersJumpCoinAndHitObstacleEffects() {
        val recorded = mutableListOf<SoundEffect>()
        val fakeSoundManager = object : SoundPlayer {
            private var enabled = true
            override fun play(effect: SoundEffect) {
                if (enabled) recorded += effect
            }
            override fun setEnabled(enabled: Boolean) {
                this.enabled = enabled
            }
            override fun isEnabled(): Boolean = enabled
            override fun release() = Unit
        }

        val engine = PlatformerEngine(
            initialStage = LevelDefinitions.getStageForLevel(1),
            config = PhysicsConfig.ArcadeTight,
            soundManager = fakeSoundManager
        )
        val liveInput = LiveInputState()

        // Settle on ground
        repeat(15) {
            engine.applyLiveInput(liveInput)
            engine.step(0.016f, paused = false)
        }

        // Jump -> SoundEffect.JUMP
        liveInput.jumpPressLatch = true
        engine.applyLiveInput(liveInput)
        engine.step(0.016f, paused = false)
        assertTrue(recorded.contains(SoundEffect.JUMP))

        // Collect shard/coin -> SoundEffect.COIN
        liveInput.moveDir = 1f
        repeat(45) {
            engine.applyLiveInput(liveInput)
            engine.step(0.016f, paused = false)
        }
        assertTrue(recorded.contains(SoundEffect.COIN))

        // Hit obstacle -> SoundEffect.HIT_OBSTACLE
        engine.triggerPlayerOut()
        assertTrue(recorded.contains(SoundEffect.HIT_OBSTACLE))

        // Verify disabling soundManager prevents new recordings
        val countBeforeMute = recorded.size
        fakeSoundManager.setEnabled(false)
        assertFalse(fakeSoundManager.isEnabled())
        engine.continueFromCheckpoint()
        liveInput.jumpPressLatch = true
        engine.applyLiveInput(liveInput)
        engine.step(0.016f, paused = false)
        assertEquals(countBeforeMute, recorded.size)
    }

    @Test
    fun roadSpecificLevelsAreDistinctAndValid() {
        val level1Layouts = mutableSetOf<String>()
        for (road in RoadCatalog.roads) {
            val stage = LevelDefinitions.getStageForLevel(1, road.id)
            assertEquals(12, stage.heightTiles)
            val w = stage.rows.first().length
            assertTrue("Road ${road.id} level too short", w >= 55)
            stage.rows.forEach { row ->
                assertEquals("Row length mismatch for road ${road.id}", w, row.length)
            }
            val joined = stage.rows.joinToString("")
            assertTrue("Missing spawn P in ${road.id}", joined.contains('P'))
            assertTrue("Missing goal G in ${road.id}", joined.contains('G'))
            assertTrue("Missing checkpoint K in ${road.id}", joined.contains('K') || road.id == "neon_highway")
            assertTrue("Missing shards C in ${road.id}", joined.contains('C'))
            level1Layouts += joined
        }
        // Every road must produce its own distinct level layout, not just recolored tiles
        assertEquals(RoadCatalog.roads.size, level1Layouts.size)
    }

    @Test
    fun springBounceFiresOncePerBounceAndRoadMultiplierScalesRewards() {
        val recorded = mutableListOf<SoundEffect>()
        val spySoundManager = object : SoundPlayer {
            override fun play(effect: SoundEffect) {
                recorded += effect
            }
            override fun setEnabled(enabled: Boolean) = Unit
            override fun isEnabled(): Boolean = true
            override fun release() = Unit
        }
        val customStage = com.example.game.StageDefinition(
            id = 99,
            levelNumber = 99,
            code = "9-9",
            name = "Spring Test",
            subtitle = "Verify spring and reward scaling",
            biome = com.example.game.BiomeType.SKYLINE,
            difficulty = "TEST",
            parTimeMs = 15_000L,
            designSummary = "Unit test stage",
            pacingBeats = emptyList(),
            rows = listOf(
                "..................",
                "..................",
                "..................",
                "..................",
                "..................",
                "..................",
                "..................",
                "..................",
                "..................",
                "..................",
                ".P.C..S.........G.",
                "##################"
            )
        )
        val richRoad = RoadCatalog.roads.last() // +30% coin bonus
        val engine = PlatformerEngine(
            initialStage = customStage,
            config = PhysicsConfig.ArcadeTight,
            activeCharacter = RunnerCatalog.characters.first(), // 1.0x
            activeRoad = richRoad,
            soundManager = spySoundManager
        )
        val liveInput = LiveInputState()
        // Run right over shard and onto spring
        liveInput.moveDir = 1f
        repeat(60) {
            engine.applyLiveInput(liveInput)
            engine.step(0.016f, paused = false)
        }
        val springCount = recorded.count { it == SoundEffect.SPRING }
        assertTrue("Expected spring bounce SFX to fire", springCount >= 1)
        // Because player launches at -vy > 40f, stepping 1 frame on pad must not double-fire
        assertEquals("Spring should fire once per bounce rather than double-triggering", 1, springCount)
        assertTrue("Expected +30% road coin bonus on shard (5 * 1.3 = 7)", engine.snapshot().stageCoins >= 7)
    }

    @Test
    fun soundPlayerHandlesMenuNavigationAndButtonClicksGlobally() {
        val played = mutableListOf<SoundEffect>()
        val globalPlayer = object : SoundPlayer {
            private var enabled = true
            override fun play(effect: SoundEffect) {
                if (enabled) played += effect
            }
            override fun setEnabled(enabled: Boolean) {
                this.enabled = enabled
            }
            override fun isEnabled(): Boolean = enabled
            override fun release() = Unit
        }

        globalPlayer.playMenuNavigate()
        globalPlayer.playButtonClick()
        globalPlayer.playUnlockSuccess()
        globalPlayer.playActionDenied()
        assertEquals(
            listOf(
                SoundEffect.MENU_NAVIGATE,
                SoundEffect.BUTTON_CLICK,
                SoundEffect.UNLOCK_SUCCESS,
                SoundEffect.ACTION_DENIED
            ),
            played
        )

        globalPlayer.setEnabled(false)
        globalPlayer.playButtonClick()
        globalPlayer.playMenuNavigate()
        assertEquals(4, played.size)
    }

    @Test
    fun offlineProgressionSerializationAndPureOfflineStateWorkWithoutInternet() {
        val bestTimes = mapOf(1 to 12_450L, 3 to 24_800L, 7 to 31_100L)
        val encodedTimes = com.example.data.TuningPreferencesRepository.encodeIntLongMap(bestTimes)
        assertEquals(
            bestTimes,
            com.example.data.TuningPreferencesRepository.decodeIntLongMap(encodedTimes)
        )

        val bestShards = mapOf(1 to 9, 3 to 12)
        val encodedShards = com.example.data.TuningPreferencesRepository.encodeIntIntMap(bestShards)
        assertEquals(
            bestShards,
            com.example.data.TuningPreferencesRepository.decodeIntIntMap(encodedShards)
        )

        val pureOfflineUi = com.example.ui.GameUiState(
            isOnline = true,
            adSettings = com.example.data.AdSettings(offlineOnlyMode = true),
            lastOfflineBonusClaimMs = 0L
        )
        assertFalse("effectiveOnline must be false when offlineOnlyMode is active", pureOfflineUi.effectiveOnline)
        assertTrue("First offline bonus claim must be immediately ready", pureOfflineUi.isOfflineBonusReady(100_000L))

        val recentlyClaimedUi = pureOfflineUi.copy(lastOfflineBonusClaimMs = 100_000L)
        assertFalse(recentlyClaimedUi.isOfflineBonusReady(100_000L + 60_000L))
        assertTrue(recentlyClaimedUi.isOfflineBonusReady(100_000L + com.example.ui.GameViewModel.OFFLINE_BONUS_COOLDOWN_MS))
    }

    @Test
    fun unlimitedLevelsAndResumeProgressionDefaultsAreConsistent() {
        val highLevels = listOf(1, 4, 25, 100, 500)
        for (lv in highLevels) {
            for (road in RoadCatalog.roads) {
                val stage = LevelDefinitions.getStageForLevel(lv, road.id)
                assertEquals(lv, stage.levelNumber)
                val joined = stage.rows.joinToString("")
                assertTrue("Level $lv road ${road.id} missing spawn P", joined.contains('P'))
                assertTrue("Level $lv road ${road.id} missing goal G", joined.contains('G'))
            }
        }
        val saved = com.example.data.PlayerProgression(
            activeLevelNumber = 14,
            highestLevel = 14,
            cumulativeScore = 18_450
        )
        assertEquals(14, saved.activeLevelNumber)
        assertEquals(18_450, saved.cumulativeScore)
    }
}
