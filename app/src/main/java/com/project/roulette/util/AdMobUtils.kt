package com.project.roulette.util

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.project.roulette.ADS_ENABLED

var mInterstitialAd: InterstitialAd? = null
var mSwitchInterstitialAd: InterstitialAd? = null

fun loadInterstitial(context: Context) {
    if (!ADS_ENABLED) {
        mInterstitialAd = null
        return
    }

    val adRequest = AdRequest.Builder().build()

    // Test ad ID
//    val adUnitId = "ca-app-pub-3940256099942544/1033173712"

    // Live ad ID
     val adUnitId = "ca-app-pub-9720007236604856/1522157778"
//    val adUnitId = ""

    InterstitialAd.load(
        context,
        adUnitId,
        adRequest,
        object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        }
    )
}

fun loadSwitchInterstitial(context: Context) {
    if (!ADS_ENABLED) {
        mSwitchInterstitialAd = null
        return
    }

    val adRequest = AdRequest.Builder().build()
    val adUnitId = "ca-app-pub-9720007236604856/7907828064"
//    val adUnitId = ""

    InterstitialAd.load(
        context,
        adUnitId,
        adRequest,
        object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mSwitchInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mSwitchInterstitialAd = interstitialAd
            }
        }
    )
}

fun showInterstitial(context: Context, onAdDismissed: () -> Unit) {
    if (!ADS_ENABLED) {
        onAdDismissed()
        return
    }

    val activity = context as? Activity

    if (mInterstitialAd != null && activity != null) {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                mInterstitialAd = null
                loadInterstitial(context)
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                mInterstitialAd = null
                onAdDismissed()
            }
        }
        // Show the ad
        mInterstitialAd?.show(activity)
    } else {
        onAdDismissed()
    }
}

fun showSwitchInterstitial(context: Context, onAdDismissed: () -> Unit) {
    if (!ADS_ENABLED) {
        onAdDismissed()
        return
    }

    val activity = context as? Activity

    if (mSwitchInterstitialAd != null && activity != null) {
        mSwitchInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                mSwitchInterstitialAd = null
                loadSwitchInterstitial(context)
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                mSwitchInterstitialAd = null
                onAdDismissed()
            }
        }
        // Show the ad
        mSwitchInterstitialAd?.show(activity)
    } else {
        onAdDismissed()
    }
}