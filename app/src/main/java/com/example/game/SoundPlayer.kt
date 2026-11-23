package com.example.game

enum class SoundEffect {
    // Gameplay events
    JUMP,
    COIN,
    HIT_OBSTACLE,
    DASH,
    SPRING,
    STAGE_CLEAR,

    // Global UI & Menu events
    BUTTON_CLICK,
    MENU_NAVIGATE,
    UNLOCK_SUCCESS,
    ACTION_DENIED
}

/**
 * Pure Kotlin audio contract used across [PlatformerEngine], ViewModels, and Compose UI
 * so all app interactions and JVM unit tests share consistent sound hooks.
 */
interface SoundPlayer {
    fun play(effect: SoundEffect)
    fun playButtonClick() = play(SoundEffect.BUTTON_CLICK)
    fun playMenuNavigate() = play(SoundEffect.MENU_NAVIGATE)
    fun playUnlockSuccess() = play(SoundEffect.UNLOCK_SUCCESS)
    fun playActionDenied() = play(SoundEffect.ACTION_DENIED)
    fun setEnabled(enabled: Boolean)
    fun isEnabled(): Boolean
    fun setMasterVolume(volume: Float) = Unit
    fun release()
}

object NoOpSoundPlayer : SoundPlayer {
    private var enabled = true
    override fun play(effect: SoundEffect) = Unit
    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
    override fun isEnabled(): Boolean = enabled
    override fun release() = Unit
}
