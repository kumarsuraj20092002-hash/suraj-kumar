package com.example.game

import androidx.compose.ui.graphics.Color

enum class RunnerArchetype {
    CYBER_CLASSIC,
    FLAME_STRIKER,
    SOLARIS_KNIGHT,
    VENOM_NINJA,
    FROST_TITAN,
    NOVA_MAGE,
    SHADOW_PHANTOM,
    OMEGA_KING
}

/**
 * Pure Kotlin character profile (no Android Bitmap dependency) so physics engine
 * and JVM unit tests run without Android framework graphics stubs.
 */
data class RunnerCharacterProfile(
    val id: String,
    val name: String,
    val roleTitle: String,
    val tagline: String,
    val priceCoins: Int,
    val perkLabel: String,
    val archetype: RunnerArchetype,
    val primaryColor: Color,
    val scarfColor: Color,
    val armorColor: Color,
    val speedMultiplier: Float = 1.0f,
    val jumpMultiplier: Float = 1.0f,
    val dashCooldownSec: Float = 0.14f,
    val bonusCoinMultiplier: Float = 1.0f
)

object RunnerCatalog {
    val characters: List<RunnerCharacterProfile> = listOf(
        RunnerCharacterProfile(
            id = "volt",
            name = "Volt Cyan",
            roleTitle = "STARTER RUNNER",
            tagline = "Neon visor cyber-sprinter built for pure speed.",
            priceCoins = 0,
            perkLabel = "Balanced Arcade Handling",
            archetype = RunnerArchetype.CYBER_CLASSIC,
            primaryColor = Color(0xFF22D3EE),
            scarfColor = Color(0xFFFB7185),
            armorColor = Color(0xFF1E293B)
        ),
        RunnerCharacterProfile(
            id = "blaze",
            name = "Blaze Striker",
            roleTitle = "FLAME BRAWLER",
            tagline = "Blazing flame crest with ember turbo boots.",
            priceCoins = 100,
            perkLabel = "+20% Coins & +5% Run Speed",
            archetype = RunnerArchetype.FLAME_STRIKER,
            primaryColor = Color(0xFFF97316),
            scarfColor = Color(0xFFFACC15),
            armorColor = Color(0xFF7C2D12),
            speedMultiplier = 1.05f,
            bonusCoinMultiplier = 1.20f
        ),
        RunnerCharacterProfile(
            id = "solaris",
            name = "Solaris Knight",
            roleTitle = "GOLD PALADIN",
            tagline = "Armored solar helm & royal crimson battle cape.",
            priceCoins = 200,
            perkLabel = "+35% Bonus Coins Per Shard",
            archetype = RunnerArchetype.SOLARIS_KNIGHT,
            primaryColor = Color(0xFFFBBF24),
            scarfColor = Color(0xFFEF4444),
            armorColor = Color(0xFF78350F),
            bonusCoinMultiplier = 1.35f
        ),
        RunnerCharacterProfile(
            id = "venom",
            name = "Venom Ninja",
            roleTitle = "CYBER SHINOBI",
            tagline = "Stealth hood with twin cyan plasma blades.",
            priceCoins = 350,
            perkLabel = "Ultra-Fast Air-Dash Recharge",
            archetype = RunnerArchetype.VENOM_NINJA,
            primaryColor = Color(0xFF34D399),
            scarfColor = Color(0xFF22D3EE),
            armorColor = Color(0xFF064E3B),
            speedMultiplier = 1.06f,
            dashCooldownSec = 0.06f
        ),
        RunnerCharacterProfile(
            id = "frost",
            name = "Frost Titan",
            roleTitle = "CRYO MECH",
            tagline = "Heavy cryo-visor mech forged in glacier steel.",
            priceCoins = 500,
            perkLabel = "+10% Speed & +25% Coins",
            archetype = RunnerArchetype.FROST_TITAN,
            primaryColor = Color(0xFF38BDF8),
            scarfColor = Color(0xFFE0F2FE),
            armorColor = Color(0xFF1E3A8A),
            speedMultiplier = 1.10f,
            bonusCoinMultiplier = 1.25f
        ),
        RunnerCharacterProfile(
            id = "nova",
            name = "Nova Mage",
            roleTitle = "PLASMA SORCERER",
            tagline = "Arcane horned crown with anti-gravity boots.",
            priceCoins = 750,
            perkLabel = "+14% Higher Super Jump",
            archetype = RunnerArchetype.NOVA_MAGE,
            primaryColor = Color(0xFFA855F7),
            scarfColor = Color(0xFFFBBF24),
            armorColor = Color(0xFF3B0764),
            jumpMultiplier = 1.14f
        ),
        RunnerCharacterProfile(
            id = "phantom",
            name = "Shadow Phantom",
            roleTitle = "VOID ASSASSIN",
            tagline = "Midnight cowl with glowing magenta spirit aura.",
            priceCoins = 1100,
            perkLabel = "+12% Speed & Fast Dash +7% Jump",
            archetype = RunnerArchetype.SHADOW_PHANTOM,
            primaryColor = Color(0xFFF472B6),
            scarfColor = Color(0xFFA855F7),
            armorColor = Color(0xFF111827),
            speedMultiplier = 1.12f,
            jumpMultiplier = 1.07f,
            dashCooldownSec = 0.07f
        ),
        RunnerCharacterProfile(
            id = "omega",
            name = "Omega King",
            roleTitle = "LEGENDARY EMPEROR",
            tagline = "Royal golden crown, arctic armor & scarlet aura.",
            priceCoins = 1600,
            perkLabel = "+15% Speed • +10% Jump • +50% Coins",
            archetype = RunnerArchetype.OMEGA_KING,
            primaryColor = Color(0xFFFB7185),
            scarfColor = Color(0xFFFBBF24),
            armorColor = Color(0xFFF1F5F9),
            speedMultiplier = 1.15f,
            jumpMultiplier = 1.10f,
            dashCooldownSec = 0.07f,
            bonusCoinMultiplier = 1.50f
        )
    )

    fun byId(id: String): RunnerCharacterProfile {
        return characters.firstOrNull { it.id == id } ?: characters.first()
    }
}
