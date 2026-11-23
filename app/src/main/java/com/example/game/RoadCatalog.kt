package com.example.game

import androidx.compose.ui.graphics.Color

enum class RoadWorldStyle {
    NEON_SKYLINE,
    MOLTEN_VOLCANO,
    BIO_JUNGLE,
    ROYAL_TEMPLE,
    FROST_GLACIER,
    GALAXY_COSMOS
}

/**
 * Road & World theme definitions. Each unlockable road upgrades:
 * 1) Custom 16x16 sculpted road, sub-surface, and rail tile pixel art,
 * 2) Multi-layer themed parallax world scenery (volcanoes, bio-canopy, pyramids, aurora, cosmos),
 * 3) Distinct road-specific level layouts and coin multipliers.
 */
data class RoadThemeProfile(
    val id: String,
    val name: String,
    val themeTag: String,
    val worldStyle: RoadWorldStyle,
    val description: String,
    val levelStyleBadge: String,
    val priceCoins: Int,
    val perkLabel: String,
    val roadTopColor: Color,
    val roadBevelColor: Color,
    val roadBodyColor: Color,
    val roadDeepColor: Color,
    val railColor: Color,
    val skyTintColor: Color,
    val skyHorizonColor: Color,
    val towerColor: Color,
    val windowColor: Color,
    val particleColor: Color,
    val coinBonusPercent: Int = 0
)

object RoadCatalog {
    val roads: List<RoadThemeProfile> = listOf(
        RoadThemeProfile(
            id = "neon_highway",
            name = "Neon Highway",
            themeTag = "STARTER WORLD",
            worldStyle = RoadWorldStyle.NEON_SKYLINE,
            description = "Midnight megacity expressway with blinking skytowers & neon steel guard-rails.",
            levelStyleBadge = "Balanced Rooftop Sprints",
            priceCoins = 0,
            perkLabel = "Classic Skyline + Expressway Level",
            roadTopColor = Color(0xFF22D3EE),
            roadBevelColor = Color(0xFF0891B2),
            roadBodyColor = Color(0xFF334155),
            roadDeepColor = Color(0xFF1E293B),
            railColor = Color(0xFF22D3EE),
            skyTintColor = Color(0xFF0E1B34),
            skyHorizonColor = Color(0xFF16284C),
            towerColor = Color(0xFF0E1628),
            windowColor = Color(0xFF22D3EE),
            particleColor = Color(0xFF38BDF8),
            coinBonusPercent = 0
        ),
        RoadThemeProfile(
            id = "lava_core",
            name = "Volcano Magma Road",
            themeTag = "VOLCANIC WORLD",
            worldStyle = RoadWorldStyle.MOLTEN_VOLCANO,
            description = "Cracked obsidian crust over glowing magma rivers beneath erupting peaks.",
            levelStyleBadge = "Magma Trenches • Bounce Springs",
            priceCoins = 150,
            perkLabel = "Volcano Background + Magma Level (+5% Coins)",
            roadTopColor = Color(0xFFF97316),
            roadBevelColor = Color(0xFFDC2626),
            roadBodyColor = Color(0xFF431407),
            roadDeepColor = Color(0xFF1C0905),
            railColor = Color(0xFFFACC15),
            skyTintColor = Color(0xFF260C08),
            skyHorizonColor = Color(0xFF5A180D),
            towerColor = Color(0xFF1A0909),
            windowColor = Color(0xFFFB923C),
            particleColor = Color(0xFFFDE047),
            coinBonusPercent = 5
        ),
        RoadThemeProfile(
            id = "cyber_jungle",
            name = "Bio-Canopy Track",
            themeTag = "CYBER JUNGLE",
            worldStyle = RoadWorldStyle.BIO_JUNGLE,
            description = "Overgrown circuit slabs & glowing vine bridges through towering neon trees.",
            levelStyleBadge = "Twin Vine Rails • Drone Swarms",
            priceCoins = 300,
            perkLabel = "Jungle Canopy + Multi-Tier Level (+10% Coins)",
            roadTopColor = Color(0xFF34D399),
            roadBevelColor = Color(0xFF059669),
            roadBodyColor = Color(0xFF064E3B),
            roadDeepColor = Color(0xFF02241B),
            railColor = Color(0xFFA3E635),
            skyTintColor = Color(0xFF061F1A),
            skyHorizonColor = Color(0xFF0C3B2E),
            towerColor = Color(0xFF041A15),
            windowColor = Color(0xFF6EE7B7),
            particleColor = Color(0xFFBEF264),
            coinBonusPercent = 10
        ),
        RoadThemeProfile(
            id = "gold_temple",
            name = "Sun Pyramid Road",
            themeTag = "ROYAL TEMPLE",
            worldStyle = RoadWorldStyle.ROYAL_TEMPLE,
            description = "Carved sandstone & gold-inlay ruins beneath a blazing eclipse & great pyramids.",
            levelStyleBadge = "Stepped Pyramids • Coin Vaults",
            priceCoins = 550,
            perkLabel = "Pyramid Scenery + Treasure Level (+15% Coins)",
            roadTopColor = Color(0xFFFBBF24),
            roadBevelColor = Color(0xFFD97706),
            roadBodyColor = Color(0xFF78350F),
            roadDeepColor = Color(0xFF3D1704),
            railColor = Color(0xFFC084FC),
            skyTintColor = Color(0xFF22122B),
            skyHorizonColor = Color(0xFF4A203A),
            towerColor = Color(0xFF1A0E22),
            windowColor = Color(0xFFFCD34D),
            particleColor = Color(0xFFFDE68A),
            coinBonusPercent = 15
        ),
        RoadThemeProfile(
            id = "frost_glacier",
            name = "Aurora Glacier Road",
            themeTag = "ARCTIC PEAKS",
            worldStyle = RoadWorldStyle.FROST_GLACIER,
            description = "Sculpted crystal-ice bridges & snow caps under shimmering Northern Lights.",
            levelStyleBadge = "Crystal Chasms • Aerial Chains",
            priceCoins = 850,
            perkLabel = "Aurora Sky + Glacial Chasm Level (+20% Coins)",
            roadTopColor = Color(0xFFE0F2FE),
            roadBevelColor = Color(0xFF38BDF8),
            roadBodyColor = Color(0xFF1E3A8A),
            roadDeepColor = Color(0xFF0F1E4D),
            railColor = Color(0xFF7DD3FC),
            skyTintColor = Color(0xFF081D33),
            skyHorizonColor = Color(0xFF113C5E),
            towerColor = Color(0xFF0B2242),
            windowColor = Color(0xFF67E8F9),
            particleColor = Color(0xFFFFFFFF),
            coinBonusPercent = 20
        ),
        RoadThemeProfile(
            id = "galaxy_grid",
            name = "Cosmic Hyperway",
            themeTag = "DEEP SPACE",
            worldStyle = RoadWorldStyle.GALAXY_COSMOS,
            description = "Holographic star-circuit void slabs suspended before ringed worlds & warp grids.",
            levelStyleBadge = "Zero-G Islands • Max Shards",
            priceCoins = 1300,
            perkLabel = "Ringed Planet + Zero-G Level (+30% Coins)",
            roadTopColor = Color(0xFFF472B6),
            roadBevelColor = Color(0xFFC026D3),
            roadBodyColor = Color(0xFF4C1D95),
            roadDeepColor = Color(0xFF1F0A4A),
            railColor = Color(0xFF22D3EE),
            skyTintColor = Color(0xFF140729),
            skyHorizonColor = Color(0xFF34115C),
            towerColor = Color(0xFF110624),
            windowColor = Color(0xFF22D3EE),
            particleColor = Color(0xFFF472B6),
            coinBonusPercent = 30
        )
    )

    fun byId(id: String): RoadThemeProfile {
        return roads.firstOrNull { it.id == id } ?: roads.first()
    }
}
