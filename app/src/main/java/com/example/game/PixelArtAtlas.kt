package com.example.game

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb

data class PaletteSwatch(
    val name: String,
    val hex: String,
    val color: Color,
    val role: String
)

data class SpriteFramePair(
    val right: ImageBitmap,
    val left: ImageBitmap,
    val rawGrid: List<String>
)

data class CharacterSpriteSet(
    val idleFrames: List<SpriteFramePair>,
    val runFrames: List<SpriteFramePair>,
    val jumpFrame: SpriteFramePair,
    val fallFrame: SpriteFramePair,
    val dashFrame: SpriteFramePair,
    val wallFrame: SpriteFramePair
)

data class RoadTileSet(
    val topTile: SpriteFramePair,
    val subTile: SpriteFramePair,
    val railTile: SpriteFramePair
)

data class RunnerCharacterSpec(
    val id: String,
    val name: String,
    val tagline: String,
    val priceCoins: Int,
    val perkLabel: String,
    val primaryColor: Color,
    val scarfColor: Color,
    val armorColor: Color,
    val speedMultiplier: Float = 1.0f,
    val jumpMultiplier: Float = 1.0f,
    val dashCooldownSec: Float = 0.14f,
    val bonusCoinMultiplier: Float = 1.0f,
    val sprites: CharacterSpriteSet
)

data class PixelSpriteSpec(
    val id: String,
    val name: String,
    val category: String,
    val dimensions: String,
    val fps: Int,
    val hitboxDesc: String,
    val designNote: String,
    val frames: List<SpriteFramePair>
)

object PixelArtAtlas {

    val paletteSwatches: List<PaletteSwatch> = listOf(
        PaletteSwatch("Obsidian Sky", "#090D16", Color(0xFF090D16), "Deep midnight void & parallax base"),
        PaletteSwatch("Armor Navy", "#1E293B", Color(0xFF1E293B), "Hero suit silhouette & tile core"),
        PaletteSwatch("Steel Rim", "#475569", Color(0xFF475569), "Platform bevel highlights & rivets"),
        PaletteSwatch("Electric Cyan", "#22D3EE", Color(0xFF22D3EE), "Runner visor, dash trails & rails"),
        PaletteSwatch("Hyper Coral", "#FB7185", Color(0xFFFB7185), "Flowing scarf, drone eye & spikes"),
        PaletteSwatch("Volt Amber", "#FBBF24", Color(0xFFFBBF24), "Shards, foundry trim & spring coils"),
        PaletteSwatch("Mint Emerald", "#34D399", Color(0xFF34D399), "Dash crystal & checkpoint active"),
        PaletteSwatch("Neon Violet", "#A855F7", Color(0xFFA855F7), "Warp gate core & combo energy")
    )

    private val colorMap: Map<Char, Int> = mapOf(
        '.' to 0x00000000,                  // Transparent
        'K' to 0xFF090D16.toInt(),          // Outline Dark
        'N' to 0xFF1E293B.toInt(),          // Armor Navy
        'B' to 0xFF334155.toInt(),          // Armor Mid
        'S' to 0xFF64748B.toInt(),          // Steel Highlight
        'W' to 0xFFF8FAFC.toInt(),          // Pure White Specular
        'C' to 0xFF22D3EE.toInt(),          // Electric Cyan
        'c' to 0xFF0891B2.toInt(),          // Deep Cyan
        'R' to 0xFFFB7185.toInt(),          // Hyper Coral
        'r' to 0xFFE11D48.toInt(),          // Deep Crimson
        'Y' to 0xFFFBBF24.toInt(),          // Volt Amber
        'y' to 0xFFD97706.toInt(),          // Deep Amber
        'G' to 0xFF34D399.toInt(),          // Mint Emerald
        'g' to 0xFF059669.toInt(),          // Deep Emerald
        'V' to 0xFFA855F7.toInt(),          // Neon Violet
        'v' to 0xFF7E22CE.toInt()           // Deep Violet
    )

    private fun buildFramePair(
        grid: List<String>,
        overrideColors: Map<Char, Int> = emptyMap()
    ): SpriteFramePair {
        val h = grid.size
        val w = grid[0].length
        val pixelsR = IntArray(w * h)
        val pixelsL = IntArray(w * h)
        for (y in 0 until h) {
            val row = grid[y]
            for (x in 0 until w) {
                val ch = row[x]
                val argb = overrideColors[ch] ?: colorMap[ch] ?: 0
                pixelsR[y * w + x] = argb
                pixelsL[y * w + (w - 1 - x)] = argb
            }
        }
        val bmpR = Bitmap.createBitmap(pixelsR, w, h, Bitmap.Config.ARGB_8888).asImageBitmap()
        val bmpL = Bitmap.createBitmap(pixelsL, w, h, Bitmap.Config.ARGB_8888).asImageBitmap()
        return SpriteFramePair(right = bmpR, left = bmpL, rawGrid = grid)
    }

    // --- HERO: VOLT RUNNER (16x16) ---
    private val heroIdle0 = listOf(
        "......KKKKK.....",
        ".....KNNNNNK....",
        "....KNNCCCCWK...",
        "....KNNCCCCNK...",
        "....KNNNNNNNK...",
        "...KRRKKKKKK....",
        "..KRRKBBBBBBK...",
        ".KRrKBNCCCCNBK..",
        ".KK.KBNBBBBNBK..",
        "....KBBBBBBBBK..",
        ".....KNNNNNK....",
        ".....KNN.NNK....",
        ".....KNN.NNK....",
        ".....KBB.BBK....",
        "....KCCK.KCCK...",
        "....KKKK.KKKK..."
    )

    private val heroIdle1 = listOf(
        "................",
        "......KKKKK.....",
        ".....KNNNNNK....",
        "....KNNCCCCWK...",
        "....KNNCCCCNK...",
        "....KNNNNNNNK...",
        "..KRRRKKKKKK....",
        ".KRrRKBBBBBBK...",
        "KK..KBNCCCCNBK..",
        "....KBNBBBBNBK..",
        ".....KNNNNNK....",
        ".....KNN.NNK....",
        ".....KNN.NNK....",
        ".....KBB.BBK....",
        "....KCCK.KCCK...",
        "....KKKK.KKKK..."
    )

    private val heroRun0 = listOf(
        "......KKKKK.....",
        ".....KNNNNNK....",
        "....KNNCCCCWK...",
        "....KNNCCCCNK...",
        "..KKKNNNNNNNK...",
        ".KRRRrKKKKKK....",
        "KRRr.KBBBBBBK...",
        "KK..KBNCCCCNBK..",
        ".....KBBBBBBBK..",
        ".....KNNNNNK....",
        "....KNNK.KNNK...",
        "...KNNK...KNNK..",
        "...KBBK...KBBK..",
        "..KCCK.....KCCK.",
        "..KKKK.....KKKK.",
        "................"
    )

    private val heroRun1 = listOf(
        "................",
        "......KKKKK.....",
        ".....KNNNNNK....",
        "....KNNCCCCWK...",
        "....KNNCCCCNK...",
        ".KKKKNNNNNNNK...",
        "KRRRrKKKKKKK....",
        "KK..KBBBBBBBK...",
        "....KBNCCCCNBK..",
        ".....KBBBBBBK...",
        ".....KNNNNNK....",
        ".....KNNNNK.....",
        ".....KNN.NNK....",
        "....KCCK.KBBK...",
        "....KKKK..KCCK..",
        "..........KKKK.."
    )

    private val heroRun2 = listOf(
        "......KKKKK.....",
        ".....KNNNNNK....",
        "....KNNCCCCWK...",
        "....KNNCCCCNK...",
        "....KNNNNNNNK...",
        ".KKKRRKKKKKK....",
        "KRRrRKBBBBBBK...",
        "KK..KBNCCCCNBK..",
        ".....KBBBBBBBK..",
        ".....KNNNNNK....",
        "....KNNK.KNNK...",
        "...KBBK...KNNK..",
        "..KCCK.....KBBK.",
        "..KKKK.....KCCK.",
        "...........KKKK.",
        "................"
    )

    private val heroRun3 = listOf(
        "................",
        "......KKKKK.....",
        ".....KNNNNNK....",
        "....KNNCCCCWK...",
        "....KNNCCCCNK...",
        "...KKNNNNNNNK...",
        "..KRRKKKKKKK....",
        ".KRrKBBBBBBBK...",
        ".KK.KBNCCCCNBK..",
        ".....KBBBBBBK...",
        ".....KNNNNNK....",
        ".....KNNNNK.....",
        ".....KNNKNNK....",
        "....KBBK.KCCK...",
        "...KCCK..KKKK...",
        "...KKKK........."
    )

    private val heroJump = listOf(
        "......KKKKK.....",
        ".....KNNNNNK....",
        "....KNNCCCCWK...",
        "....KNNCCCCNK...",
        "....KNNNNNNNK...",
        "...KRRKKKKKKK...",
        "..KRrKBBBBBBK...",
        ".KRK.KBNCCCCNK..",
        ".KK..KBBBBBBBK..",
        ".....KNNNNNK....",
        "....KNNK.KNNK...",
        "...KBBK...KNNK..",
        "...KCCK...KBBK..",
        "...KKKK...KCCK..",
        "..........KKKK..",
        "................"
    )

    private val heroFall = listOf(
        "..KRK.KKKKK.....",
        ".KRRKKNNNNNK....",
        ".KRKKNNCCCCWK...",
        "..KKKNNCCCCNK...",
        "....KNNNNNNNK...",
        "....KBBBBBBBK...",
        "...KBNCCCCNBK...",
        "...KBBBBBBBBK...",
        ".....KNNNNNK....",
        "....KNNK.KNNK...",
        "....KBBK.KBBK...",
        "....KCCK.KCCK...",
        "....KKKK.KKKK...",
        "................",
        "................",
        "................"
    )

    private val heroDash = listOf(
        "................",
        "........KKKKK...",
        ".......KNNNNNK..",
        "......KNNCCCCWK.",
        "KKKKKKKNNCCCCNK.",
        "RRRRRRKNNNNNNNK.",
        ".KKKKKBBBBBBBBK.",
        "CCCCCCBNCCCCNBK.",
        "......KBBBBBBK..",
        "...KKKNNNNNK....",
        "..KBBKNNNK......",
        ".KCCKKCCK.......",
        ".KKKK.KKKK......",
        "................",
        "................",
        "................"
    )

    private val heroWall = listOf(
        ".....KKKKK......",
        "....KNNNNNK.....",
        "....KNNCCCCWK...",
        "....KNNCCCCNK...",
        "....KNNNNNNNK...",
        "...KRRKKKKKK....",
        "..KRRKBBBBBBK...",
        "..KK.KBNCCCCNK..",
        ".....KBBBBBBBK..",
        ".....KNNNNNK....",
        "....KNNK.KNNK...",
        "....KBBK..KBBK..",
        "....KCCK..KCCK..",
        "....KKKK..KKKK..",
        "................",
        "................"
    )

    private fun applyArchetypeHeadgear(grid: List<String>, archetype: RunnerArchetype): List<String> {
        if (archetype == RunnerArchetype.CYBER_CLASSIC) return grid
        val mutable = grid.map { it.toCharArray() }
        // Find top-most filled helmet row around columns 5..11
        val topRow = mutable.indexOfFirst { row -> row.any { it == 'K' || it == 'N' } }.coerceAtLeast(0)
        when (archetype) {
            RunnerArchetype.FLAME_STRIKER -> {
                // Spiky flame crest above helmet
                if (topRow >= 0) {
                    val r0 = mutable[0]
                    r0[6] = 'R'; r0[7] = 'Y'; r0[9] = 'R'; r0[10] = 'Y'
                    if (topRow > 0) {
                        mutable[1][6] = 'R'; mutable[1][8] = 'Y'; mutable[1][10] = 'R'
                    }
                }
            }
            RunnerArchetype.SOLARIS_KNIGHT -> {
                // Golden knight plume crest
                mutable[0][6] = 'K'; mutable[0][7] = 'R'; mutable[0][8] = 'R'; mutable[0][9] = 'K'
                if (mutable.size > 1) {
                    mutable[1][5] = 'R'; mutable[1][6] = 'Y'; mutable[1][7] = 'Y'; mutable[1][8] = 'Y'
                }
            }
            RunnerArchetype.VENOM_NINJA -> {
                // Flowing twin stealth headband tails on left
                val bandRow = (topRow + 2).coerceAtMost(mutable.lastIndex)
                mutable[bandRow][1] = 'R'; mutable[bandRow][2] = 'R'; mutable[bandRow][3] = 'C'
                if (bandRow + 1 < mutable.size) {
                    mutable[bandRow + 1][0] = 'R'; mutable[bandRow + 1][2] = 'C'
                }
            }
            RunnerArchetype.FROST_TITAN -> {
                // Twin mecha horns on left & right of helmet
                mutable[0][4] = 'W'; mutable[0][5] = 'C'; mutable[0][11] = 'C'; mutable[0][12] = 'W'
                mutable[1][5] = 'C'; mutable[1][11] = 'C'
            }
            RunnerArchetype.NOVA_MAGE -> {
                // Arcane 3-point crystal crown + glowing orb
                mutable[0][6] = 'R'; mutable[0][8] = 'W'; mutable[0][10] = 'R'
                mutable[1][6] = 'C'; mutable[1][7] = 'C'; mutable[1][8] = 'R'; mutable[1][9] = 'C'; mutable[1][10] = 'C'
            }
            RunnerArchetype.SHADOW_PHANTOM -> {
                // Sharp glowing ghost cowl ears
                mutable[0][5] = 'C'; mutable[0][11] = 'R'
                mutable[1][4] = 'K'; mutable[1][5] = 'C'; mutable[1][11] = 'R'; mutable[1][12] = 'K'
            }
            RunnerArchetype.OMEGA_KING -> {
                // Royal 3-prong golden crown with crimson jewel
                mutable[0][5] = 'R'; mutable[0][7] = 'R'; mutable[0][8] = 'W'; mutable[0][10] = 'R'
                mutable[1][5] = 'R'; mutable[1][6] = 'R'; mutable[1][7] = 'C'; mutable[1][8] = 'R'; mutable[1][9] = 'R'; mutable[1][10] = 'R'
            }
        }
        return mutable.map { String(it) }
    }

    private fun buildCharacterSpriteSet(
        archetype: RunnerArchetype,
        overrides: Map<Char, Int>
    ): CharacterSpriteSet {
        fun frame(raw: List<String>) = buildFramePair(applyArchetypeHeadgear(raw, archetype), overrides)
        return CharacterSpriteSet(
            idleFrames = listOf(frame(heroIdle0), frame(heroIdle1)),
            runFrames = listOf(frame(heroRun0), frame(heroRun1), frame(heroRun2), frame(heroRun3)),
            jumpFrame = frame(heroJump),
            fallFrame = frame(heroFall),
            dashFrame = buildFramePair(heroDash, overrides),
            wallFrame = frame(heroWall)
        )
    }

    private fun colorOverridesFor(profile: RunnerCharacterProfile): Map<Char, Int> {
        if (profile.id == "volt") return emptyMap()
        val p = profile.primaryColor.toArgb()
        val s = profile.scarfColor.toArgb()
        val a = profile.armorColor.toArgb()
        return mapOf(
            'C' to p,
            'c' to blendArgb(p, 0xFF000000.toInt(), 0.25f),
            'N' to a,
            'B' to blendArgb(a, 0xFFFFFFFF.toInt(), 0.18f),
            'R' to s,
            'r' to blendArgb(s, 0xFF000000.toInt(), 0.28f)
        )
    }

    private fun blendArgb(c1: Int, c2: Int, ratio: Float): Int {
        val r1 = (c1 ushr 16) and 0xFF
        val g1 = (c1 ushr 8) and 0xFF
        val b1 = c1 and 0xFF
        val r2 = (c2 ushr 16) and 0xFF
        val g2 = (c2 ushr 8) and 0xFF
        val b2 = c2 and 0xFF
        val r = (r1 + (r2 - r1) * ratio).toInt().coerceIn(0, 255)
        val g = (g1 + (g2 - g1) * ratio).toInt().coerceIn(0, 255)
        val b = (b1 + (b2 - b1) * ratio).toInt().coerceIn(0, 255)
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }

    val runnerCharacters: List<RunnerCharacterSpec> = RunnerCatalog.characters.map { profile ->
        RunnerCharacterSpec(
            id = profile.id,
            name = profile.name,
            tagline = profile.tagline,
            priceCoins = profile.priceCoins,
            perkLabel = profile.perkLabel,
            primaryColor = profile.primaryColor,
            scarfColor = profile.scarfColor,
            armorColor = profile.armorColor,
            speedMultiplier = profile.speedMultiplier,
            jumpMultiplier = profile.jumpMultiplier,
            dashCooldownSec = profile.dashCooldownSec,
            bonusCoinMultiplier = profile.bonusCoinMultiplier,
            sprites = buildCharacterSpriteSet(profile.archetype, colorOverridesFor(profile))
        )
    }

    fun characterById(id: String): RunnerCharacterSpec {
        return runnerCharacters.firstOrNull { it.id == id } ?: runnerCharacters.first()
    }

    // --- ENEMY: PATROL DRONE (16x16) ---
    private val drone0 = listOf(
        "......KKKK......",
        "....KKSSSSKK....",
        "...KSBBBBBBKS...",
        "..KSBNNNNNNBSK..",
        "..KBNKKKKKKNBK..",
        ".KBNKrrRRRRKNBK.",
        ".KBNKrWWWWKKNBK.",
        ".KBNKrrRRRRKNBK.",
        "..KBNKKKKKKNBK..",
        "..KSBNNNNNNBSK..",
        "...KKBBBBBBKK...",
        "....KYK..KYK....",
        "....KYK..KYK....",
        ".....K....K.....",
        "................",
        "................"
    )

    private val drone1 = listOf(
        "......KKKK......",
        "....KKSSSSKK....",
        "...KSBBBBBBKS...",
        "..KSBNNNNNNBSK..",
        "..KBNKKKKKKNBK..",
        ".KBNKrRRRRRKNBK.",
        ".KBNKrWWWWKKNBK.",
        ".KBNKrRRRRRKNBK.",
        "..KBNKKKKKKNBK..",
        "..KSBNNNNNNBSK..",
        "...KKBBBBBBKK...",
        "....KyK..KyK....",
        "....KYK..KYK....",
        "....KYK..KYK....",
        ".....K....K.....",
        "................"
    )

    // --- VOLT SHARD (16x16) ---
    private val shard0 = listOf(
        "................",
        ".......KK.......",
        "......KYYK......",
        ".....KYWWYK.....",
        "....KYWWWWYK....",
        "...KYWWYYYYYK...",
        "...KYYYYyYYYK...",
        "...KYYYYyYYYK...",
        "....KYYyyYYK....",
        ".....KYYYYK.....",
        "......KYYK......",
        ".......KK.......",
        "................",
        "................",
        "................",
        "................"
    )

    private val shard1 = listOf(
        "................",
        ".......KK.......",
        "......KYYK......",
        ".....KYWWYK.....",
        ".....KYWWYK.....",
        "....KYWYYYYK....",
        "....KYYyYYYK....",
        "....KYYyYYYK....",
        ".....KYyyYK.....",
        ".....KYYYYK.....",
        "......KYYK......",
        ".......KK.......",
        "................",
        "................",
        "................",
        "................"
    )

    private val shard2 = listOf(
        "................",
        ".......KK.......",
        ".......KK.......",
        "......KWWK......",
        "......KYYK......",
        "......KYYK......",
        "......KYYK......",
        "......KYYK......",
        "......KyyK......",
        "......KYYK......",
        ".......KK.......",
        ".......KK.......",
        "................",
        "................",
        "................",
        "................"
    )

    // --- DASH CRYSTAL (16x16) ---
    private val crystal0 = listOf(
        ".......KK.......",
        "......KGGK......",
        ".....KGWWGK.....",
        "....KGWCCCGK....",
        "...KGWCCCCCGK...",
        "..KGGCCCCCCCGGK.",
        "..KGGCCWWCCCGGK.",
        "...KGCgCCgCGK...",
        "....KGGggGGK....",
        ".....KGGGGK.....",
        "......KGGK......",
        ".......KK.......",
        "................",
        "................",
        "................",
        "................"
    )

    private val crystal1 = listOf(
        ".......KK.......",
        "......KCCK......",
        ".....KCWWCK.....",
        "....KCWGGGWCK...",
        "...KCWGGGGGWCK..",
        "..KCCGGGGGGGCCK.",
        "..KCCGGWWGGGCCK.",
        "...KCGgGGgGCK...",
        "....KCCggCCK....",
        ".....KCCCCK.....",
        "......KCCK......",
        ".......KK.......",
        "................",
        "................",
        "................",
        "................"
    )

    // --- SPRING COIL (16x16) ---
    private val spring0 = listOf(
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "..KKKKKKKKKKKK..",
        ".KYYYYYYYYYYYYK.",
        ".KWWWWCCCCCCCCK.",
        "..KKKKKKKKKKKK..",
        "....KSS..SSK....",
        "....KBB..BBK....",
        "...KKKKKKKKKK...",
        "..KBBBBBBBBBBK..",
        "..KKKKKKKKKKKK..",
        "................"
    )

    private val spring1 = listOf(
        "................",
        "..KKKKKKKKKKKK..",
        ".KYYYYYYYYYYYYK.",
        ".KWWWWCCCCCCCCK.",
        "..KKKKKKKKKKKK..",
        ".....KSSK.......",
        "......KBBK......",
        ".....KSSK.......",
        "....KBBK........",
        ".....KSSK.......",
        "......KBBK......",
        "....KSS..SSK....",
        "...KKKKKKKKKK...",
        "..KBBBBBBBBBBK..",
        "..KKKKKKKKKKKK..",
        "................"
    )

    // --- WARP PORTAL (16x16) ---
    private val portal0 = listOf(
        "....KKKKKKKK....",
        "..KKVVVVVVVVKK..",
        ".KVVCCCCCCCCVVK.",
        ".KVCCWWWWWWCCVK.",
        "KVCWWVVVVVVWWCVK",
        "KVCWVVCCCCVVWCVK",
        "KVCWVCWWWWCVWCVK",
        "KVCWVCWWWWCVWCVK",
        "KVCWVCWWWWCVWCVK",
        "KVCWVCWWWWCVWCVK",
        "KVCWVVCCCCVVWCVK",
        "KVCWWVVVVVVWWCVK",
        ".KVCCWWWWWWCCVK.",
        ".KVVCCCCCCCCVVK.",
        "..KKVVVVVVVVKK..",
        "....KKKKKKKK...."
    )

    private val portal1 = listOf(
        "....KKKKKKKK....",
        "..KKCCCCCCCCKK..",
        ".KCCVVVVVVVVCCK.",
        ".KCVVCCCCCCVVCK.",
        "KCVCCWWWWWWCCVCK",
        "KCVCWVVVVVVWCVCK",
        "KCVCWVCCCCVWCVCK",
        "KCVCWVCCCCVWCVCK",
        "KCVCWVCCCCVWCVCK",
        "KCVCWVCCCCVWCVCK",
        "KCVCWVVVVVVWCVCK",
        "KCVCCWWWWWWCCVCK",
        ".KCVVCCCCCCVVCK.",
        ".KCCVVVVVVVVCCK.",
        "..KKCCCCCCCCKK..",
        "....KKKKKKKK...."
    )

    // --- TILESET (16x16) ---
    private val tileRooftopTop = listOf(
        "CCCCCCCCCCCCCCCC",
        "WWWWWWWWWWWWWWWW",
        "cCCCCCCCCCCCCCCc",
        "KKKKKKKKKKKKKKKK",
        "SBBBBBBBBBBBBBBS",
        "BNNNNNNNNNNNNNNB",
        "BNNSSNNNNNNSSNNB",
        "BNNSSNNNNNNSSNNB",
        "BNNNNNNNNNNNNNNB",
        "BNNNNKKKKKKNNNNB",
        "BNNNNKCCCCKNNNNB",
        "BNNNNKKKKKKNNNNB",
        "BNNNNNNNNNNNNNNB",
        "BNNSSNNNNNNSSNNB",
        "BNNNNNNNNNNNNNNB",
        "KKKKKKKKKKKKKKKK"
    )

    private val tileFoundryTop = listOf(
        "YYYYYYYYYYYYYYYY",
        "WWYYYYYYYYYYYYWW",
        "yYYYYyYYYYyYYYYy",
        "KKKKKKKKKKKKKKKK",
        "SBBBBBBBBBBBBBBS",
        "BNYYNNBNNYYNNBNB",
        "BNNYYNBNNNYYNBNB",
        "BNNNNNBNNNNNNBNB",
        "BNNNNNBNNNNNNBNB",
        "BNNSSNBNNSSNNBNB",
        "BNNNNNBNNNNNNBNB",
        "BNNNNNBNNNNNNBNB",
        "BNNRRNBNNRRNNBNB",
        "BNNNNNBNNNNNNBNB",
        "BNNNNNNNNNNNNNNB",
        "KKKKKKKKKKKKKKKK"
    )

    private val tileSubSurface = listOf(
        "KKKKKKKKKKKKKKKK",
        "BNNNNNNBNNNNNNNB",
        "BNNSSNNBNNSSNNNB",
        "BNNNNNNBNNNNNNNB",
        "BNNNNNNBNNNNNNNB",
        "BBBBBBBBBBBBBBBB",
        "KKKKKKKKKKKKKKKK",
        "BNNNBNNNNNNNBNNB",
        "BNNNBNNSSNNNBNNB",
        "BNNNBNNNNNNNBNNB",
        "BNNNBNNNNNNNBNNB",
        "BBBBBBBBBBBBBBBB",
        "KKKKKKKKKKKKKKKK",
        "BNNNNNNBNNNNNNNB",
        "BNNNNNNBNNNNNNNB",
        "KKKKKKKKKKKKKKKK"
    )

    private val tileOneWayRail = listOf(
        "CCCCCCCCCCCCCCCC",
        "WWWWWWWWWWWWWWWW",
        "cCCcBBBBBBBBcCCc",
        "KKKKSSSSSSSSKKKK",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................"
    )

    private val tileSpike = listOf(
        "................",
        "................",
        "................",
        "................",
        "................",
        ".......KK.......",
        "......KWWK......",
        "...KK.KRRK.KK...",
        "..KWWKRRRRKWWK..",
        "..KRRKRRRRKRRK..",
        ".KRRRRRRRRRRRRK.",
        ".KrrRRRRRRRRrrK.",
        "KrrrKKKKKKKKrrrK",
        "KSSKKKKKKKKKKSSK",
        "KKKKKKKKKKKKKKKK",
        "KKKKKKKKKKKKKKKK"
    )

    // Pre-built Sprite Frame Pairs
    val heroIdleFrames = runnerCharacters.first().sprites.idleFrames
    val heroRunFrames = runnerCharacters.first().sprites.runFrames
    val heroJumpFrame = runnerCharacters.first().sprites.jumpFrame
    val heroFallFrame = runnerCharacters.first().sprites.fallFrame
    val heroDashFrame = runnerCharacters.first().sprites.dashFrame
    val heroWallFrame = runnerCharacters.first().sprites.wallFrame

    val droneFrames = listOf(buildFramePair(drone0), buildFramePair(drone1))
    val shardFrames = listOf(
        buildFramePair(shard0),
        buildFramePair(shard1),
        buildFramePair(shard2),
        buildFramePair(shard1)
    )
    val crystalFrames = listOf(buildFramePair(crystal0), buildFramePair(crystal1))
    val springFrames = listOf(buildFramePair(spring0), buildFramePair(spring1))
    val portalFrames = listOf(buildFramePair(portal0), buildFramePair(portal1))

    val rooftopTopTile = buildFramePair(tileRooftopTop)
    val foundryTopTile = buildFramePair(tileFoundryTop)
    val subSurfaceTile = buildFramePair(tileSubSurface)
    val oneWayRailTile = buildFramePair(tileOneWayRail)
    val spikeTile = buildFramePair(tileSpike)

    // --- ROAD-SPECIFIC SCULPTED TILESETS (16x16) ---
    // 1. Molten Volcano (Cracked obsidian + glowing magma fissures + volcanic bridge grate)
    private val tileVolcanoTop = listOf(
        "CCcCCCCcCCcCCCCc",
        "WWCWWWWCWWCWWWWC",
        "KccKKKKccKKccKKc",
        "BBBBKCCBBBBKCCBB",
        "BNNBKcCBNBNKcCNN",
        "BNBBKCCBNBBKCCBN",
        "KKKKcCCKKKKcCCKK",
        "BNNNKCCNNNNKCCNN",
        "BNNNNcCNNNNcCNNN",
        "BNNSSNNBNNSSNNBN",
        "BNNNNNNBNNNNNNBN",
        "BNNKKKKBNNKKKKBN",
        "BNNcCCcBNNcCCcBN",
        "BNNNNNNNNNNNNNNB",
        "BNNNNNNNNNNNNNNB",
        "KKKKKKKKKKKKKKKK"
    )

    private val tileVolcanoSub = listOf(
        "KKKKKKKKKKKKKKKK",
        "BNNNKCCKNNNNKCCK",
        "BNNNKcCKNNNNKcCK",
        "BBBBKCCKBBBBKCCK",
        "BNNNKCCKNNNNKCCK",
        "KKKKcCCKKKKKcCCK",
        "BNNNNNNBNNNNNNNB",
        "BNKCCKNNBNKCCKNN",
        "BNKcCKNNBNKcCKNN",
        "BBBBBBBBBBBBBBBB",
        "KKKKKKKKKKKKKKKK",
        "BNNKCCKNNNNKCCKN",
        "BNNKcCKNNNNKcCKN",
        "BNNKCCKNNNNKCCKN",
        "BNNNNNNNNNNNNNNB",
        "KKKKKKKKKKKKKKKK"
    )

    private val tileVolcanoRail = listOf(
        "CCcCCcCCCCcCCcCC",
        "WWCWWCWWWWCWWCWW",
        "KKBKKBKKKKBKKBKK",
        ".KCKKCK..KCKKCK.",
        "..KKKK....KKKK..",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................"
    )

    // 2. Bio-Jungle (Overgrown stepped circuit moss + hanging cyber vines + suspended vine bridge)
    private val tileJungleTop = listOf(
        "CCCCCCCCCCCCCCCC",
        "WWCCWWCCCCWWCCWW",
        "ccCccCccCCccCccC",
        "KcCKKcCKKKKcCKKc",
        ".K.KK.KBBBBK.K.K",
        "BNNNNNNKCCKNNNNB",
        "BNNSSNNKccKNNSSB",
        "BNNNNNNKKKKNNNNB",
        "BNNKKKKNNNNKKKKB",
        "BNNKCCKNNNNKCCKB",
        "BNNKcCKNNNNKcCKB",
        "BNNKKKKNNNNKKKKB",
        "BNNNNNNNNNNNNNNB",
        "BNNNNKCKNNKCKNNB",
        "BNNNNKKKNNKKKNNB",
        "KKKKKKKKKKKKKKKK"
    )

    private val tileJungleSub = listOf(
        "KKKKKKKKKKKKKKKK",
        "BNKCKNNNBNNKCKNB",
        "BNKcKNNNBNNKcKNB",
        "BNNKNNNNBNNNKNNB",
        "BBBBBBBBBBBBBBBB",
        "KKKKKCKKKKKKKCKK",
        "BNNNKcKNNNNNKcKB",
        "BNNNKCKNNNNNKCKB",
        "BNNNNNNNNNNNNNNB",
        "BBBBKCKKBBBBKCKB",
        "BNNNKCKNNNNNKCKB",
        "BNNNNNNNNNNNNNNB",
        "BNNSSNNNNNNSSNNB",
        "BNNNNNNNNNNNNNNB",
        "KKKKKKKKKKKKKKKK",
        "KKKKKKKKKKKKKKKK"
    )

    private val tileJungleRail = listOf(
        "CCcCCCCccCCCCcCC",
        "WWCWWWWCCWWWWCWW",
        "cKKBBBBccBBBBKKc",
        ".KcK..KccK..KcK.",
        "..K....KK....K..",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................"
    )

    // 3. Royal Gold Temple (Battlement gold bricks + carved pyramid pillars + jeweled altar rail)
    private val tileTempleTop = listOf(
        "CCcCCcCCCCcCCcCC",
        "WWWWWWWWWWWWWWWW",
        "KKKKKKKKKKKKKKKK",
        "CBBBBBCKKCBBBBBC",
        "CNSSSNcKKcNSSSNC",
        "CNSWSNcKKcNSWSNC",
        "CNSSSNcKKcNSSSNC",
        "CBBBBBCKKCBBBBBC",
        "KKKKKKKKKKKKKKKK",
        "BBKCCCCKKCCCCKBB",
        "NNKcWWcKKcWWcKNN",
        "NNKCCCCKKCCCCKNN",
        "BBKKKKKKKKKKKKBB",
        "NNNNNNNNNNNNNNNN",
        "CCCCCCCCCCCCCCCC",
        "KKKKKKKKKKKKKKKK"
    )

    private val tileTempleSub = listOf(
        "KKKKKKKKKKKKKKKK",
        "CKBBBBBBBBBBBBKC",
        "CKBNNKKKKKKNNBKC",
        "CKBNKCCCCCCKNBKC",
        "CKBNKCWWWWCKNBKC",
        "CKBNKCCCCCCKNBKC",
        "CKBNNKKKKKKNNBKC",
        "KKKKKKKKKKKKKKKK",
        "NNNNKBBBBBBKNNNN",
        "NKKNKCCCCCCKNKKN",
        "NKCKKBBBBBBKKCKN",
        "NKKNNNNNNNNNNKKN",
        "BBBBBBBBBBBBBBBB",
        "CCCCCCCCCCCCCCCC",
        "KKKKKKKKKKKKKKKK",
        "KKKKKKKKKKKKKKKK"
    )

    private val tileTempleRail = listOf(
        "CCCCCCCCCCCCCCCC",
        "WKWCCCCCCCCCCWKW",
        "KKKBBBBWWBBBBKKK",
        ".KKKKKCCCCKKKKK.",
        "...KKKKKKKKKK...",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................"
    )

    // 4. Frost Glacier (Jagged snow cap + diagonal diamond prism fractures + icicle bridge rail)
    private val tileGlacierTop = listOf(
        "WWWWWWWWWWWWWWWW",
        "WWWCWWWWWWWCWWWW",
        "CCCCcCCCCCCcCCCC",
        "cKKcCCcKKcCCcKKc",
        ".KK.cc.KK.cc.KK.",
        "BNNSSNNBBNNSSNNB",
        "BNNWSSNNBNNWSSNB",
        "BNNNWSSNNBNNWSNB",
        "BNNNNWSSNBNNNSNB",
        "BNNNNNWSSNNNNNNB",
        "BNNCCNNWSSNNCCNB",
        "BNNcWCNNWSNNcWCB",
        "BNNCCNNNNNNNNCNB",
        "BNNNNNNNNNNNNNNB",
        "KKKKKKKKKKKKKKKK",
        "KKKKKKKKKKKKKKKK"
    )

    private val tileGlacierSub = listOf(
        "KKKKKKKKKKKKKKKK",
        "BNNWSSNNNNNWSSNB",
        "BNNNWSSNNNNNWSSB",
        "BNNNNWSSNNNNNWSB",
        "BNNNNNWSSNNNNNNB",
        "BBCCNNNWSSNNCCBB",
        "BNCWCNNNWSNNCWCB",
        "BBCCNNNNNNNNCCBB",
        "BNNNNSSNNNNSSNNB",
        "BNNNWSSNNNWSSNNB",
        "BNNNNWSSNNNWSSNB",
        "BNNNNNWSSNNNWSSB",
        "BNNNNNNWSSNNNNNB",
        "BNNNNNNNNNNNNNNB",
        "KKKKKKKKKKKKKKKK",
        "KKKKKKKKKKKKKKKK"
    )

    private val tileGlacierRail = listOf(
        "WWWWWWWWWWWWWWWW",
        "CCCWWCCCCCCWWCCC",
        "cKKcCCCCCCCCcKKc",
        ".K.K..cKKc..K.K.",
        "...K...KK...K...",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................"
    )

    // 5. Cosmic Hyperway (Star-circuit void frame + hex core + twin-beam photon rail)
    private val tileGalaxyTop = listOf(
        "CCCCCCCCCCCCCCCC",
        "WCCWWCCCCCCWWCCW",
        "KKKKKKKKKKKKKKKK",
        "KBBCCCCCCCCCCBBK",
        "KBNKKWWKKWWKKNBK",
        "KBNKCBBCCBBCKNBK",
        "KBNKCBWKKWBCKNBK",
        "KBNKCBKCCKBCKNBK",
        "KBNKCBKCCKBCKNBK",
        "KBNKCBWKKWBCKNBK",
        "KBNKCBBCCBBCKNBK",
        "KBNKKWWKKWWKKNBK",
        "KBBCCCCCCCCCCBBK",
        "KKKKKKKKKKKKKKKK",
        "cCCCCCCCCCCCCCCc",
        "KKKKKKKKKKKKKKKK"
    )

    private val tileGalaxySub = listOf(
        "KKKKKKKKKKKKKKKK",
        "KNNNKCCCCCCKNNNK",
        "KNNKCBBBBBBCKNNK",
        "KNKCBNNNNNNBCKNK",
        "KNKCNKKCCKKNCKNK",
        "KNKCNKCWWCKNCKNK",
        "KNKCNKCWWCKNCKNK",
        "KNKCNKKCCKKNCKNK",
        "KNKCBNNNNNNBCKNK",
        "KNNKCBBBBBBCKNNK",
        "KNNNKCCCCCCKNNNK",
        "KKKKcBBBBBBcKKKK",
        "KNNNNNNCCNNNNNNK",
        "KNNNNNNCCNNNNNNK",
        "KKKKKKKKKKKKKKKK",
        "KKKKKKKKKKKKKKKK"
    )

    private val tileGalaxyRail = listOf(
        "CCCCCCCCCCCCCCCC",
        "WWCCCCCCCCCCCCWW",
        "KKc..........cKK",
        "CCWWWWWWWWWWWWCC",
        "KKKKKKKKKKKKKKKK",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................"
    )

    private val roadTileMap: Map<String, RoadTileSet> = RoadCatalog.roads.associate { road ->
        val topArgb = road.roadTopColor.toArgb()
        val bevelArgb = road.roadBevelColor.toArgb()
        val bodyArgb = road.roadBodyColor.toArgb()
        val deepArgb = road.roadDeepColor.toArgb()
        val railArgb = road.railColor.toArgb()
        val topOverrides = mapOf(
            'C' to topArgb,
            'c' to bevelArgb,
            'B' to bodyArgb,
            'N' to deepArgb,
            'S' to blendArgb(bodyArgb, 0xFFFFFFFF.toInt(), 0.28f)
        )
        val railOverrides = mapOf(
            'C' to railArgb,
            'c' to blendArgb(railArgb, 0xFF000000.toInt(), 0.28f),
            'B' to bodyArgb,
            'S' to blendArgb(railArgb, 0xFFFFFFFF.toInt(), 0.35f)
        )
        val (topPattern, subPattern, railPattern) = when (road.worldStyle) {
            RoadWorldStyle.NEON_SKYLINE -> Triple(tileRooftopTop, tileSubSurface, tileOneWayRail)
            RoadWorldStyle.MOLTEN_VOLCANO -> Triple(tileVolcanoTop, tileVolcanoSub, tileVolcanoRail)
            RoadWorldStyle.BIO_JUNGLE -> Triple(tileJungleTop, tileJungleSub, tileJungleRail)
            RoadWorldStyle.ROYAL_TEMPLE -> Triple(tileTempleTop, tileTempleSub, tileTempleRail)
            RoadWorldStyle.FROST_GLACIER -> Triple(tileGlacierTop, tileGlacierSub, tileGlacierRail)
            RoadWorldStyle.GALAXY_COSMOS -> Triple(tileGalaxyTop, tileGalaxySub, tileGalaxyRail)
        }
        road.id to RoadTileSet(
            topTile = buildFramePair(topPattern, topOverrides),
            subTile = buildFramePair(subPattern, topOverrides),
            railTile = buildFramePair(railPattern, railOverrides)
        )
    }

    fun roadTilesById(roadId: String): RoadTileSet {
        return roadTileMap[roadId] ?: roadTileMap.values.first()
    }

    val spriteCatalog: List<PixelSpriteSpec> = listOf(
        PixelSpriteSpec(
            id = "hero_run",
            name = "Volt Runner (Run Cycle)",
            category = "Player Character",
            dimensions = "16 x 16 px",
            fps = 12,
            hitboxDesc = "10 x 14 px forgiving inner AABB",
            designNote = "4-frame stride with coral scarf lag and bright cyan visor for instant silhouette read against dark backgrounds.",
            frames = heroRunFrames
        ),
        PixelSpriteSpec(
            id = "hero_dash",
            name = "Volt Runner (Air Dash & States)",
            category = "Player Character",
            dimensions = "16 x 16 px",
            fps = 6,
            hitboxDesc = "12 x 12 px compressed dash box",
            designNote = "Displays Idle, Jump, Fall, Wall-Slide, and Neon Dash poses. Horizontal stretch communicates speed burst.",
            frames = listOf(heroIdleFrames[0], heroJumpFrame, heroFallFrame, heroWallFrame, heroDashFrame)
        ),
        PixelSpriteSpec(
            id = "drone",
            name = "Patrol Drone",
            category = "Hazard / Stompable",
            dimensions = "16 x 16 px",
            fps = 6,
            hitboxDesc = "12 x 11 px core (top stomp forgiving)",
            designNote = "Crimson glowing optic indicates threat from front/side; top steel chassis invites downward stomp bounce.",
            frames = droneFrames
        ),
        PixelSpriteSpec(
            id = "shard",
            name = "Volt Shard",
            category = "Collectible",
            dimensions = "16 x 16 px",
            fps = 8,
            hitboxDesc = "12 x 12 px generous pickup magnet",
            designNote = "4-step faux-3D spin using width compression + amber specular flash to guide player jump arcs.",
            frames = shardFrames
        ),
        PixelSpriteSpec(
            id = "crystal",
            name = "Dash Crystal",
            category = "Mechanic Object",
            dimensions = "16 x 16 px",
            fps = 4,
            hitboxDesc = "14 x 14 px aerial trigger",
            designNote = "Mint-cyan pulsing core signals mid-air Dash Reset, enabling multi-gap aerial chains.",
            frames = crystalFrames
        ),
        PixelSpriteSpec(
            id = "spring",
            name = "Bounce Coil",
            category = "Mechanic Object",
            dimensions = "16 x 16 px",
            fps = 4,
            hitboxDesc = "14 x 8 px upward launch pad",
            designNote = "Amber/cyan top plate visually contrasts with dark floors and compresses/extends on contact.",
            frames = springFrames
        ),
        PixelSpriteSpec(
            id = "portal",
            name = "Warp Portal",
            category = "Goal Gate",
            dimensions = "16 x 16 px",
            fps = 6,
            hitboxDesc = "14 x 16 px stage finish trigger",
            designNote = "Alternating violet/cyan vortex rings anchor the end-of-stage speedrun target.",
            frames = portalFrames
        ),
        PixelSpriteSpec(
            id = "tileset",
            name = "Modular 16px Tileset",
            category = "Environment Tiles",
            dimensions = "16 x 16 px",
            fps = 2,
            hitboxDesc = "16 x 16 px solid / 16 x 4 px rail",
            designNote = "Includes Rooftop Crust, Foundry Crust, Sub-Surface Rivets, One-Way Neon Rail, and Plasma Spikes.",
            frames = listOf(rooftopTopTile, foundryTopTile, subSurfaceTile, oneWayRailTile, spikeTile)
        )
    )
}
