package com.zenith26.fest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenith26.fest.data.model.EventOut
import com.zenith26.fest.data.model.RegistrationIn
import com.zenith26.fest.data.repository.FestRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentPortalUiState(
    val events: List<EventOut> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val activeEventForModal: EventOut? = null,
    val isSubmittingRegistration: Boolean = false,
    val registrationSuccessMessage: String? = null,
    val registrationServerError: String? = null
)

class StudentPortalViewModel(
    private val repository: FestRepository = FestRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentPortalUiState())
    val uiState: StateFlow<StudentPortalUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        fetchEvents()
    }

    /**
     * Fetch events from the server with current search & category query params.
     * Exercises the real backend API: GET /api/events?category=...&search=...
     */
    fun fetchEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = repository.getEvents(
                category = _uiState.value.selectedCategory,
                search = _uiState.value.searchQuery
            )

            result.fold(
                onSuccess = { eventsList ->
                    _uiState.update {
                        it.copy(
                            events = eventsList,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load events."
                        )
                    }
                }
            )
        }
    }

    fun onCategorySelected(category: String) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.update { it.copy(selectedCategory = category) }
        fetchEvents()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350) // 350ms debounce
            fetchEvents()
        }
    }

    fun resetFilters() {
        _uiState.update { it.copy(selectedCategory = "All", searchQuery = "") }
        fetchEvents()
    }

    fun openEventModal(event: EventOut) {
        _uiState.update {
            it.copy(
                activeEventForModal = event,
                registrationSuccessMessage = null,
                registrationServerError = null
            )
        }
    }

    fun closeEventModal() {
        _uiState.update {
            it.copy(
                activeEventForModal = null,
                registrationSuccessMessage = null,
                registrationServerError = null,
                isSubmittingRegistration = false
            )
        }
    }

    fun submitRegistration(eventId: Int, reg: RegistrationIn) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmittingRegistration = true,
                    registrationServerError = null,
                    registrationSuccessMessage = null
                )
            }

            val result = repository.registerForEvent(eventId, reg)

            result.fold(
                onSuccess = { regOut ->
                    val eventTitle = _uiState.value.activeEventForModal?.title ?: "the event"
                    _uiState.update {
                        it.copy(
                            isSubmittingRegistration = false,
                            registrationSuccessMessage = "Success! ${reg.studentName} (${reg.rollNumber}) is registered for $eventTitle. Registration ID: #${regOut.id}."
                        )
                    }
                    // Refresh events immediately so available seats count updates
                    fetchEvents()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSubmittingRegistration = false,
                            registrationServerError = error.message ?: "Registration failed."
                        )
                    }
                }
            )
        }
    }
}
