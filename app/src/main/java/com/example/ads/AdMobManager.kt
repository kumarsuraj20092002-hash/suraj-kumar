package com.example.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Offline-safe AdMob manager.
 * - When offline: never blocks gameplay, never throws, collapses ad containers cleanly.
 * - When online: initializes MobileAds and preloads Banner / Rewarded / Interstitial units.
 */
class AdMobManager(private val appContext: Context) {

    private var sdkInitialized = false
    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingRewarded = false
    private var isLoadingInterstitial = false

    private val _isOnline = MutableStateFlow(checkOnline(appContext))
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _rewardedReady = MutableStateFlow(false)
    val rewardedReady: StateFlow<Boolean> = _rewardedReady.asStateFlow()

    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateOnlineState()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            updateOnlineState()
        }

        override fun onLost(network: Network) {
            updateOnlineState()
        }
    }

    private fun updateOnlineState() {
        val online = checkOnline(appContext)
        _isOnline.value = online
        if (!online) {
            _rewardedReady.value = false
        }
    }

    init {
        runCatching {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        }
    }

    fun initializeAndPreload(
        rewardedUnitId: String,
        interstitialUnitId: String,
        adsEnabled: Boolean,
        offlineOnlyMode: Boolean = false
    ) {
        val online = checkOnline(appContext)
        _isOnline.value = online
        if (!online || !adsEnabled || offlineOnlyMode) {
            _rewardedReady.value = false
            return
        }

        if (!sdkInitialized) {
            runCatching {
                MobileAds.initialize(appContext) {
                    sdkInitialized = true
                    preloadRewarded(rewardedUnitId)
                    preloadInterstitial(interstitialUnitId)
                }
            }
        } else {
            preloadRewarded(rewardedUnitId)
            preloadInterstitial(interstitialUnitId)
        }
    }

    fun preloadRewarded(adUnitId: String) {
        if (!checkOnline(appContext) || adUnitId.isBlank() || rewardedAd != null || isLoadingRewarded) return
        isLoadingRewarded = true
        runCatching {
            RewardedAd.load(
                appContext,
                adUnitId,
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        isLoadingRewarded = false
                        rewardedAd = ad
                        _rewardedReady.value = true
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        isLoadingRewarded = false
                        rewardedAd = null
                        _rewardedReady.value = false
                    }
                }
            )
        }.onFailure {
            isLoadingRewarded = false
        }
    }

    fun showRewardedAd(
        activity: Activity?,
        rewardedUnitId: String,
        onEarnedCoins: (Int) -> Unit,
        onClosedOrUnavailable: () -> Unit
    ) {
        val ad = rewardedAd
        if (activity == null || ad == null || !checkOnline(appContext)) {
            _rewardedReady.value = false
            onClosedOrUnavailable()
            return
        }
        rewardedAd = null
        _rewardedReady.value = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preloadRewarded(rewardedUnitId)
                onClosedOrUnavailable()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                preloadRewarded(rewardedUnitId)
                onClosedOrUnavailable()
            }
        }
        ad.show(activity) {
            onEarnedCoins(75)
        }
    }

    fun preloadInterstitial(adUnitId: String) {
        if (!checkOnline(appContext) || adUnitId.isBlank() || interstitialAd != null || isLoadingInterstitial) return
        isLoadingInterstitial = true
        runCatching {
            InterstitialAd.load(
                appContext,
                adUnitId,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        isLoadingInterstitial = false
                        interstitialAd = ad
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        isLoadingInterstitial = false
                        interstitialAd = null
                    }
                }
            )
        }.onFailure {
            isLoadingInterstitial = false
        }
    }

    fun maybeShowInterstitial(activity: Activity?, interstitialUnitId: String) {
        val ad = interstitialAd ?: return
        if (activity == null || !checkOnline(appContext)) return
        interstitialAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preloadInterstitial(interstitialUnitId)
            }
        }
        ad.show(activity)
    }

    fun release() {
        runCatching {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        }
    }

    companion object {
        fun checkOnline(context: Context): Boolean {
            return runCatching {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    ?: return false
                val network = cm.activeNetwork ?: return false
                val caps = cm.getNetworkCapabilities(network) ?: return false
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }.getOrDefault(false)
        }
    }
}

@Composable
fun OfflineSafeAdBanner(
    adUnitId: String,
    adsEnabled: Boolean,
    isOnline: Boolean,
    offlineOnlyMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (!adsEnabled || offlineOnlyMode || !isOnline || adUnitId.isBlank()) {
        return
    }

    val context = LocalContext.current
    var adLoaded by remember(adUnitId) { mutableStateOf(false) }

    val adView = remember(adUnitId) {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            this.adUnitId = adUnitId
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    adLoaded = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    adLoaded = false
                }
            }
            runCatching {
                loadAd(AdRequest.Builder().build())
            }
        }
    }

    DisposableEffect(adView) {
        onDispose {
            runCatching { adView.destroy() }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (adLoaded) 54.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        if (adLoaded) {
            AndroidView(
                factory = { adView }
            )
        }
    }
}
