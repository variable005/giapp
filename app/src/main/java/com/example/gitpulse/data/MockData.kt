package com.example.gitpulse.data

import com.example.gitpulse.theme.*

object MockData {
    val userProfile = UserProfile(
        name = "Alex Chen",
        username = "alexchen.dev",
        bio = "Staff Systems Architect. Specializing in high-throughput distributed state machines, async Rust runtimes, and local-first databases.",
        reposCount = 42,
        followersCount = "1.8k",
        followingCount = 384,
        starsCount = "3.4k",
        activeStreakDays = 48,
        longestStreakDays = 114,
        commitsToday = 4,
        totalAnnualCommits = 1482,
        currentGoalDays = 50,
        goalPercentage = 96
    )

    val recentActivities = listOf(
        GitActivityItem(
            id = "1",
            repoName = "alexchen/distributed-kv",
            timeAgo = "18m ago",
            message = "raft: optimize election timeout backoff curve for high-latency clusters",
            type = ActivityType.COMMIT,
            commitHash = "b78f4e2",
            branch = "main",
            statusText = "Passed"
        ),
        GitActivityItem(
            id = "2",
            repoName = "rust-lang/cargo",
            timeAgo = "2h ago",
            message = "feat: implement speculative dependency resolution caching in resolver v3",
            type = ActivityType.PULL_REQUEST,
            prNumber = "#14289",
            statusText = "3 reviewers assigned"
        ),
        GitActivityItem(
            id = "3",
            repoName = "supabase/realtime",
            timeAgo = "5h ago",
            message = "fix: reconnect backpressure on WebSocket drop during node migration",
            type = ActivityType.COMMIT,
            commitHash = "92c1a8e",
            branch = "staging",
            statusText = "Merged"
        )
    )

    val daySummary = DayInspectorSummary(
        dateDisplay = "Wed, Dec 18, 2024",
        totalActions = 8,
        subtitle = "Optimal flow • Peak productivity window",
        commitsCount = 5,
        commitsPercent = 68,
        prCount = 2,
        prPercent = 18,
        reviewCount = 1,
        reviewPercent = 10,
        issueCount = 0,
        issuePercent = 4
    )

    val inspectorActivities = listOf(
        GitActivityItem(
            id = "insp-1",
            repoName = "core-engine / pipeline",
            timeAgo = "18:42",
            message = "feat(runtime): introduce isolated worker threads for telemetry aggregation",
            type = ActivityType.COMMIT,
            commitHash = "7a29e41",
            branch = "main",
            additions = 142,
            deletions = 18
        ),
        GitActivityItem(
            id = "insp-2",
            repoName = "mobile-ui / redesign",
            timeAgo = "15:10",
            message = "PR #418: Refactor contribution calendar grid to strict Material 3 flat container tokens",
            type = ActivityType.PULL_REQUEST,
            statusText = "Merged",
            branch = "feat/m3-day-view",
            additions = 120,
            deletions = 42
        ),
        GitActivityItem(
            id = "insp-3",
            repoName = "auth-gateway / v2",
            timeAgo = "11:24",
            message = "Approved changes on PR #129: Zero-trust token signature verification algorithm",
            type = ActivityType.REVIEW,
            statusText = "LGTM",
            branch = "security-audit"
        ),
        GitActivityItem(
            id = "insp-4",
            repoName = "devops-infra / k8s",
            timeAgo = "09:15",
            message = "fix(Ingress): configure strict TLS 1.3 ciphers and disable fallback renegotiation",
            type = ActivityType.COMMIT,
            commitHash = "c82f09d",
            branch = "release/v4.2",
            additions = 56,
            deletions = 24
        )
    )

    val languageStats = listOf(
        LanguageStat("Rust", LangRust, 12, 5.68, 38.4),
        LanguageStat("TypeScript", LangTypeScript, 18, 4.61, 31.2),
        LanguageStat("Go", LangGo, 6, 2.75, 18.6),
        LanguageStat("Kotlin", LangKotlin, 4, 1.11, 7.5),
        LanguageStat("Python", LangPython, 2, 0.65, 4.3)
    )

    val showcaseRepos = listOf(
        ShowcaseRepo(
            owner = "alexchen",
            name = "hyper-router",
            description = "Sub-millisecond latency asynchronous distributed edge proxy for Rust applications with zero-copy packet slicing.",
            language = "Rust",
            languageColor = LangRust,
            stars = 1124,
            forks = 189,
            license = "Apache-2.0"
        ),
        ShowcaseRepo(
            owner = "alexchen",
            name = "react-native-haptics-v3",
            description = "Core vibration pattern synthesis and precision tactile driver interface for Android & iOS micro-actuators.",
            language = "TypeScript",
            languageColor = LangTypeScript,
            stars = 512,
            forks = 94,
            license = "MIT"
        ),
        ShowcaseRepo(
            owner = "alexchen",
            name = "git-pulse-core",
            description = "High throughput streaming analytics consumer engine with zero-alloc Webhook parsers for enterprise git relays.",
            language = "Go",
            languageColor = LangGo,
            stars = 206,
            forks = 35,
            license = "MIT"
        )
    )

    val profileA = ComparisonProfile(
        name = "Alex Chen",
        username = "@alexchen",
        initials = "AC",
        activeStreak = "48d",
        annualCommits = "1,429",
        longestStreak = "114d",
        isLongestStreakLeader = false,
        primaryStack = "TypeScript",
        primaryStackColor = SecondaryContainerEmerald,
        publicStars = "842"
    )

    val profileB = ComparisonProfile(
        name = "Sarah Lin",
        username = "@sarah-codes",
        initials = "SL",
        activeStreak = "32d",
        annualCommits = "1,288",
        longestStreak = "142d",
        isLongestStreakLeader = true,
        primaryStack = "Rust",
        primaryStackColor = TertiaryContainerAmber,
        publicStars = "619"
    )

    val watchlistAccounts = listOf(
        WatchlistAccount(
            id = "1",
            name = "Alex Chen",
            handle = "alex-personal",
            initials = "AC",
            tag = "Active",
            subtitle = "alex-personal • 48d streak",
            isCurrent = true,
            actionText = "Current"
        ),
        WatchlistAccount(
            id = "2",
            name = "alex-work-corp",
            handle = "work-org",
            initials = "Corp",
            tag = "SSO",
            subtitle = "SAML 2.0 • Synced 12m ago",
            isCurrent = false,
            actionText = "Switch"
        ),
        WatchlistAccount(
            id = "3",
            name = "sarah-codes",
            handle = "sarah",
            initials = "SL",
            tag = "Peer",
            subtitle = "32d streak • 1.2k commits",
            isCurrent = false,
            actionText = "Inspect"
        ),
        WatchlistAccount(
            id = "4",
            name = "torvalds",
            handle = "torvalds",
            initials = "LT",
            tag = "Public",
            subtitle = "Kernel maintainer • Linux",
            isCurrent = false,
            actionText = "View"
        )
    )
}
