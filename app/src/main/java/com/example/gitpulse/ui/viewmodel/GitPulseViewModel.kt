package com.example.gitpulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gitpulse.data.*
import com.example.gitpulse.data.remote.GitHubApiService
import com.example.gitpulse.data.remote.GitPulseAggregateData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface GitPulseUiState {
    object Loading : GitPulseUiState
    data class Success(val data: GitPulseAggregateData) : GitPulseUiState
    data class Error(val message: String) : GitPulseUiState
}

enum class AppThemeMode(val title: String) {
    SYSTEM("System"),
    DARK("Dark"),
    LIGHT("Light")
}

data class SavedAccount(
    val username: String,
    val token: String? = null,
    val isPrimary: Boolean = false
)

class GitPulseViewModel(
    private val apiService: GitHubApiService = GitHubApiService()
) : ViewModel() {

    private val _uiState = MutableStateFlow<GitPulseUiState>(GitPulseUiState.Loading)
    val uiState: StateFlow<GitPulseUiState> = _uiState.asStateFlow()

    /** Increments after every successful data load; the companion fox
     *  celebrates fresh pulls (skipping the very first one). */
    private val _syncCount = MutableStateFlow(0)
    val syncCount: StateFlow<Int> = _syncCount.asStateFlow()

    private val _currentUsername = MutableStateFlow("variable005")
    val currentUsername: StateFlow<String> = _currentUsername.asStateFlow()

    private val _personalToken = MutableStateFlow<String?>(null)
    val personalToken: StateFlow<String?> = _personalToken.asStateFlow()

    private val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val availableYears: List<Int> = listOf(currentYear, currentYear - 1, currentYear - 2)

    private val _selectedYear = MutableStateFlow(currentYear)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    // Appearance & Theming Settings
    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _dynamicColor = MutableStateFlow(false)
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    private val _amoledPureBlack = MutableStateFlow(false)
    val amoledPureBlack: StateFlow<Boolean> = _amoledPureBlack.asStateFlow()

    private val _highRefreshAnimations = MutableStateFlow(true)
    val highRefreshAnimations: StateFlow<Boolean> = _highRefreshAnimations.asStateFlow()

    // Goals & Streaks Settings
    private val _dailyCommitGoal = MutableStateFlow(3)
    val dailyCommitGoal: StateFlow<Int> = _dailyCommitGoal.asStateFlow()

    private val _targetDaysPerWeek = MutableStateFlow(7)
    val targetDaysPerWeek: StateFlow<Int> = _targetDaysPerWeek.asStateFlow()

    private val _allowWeekendFreeze = MutableStateFlow(true)
    val allowWeekendFreeze: StateFlow<Boolean> = _allowWeekendFreeze.asStateFlow()

    private val _dailyPushWarningEnabled = MutableStateFlow(true)
    val dailyPushWarningEnabled: StateFlow<Boolean> = _dailyPushWarningEnabled.asStateFlow()

    private val _alertCutoffTime = MutableStateFlow("08:30 PM")
    val alertCutoffTime: StateFlow<String> = _alertCutoffTime.asStateFlow()

    // Privacy & Data Settings
    private val _includePrivateContributions = MutableStateFlow(true)
    val includePrivateContributions: StateFlow<Boolean> = _includePrivateContributions.asStateFlow()

    private val _maskSensitiveRepositories = MutableStateFlow(false)
    val maskSensitiveRepositories: StateFlow<Boolean> = _maskSensitiveRepositories.asStateFlow()

    private val _anonymizeCommitMessages = MutableStateFlow(false)
    val anonymizeCommitMessages: StateFlow<Boolean> = _anonymizeCommitMessages.asStateFlow()

    // Cache & Network Settings
    private val _cacheRetentionHours = MutableStateFlow(6)
    val cacheRetentionHours: StateFlow<Int> = _cacheRetentionHours.asStateFlow()

    // Saved Accounts
    private val _savedAccounts = MutableStateFlow(
        listOf(
            SavedAccount(username = "variable005", isPrimary = true),
            SavedAccount(username = "torvalds"),
            SavedAccount(username = "octocat")
        )
    )
    val savedAccounts: StateFlow<List<SavedAccount>> = _savedAccounts.asStateFlow()

    init {
        loadUserData(_currentUsername.value, _personalToken.value, _selectedYear.value)
    }

    fun loadUserData(username: String, token: String? = null, year: Int = _selectedYear.value) {
        _currentUsername.value = username
        _personalToken.value = token
        _selectedYear.value = year

        viewModelScope.launch {
            _uiState.value = GitPulseUiState.Loading
            val result = apiService.fetchCompleteUserData(username, token, year)
            result.onSuccess { data ->
                _uiState.value = GitPulseUiState.Success(data)
                _syncCount.value += 1
            }.onFailure { error ->
                _uiState.value = GitPulseUiState.Error(
                    error.message ?: "Failed to load GitHub data for $username. Please check username or connectivity."
                )
            }
        }
    }

    fun refresh() {
        loadUserData(_currentUsername.value, _personalToken.value, _selectedYear.value)
    }

    fun selectYear(year: Int) {
        if (year != _selectedYear.value) {
            loadUserData(_currentUsername.value, _personalToken.value, year)
        }
    }

    fun switchUser(username: String) {
        val savedToken = _savedAccounts.value
            .find { it.username.equals(username, ignoreCase = true) }?.token
            ?: _personalToken.value
        loadUserData(username, savedToken, _selectedYear.value)
    }

    fun addAccount(username: String, token: String?) {
        val trimmed = username.trim()
        if (trimmed.isNotBlank()) {
            val updated = _savedAccounts.value.toMutableList()
            val existingIndex = updated.indexOfFirst { it.username.equals(trimmed, ignoreCase = true) }
            if (existingIndex == -1) {
                updated.add(SavedAccount(username = trimmed, token = token))
            } else {
                updated[existingIndex] = updated[existingIndex].copy(token = token)
            }
            _savedAccounts.value = updated
            loadUserData(trimmed, token, _selectedYear.value)
        }
    }

    fun removeSavedAccount(username: String) {
        val updated = _savedAccounts.value.filterNot { it.username.equals(username, ignoreCase = true) }
        _savedAccounts.value = updated
    }

    // Setters for settings configuration
    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }

    fun setDynamicColor(enabled: Boolean) {
        _dynamicColor.value = enabled
    }

    fun setAmoledPureBlack(enabled: Boolean) {
        _amoledPureBlack.value = enabled
    }

    fun setHighRefreshAnimations(enabled: Boolean) {
        _highRefreshAnimations.value = enabled
    }

    fun setDailyCommitGoal(goal: Int) {
        _dailyCommitGoal.value = goal
    }

    fun setTargetDaysPerWeek(days: Int) {
        _targetDaysPerWeek.value = days
    }

    fun setAllowWeekendFreeze(enabled: Boolean) {
        _allowWeekendFreeze.value = enabled
    }

    fun setDailyPushWarningEnabled(enabled: Boolean) {
        _dailyPushWarningEnabled.value = enabled
    }

    fun setAlertCutoffTime(time: String) {
        _alertCutoffTime.value = time
    }

    fun setIncludePrivateContributions(enabled: Boolean) {
        _includePrivateContributions.value = enabled
    }

    fun setMaskSensitiveRepositories(enabled: Boolean) {
        _maskSensitiveRepositories.value = enabled
    }

    fun setAnonymizeCommitMessages(enabled: Boolean) {
        _anonymizeCommitMessages.value = enabled
    }

    fun setCacheRetentionHours(hours: Int) {
        _cacheRetentionHours.value = hours
    }

    fun purgeCacheAndReload() {
        refresh()
    }

    fun resetAllSettingsToDefaults() {
        _themeMode.value = AppThemeMode.SYSTEM
        _dynamicColor.value = false
        _amoledPureBlack.value = false
        _highRefreshAnimations.value = true
        _dailyCommitGoal.value = 3
        _targetDaysPerWeek.value = 7
        _allowWeekendFreeze.value = true
        _dailyPushWarningEnabled.value = true
        _alertCutoffTime.value = "08:30 PM"
        _includePrivateContributions.value = true
        _maskSensitiveRepositories.value = false
        _anonymizeCommitMessages.value = false
        _cacheRetentionHours.value = 6
        loadUserData("variable005", null, currentYear)
    }

    private val _readmeCache = mutableMapOf<String, String>()

    suspend fun getRepoReadme(owner: String, repo: String): String {
        val key = "$owner/$repo"
        _readmeCache[key]?.let { return it }
        val content = apiService.fetchRepoReadme(owner, repo, _personalToken.value)
        _readmeCache[key] = content
        return content
    }

    fun getProductReadme(context: android.content.Context): String {
        return try {
            context.assets.open("README.md").bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            """
# GitPulse

GitPulse is a native Android application built with Jetpack Compose and Material Design 3 for tracking GitHub streaks, developer velocity, repository portfolios, and annual contribution heatmaps.

> WARNING: ACTIVE DEVELOPMENT
> This repository is currently under heavy active development. Architectural patterns, screen layouts, and API integrations are subject to rapid change without backward compatibility guarantees.

## Overview
GitPulse provides an expressive, fluid dashboard for software developers who want deep visibility into their open-source cadence and commit velocity.
""".trimIndent()
        }
    }
}
