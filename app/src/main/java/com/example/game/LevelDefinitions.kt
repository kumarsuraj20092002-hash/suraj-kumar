package com.example.game

import kotlin.random.Random

enum class BiomeType(val displayName: String) {
    SKYLINE("Neon Rooftops"),
    FOUNDRY("Synth Foundry")
}

data class PacingBeat(
    val title: String,
    val zonePercent: String,
    val description: String
)

data class StageDefinition(
    val id: Int,
    val levelNumber: Int,
    val code: String,
    val name: String,
    val subtitle: String,
    val biome: BiomeType,
    val difficulty: String,
    val parTimeMs: Long,
    val designSummary: String,
    val pacingBeats: List<PacingBeat>,
    val rows: List<String>,
    val isProcedural: Boolean = false
) {
    val heightTiles: Int get() = rows.size
    val widthTiles: Int get() = rows.firstOrNull()?.length ?: 0
}

/**
 * Tile Legend (each character = 16x16 logical pixel tile):
 * '.' = Air
 * '#' = Solid Platform / Wall Tile
 * '=' = One-Way Neon Rail (passable from below)
 * '^' = Plasma Spike Hazard
 * 'S' = Bounce Spring Coil
 * 'C' = Collectible Volt Shard
 * 'D' = Air-Dash Reset Crystal
 * 'E' = Patrol Drone Enemy
 * 'K' = Checkpoint Beacon
 * 'P' = Player Spawn
 * 'G' = Warp Portal Goal
 */
object LevelDefinitions {

    val stages: List<StageDefinition> = listOf(
        StageDefinition(
            id = 0,
            levelNumber = 1,
            code = "01",
            name = "Neon Rooftops",
            subtitle = "Momentum Ramp & Air Dash Intro",
            biome = BiomeType.SKYLINE,
            difficulty = "FLOW I",
            parTimeMs = 19_000L,
            designSummary = "Designed as a frictionless 20-second onboarding sprint. Coins trace optimal jump parabolas over wide skyscraper gaps, teaching Coyote Time and Air Dash without text tutorials.",
            pacingBeats = listOf(
                PacingBeat("01. Safe Runway", "0–25%", "Flat rooftop + low hop teaches jump height & variable tap release."),
                PacingBeat("02. Rail & Stomp", "25–55%", "One-way neon rails let runners climb fast and stomp a patrol drone."),
                PacingBeat("03. Dash Gap", "55–80%", "Wide skyline chasm framed by Volt Shards begs for a mid-air Neon Dash."),
                PacingBeat("04. Final Sprint", "80–100%", "Spring boost straight into the Warp Portal for a clean speedrun finish.")
            ),
            rows = listOf(
                "................................................................................",
                "................................................................................",
                "................................................................................",
                "........................................................................C...C...",
                "........................................C..C..........................======..G.",
                "....................C..C...............======..........C..C..................###",
                "...........C..C....======.....C..C.............C..C...======......S..........###",
                "....C.....======.............======...........======.............###.........###",
                ".P..C...............E.................................E.....K................###",
                "#######..########..#####^^###......#######..####..##########..#####^^####..#####",
                "#######..########..##########......#######..####..##########..###########..#####",
                "#######..########..##########......#######..####..##########..###########..#####"
            )
        ),
        StageDefinition(
            id = 1,
            levelNumber = 2,
            code = "02",
            name = "Synth Foundry",
            subtitle = "Dash Crystals & Bounce Chains",
            biome = BiomeType.FOUNDRY,
            difficulty = "FLOW II",
            parTimeMs = 24_000L,
            designSummary = "Escalates verticality inside a dark plasma foundry. Introduces floating Dash Crystals that refresh air-dash mid-flight, linking high-altitude bounce coils over spike pits.",
            pacingBeats = listOf(
                PacingBeat("01. Coil Launch", "0–25%", "Early Bounce Coil catapults the player onto upper catwalks above spikes."),
                PacingBeat("02. Crystal Chain", "25–60%", "Two consecutive Dash Crystals across a molten pit reward crisp air-dash timing."),
                PacingBeat("03. Drone Gauntlet", "60–85%", "Stomp or dash-slice through twin patrol drones guarding the checkpoint."),
                PacingBeat("04. Spire Ascent", "85–100%", "Fast stair-step rails climb to the upper foundry Warp Gate.")
            ),
            rows = listOf(
                "......................................................................................",
                "......................................................................................",
                "..............................................................................C..C....",
                ".......................................C.....C...............................======.G.",
                ".......................C..C...........D.......D..............C..C..................###",
                "..............C.......======........................C..C....======......S..........###",
                ".......S.....===...................====......====..======..............###.........###",
                "......###.............E.....................................E.....K................###",
                ".P................#########..^^....................###..###########..#####^^####..####",
                "#####..###^^####..#########..####..............#######..###########..###########..####",
                "#####..#########..#########..####..^^^^^^^^^^..#######..###########..###########..####",
                "#####..#########..#########..#########################..###########..###########..####"
            )
        ),
        StageDefinition(
            id = 2,
            levelNumber = 3,
            code = "03",
            name = "Sky Spire",
            subtitle = "Wall-Jumps & Precision Speedrun",
            biome = BiomeType.SKYLINE,
            difficulty = "FLOW III",
            parTimeMs = 28_000L,
            designSummary = "A high-velocity vertical & horizontal gauntlet. Combines wall-slide/wall-jump chimneys, mid-air Dash Crystal resets, and tight spike corridors for true mobile mastery.",
            pacingBeats = listOf(
                PacingBeat("01. Chimney Climb", "0–30%", "Twin vertical shafts teach wall-slide friction and alternating wall-jumps."),
                PacingBeat("02. Skyway Express", "30–60%", "Elevated neon rails + drone bounce chain rewards never touching the floor."),
                PacingBeat("03. Crystal Canyon", "60–85%", "Dash Crystal suspended above plasma spikes tests horizontal dash precision."),
                PacingBeat("04. Summit Leap", "85–100%", "Double spring + air dash straight through the high-altitude Warp Portal.")
            ),
            rows = listOf(
                "..........................................................................................",
                "..........................................................................................",
                "....................C...........................................................C..C......",
                "...................===..C....C......................................C..C.......======..G..",
                "..................#....========........C..C................C.......======.............###.",
                "................C.#...............#...======.....C..C.....D...........................###.",
                ".............#..=.#...............#.............======..........S.............S.......###.",
                "............#.....#.........E.....#.......E....................###...........###......###.",
                ".P....S.....#.....#....###########....########^^########..K...........^^^^^^..........###.",
                "######^^##..#.....#....###########....##################..####..####..######..######..###.",
                "##########..#.....#....###########....##################..####..####..######..######..###.",
                "##########..#######....###########....##################..####..####..######..######..###."
            )
        )
    )

    /**
     * Returns handcrafted stages for Starter Neon Highway Levels 1..3, and builds
     * road-specific unique levels for every unlockable Road and unlimited procedural levels 4..∞.
     */
    fun getStageForLevel(levelNumber: Int, roadId: String = "neon_highway"): StageDefinition {
        val safeLevel = levelNumber.coerceAtLeast(1)
        if (roadId == "neon_highway" && safeLevel <= stages.size) {
            return stages[safeLevel - 1]
        }
        return ProceduralLevelGenerator.generateLevel(safeLevel, roadId)
    }
}

object ProceduralLevelGenerator {

    private val startChunk = listOf(
        "........",
        "........",
        "........",
        "........",
        "........",
        "........",
        "....C...",
        "....=...",
        ".P..C...",
        "########",
        "########",
        "########"
    )

    private val checkpointChunk = listOf(
        "........",
        "........",
        "........",
        "........",
        "........",
        "........",
        "........",
        "...C....",
        "....K...",
        "########",
        "########",
        "########"
    )

    private val goalChunk = listOf(
        "..........",
        "..........",
        "..........",
        "....C.....",
        "...===..G.",
        ".......###",
        ".S.....###",
        "###....###",
        "###....###",
        "##########",
        "##########",
        "##########"
    )

    // Classic Neon Skyline chunks (balanced gaps, rails & crystals)
    private val skylineChunks: List<List<String>> = listOf(
        listOf(
            "............",
            "............",
            "............",
            "............",
            "............",
            "....C..C....",
            "...======...",
            "............",
            "............",
            "###......###",
            "###......###",
            "###......###"
        ),
        listOf(
            "............",
            "............",
            "............",
            "............",
            "............",
            "....C..C....",
            "...======...",
            "............",
            "......E.....",
            "############",
            "############",
            "############"
        ),
        listOf(
            "............",
            "............",
            "............",
            "............",
            ".....C......",
            ".....D......",
            "............",
            "............",
            "............",
            "###......###",
            "###^^^^^^###",
            "############"
        ),
        listOf(
            "............",
            "............",
            "............",
            ".......C..C.",
            "......======",
            "............",
            "............",
            "..S.........",
            "..#.........",
            "####^^######",
            "############",
            "############"
        ),
        listOf(
            "............",
            "............",
            "............",
            ".....C......",
            "....===.....",
            "............",
            "..S......C..",
            ".###....===.",
            ".###........",
            "####^^..####",
            "############",
            "############"
        )
    )

    // Volcano Magma Road chunks: wide lava spike trenches, spring vaults, high catwalks
    private val volcanoChunks: List<List<String>> = listOf(
        listOf(
            "..............",
            "..............",
            "..............",
            ".....C..C.....",
            "....======....",
            "..............",
            "..S........C..",
            ".###......===.",
            ".###..........",
            "####^^^^^^####",
            "##############",
            "##############"
        ),
        listOf(
            "..............",
            "..............",
            "..............",
            "......CC......",
            ".....D..D.....",
            "..............",
            "..............",
            "....======....",
            "..............",
            "###^^^^^^^^###",
            "##############",
            "##############"
        ),
        listOf(
            "............",
            "............",
            "............",
            "............",
            ".....CC.....",
            "....====....",
            "..S......E..",
            ".###..######",
            ".###..######",
            "####^^######",
            "############",
            "############"
        ),
        listOf(
            "............",
            "............",
            "............",
            "....C..C....",
            "...======...",
            "............",
            "......S.....",
            ".....###....",
            ".....###....",
            "##^^^###^^##",
            "############",
            "############"
        )
    )

    // Bio-Jungle chunks: stacked treetop canopy rails, drone swarms & vine drops
    private val jungleChunks: List<List<String>> = listOf(
        listOf(
            "..............",
            "..............",
            ".....C..C.....",
            "....======....",
            "..............",
            "..C........C..",
            ".====....====.",
            "..............",
            "......E.......",
            "###..#########",
            "###..#########",
            "###..#########"
        ),
        listOf(
            "..............",
            "..............",
            "..............",
            "..C..====..C..",
            ".===......===.",
            "..............",
            "......D.......",
            "..............",
            "....E....E....",
            "##############",
            "##############",
            "##############"
        ),
        listOf(
            "............",
            "............",
            "....C..C....",
            "...======...",
            "............",
            ".......C....",
            "......====..",
            "..S.........",
            "..#....E....",
            "###..#######",
            "###..#######",
            "###..#######"
        ),
        listOf(
            "............",
            "............",
            "............",
            "..CC....CC..",
            ".====..====.",
            "............",
            ".....D......",
            "............",
            "............",
            "###..^^..###",
            "###..##..###",
            "###..##..###"
        )
    )

    // Royal Gold Temple chunks: stepped pyramid climbs + rich coin vault rooms
    private val templeChunks: List<List<String>> = listOf(
        listOf(
            "..............",
            "..............",
            "......CC......",
            ".....####.....",
            "....######....",
            "...##C..C##...",
            "..##======##..",
            "......E.......",
            ".S..........C.",
            "####^^##^^####",
            "##############",
            "##############"
        ),
        listOf(
            "..............",
            "..............",
            "..............",
            "....CCCCCC....",
            "...========...",
            "..............",
            "..C........C..",
            ".===......===.",
            "......E.......",
            "###..####..###",
            "###..####..###",
            "###..####..###"
        ),
        listOf(
            "............",
            "............",
            "............",
            ".....CC.....",
            "....====....",
            "...#....#...",
            "..S#.CC.#...",
            ".###====#...",
            ".###....#...",
            "####....####",
            "############",
            "############"
        ),
        listOf(
            "............",
            "............",
            "....CCCC....",
            "...======...",
            "............",
            ".....D......",
            "............",
            "..##....##..",
            "..##..E.##..",
            "####^^######",
            "############",
            "############"
        )
    )

    // Frost Glacier chunks: wide crystal chasms, elevated ice bridges, rapid springs
    private val glacierChunks: List<List<String>> = listOf(
        listOf(
            "..............",
            "..............",
            "..............",
            "......CC......",
            "....D....D....",
            "..............",
            "..............",
            "..............",
            "..............",
            "###........###",
            "###........###",
            "###........###"
        ),
        listOf(
            "..............",
            "..............",
            "..............",
            "....C....C....",
            "...===..===...",
            "..............",
            ".......D......",
            "..S...........",
            "..#...........",
            "###^^^^^^^^###",
            "##############",
            "##############"
        ),
        listOf(
            "............",
            "............",
            "....C..C....",
            "...======...",
            "............",
            "............",
            "......E.....",
            "....#####...",
            "............",
            "##........##",
            "##........##",
            "##........##"
        ),
        listOf(
            "............",
            "............",
            "............",
            "....C..C....",
            "...======...",
            "............",
            "..D......D..",
            "............",
            "............",
            "##..^^^^..##",
            "##..####..##",
            "##..####..##"
        )
    )

    // Galaxy Cosmos chunks: Zero-G floating sky islands, rapid bounce/dash speedways & shard clusters
    private val galaxyChunks: List<List<String>> = listOf(
        listOf(
            "..............",
            "..............",
            ".....CCCC.....",
            "....======....",
            "..............",
            "..D........D..",
            "..............",
            "......S.......",
            ".....###......",
            "##..........##",
            "##..........##",
            "##..........##"
        ),
        listOf(
            "..............",
            "..............",
            "..CC......CC..",
            ".====....====.",
            "..............",
            "......DD......",
            "..............",
            "......E.......",
            "....######....",
            "##..........##",
            "##..........##",
            "##..........##"
        ),
        listOf(
            "............",
            "............",
            "....CCCC....",
            "...======...",
            "............",
            "..S......D..",
            ".###........",
            ".....E......",
            "....####....",
            "##........##",
            "##........##",
            "##........##"
        ),
        listOf(
            "..............",
            "..............",
            "......CC......",
            "....======....",
            "..C........C..",
            ".===......===.",
            "......D.......",
            "..S...........",
            "..#...........",
            "###........###",
            "###........###",
            "###........###"
        )
    )

    private val biomeNames = listOf(
        "Hyper Grid", "Neon Sector", "Pulse Core", "Cyber Spire", "Plasma Run", "Volt Horizon"
    )

    fun generateLevel(levelNumber: Int, roadId: String = "neon_highway"): StageDefinition {
        val road = RoadCatalog.byId(roadId)
        val rng = Random(levelNumber * 31337 + road.id.hashCode() * 131 + 911)
        val biome = when (road.worldStyle) {
            RoadWorldStyle.MOLTEN_VOLCANO, RoadWorldStyle.ROYAL_TEMPLE -> BiomeType.FOUNDRY
            RoadWorldStyle.NEON_SKYLINE, RoadWorldStyle.BIO_JUNGLE,
            RoadWorldStyle.FROST_GLACIER, RoadWorldStyle.GALAXY_COSMOS -> {
                if (levelNumber % 2 == 0) BiomeType.FOUNDRY else BiomeType.SKYLINE
            }
        }
        val activePool = when (road.worldStyle) {
            RoadWorldStyle.NEON_SKYLINE -> skylineChunks
            RoadWorldStyle.MOLTEN_VOLCANO -> volcanoChunks
            RoadWorldStyle.BIO_JUNGLE -> jungleChunks
            RoadWorldStyle.ROYAL_TEMPLE -> templeChunks
            RoadWorldStyle.FROST_GLACIER -> glacierChunks
            RoadWorldStyle.GALAXY_COSMOS -> galaxyChunks
        }

        val chunkCount = (4 + (levelNumber - 1) / 2).coerceIn(4, 9)
        val assembled = MutableList(12) { StringBuilder() }
        fun appendChunk(chunk: List<String>) {
            for (r in 0 until 12) {
                assembled[r].append(chunk[r])
            }
        }

        appendChunk(startChunk)
        val halfPoint = chunkCount / 2
        var lastChunkIdx = -1
        for (i in 0 until chunkCount) {
            if (i == halfPoint) {
                appendChunk(checkpointChunk)
            }
            var idx = rng.nextInt(activePool.size)
            if (idx == lastChunkIdx && activePool.size > 1) {
                idx = (idx + 1) % activePool.size
            }
            lastChunkIdx = idx
            appendChunk(activePool[idx])
        }
        appendChunk(goalChunk)

        val rows = assembled.map { it.toString() }
        val width = rows.first().length
        val code = levelNumber.toString().padStart(2, '0')
        val worldTitle = when (road.worldStyle) {
            RoadWorldStyle.NEON_SKYLINE -> "${biomeNames[levelNumber % biomeNames.size]} $code"
            RoadWorldStyle.MOLTEN_VOLCANO -> "Magma Sector $code"
            RoadWorldStyle.BIO_JUNGLE -> "Canopy Circuit $code"
            RoadWorldStyle.ROYAL_TEMPLE -> "Pyramid Vault $code"
            RoadWorldStyle.FROST_GLACIER -> "Glacier Drift $code"
            RoadWorldStyle.GALAXY_COSMOS -> "Cosmos Rift $code"
        }
        val parMs = (width * 240L).coerceAtLeast(18_000L)

        return StageDefinition(
            id = levelNumber - 1,
            levelNumber = levelNumber,
            code = code,
            name = worldTitle,
            subtitle = "${road.name} • ${road.levelStyleBadge}",
            biome = biome,
            difficulty = "${road.themeTag} LV $levelNumber",
            parTimeMs = parMs,
            designSummary = "Custom ${road.name} stage layout (${road.levelStyleBadge}) with guaranteed jump/dash reachability and mid-stage checkpoint.",
            pacingBeats = listOf(
                PacingBeat("01. World Entry", "0–20%", "Immediate safe launch runway styled with ${road.name} geometry."),
                PacingBeat("02. Signature Gauntlet", "20–50%", "${road.levelStyleBadge} obstacle chain tailored to this world."),
                PacingBeat("03. Checkpoint Relay", "50–55%", "Mid-level respawn beacon locks in progress cleanly."),
                PacingBeat("04. Warp Ascent", "55–100%", "Escalated final sprint to the Warp Portal (+${road.coinBonusPercent}% road coin bonus).")
            ),
            rows = rows,
            isProcedural = levelNumber > 3 || roadId != "neon_highway"
        )
    }
}
