package com.example.gitpulse.ui.companion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompanionLogicTest {

    @Test
    fun moodThresholdsFollowTheWeeklyPace() {
        assertEquals(CompanionMood.RESTING, companionMood(0))
        assertEquals(CompanionMood.RESTING, companionMood(1))
        assertEquals(CompanionMood.STEADY, companionMood(2))
        assertEquals(CompanionMood.STEADY, companionMood(4))
        assertEquals(CompanionMood.THRIVING, companionMood(5))
        assertEquals(CompanionMood.THRIVING, companionMood(7))
    }

    @Test
    fun tailsGrowWithLongestStreakAndNeverRegress() {
        assertEquals(1, kitsuTailCount(0))
        assertEquals(1, kitsuTailCount(29))
        assertEquals(2, kitsuTailCount(30))
        assertEquals(2, kitsuTailCount(99))
        assertEquals(3, kitsuTailCount(100))
        assertEquals(3, kitsuTailCount(365))
    }

    @Test
    fun nextTailMilestoneStepsThirtyThenHundredThenDone() {
        assertEquals(30, nextTailMilestone(0))
        assertEquals(30, nextTailMilestone(29))
        assertEquals(100, nextTailMilestone(30))
        assertEquals(100, nextTailMilestone(99))
        assertNull(nextTailMilestone(100))
        assertNull(nextTailMilestone(400))
    }

    @Test
    fun greetingBucketsCoverTheDay() {
        assertEquals("Late night", timeGreeting(2))
        assertEquals("Morning", timeGreeting(9))
        assertEquals("Afternoon", timeGreeting(14))
        assertEquals("Evening", timeGreeting(21))
    }

    @Test
    fun quipsAreMoodAwareAndIncludeATimeLine() {
        CompanionMood.values().forEach { mood ->
            assertTrue(kitsuQuips(mood, 10).isNotEmpty())
        }
        val lateNight = kitsuQuips(CompanionMood.RESTING, 2)
        assertTrue(lateNight.any { it.contains("lantern") })
        val morning = kitsuQuips(CompanionMood.STEADY, 9)
        assertTrue(morning.any { it.contains("Morning") })
    }
}
