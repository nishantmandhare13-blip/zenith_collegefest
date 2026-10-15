package com.zenith26.fest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenith26.fest.data.model.EventCreateIn
import com.zenith26.fest.data.model.EventOut
import com.zenith26.fest.data.model.RegistrationDetail
import com.zenith26.fest.data.model.StatsOut
import com.zenith26.fest.data.repository.FestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AdminTab {
    EVENTS_DEMAND,
    REGISTRATIONS_LOG
}

data class AdminUiState(
    val stats: StatsOut? = null,
    val events: List<EventOut> = emptyList(),
    val registrations: List<RegistrationDetail> = emptyList(),
    val selectedTab: AdminTab = AdminTab.EVENTS_DEMAND,
    val selectedEventFilterId: Int? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val activeDeleteRegistration: RegistrationDetail? = null,
    val isDeletingRegistration: Boolean = false,
    val showAddEventModal: Boolean = false,
    val isCreatingEvent: Boolean = false,
    val addEventError: String? = null
)

class AdminViewModel(
    private val repository: FestRepository = FestRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    /**
     * Loads live stats, events list, and registrations from API.
     */
    fun loadDashboardData(isPullRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isPullRefresh) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            // 1. Fetch live fest stats (GET /api/stats)
            val statsResult = repository.getStats()
            // 2. Fetch live events (GET /api/events)
            val eventsResult = repository.getEvents(category = null, search = null)
            // 3. Fetch registrations (GET /api/registrations?event_id=...)
            val regResult = repository.getRegistrations(_uiState.value.selectedEventFilterId)

            if (statsResult.isSuccess && eventsResult.isSuccess) {
                _uiState.update {
                    it.copy(
                        stats = statsResult.getOrNull(),
                        events = eventsResult.getOrDefault(emptyList()),
                        registrations = regResult.getOrDefault(emptyList()),
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
            } else {
                val err = statsResult.exceptionOrNull() ?: eventsResult.exceptionOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = err?.message ?: "Could not load dashboard metrics from backend API."
                    )
                }
            }
        }
    }

    fun selectTab(tab: AdminTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == AdminTab.REGISTRATIONS_LOG) {
            loadRegistrations()
        }
    }

    fun setEventFilter(eventId: Int?) {
        _uiState.update { it.copy(selectedEventFilterId = eventId) }
        loadRegistrations()
    }

    fun loadRegistrations() {
        viewModelScope.launch {
            val result = repository.getRegistrations(_uiState.value.selectedEventFilterId)
            result.onSuccess { list ->
                _uiState.update { it.copy(registrations = list) }
            }
        }
    }

    fun promptDeleteRegistration(reg: RegistrationDetail) {
        _uiState.update { it.copy(activeDeleteRegistration = reg) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(activeDeleteRegistration = null, isDeletingRegistration = false) }
    }

    fun confirmDeleteRegistration(regId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingRegistration = true) }
            val result = repository.deleteRegistration(regId)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDeletingRegistration = false,
                            activeDeleteRegistration = null
                        )
                    }
                    // Refresh dashboard data so metrics & seats reflect the cancellation live
                    loadDashboardData()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isDeletingRegistration = false,
                            errorMessage = error.message ?: "Failed to cancel registration."
                        )
                    }
                }
            )
        }
    }

    fun openAddEventModal() {
        _uiState.update { it.copy(showAddEventModal = true, addEventError = null) }
    }

    fun dismissAddEventModal() {
        _uiState.update { it.copy(showAddEventModal = false, addEventError = null, isCreatingEvent = false) }
    }

    fun submitCreateEvent(eventCreateIn: EventCreateIn) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingEvent = true, addEventError = null) }
            val result = repository.createEvent(eventCreateIn)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isCreatingEvent = false,
                            showAddEventModal = false
                        )
                    }
                    loadDashboardData()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isCreatingEvent = false,
                            addEventError = error.message ?: "Failed to create event."
                        )
                    }
                }
            )
        }
    }
}
