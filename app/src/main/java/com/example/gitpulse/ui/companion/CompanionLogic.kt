package com.example.gitpulse.ui.companion

/** Working companion name; rename here when the fox earns a real one. */
internal const val COMPANION_NAME = "Kitsu"

/** The fox's weekly outlook. Non-punitive by design: quiet weeks mean rest. */
internal enum class CompanionMood(val label: String, val subtitle: String) {
    THRIVING("THRIVING", "$COMPANION_NAME's been thriving this week."),
    STEADY("STEADY", "$COMPANION_NAME's keeping an easy pace."),
    RESTING("RESTING", "$COMPANION_NAME's having a quiet week — no rush.")
}

/** Mood from the number of active days in the last seven. */
internal fun companionMood(activeThisWeek: Int): CompanionMood = when {
    activeThisWeek >= 5 -> CompanionMood.THRIVING
    activeThisWeek >= 2 -> CompanionMood.STEADY
    else -> CompanionMood.RESTING
}

/** Kitsune tails grow with the longest streak ever reached; progress never
 *  regresses because the longest streak is monotonic. */
internal fun kitsuTailCount(longestStreakDays: Int): Int = when {
    longestStreakDays >= 100 -> 3
    longestStreakDays >= 30 -> 2
    else -> 1
}

/** Streak milestone that grows the next tail, or null when fully evolved. */
internal fun nextTailMilestone(longestStreakDays: Int): Int? = when {
    longestStreakDays < 30 -> 30
    longestStreakDays < 100 -> 100
    else -> null
}

/** Time-of-day greeting for the hero headline. */
internal fun timeGreeting(hour: Int): String = when {
    hour < 5 -> "Late night"
    hour < 12 -> "Morning"
    hour < 18 -> "Afternoon"
    else -> "Evening"
}

/** Tap quips the fox can say: mood-aware lines plus a time-of-day nudge.
 *  The tone stays encouraging no matter how the week is going. */
internal fun kitsuQuips(mood: CompanionMood, hour: Int): List<String> {
    val moodLines = when (mood) {
        CompanionMood.THRIVING -> listOf(
            "The meadow's looking lush — you've been busy!",
            "*happy tail wag* Keep the rhythm going.",
            "This week? Chef's kiss. Even the grass looks proud."
        )
        CompanionMood.STEADY -> listOf(
            "Steady pace. That's the way things grow.",
            "One commit at a time, nice and easy.",
            "A calm week to tend the meadow."
        )
        CompanionMood.RESTING -> listOf(
            "Rest is part of the cycle. No rush at all.",
            "*content fox noises*",
            "Quiet weeks happen — I'll keep watch."
        )
    }
    val timeLines = when {
        hour < 5 -> listOf("Late night? I'll keep the meadow lantern on.")
        hour < 12 -> listOf("Morning! Fresh start, fresh commits?")
        hour < 18 -> listOf("Afternoon check-in. How's the code treating you?")
        else -> listOf("Evening wind-down... or one more commit?")
    }
    return moodLines + timeLines
}
