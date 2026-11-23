package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.data.PhysicsConfig
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sign

enum class HeroAnimState {
    IDLE, RUN, JUMP, FALL, DASH, WALL_SLIDE
}

enum class HapticCue {
    JUMP, DASH, SHARD, STOMP, SPRING, HIT, CLEAR
}

/**
 * Lock-free live input buffer updated directly by touch/key handlers
 * so fast taps never wait for a Compose recomposition cycle.
 */
class LiveInputState {
    @Volatile var moveDir: Float = 0f
    @Volatile var jumpHeld: Boolean = false
    @Volatile var jumpPressLatch: Boolean = false
    @Volatile var dashPressLatch: Boolean = false
}

data class PlayerState(
    var x: Float = 24f,
    var y: Float = 100f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val width: Float = 11f,
    val height: Float = 14f,
    var facingRight: Boolean = true,
    var onGround: Boolean = false,
    var wallDir: Int = 0,
    var coyoteTimer: Float = 0f,
    var jumpBufferTimer: Float = 0f,
    var canAirDash: Boolean = true,
    var dashTimer: Float = 0f,
    var dashCooldownTimer: Float = 0f,
    var dashDirX: Float = 1f,
    var animState: HeroAnimState = HeroAnimState.IDLE,
    var animClock: Float = 0f
)

data class ShardEntity(
    val id: Int,
    val x: Float,
    val y: Float,
    var collected: Boolean = false
)

data class CrystalEntity(
    val id: Int,
    val x: Float,
    val y: Float,
    var cooldownTimer: Float = 0f
) {
    val isAvailable: Boolean get() = cooldownTimer <= 0f
}

data class DroneEntity(
    val id: Int,
    val spawnX: Float,
    val spawnY: Float,
    var x: Float,
    var y: Float,
    var vx: Float = 52f,
    var alive: Boolean = true
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val maxLife: Float,
    val size: Float,
    val color: Color
)

data class Afterimage(
    val x: Float,
    val y: Float,
    val facingRight: Boolean,
    var life: Float,
    val maxLife: Float = 0.14f
)

data class WorldSnapshot(
    val levelNumber: Int = 1,
    val elapsedMs: Long = 0L,
    val stageScore: Int = 0,
    val stageCoins: Int = 0,
    val shardsCollected: Int = 0,
    val totalShards: Int = 0,
    val combo: Int = 1,
    val maxCombo: Int = 1,
    val canDash: Boolean = true,
    val deaths: Int = 0,
    val isPlayerOut: Boolean = false,
    val stageCleared: Boolean = false
)

class PlatformerEngine(
    initialStage: StageDefinition,
    var config: PhysicsConfig,
    var activeCharacter: RunnerCharacterProfile = RunnerCatalog.characters.first(),
    var activeRoad: RoadThemeProfile = RoadCatalog.roads.first(),
    var soundManager: SoundPlayer = NoOpSoundPlayer
) {
    companion object {
        const val TILE_SIZE = 16f
    }

    var stage: StageDefinition = initialStage
        private set

    private var grid: Array<CharArray> = emptyArray()

    val player = PlayerState()
    val shards = mutableListOf<ShardEntity>()
    val crystals = mutableListOf<CrystalEntity>()
    val drones = mutableListOf<DroneEntity>()
    val particles = mutableListOf<Particle>()
    val afterimages = mutableListOf<Afterimage>()

    private var spawnX = 24f
    private var spawnY = 100f
    private var checkpointX = 24f
    private var checkpointY = 100f
    private var checkpointActivated = false
    private var checkpointTilePos: Pair<Int, Int>? = null

    var goalX = 0f
        private set
    var goalY = 0f
        private set

    var cameraX = 0f
        private set
    var cameraY = 0f
        private set
    var visibleLogicalWidth = 240f
    var shakeIntensity = 0f
        private set
    var respawnFlashTimer = 0f
        private set
    var worldTime = 0f
        private set

    private var elapsedSec = 0f
    private var stageScore = 0
    private var stageCoins = 0
    private var combo = 1
    private var maxCombo = 1
    private var comboTimer = 0f
    private var deaths = 0
    private var playerOut = false
    private var stageCleared = false

    // Input state
    var moveInput: Float = 0f
    private var jumpHeld = false
    private var prevJumpHeld = false
    private var prevDashPressed = false

    private var pendingCue: HapticCue? = null

    init {
        loadStage(initialStage)
    }

    fun loadStage(newStage: StageDefinition) {
        stage = newStage
        val h = newStage.heightTiles
        val w = newStage.widthTiles
        grid = Array(h) { r ->
            val rowStr = newStage.rows[r]
            CharArray(w) { c -> if (c < rowStr.length) rowStr[c] else '.' }
        }
        shards.clear()
        crystals.clear()
        drones.clear()
        particles.clear()
        afterimages.clear()

        var shardId = 0
        var crystalId = 0
        var droneId = 0
        checkpointActivated = false
        checkpointTilePos = null

        for (r in 0 until h) {
            for (c in 0 until w) {
                val ch = grid[r][c]
                val wx = c * TILE_SIZE
                val wy = r * TILE_SIZE
                when (ch) {
                    'P' -> {
                        spawnX = wx + 2f
                        spawnY = wy + 1f
                        checkpointX = spawnX
                        checkpointY = spawnY
                        grid[r][c] = '.'
                    }
                    'G' -> {
                        goalX = wx
                        goalY = wy
                        grid[r][c] = '.'
                    }
                    'C' -> {
                        shards.add(ShardEntity(shardId++, wx, wy))
                        grid[r][c] = '.'
                    }
                    'D' -> {
                        crystals.add(CrystalEntity(crystalId++, wx, wy))
                        grid[r][c] = '.'
                    }
                    'E' -> {
                        drones.add(DroneEntity(droneId++, wx, wy, wx, wy))
                        grid[r][c] = '.'
                    }
                    'K' -> {
                        checkpointTilePos = c to r
                        grid[r][c] = '.'
                    }
                }
            }
        }

        elapsedSec = 0f
        stageScore = 0
        stageCoins = 0
        combo = 1
        maxCombo = 1
        comboTimer = 0f
        deaths = 0
        playerOut = false
        stageCleared = false
        respawnFlashTimer = 0f
        shakeIntensity = 0f

        resetPlayerTo(spawnX, spawnY)
        cameraX = max(0f, player.x - 100f)
        cameraY = 0f
    }

    private fun resetPlayerTo(px: Float, py: Float) {
        player.x = px
        player.y = py
        player.vx = 0f
        player.vy = 0f
        player.facingRight = true
        player.onGround = false
        player.wallDir = 0
        player.coyoteTimer = 0f
        player.jumpBufferTimer = 0f
        player.canAirDash = true
        player.dashTimer = 0f
        player.dashCooldownTimer = 0f
        player.animState = HeroAnimState.IDLE
    }

    fun triggerPlayerOut() {
        if (playerOut || stageCleared) return
        deaths++
        playerOut = true
        respawnFlashTimer = 0.25f
        shakeIntensity = 5.0f
        emitBurst(player.x + 5f, player.y + 7f, Color(0xFFFB7185), 18, 125f)
        combo = 1
        comboTimer = 0f
        pendingCue = HapticCue.HIT
        soundManager.play(SoundEffect.HIT_OBSTACLE)
    }

    fun continueFromCheckpoint() {
        playerOut = false
        respawnFlashTimer = 0.14f
        resetPlayerTo(checkpointX, checkpointY)
        crystals.forEach { it.cooldownTimer = 0f }
        drones.forEach { d ->
            d.alive = true
            d.x = d.spawnX
            d.y = d.spawnY
        }
        combo = 1
        comboTimer = 0f
    }

    fun applyLiveInput(input: LiveInputState) {
        moveInput = input.moveDir.coerceIn(-1f, 1f)
        val jumpEdge = input.jumpPressLatch || (input.jumpHeld && !prevJumpHeld)
        input.jumpPressLatch = false
        jumpHeld = input.jumpHeld
        if (jumpEdge) {
            player.jumpBufferTimer = config.jumpBufferMs / 1000f
        }
        val dashEdge = input.dashPressLatch
        input.dashPressLatch = false
        if (dashEdge) {
            tryDash()
        }
        prevJumpHeld = input.jumpHeld
    }

    fun setControls(moveDir: Float, isJumpHeld: Boolean, isDashPressed: Boolean) {
        moveInput = moveDir.coerceIn(-1f, 1f)
        jumpHeld = isJumpHeld
        if (isJumpHeld && !prevJumpHeld) {
            player.jumpBufferTimer = config.jumpBufferMs / 1000f
        }
        if (isDashPressed && !prevDashPressed) {
            tryDash()
        }
        prevJumpHeld = isJumpHeld
        prevDashPressed = isDashPressed
    }

    private fun tryDash() {
        if (stageCleared || playerOut || !player.canAirDash || player.dashCooldownTimer > 0f || player.dashTimer > 0f) {
            return
        }
        player.canAirDash = false
        player.dashTimer = 0.145f
        player.dashCooldownTimer = activeCharacter.dashCooldownSec
        val dir = if (abs(moveInput) > 0.1f) sign(moveInput) else if (player.facingRight) 1f else -1f
        player.dashDirX = dir
        player.facingRight = dir > 0f
        player.vx = dir * config.dashSpeed
        player.vy = 0f
        shakeIntensity = 2.6f
        emitBurst(
            player.x + player.width / 2f,
            player.y + player.height / 2f,
            activeCharacter.primaryColor,
            10,
            95f
        )
        pendingCue = HapticCue.DASH
        soundManager.play(SoundEffect.DASH)
    }

    fun consumeHapticCue(): HapticCue? {
        val cue = pendingCue
        pendingCue = null
        return cue
    }

    fun step(rawDt: Float, paused: Boolean) {
        val dt = rawDt.coerceIn(0f, 0.032f)
        worldTime += dt

        if (paused || stageCleared || playerOut) {
            if (respawnFlashTimer > 0f) respawnFlashTimer = max(0f, respawnFlashTimer - dt)
            if (shakeIntensity > 0f) shakeIntensity = max(0f, shakeIntensity - dt * 18f)
            updateParticles(dt)
            return
        }

        elapsedSec += dt
        if (respawnFlashTimer > 0f) respawnFlashTimer = max(0f, respawnFlashTimer - dt)
        if (shakeIntensity > 0f) shakeIntensity = max(0f, shakeIntensity - dt * 18f)
        if (comboTimer > 0f) {
            comboTimer -= dt
            if (comboTimer <= 0f) combo = 1
        }

        crystals.forEach {
            if (it.cooldownTimer > 0f) it.cooldownTimer = max(0f, it.cooldownTimer - dt)
        }

        if (player.coyoteTimer > 0f) player.coyoteTimer = max(0f, player.coyoteTimer - dt)
        if (player.jumpBufferTimer > 0f) player.jumpBufferTimer = max(0f, player.jumpBufferTimer - dt)
        if (player.dashCooldownTimer > 0f) player.dashCooldownTimer = max(0f, player.dashCooldownTimer - dt)

        val effectiveRunSpeed = config.runSpeed * activeCharacter.speedMultiplier
        val effectiveJumpVel = config.jumpVelocity * activeCharacter.jumpMultiplier

        if (player.dashTimer > 0f) {
            player.dashTimer -= dt
            player.vx = player.dashDirX * config.dashSpeed
            player.vy = 0f
            if ((worldTime * 60f).toInt() % 2 == 0) {
                afterimages.add(Afterimage(player.x, player.y, player.facingRight, 0.14f))
            }
            if (player.dashTimer <= 0f) {
                player.vx *= 0.62f
            }
        } else {
            val targetVx = moveInput * effectiveRunSpeed
            val accel = if (player.onGround) 2800f else 1950f
            val decel = if (player.onGround) 3200f else 1100f
            player.vx = when {
                abs(moveInput) > 0.05f -> {
                    player.facingRight = moveInput > 0f
                    moveTowards(player.vx, targetVx, accel * dt)
                }
                else -> moveTowards(player.vx, 0f, decel * dt)
            }

            val gravMultiplier = if (player.vy > 0f) 1.22f else 1.0f
            player.vy += config.gravity * gravMultiplier * dt

            if (!jumpHeld && player.vy < -60f) {
                player.vy += config.gravity * 1.45f * dt
            }

            if (!player.onGround && player.wallDir != 0 && player.vy > 52f &&
                ((player.wallDir == -1 && moveInput < -0.1f) || (player.wallDir == 1 && moveInput > 0.1f))
            ) {
                player.vy = 52f
                if ((worldTime * 30f).toInt() % 4 == 0) {
                    val sparkX = if (player.wallDir == 1) player.x + player.width else player.x
                    particles.add(
                        Particle(sparkX, player.y + 4f, -player.wallDir * 18f, -15f, 0.22f, 0.22f, 2f, activeCharacter.primaryColor)
                    )
                }
            } else {
                player.vy = min(player.vy, 450f)
            }
        }

        if (player.jumpBufferTimer > 0f) {
            if (player.coyoteTimer > 0f || player.onGround) {
                player.vy = -effectiveJumpVel
                player.onGround = false
                player.coyoteTimer = 0f
                player.jumpBufferTimer = 0f
                player.dashTimer = 0f
                emitBurst(player.x + player.width / 2f, player.y + player.height, activeCharacter.scarfColor, 6, 45f)
                pendingCue = HapticCue.JUMP
                soundManager.play(SoundEffect.JUMP)
            } else if (player.wallDir != 0) {
                player.vy = -effectiveJumpVel * 0.94f
                player.vx = -player.wallDir * effectiveRunSpeed * 1.15f
                player.facingRight = player.vx > 0f
                player.jumpBufferTimer = 0f
                player.dashTimer = 0f
                emitBurst(
                    if (player.wallDir == 1) player.x + player.width else player.x,
                    player.y + player.height / 2f,
                    activeCharacter.primaryColor,
                    7,
                    65f
                )
                pendingCue = HapticCue.JUMP
                soundManager.play(SoundEffect.JUMP)
            }
        }

        val prevY = player.y
        player.x += player.vx * dt
        player.x = player.x.coerceIn(0f, stage.widthTiles * TILE_SIZE - player.width)
        resolveHorizontalCollisions()

        val wasOnGround = player.onGround
        player.y += player.vy * dt
        player.onGround = false
        resolveVerticalCollisions(prevY)

        if (player.onGround) {
            player.coyoteTimer = config.coyoteTimeMs / 1000f
            player.canAirDash = true
            if (!wasOnGround && player.vy >= 0f) {
                emitBurst(player.x + player.width / 2f, player.y + player.height, Color(0xFF64748B), 5, 35f)
            }
        }

        player.wallDir = detectWallContact()

        if (player.y > stage.heightTiles * TILE_SIZE + 24f || touchesHazardTile()) {
            triggerPlayerOut()
            return
        }

        if (player.vy >= -40f && touchesSpringPad()) {
            player.vy = -effectiveJumpVel * 1.38f
            player.onGround = false
            player.canAirDash = true
            player.dashTimer = 0f
            shakeIntensity = 2.5f
            emitBurst(player.x + player.width / 2f, player.y + player.height, Color(0xFFFBBF24), 10, 80f)
            pendingCue = HapticCue.SPRING
            soundManager.play(SoundEffect.SPRING)
        }

        updateEntities(dt, effectiveJumpVel)
        updateParticles(dt)
        updateAnimationState(dt)
        updateCamera(dt)
    }

    private fun moveTowards(current: Float, target: Float, maxDelta: Float): Float {
        val diff = target - current
        return if (abs(diff) <= maxDelta) target else current + sign(diff) * maxDelta
    }

    fun charAt(col: Int, row: Int): Char = tileAt(col, row)

    private fun tileAt(col: Int, row: Int): Char {
        if (row < 0 || row >= stage.heightTiles || col < 0 || col >= stage.widthTiles) return '.'
        return grid[row][col]
    }

    private fun isSolid(ch: Char): Boolean = ch == '#'

    private fun resolveHorizontalCollisions() {
        val topRow = ((player.y + 1f) / TILE_SIZE).toInt()
        val botRow = ((player.y + player.height - 1f) / TILE_SIZE).toInt()
        if (player.vx > 0f) {
            val rightCol = ((player.x + player.width) / TILE_SIZE).toInt()
            for (r in topRow..botRow) {
                if (isSolid(tileAt(rightCol, r))) {
                    player.x = rightCol * TILE_SIZE - player.width - 0.01f
                    player.vx = 0f
                    break
                }
            }
        } else if (player.vx < 0f) {
            val leftCol = (player.x / TILE_SIZE).toInt()
            for (r in topRow..botRow) {
                if (isSolid(tileAt(leftCol, r))) {
                    player.x = (leftCol + 1) * TILE_SIZE + 0.01f
                    player.vx = 0f
                    break
                }
            }
        }
    }

    private fun resolveVerticalCollisions(prevY: Float) {
        val leftCol = ((player.x + 1f) / TILE_SIZE).toInt()
        val rightCol = ((player.x + player.width - 1f) / TILE_SIZE).toInt()
        if (player.vy >= 0f) {
            val botRow = ((player.y + player.height) / TILE_SIZE).toInt()
            val prevFootY = prevY + player.height
            for (c in leftCol..rightCol) {
                val ch = tileAt(c, botRow)
                val tileTop = botRow * TILE_SIZE
                if (isSolid(ch) || (ch == '=' && prevFootY <= tileTop + 4f)) {
                    player.y = tileTop - player.height
                    player.vy = 0f
                    player.onGround = true
                    break
                }
            }
        } else {
            val topRow = (player.y / TILE_SIZE).toInt()
            for (c in leftCol..rightCol) {
                if (isSolid(tileAt(c, topRow))) {
                    player.y = (topRow + 1) * TILE_SIZE + 0.01f
                    player.vy = 0f
                    break
                }
            }
        }
    }

    private fun detectWallContact(): Int {
        val midRow = ((player.y + player.height * 0.5f) / TILE_SIZE).toInt()
        val rightCol = ((player.x + player.width + 1.5f) / TILE_SIZE).toInt()
        if (isSolid(tileAt(rightCol, midRow))) return 1
        val leftCol = ((player.x - 1.5f) / TILE_SIZE).toInt()
        if (isSolid(tileAt(leftCol, midRow))) return -1
        return 0
    }

    private fun touchesHazardTile(): Boolean {
        val leftCol = ((player.x + 2.5f) / TILE_SIZE).toInt()
        val rightCol = ((player.x + player.width - 2.5f) / TILE_SIZE).toInt()
        val topRow = ((player.y + 5f) / TILE_SIZE).toInt()
        val botRow = ((player.y + player.height - 1f) / TILE_SIZE).toInt()
        for (r in topRow..botRow) {
            for (c in leftCol..rightCol) {
                if (tileAt(c, r) == '^') return true
            }
        }
        return false
    }

    private fun touchesSpringPad(): Boolean {
        val leftCol = ((player.x + 1f) / TILE_SIZE).toInt()
        val rightCol = ((player.x + player.width - 1f) / TILE_SIZE).toInt()
        val botRow = ((player.y + player.height - 1f) / TILE_SIZE).toInt()
        for (c in leftCol..rightCol) {
            if (tileAt(c, botRow) == 'S') return true
        }
        return false
    }

    private fun updateEntities(dt: Float, effectiveJumpVel: Float) {
        val pxCenter = player.x + player.width / 2f
        val pyCenter = player.y + player.height / 2f

        for (s in shards) {
            if (!s.collected) {
                val dx = pxCenter - (s.x + 8f)
                val dy = pyCenter - (s.y + 8f)
                if (dx * dx + dy * dy < 13f * 13f) {
                    s.collected = true
                    comboTimer = 2.8f
                    combo = min(9, combo + 1)
                    maxCombo = max(maxCombo, combo)
                    stageScore += 100 * combo
                    val roadMult = 1f + (activeRoad.coinBonusPercent / 100f)
                    stageCoins += (5f * activeCharacter.bonusCoinMultiplier * roadMult).roundToInt()
                    emitBurst(s.x + 8f, s.y + 8f, Color(0xFFFBBF24), 9, 75f)
                    pendingCue = HapticCue.SHARD
                    soundManager.play(SoundEffect.COIN)
                }
            }
        }

        for (c in crystals) {
            if (c.isAvailable) {
                val dx = pxCenter - (c.x + 8f)
                val dy = pyCenter - (c.y + 8f)
                if (dx * dx + dy * dy < 13.5f * 13.5f) {
                    c.cooldownTimer = 1.8f
                    player.canAirDash = true
                    stageScore += 50
                    emitBurst(c.x + 8f, c.y + 8f, Color(0xFF34D399), 12, 90f)
                    pendingCue = HapticCue.SHARD
                    soundManager.play(SoundEffect.DASH)
                }
            }
        }

        checkpointTilePos?.let { (cc, cr) ->
            if (!checkpointActivated) {
                val cx = cc * TILE_SIZE + 8f
                val cy = cr * TILE_SIZE + 8f
                if (abs(pxCenter - cx) < 16f && abs(pyCenter - cy) < 20f) {
                    checkpointActivated = true
                    checkpointX = cc * TILE_SIZE + 2f
                    checkpointY = cr * TILE_SIZE
                    stageScore += 150
                    val roadMult = 1f + (activeRoad.coinBonusPercent / 100f)
                    stageCoins += (5f * roadMult).roundToInt()
                    emitBurst(cx, cy, Color(0xFF34D399), 14, 95f)
                    soundManager.play(SoundEffect.COIN)
                }
            }
        }

        for (d in drones) {
            if (!d.alive) continue
            val nextX = d.x + d.vx * dt
            val checkCol = if (d.vx > 0f) ((nextX + 14f) / TILE_SIZE).toInt() else ((nextX + 2f) / TILE_SIZE).toInt()
            val row = ((d.y + 8f) / TILE_SIZE).toInt()
            val floorBelow = tileAt(checkCol, row + 1)
            val wallAhead = tileAt(checkCol, row)
            if (isSolid(wallAhead) || (!isSolid(floorBelow) && floorBelow != '=' && floorBelow != '^') ||
                abs(nextX - d.spawnX) > TILE_SIZE * 4.5f
            ) {
                d.vx = -d.vx
            } else {
                d.x = nextX
            }

            val overlapX = player.x + player.width > d.x + 2f && player.x < d.x + 14f
            val overlapY = player.y + player.height > d.y + 3f && player.y < d.y + 13f
            if (overlapX && overlapY) {
                val stomped = player.vy > 0f && (player.y + player.height - d.y) < 9.5f
                if (stomped || player.dashTimer > 0f) {
                    d.alive = false
                    if (stomped) {
                        player.vy = -effectiveJumpVel * 0.90f
                    }
                    player.canAirDash = true
                    comboTimer = 3.0f
                    combo = min(9, combo + 1)
                    maxCombo = max(maxCombo, combo)
                    stageScore += 250 * combo
                    val roadMult = 1f + (activeRoad.coinBonusPercent / 100f)
                    stageCoins += (10f * roadMult).roundToInt()
                    shakeIntensity = 3.0f
                    emitBurst(d.x + 8f, d.y + 8f, Color(0xFFFB7185), 14, 115f)
                    emitBurst(d.x + 8f, d.y + 8f, activeCharacter.primaryColor, 6, 85f)
                    pendingCue = HapticCue.STOMP
                    soundManager.play(SoundEffect.COIN)
                } else {
                    triggerPlayerOut()
                    return
                }
            }
        }

        val gdx = pxCenter - (goalX + 8f)
        val gdy = pyCenter - (goalY + 8f)
        if (gdx * gdx + gdy * gdy < 16f * 16f) {
            stageCleared = true
            val parBonus = max(0, ((stage.parTimeMs - (elapsedSec * 1000f).toLong()) / 50L).toInt())
            stageScore += 500 + parBonus
            val roadMult = 1f + (activeRoad.coinBonusPercent / 100f)
            stageCoins += (30f * roadMult).roundToInt()
            emitBurst(goalX + 8f, goalY + 8f, Color(0xFFA855F7), 22, 130f)
            emitBurst(goalX + 8f, goalY + 8f, activeCharacter.primaryColor, 14, 100f)
            pendingCue = HapticCue.CLEAR
            soundManager.play(SoundEffect.STAGE_CLEAR)
        }
    }

    private fun emitBurst(x: Float, y: Float, color: Color, count: Int, speed: Float) {
        for (i in 0 until count) {
            val angle = (i.toFloat() / count) * 6.28318f + (worldTime * 3.1f)
            val spd = speed * (0.45f + ((i * 37) % 10) / 14f)
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = kotlin.math.cos(angle) * spd,
                    vy = kotlin.math.sin(angle) * spd - 20f,
                    life = 0.35f,
                    maxLife = 0.35f,
                    size = if (i % 2 == 0) 2.5f else 1.8f,
                    color = color
                )
            )
        }
    }

    private fun updateParticles(dt: Float) {
        val itP = particles.iterator()
        while (itP.hasNext()) {
            val p = itP.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vy += 220f * dt
            p.life -= dt
            if (p.life <= 0f) itP.remove()
        }
        val itA = afterimages.iterator()
        while (itA.hasNext()) {
            val a = itA.next()
            a.life -= dt
            if (a.life <= 0f) itA.remove()
        }
    }

    private fun updateAnimationState(dt: Float) {
        player.animClock += dt
        player.animState = when {
            player.dashTimer > 0f -> HeroAnimState.DASH
            !player.onGround && player.wallDir != 0 && player.vy > 0f -> HeroAnimState.WALL_SLIDE
            !player.onGround && player.vy < 0f -> HeroAnimState.JUMP
            !player.onGround -> HeroAnimState.FALL
            abs(player.vx) > 12f -> HeroAnimState.RUN
            else -> HeroAnimState.IDLE
        }
    }

    private fun updateCamera(dt: Float) {
        val leadX = if (player.facingRight) 26f else -12f
        val targetCamX = (player.x + leadX - visibleLogicalWidth * 0.34f).coerceIn(
            0f,
            max(0f, stage.widthTiles * TILE_SIZE - visibleLogicalWidth)
        )
        cameraX += (targetCamX - cameraX) * min(1f, dt * 11f)
        cameraY = 0f
    }

    fun isCheckpointActive(): Boolean = checkpointActivated
    fun getCheckpointTile(): Pair<Int, Int>? = checkpointTilePos

    fun snapshot(): WorldSnapshot {
        return WorldSnapshot(
            levelNumber = stage.levelNumber,
            elapsedMs = (elapsedSec * 1000f).toLong(),
            stageScore = stageScore,
            stageCoins = stageCoins,
            shardsCollected = shards.count { it.collected },
            totalShards = shards.size,
            combo = combo,
            maxCombo = maxCombo,
            canDash = player.canAirDash,
            deaths = deaths,
            isPlayerOut = playerOut,
            stageCleared = stageCleared
        )
    }
}
