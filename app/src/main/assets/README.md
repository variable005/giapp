# GitPulse

GitPulse is a native Android application built with Jetpack Compose and Material Design 3 for tracking GitHub streaks, developer velocity, repository portfolios, and annual contribution heatmaps.

> WARNING: ACTIVE DEVELOPMENT
> This repository is currently under heavy active development. Architectural patterns, screen layouts, and API integrations are subject to rapid change without backward compatibility guarantees.

---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Architecture and Tech Stack](#architecture-and-tech-stack)
- [Project Structure](#project-structure)
- [Configuration and Authentication](#configuration-and-authentication)
- [Navigation and Gestures](#navigation-and-gestures)

---

## Overview

GitPulse provides an expressive, fluid dashboard for software developers who want deep visibility into their open-source cadence and commit velocity. Moving beyond simple static statistics, GitPulse presents interactive visualizations, full 52-week annual heatmaps, repository showcase metrics, language breakdown percentages, and live GitHub event feeds.

The interface adheres strictly to Google Material Design 3 guidelines, employing spring-based physics motion tokens, tactile haptic feedback, and balanced symmetrical card layouts.

---

## Key Features

- Interactive 52-Week Contribution Matrix:
  Horizontally scrollable calendar showing an entire year of commits with weekday markers (Mon, Wed, Fri), dynamic month headers, theme-aware activity tiers, spring-scaled cell selection, and an on-demand day inspection panel.

- Developer Velocity and Telemetry Grid:
  Uniform 2x2 telemetry cards tracking current active streak, longest historical streak, daily commit velocity, and weekend warrior ratios with directional delta badges and standardized dimensions.

- Repository Showcase and Language Breakdown:
  Portfolio display highlighting top repositories with real-time star counts, fork tracking, and license capsules. Complete language distribution breakdown with individualized progress indicators and repo counts.

- Live Activity Stream:
  Chronological event feed of commits, pull requests, releases, and repository actions with pull-to-refresh capabilities.

- Ergonomic Motion and Tactile Feedback:
  Bottom navigation with bouncy spring physics, icon pop scaling, physical haptic actuator impulses, and zero-flicker concurrent crossfade transitions between top-level destinations.

- Predictive Screen Mapping:
  Hierarchical system back navigation that smoothly closes active search dialogs, bottom sheets, or preferences, and returns to the primary Home dashboard before exiting the application.

- Offline-First Resilience and Local Cache:
  Configurable caching layer with cache purging, reset-to-defaults capabilities, and mock fallback data when offline or rate-limited.

---

## Architecture and Tech Stack

GitPulse is built using modern Android architecture guidelines with unidirectional data flow (UDF) and reactive state management:

- Language: Kotlin 2.1.0 with JVM Toolchain 17
- UI Framework: Jetpack Compose with Material Design 3 (Material You)
- Architecture Pattern: MVVM (Model-View-ViewModel) with StateFlow and Coroutines
- Motion System: Custom physics-based spring specifications and animated content transitions
- Networking: OkHttp 4.12.0 consuming GitHub REST API v3 and GraphQL API v4
- Image Loading: Coil Compose 2.7.0 with crossfade and memory disk caching
- Platform SDK:
  - Minimum SDK: 24 (Android 7.0 Nougat)
  - Target SDK: 36 (Android 16 preview / latest release)
  - Compile SDK: 36
- Build System: Gradle 9.1 with Android Gradle Plugin 8.9.0 and Version Catalogs

---

## Project Structure

```text
giapp/
|-- app/
|   |-- build.gradle.kts                 # Application-level Gradle build configuration
|   |-- proguard-rules.pro               # ProGuard and R8 optimization rules
|   `-- src/
|       |-- main/
|       |   |-- AndroidManifest.xml      # App permissions, activities, and theme definitions
|       |   |-- java/com/example/gitpulse/
|       |   |   |-- MainActivity.kt      # Single-activity container and Edge-to-Edge setup
|       |   |   |-- data/
|       |   |   |   |-- Models.kt        # Data classes for telemetry, streaks, and repositories
|       |   |   |   `-- remote/
|       |   |   |       `-- GitHubApiService.kt # REST and GraphQL client implementation
|       |   |   |-- theme/
|       |   |   |   |-- Color.kt         # Emerald-tailored palette and M3 color tokens
|       |   |   |   |-- Theme.kt         # Dynamic and static theme composables
|       |   |   |   `-- Type.kt          # Material 3 typographic scale
|       |   |   `-- ui/
|       |   |       |-- MainAppScreen.kt # Root scaffold, back navigation mapping, and transitions
|       |   |       |-- animation/
|       |   |       |   |-- AnimatedNumberCounter.kt # Rolling digit counter
|       |   |       |   `-- MotionTokens.kt          # Curated spring specs and shimmer brushes
|       |   |       |-- components/
|       |   |       |   `-- FloatingBottomNav.kt     # Bouncy navigation bar with haptics
|       |   |       |-- screens/
|       |   |       |   |-- AnalyticsScreen.kt       # Symmetrical telemetry and language breakdowns
|       |   |       |   |-- DashboardScreen.kt       # Primary overview and 52-week heatmap
|       |   |       |   |-- DayInspectorScreen.kt    # Deep dive into cycle days and hour distribution
|       |   |       |   `-- SettingsScreen.kt        # Configuration, token setup, and cache management
|       |   |       `-- viewmodel/
|       |   |           `-- GitPulseViewModel.kt     # Unidirectional UI state management
|       |   `-- res/                     # Strings, XML themes, vector drawables, and mipmaps
|       `-- test/                        # Unit tests for repositories and streak calculators
|-- gradle/
|   |-- libs.versions.toml               # Centralized Gradle version catalog
|   `-- wrapper/                         # Gradle wrapper binaries and properties
|-- build.gradle.kts                     # Root project build configuration
|-- settings.gradle.kts                  # Repository management and module declarations
`-- README.md                            # Project documentation
```

---

## Configuration and Authentication

GitPulse works out of the box with public GitHub profiles without requiring credentials. However, unauthenticated GitHub REST requests are limited by GitHub to 60 requests per hour per IP address.

To unlock high-rate limits (5,000 requests per hour) and enable GraphQL contribution calendar queries:

1. Generate a GitHub Personal Access Token (classic or fine-grained):
   - Navigate to GitHub -> Settings -> Developer settings -> Personal access tokens.
   - For fine-grained tokens, select read-only access to user profile and repository public data.
   - For classic tokens, select `read:user` and `repo:status`.
2. Open GitPulse on your device.
3. Tap the Settings icon in the top app bar or switch to the Preferences tab.
4. Enter your target GitHub username (default is `torvalds` or your own username).
5. Paste your Personal Access Token into the token field.
6. Tap "Save & Synchronize" to fetch verified live data immediately.

---

## Navigation and Gestures

- Tab Switching: Tap items in the bottom floating pill to switch between Home, Day Inspector, Analytics, and Settings.
- Pull-to-Refresh: Drag down on any top-level screen to trigger a live refresh with tactile haptic confirmation.
- Heatmap Exploration: On the Home dashboard, swipe horizontally on the Contribution graph to view all 52 weeks. Tap any square to highlight that day, display its commit metrics, and reveal an inspection button.
- System Back Gesture:
  - If a dialog or bottom sheet is open, Back dismisses it.
  - If viewing Settings, Inspector, or Analytics, Back navigates directly to the Home dashboard.
  - From the Home dashboard, Back exits the app gracefully.
