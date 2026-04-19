package com.example.aurascan

import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import kotlin.math.roundToInt

private const val AdsLogTag = "AuraScanAds"

/**
 * Bottom banner using AdMob **anchored adaptive** size so the creative spans the slot width
 * (standard [AdSize.BANNER] is only 320dp wide and shows side gutters on phones).
 * Height follows the SDK’s anchored adaptive rules (two-arg API on current Play services).
 * [navigationBarsPadding] keeps the strip clear of the gesture / 3-button nav bar with edge-to-edge.
 */
@Composable
fun AdMobBannerStripe(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .navigationBarsPadding(),
    ) {
        val adWidthDp = maxWidth.value.roundToInt().coerceAtLeast(AdSize.BANNER.width)

        key(adWidthDp) {
            val adView = remember(context, adWidthDp) {
                val size = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                    context,
                    adWidthDp,
                )
                AdView(context).apply {
                    setAdSize(size)
                    adUnitId = context.getString(R.string.admob_banner_unit_id)
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d(AdsLogTag, "Banner loaded")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.w(
                                AdsLogTag,
                                "Banner failed code=${error.code} domain=${error.domain} message=${error.message}",
                            )
                        }
                    }
                }
            }

            DisposableEffect(adView, lifecycle) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_PAUSE -> adView.pause()
                        Lifecycle.Event.ON_RESUME -> adView.resume()
                        else -> Unit
                    }
                }
                lifecycle.addObserver(observer)

                var cancelled = false
                val appContext = context.applicationContext
                MobileAds.initialize(
                    appContext,
                    OnInitializationCompleteListener {
                        if (cancelled) return@OnInitializationCompleteListener
                        runCatching {
                            adView.loadAd(AdRequest.Builder().build())
                        }.onFailure { e ->
                            Log.w(AdsLogTag, "loadAd failed", e)
                        }
                    },
                )

                onDispose {
                    cancelled = true
                    lifecycle.removeObserver(observer)
                    adView.destroy()
                }
            }

            AndroidView(
                factory = { adView },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            )
        }
    }
}
