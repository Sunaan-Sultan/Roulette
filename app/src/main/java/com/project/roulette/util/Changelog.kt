package com.project.roulette.util

data class ChangelogEntry(
    val versionCode: Int,
    val versionName: String,
    val highlights: List<String>
)

/**
 * Bump versionCode/versionName to match app/build.gradle and add an entry
 * whenever a release should show a "What's new" card to users.
 */
object AppChangelog {
    val entries: List<ChangelogEntry> = listOf(
        ChangelogEntry(
            versionCode = 22,
            versionName = "2.2",
            highlights = listOf(
                "Templates gallery — start from ready-made wheels like Yes or No, What to Eat and more",
                "Creating a wheel now returns you straight to Home when you save"
            )
        ),
        ChangelogEntry(
            versionCode = 21,
            versionName = "2.1",
            highlights = listOf(
                "New Statistics page to track your spin history",
                "Share your spin history as a PDF",
                "Redesigned Settings page"
            )
        )
    ).sortedByDescending { it.versionCode }

    val latest: ChangelogEntry? = entries.firstOrNull()
}
