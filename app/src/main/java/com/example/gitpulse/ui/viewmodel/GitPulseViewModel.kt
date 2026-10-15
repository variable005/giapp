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

class GitPulseViewModel(
    private val apiService: GitHubApiService = GitHubApiService()
) : ViewModel() {

    private val _uiState = MutableStateFlow<GitPulseUiState>(GitPulseUiState.Loading)
    val uiState: StateFlow<GitPulseUiState> = _uiState.asStateFlow()

    private val _currentUsername = MutableStateFlow("variable005")
    val currentUsername: StateFlow<String> = _currentUsername.asStateFlow()

    private val _personalToken = MutableStateFlow<String?>(null)
    val personalToken: StateFlow<String?> = _personalToken.asStateFlow()

    var includePrivateContributions = MutableStateFlow(true)
    var maskSensitiveRepositories = MutableStateFlow(false)
    var dailyPushWarningEnabled = MutableStateFlow(true)
    var alertCutoffTime = MutableStateFlow("08:30 PM")

    init {
        loadUserData(_currentUsername.value, _personalToken.value)
    }

    fun loadUserData(username: String, token: String? = null) {
        _currentUsername.value = username
        _personalToken.value = token

        viewModelScope.launch {
            _uiState.value = GitPulseUiState.Loading
            val result = apiService.fetchCompleteUserData(username, token)
            result.onSuccess { data ->
                _uiState.value = GitPulseUiState.Success(data)
            }.onFailure { error ->
                _uiState.value = GitPulseUiState.Error(
                    error.message ?: "Failed to load GitHub data for $username. Please check username or connectivity."
                )
            }
        }
    }

    fun refresh() {
        loadUserData(_currentUsername.value, _personalToken.value)
    }

    fun switchUser(username: String) {
        loadUserData(username, _personalToken.value)
    }

    fun addAccount(username: String, token: String?) {
        loadUserData(username, token)
    }
}
