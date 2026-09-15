package com.project.roulette

/** Master kill switch for every ad in the app. */
const val ADS_ENABLED = true

// ---------------------------------------------------------------------------
// Frequency capping
//
// An interstitial is only shown when EVERY rule below passes. They are
// deliberately layered: a single time-based cooldown is not enough, because a
// process restart, a fast double tap, or a burst of navigation can all slip
// past it.
// ---------------------------------------------------------------------------

/** Minimum wall-clock gap between two interstitials. Survives process death. */
const val AD_COOLDOWN_MS = 100_000L

/**
 * No ad during the first moments after a cold start. Keeps the app feeling fast
 * on launch and stops a fresh process from burning an impression before the
 * user has done anything.
 *
 * Only applies to placements the user has NOT earned through engagement — see
 * `AdPlacement.isEarned`. A milestone reached by actually spinning the wheel is
 * engagement by definition, and must not be swallowed just because it happened
 * quickly after launch.
 */
const val AD_SESSION_GRACE_MS = 30_000L

/**
 * Qualifying ad opportunities that must pass before the next interstitial, on
 * top of the time cooldown. Stops "one ad per tap" behaviour on a device that
 * has been sitting idle longer than the cooldown.
 *
 * Skipped for earned placements, which carry their own pacing (a milestone ad
 * already only fires every [AD_SPINS_PER_AD] spins; applying this rule on top
 * would silently push the second one out to nine spins).
 */
const val AD_MIN_OPPORTUNITIES_BETWEEN = 2

/** Hard ceiling on interstitials per app session. */
const val AD_MAX_PER_SESSION = 6

/** Show a milestone interstitial after every Nth completed spin. */
const val AD_SPINS_PER_AD = 3

/** Logs the exact rule that allowed or blocked each ad, under the "RouletteAds" tag. */
const val AD_DEBUG_LOGGING = true

/** Bounded retries when an ad unit fails to fill, so an empty slot recovers. */
const val AD_MAX_LOAD_RETRIES = 3
