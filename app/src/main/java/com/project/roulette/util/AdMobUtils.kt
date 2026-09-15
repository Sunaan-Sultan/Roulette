package com.project.roulette.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.project.roulette.ADS_ENABLED
import com.project.roulette.AD_COOLDOWN_MS
import com.project.roulette.AD_DEBUG_LOGGING
import com.project.roulette.AD_MAX_LOAD_RETRIES
import com.project.roulette.AD_MAX_PER_SESSION
import com.project.roulette.AD_MIN_OPPORTUNITIES_BETWEEN
import com.project.roulette.AD_SESSION_GRACE_MS
import com.project.roulette.AD_SPINS_PER_AD

internal enum class AdSlot { NAVIGATION, SWITCH }

/**
 * Where an interstitial is being requested from. Each placement maps to its own
 * ad unit so AdMob reporting can tell them apart, but they all share one
 * frequency cap — the user experiences a single stream of ads, not two.
 */
enum class AdPlacement(
    internal val slot: AdSlot,
    /**
     * True when the user reached this placement through real engagement rather
     * than by merely moving around the app.
     *
     * Earned placements skip the cold-start grace and the opportunity-pacing
     * rule. Both of those exist to stop ads firing at users who have barely
     * used the app — which is precisely not the case here, and applying them
     * anyway silently swallows the ad.
     */
    internal val isEarned: Boolean,
) {
    /** Leaving a wheel. A natural break, but reachable seconds after launch. */
    WHEEL_EXIT(AdSlot.NAVIGATION, isEarned = false),

    /** After every Nth spin, once the winner dialog is dismissed. */
    SPIN_MILESTONE(AdSlot.SWITCH, isEarned = true),

    /** After a wheel is created or edited and saved. */
    WHEEL_SAVED(AdSlot.NAVIGATION, isEarned = true),
}

private val AdSlot.adUnitId: String
    get() = when (this) {
        // Test ids, kept for local debugging:
        //   NAVIGATION -> "ca-app-pub-3940256099942544/1033173712"
        AdSlot.NAVIGATION -> "ca-app-pub-9720007236604856/1522157778"
        AdSlot.SWITCH -> "ca-app-pub-9720007236604856/7907828064"
    }

/**
 * Single owner of every interstitial in the app: loading, frequency capping and
 * presentation.
 *
 * The frequency cap is deliberately layered, because a bare in-memory timestamp
 * leaks impressions in several ways:
 *
 *  - it is lost on every process start, so a backgrounded-and-killed app (or a
 *    re-run from the IDE) starts with no cooldown at all. The timestamp is
 *    therefore persisted, in wall-clock time, and guarded against clock rollback.
 *  - `SystemClock.elapsedRealtime()` restarts at zero on reboot, so it cannot be
 *    persisted meaningfully. Wall clock is used instead.
 *  - marking the cooldown from `onAdShowedFullScreenContent` is too late: two
 *    `show()` requests dispatched in the same frame both pass the gate before
 *    either callback lands. The cooldown is claimed synchronously, before
 *    `show()`, and released again only if the ad genuinely fails to present.
 *
 * Layered rules are easy to get wrong in ways that are invisible from the
 * outside, so every decision is logged with the rule that drove it. Watch it
 * with `adb logcat -s RouletteAds`.
 */
object AdManager {

    private const val TAG = "RouletteAds"
    private const val PREFS_NAME = "roulette_ads"
    private const val KEY_LAST_AD_AT = "last_ad_shown_at"

    private val ads = mutableMapOf<AdSlot, InterstitialAd?>()
    private val loading = mutableSetOf<AdSlot>()
    private val loadFailures = mutableMapOf<AdSlot, Int>()

    private val handler = Handler(Looper.getMainLooper())

    private var prefs: SharedPreferences? = null
    private var appContext: Context? = null

    /** Wall-clock time of the last impression, mirrored in memory to avoid disk reads. */
    private var lastAdShownAt = 0L

    /** Set for the whole presentation, so nothing else can open an ad underneath one. */
    private var adInFlight = false

    private var sessionStartedAt = 0L

    /**
     * Completed spins this session, counted across every wheel. Scoping this per
     * wheel visit would mean two spins on one wheel and two on another never
     * reach a milestone, despite four spins of engagement.
     */
    private var spinsCompleted = 0
    private var shownThisSession = 0
    private var opportunitiesSinceLastAd = AD_MIN_OPPORTUNITIES_BETWEEN

    /** Call once, from the Activity, after `MobileAds.initialize`. */
    fun init(context: Context) {
        if (!ADS_ENABLED) return
        appContext = context.applicationContext
        if (prefs == null) {
            prefs = context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            lastAdShownAt = prefs?.getLong(KEY_LAST_AD_AT, 0L) ?: 0L

            val now = System.currentTimeMillis()
            // Clock moved backwards (reboot with no network time, manual change).
            // Treat the stored window as spent rather than blocking ads forever.
            if (lastAdShownAt > now) {
                lastAdShownAt = 0L
                prefs?.edit()?.remove(KEY_LAST_AD_AT)?.apply()
            }
        }
        if (sessionStartedAt == 0L) sessionStartedAt = System.currentTimeMillis()
        log("init: ${(System.currentTimeMillis() - lastAdShownAt) / 1000}s since last ad")
        preload(context)
    }

    /** Record a finished spin. Call once per completed spin animation. */
    fun onSpinCompleted() {
        spinsCompleted++
    }

    /** True when the spin just completed lands on a milestone worth an ad. */
    fun isSpinMilestone(): Boolean =
        spinsCompleted > 0 && spinsCompleted % AD_SPINS_PER_AD == 0

    /** Makes sure both slots have an ad in hand. Cheap and idempotent. */
    fun preload(context: Context) {
        if (!ADS_ENABLED) return
        AdSlot.entries.forEach { load(context, it) }
    }

    /**
     * Shows an interstitial for [placement] if every frequency rule allows it,
     * then runs [onDone]. [onDone] is always invoked exactly once, ad or no ad,
     * so callers can use it directly as their navigation continuation.
     */
    fun show(context: Context, placement: AdPlacement, onDone: () -> Unit) {
        if (!ADS_ENABLED) {
            onDone()
            return
        }

        val slot = placement.slot
        val activity = context.findActivity()
        val ad = ads[slot]

        val blockedBy = when {
            activity == null -> "no Activity in the context chain"
            ad == null -> "$slot has no ad loaded yet"
            else -> blockingRule(placement)
        }

        if (blockedBy != null) {
            log("$placement blocked: $blockedBy")
            // A blocked opportunity still counts towards the pacing rule, and is
            // a good moment to refill an empty slot for next time.
            if (opportunitiesSinceLastAd < AD_MIN_OPPORTUNITIES_BETWEEN) {
                opportunitiesSinceLastAd++
            }
            load(context, slot)
            onDone()
            return
        }

        log("$placement showing from $slot")

        // Claim the cooldown up front. Anything else that reaches show() in the
        // same frame now sees an active cooldown instead of racing us.
        val previousLastAdShownAt = lastAdShownAt
        claimCooldown()

        var dismissed = false
        val finish = {
            if (!dismissed) {
                dismissed = true
                adInFlight = false
                onDone()
            }
        }

        ad!!.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                log("$placement dismissed")
                ads[slot] = null
                load(context, slot)
                finish()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                log("$placement failed to present: ${adError.message}")
                // Nothing was presented, so give the window back rather than
                // making the user wait out a cooldown they never paid for.
                releaseCooldown(previousLastAdShownAt)
                ads[slot] = null
                load(context, slot)
                finish()
            }
        }

        ads[slot] = null
        ad.show(activity!!)
    }

    // -- frequency rules ----------------------------------------------------

    /** The first rule that rejects [placement], or null when it may be shown. */
    private fun blockingRule(placement: AdPlacement): String? {
        if (adInFlight) return "another ad is already on screen"
        if (shownThisSession >= AD_MAX_PER_SESSION) {
            return "session cap reached ($shownThisSession/$AD_MAX_PER_SESSION)"
        }

        val now = System.currentTimeMillis()

        if (lastAdShownAt != 0L && now - lastAdShownAt < AD_COOLDOWN_MS) {
            val waitS = (AD_COOLDOWN_MS - (now - lastAdShownAt)) / 1000
            return "cooldown, ${waitS}s remaining"
        }

        // Both remaining rules exist to protect users who have not engaged yet,
        // so neither applies to a placement the user earned.
        if (!placement.isEarned) {
            val sinceStart = now - sessionStartedAt
            if (sinceStart < AD_SESSION_GRACE_MS) {
                return "cold-start grace, ${(AD_SESSION_GRACE_MS - sinceStart) / 1000}s remaining"
            }
            if (opportunitiesSinceLastAd < AD_MIN_OPPORTUNITIES_BETWEEN) {
                return "pacing, $opportunitiesSinceLastAd/$AD_MIN_OPPORTUNITIES_BETWEEN opportunities"
            }
        }

        return null
    }

    private fun claimCooldown() {
        adInFlight = true
        shownThisSession++
        opportunitiesSinceLastAd = 0
        lastAdShownAt = System.currentTimeMillis()
        prefs?.edit()?.putLong(KEY_LAST_AD_AT, lastAdShownAt)?.apply()
    }

    private fun releaseCooldown(previous: Long) {
        shownThisSession--
        opportunitiesSinceLastAd = AD_MIN_OPPORTUNITIES_BETWEEN
        lastAdShownAt = previous
        prefs?.edit()?.putLong(KEY_LAST_AD_AT, previous)?.apply()
    }

    // -- loading ------------------------------------------------------------

    private fun load(context: Context, slot: AdSlot) {
        if (!ADS_ENABLED || ads[slot] != null || slot in loading) return

        loading += slot
        InterstitialAd.load(
            context.applicationContext,
            slot.adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    loading -= slot
                    ads[slot] = null

                    // Without a retry an unfilled slot stays empty until the next
                    // screen entry, which reads as "the ad never shows".
                    val attempt = (loadFailures[slot] ?: 0) + 1
                    loadFailures[slot] = attempt
                    log("$slot failed to load (attempt $attempt): ${adError.message}")
                    if (attempt <= AD_MAX_LOAD_RETRIES) {
                        val delayMs = 2000L * attempt
                        handler.postDelayed({
                            appContext?.let { load(it, slot) }
                        }, delayMs)
                    }
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    loading -= slot
                    loadFailures[slot] = 0
                    ads[slot] = interstitialAd
                    log("$slot loaded")
                }
            }
        )
    }

    private fun log(message: String) {
        if (AD_DEBUG_LOGGING) Log.d(TAG, message)
    }
}

/**
 * Compose hands out whatever context the host view was built with, which for
 * this app is a configuration-wrapped one. Walk the wrapper chain rather than
 * casting, otherwise `show()` silently never finds an Activity.
 */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
