package com.example.gitpulse.data

import androidx.compose.ui.graphics.Color
import com.example.gitpulse.theme.*

data class UserProfile(
    val name: String,
    val username: String,
    val bio: String,
    val reposCount: Int,
    val followersCount: String,
    val followingCount: Int,
    val starsCount: String,
    val activeStreakDays: Int,
    val longestStreakDays: Int,
    val commitsToday: Int,
    val totalAnnualCommits: Int,
    val currentGoalDays: Int,
    val goalPercentage: Int
)

data class HeatmapDay(
    val dateString: String,
    val dayOfWeek: Int, // 0 to 6
    val weekOfYear: Int,
    val level: Int, // 0 to 4
    val count: Int
)

data class GitActivityItem(
    val id: String,
    val repoName: String,
    val timeAgo: String,
    val message: String,
    val type: ActivityType,
    val commitHash: String? = null,
    val branch: String? = null,
    val statusText: String? = null,
    val prNumber: String? = null,
    val additions: Int = 0,
    val deletions: Int = 0
)

enum class ActivityType {
    COMMIT, PULL_REQUEST, REVIEW, ISSUE
}

data class DayInspectorSummary(
    val dateDisplay: String,
    val totalActions: Int,
    val subtitle: String,
    val commitsCount: Int,
    val commitsPercent: Int,
    val prCount: Int,
    val prPercent: Int,
    val reviewCount: Int,
    val reviewPercent: Int,
    val issueCount: Int,
    val issuePercent: Int
)

data class LanguageStat(
    val name: String,
    val color: Color,
    val reposCount: Int,
    val sizeMb: Double,
    val percentage: Double
)

data class ShowcaseRepo(
    val owner: String,
    val name: String,
    val description: String,
    val language: String,
    val languageColor: Color,
    val stars: Int,
    val forks: Int,
    val license: String
)

data class ComparisonProfile(
    val name: String,
    val username: String,
    val initials: String,
    val activeStreak: String,
    val annualCommits: String,
    val longestStreak: String,
    val isLongestStreakLeader: Boolean,
    val primaryStack: String,
    val primaryStackColor: Color,
    val publicStars: String
)

data class WatchlistAccount(
    val id: String,
    val name: String,
    val handle: String,
    val initials: String,
    val tag: String,
    val subtitle: String,
    val isCurrent: Boolean,
    val actionText: String
)
