package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.tuningDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "volt_dash_tuning"
)

data class PhysicsConfig(
    val presetName: String = "Arcade Tight",
    val runSpeed: Float = 166f,
    val jumpVelocity: Float = 294f,
    val gravity: Float = 880f,
    val dashSpeed: Float = 380f,
    val coyoteTimeMs: Int = 120,
    val jumpBufferMs: Int = 135,
    val showHitboxes: Boolean = false,
    val showPixelGrid: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = true
) {
    companion object {
        val ArcadeTight = PhysicsConfig(
            presetName = "Arcade Tight",
            runSpeed = 166f,
            jumpVelocity = 294f,
            gravity = 880f,
            dashSpeed = 380f,
            coyoteTimeMs = 120,
            jumpBufferMs = 135
        )

        val TurboSpeedrun = PhysicsConfig(
            presetName = "Turbo Speedrun",
            runSpeed = 198f,
            jumpVelocity = 315f,
            gravity = 980f,
            dashSpeed = 430f,
            coyoteTimeMs = 130,
            jumpBufferMs = 150
        )

        val LowGravMoon = PhysicsConfig(
            presetName = "Low-G Synth",
            runSpeed = 148f,
            jumpVelocity = 250f,
            gravity = 540f,
            dashSpeed = 345f,
            coyoteTimeMs = 145,
            jumpBufferMs = 145
        )
    }
}

data class AdSettings(
    val adsEnabled: Boolean = true,
    val offlineOnlyMode: Boolean = false,
    val bannerUnitId: String = BuildConfig.ADMOB_BANNER_ID,
    val rewardedUnitId: String = BuildConfig.ADMOB_REWARDED_ID,
    val interstitialUnitId: String = BuildConfig.ADMOB_INTERSTITIAL_ID
)

data class PlayerProgression(
    val highScore: Int = 0,
    val highestLevel: Int = 1,
    val activeLevelNumber: Int = 1,
    val cumulativeScore: Int = 0,
    val coins: Int = 0,
    val unlockedCharacterIds: Set<String> = setOf("volt"),
    val equippedCharacterId: String = "volt",
    val unlockedRoadIds: Set<String> = setOf("neon_highway"),
    val equippedRoadId: String = "neon_highway",
    val bestTimesByLevel: Map<Int, Long> = emptyMap(),
    val bestShardsByLevel: Map<Int, Int> = emptyMap(),
    val lastOfflineBonusClaimMs: Long = 0L
)

class TuningPreferencesRepository(private val context: Context) {

    private object Keys {
        val PRESET_NAME = stringPreferencesKey("preset_name")
        val RUN_SPEED = floatPreferencesKey("run_speed")
        val JUMP_VELOCITY = floatPreferencesKey("jump_velocity")
        val GRAVITY = floatPreferencesKey("gravity")
        val DASH_SPEED = floatPreferencesKey("dash_speed")
        val COYOTE_MS = intPreferencesKey("coyote_ms")
        val SHOW_HITBOXES = booleanPreferencesKey("show_hitboxes")
        val SHOW_PIXEL_GRID = booleanPreferencesKey("show_pixel_grid")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")

        val HIGH_SCORE = intPreferencesKey("offline_high_score")
        val HIGHEST_LEVEL = intPreferencesKey("offline_highest_level")
        val ACTIVE_LEVEL = intPreferencesKey("offline_active_level")
        val CUMULATIVE_SCORE = intPreferencesKey("offline_cumulative_score")
        val COINS = intPreferencesKey("offline_coins")
        val UNLOCKED_CHARS = stringSetPreferencesKey("unlocked_characters")
        val EQUIPPED_CHAR = stringPreferencesKey("equipped_character")
        val UNLOCKED_ROADS = stringSetPreferencesKey("unlocked_roads")
        val EQUIPPED_ROAD = stringPreferencesKey("equipped_road")
        val BEST_TIMES = stringPreferencesKey("offline_best_times")
        val BEST_SHARDS = stringPreferencesKey("offline_best_shards")
        val LAST_OFFLINE_BONUS_MS = longPreferencesKey("offline_last_bonus_ms")

        val ADS_ENABLED = booleanPreferencesKey("ads_enabled")
        val OFFLINE_ONLY_MODE = booleanPreferencesKey("offline_only_mode")
        val BANNER_ID = stringPreferencesKey("ad_banner_id")
        val REWARDED_ID = stringPreferencesKey("ad_rewarded_id")
        val INTERSTITIAL_ID = stringPreferencesKey("ad_interstitial_id")
    }

    val physicsConfigFlow: Flow<PhysicsConfig> = context.tuningDataStore.data.map { prefs ->
        PhysicsConfig(
            presetName = prefs[Keys.PRESET_NAME] ?: PhysicsConfig.ArcadeTight.presetName,
            runSpeed = prefs[Keys.RUN_SPEED] ?: PhysicsConfig.ArcadeTight.runSpeed,
            jumpVelocity = prefs[Keys.JUMP_VELOCITY] ?: PhysicsConfig.ArcadeTight.jumpVelocity,
            gravity = prefs[Keys.GRAVITY] ?: PhysicsConfig.ArcadeTight.gravity,
            dashSpeed = prefs[Keys.DASH_SPEED] ?: PhysicsConfig.ArcadeTight.dashSpeed,
            coyoteTimeMs = prefs[Keys.COYOTE_MS] ?: PhysicsConfig.ArcadeTight.coyoteTimeMs,
            jumpBufferMs = PhysicsConfig.ArcadeTight.jumpBufferMs,
            showHitboxes = prefs[Keys.SHOW_HITBOXES] ?: false,
            showPixelGrid = prefs[Keys.SHOW_PIXEL_GRID] ?: false,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true
        )
    }

    val progressionFlow: Flow<PlayerProgression> = context.tuningDataStore.data.map { prefs ->
        val highestLv = (prefs[Keys.HIGHEST_LEVEL] ?: 1).coerceAtLeast(1)
        val activeLv = (prefs[Keys.ACTIVE_LEVEL] ?: highestLv).coerceAtLeast(1)
        PlayerProgression(
            highScore = prefs[Keys.HIGH_SCORE] ?: 0,
            highestLevel = maxOf(highestLv, activeLv),
            activeLevelNumber = activeLv,
            cumulativeScore = (prefs[Keys.CUMULATIVE_SCORE] ?: 0).coerceAtLeast(0),
            coins = prefs[Keys.COINS] ?: 0,
            unlockedCharacterIds = prefs[Keys.UNLOCKED_CHARS] ?: setOf("volt"),
            equippedCharacterId = prefs[Keys.EQUIPPED_CHAR] ?: "volt",
            unlockedRoadIds = prefs[Keys.UNLOCKED_ROADS] ?: setOf("neon_highway"),
            equippedRoadId = prefs[Keys.EQUIPPED_ROAD] ?: "neon_highway",
            bestTimesByLevel = decodeIntLongMap(prefs[Keys.BEST_TIMES]),
            bestShardsByLevel = decodeIntIntMap(prefs[Keys.BEST_SHARDS]),
            lastOfflineBonusClaimMs = prefs[Keys.LAST_OFFLINE_BONUS_MS] ?: 0L
        )
    }

    val adSettingsFlow: Flow<AdSettings> = context.tuningDataStore.data.map { prefs ->
        AdSettings(
            adsEnabled = prefs[Keys.ADS_ENABLED] ?: true,
            offlineOnlyMode = prefs[Keys.OFFLINE_ONLY_MODE] ?: false,
            bannerUnitId = prefs[Keys.BANNER_ID]?.takeIf { it.isNotBlank() } ?: BuildConfig.ADMOB_BANNER_ID,
            rewardedUnitId = prefs[Keys.REWARDED_ID]?.takeIf { it.isNotBlank() } ?: BuildConfig.ADMOB_REWARDED_ID,
            interstitialUnitId = prefs[Keys.INTERSTITIAL_ID]?.takeIf { it.isNotBlank() } ?: BuildConfig.ADMOB_INTERSTITIAL_ID
        )
    }

    suspend fun savePhysicsConfig(config: PhysicsConfig) {
        context.tuningDataStore.edit { prefs ->
            prefs[Keys.PRESET_NAME] = config.presetName
            prefs[Keys.RUN_SPEED] = config.runSpeed
            prefs[Keys.JUMP_VELOCITY] = config.jumpVelocity
            prefs[Keys.GRAVITY] = config.gravity
            prefs[Keys.DASH_SPEED] = config.dashSpeed
            prefs[Keys.COYOTE_MS] = config.coyoteTimeMs
            prefs[Keys.SHOW_HITBOXES] = config.showHitboxes
            prefs[Keys.SHOW_PIXEL_GRID] = config.showPixelGrid
            prefs[Keys.HAPTICS_ENABLED] = config.hapticsEnabled
            prefs[Keys.SOUND_ENABLED] = config.soundEnabled
        }
    }

    suspend fun saveProgression(progression: PlayerProgression) {
        context.tuningDataStore.edit { prefs ->
            val safeActive = progression.activeLevelNumber.coerceAtLeast(1)
            val safeHighest = maxOf(progression.highestLevel.coerceAtLeast(1), safeActive)
            prefs[Keys.HIGH_SCORE] = progression.highScore
            prefs[Keys.HIGHEST_LEVEL] = safeHighest
            prefs[Keys.ACTIVE_LEVEL] = safeActive
            prefs[Keys.CUMULATIVE_SCORE] = progression.cumulativeScore.coerceAtLeast(0)
            prefs[Keys.COINS] = progression.coins
            prefs[Keys.UNLOCKED_CHARS] = progression.unlockedCharacterIds
            prefs[Keys.EQUIPPED_CHAR] = progression.equippedCharacterId
            prefs[Keys.UNLOCKED_ROADS] = progression.unlockedRoadIds
            prefs[Keys.EQUIPPED_ROAD] = progression.equippedRoadId
            prefs[Keys.BEST_TIMES] = encodeIntLongMap(progression.bestTimesByLevel)
            prefs[Keys.BEST_SHARDS] = encodeIntIntMap(progression.bestShardsByLevel)
            prefs[Keys.LAST_OFFLINE_BONUS_MS] = progression.lastOfflineBonusClaimMs
        }
    }

    suspend fun saveAdSettings(settings: AdSettings) {
        context.tuningDataStore.edit { prefs ->
            prefs[Keys.ADS_ENABLED] = settings.adsEnabled
            prefs[Keys.OFFLINE_ONLY_MODE] = settings.offlineOnlyMode
            prefs[Keys.BANNER_ID] = settings.bannerUnitId.trim()
            prefs[Keys.REWARDED_ID] = settings.rewardedUnitId.trim()
            prefs[Keys.INTERSTITIAL_ID] = settings.interstitialUnitId.trim()
        }
    }

    companion object {
        fun encodeIntLongMap(map: Map<Int, Long>): String {
            if (map.isEmpty()) return ""
            return map.entries.sortedBy { it.key }.joinToString(",") { "${it.key}:${it.value}" }
        }

        fun decodeIntLongMap(raw: String?): Map<Int, Long> {
            if (raw.isNullOrBlank()) return emptyMap()
            return buildMap {
                raw.split(',').forEach { entry ->
                    val parts = entry.split(':')
                    if (parts.size == 2) {
                        val k = parts[0].toIntOrNull()
                        val v = parts[1].toLongOrNull()
                        if (k != null && v != null) put(k, v)
                    }
                }
            }
        }

        fun encodeIntIntMap(map: Map<Int, Int>): String {
            if (map.isEmpty()) return ""
            return map.entries.sortedBy { it.key }.joinToString(",") { "${it.key}:${it.value}" }
        }

        fun decodeIntIntMap(raw: String?): Map<Int, Int> {
            if (raw.isNullOrBlank()) return emptyMap()
            return buildMap {
                raw.split(',').forEach { entry ->
                    val parts = entry.split(':')
                    if (parts.size == 2) {
                        val k = parts[0].toIntOrNull()
                        val v = parts[1].toIntOrNull()
                        if (k != null && v != null) put(k, v)
                    }
                }
            }
        }
    }
}
