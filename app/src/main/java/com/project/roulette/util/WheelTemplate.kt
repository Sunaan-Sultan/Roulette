package com.project.roulette.util

/**
 * A bundled starter wheel users can create with one tap.
 *
 * Templates are intentionally lightweight: just names + a palette. The editor
 * turns [segmentNames] into real [com.project.roulette.domain.model.Segment]s
 * (assigning ids and colors) so the user can customize before saving.
 *
 * Add an entry here to surface a new card in the Templates gallery.
 */
data class WheelTemplate(
    val id: String,
    val emoji: String,
    val name: String,
    val description: String,
    val paletteIndex: Int,
    val segmentNames: List<String>
)

object WheelTemplates {
    val all: List<WheelTemplate> = listOf(
        WheelTemplate(
            id = "yes_no",
            emoji = "🤔",
            name = "Yes or No",
            description = "Settle it in one spin",
            paletteIndex = 0,
            segmentNames = listOf("Yes", "No", "Maybe")
        ),
        WheelTemplate(
            id = "what_to_eat",
            emoji = "🍽️",
            name = "What to Eat",
            description = "End the dinner debate",
            paletteIndex = 4,
            segmentNames = listOf(
                "Pizza", "Burgers", "Sushi", "Pasta",
                "Tacos", "Salad", "Chinese", "Sandwich"
            )
        ),
        WheelTemplate(
            id = "team_picker",
            emoji = "👥",
            name = "Team Picker",
            description = "Split into fair teams",
            paletteIndex = 3,
            segmentNames = listOf("Team Red", "Team Blue", "Team Green", "Team Yellow")
        ),
        WheelTemplate(
            id = "truth_or_dare",
            emoji = "🎯",
            name = "Truth or Dare",
            description = "Party game classic",
            paletteIndex = 2,
            segmentNames = listOf("Truth", "Dare")
        ),
        WheelTemplate(
            id = "decision_maker",
            emoji = "⚖️",
            name = "Decision Maker",
            description = "Let the wheel decide",
            paletteIndex = 1,
            segmentNames = listOf("Do it", "Don't", "Wait", "Ask a friend")
        ),
        WheelTemplate(
            id = "who_pays",
            emoji = "💳",
            name = "Who Pays?",
            description = "Pick who covers the bill",
            paletteIndex = 5,
            segmentNames = listOf("Me", "You", "Split it", "Not me!")
        ),
        WheelTemplate(
            id = "chores",
            emoji = "🧹",
            name = "Chore Chart",
            description = "Assign the housework",
            paletteIndex = 6,
            segmentNames = listOf("Dishes", "Laundry", "Vacuum", "Trash", "Bathroom", "Cook")
        ),
        WheelTemplate(
            id = "movie_night",
            emoji = "🎬",
            name = "Movie Night",
            description = "Pick tonight's genre",
            paletteIndex = 7,
            segmentNames = listOf(
                "Action", "Comedy", "Horror", "Sci-Fi", "Drama", "Documentary"
            )
        )
    )

    fun byId(id: String): WheelTemplate? = all.firstOrNull { it.id == id }
}
