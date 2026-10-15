package com.zenith26.fest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith26.fest.ui.components.CategoryFilters
import com.zenith26.fest.ui.components.EmptyView
import com.zenith26.fest.ui.components.ErrorView
import com.zenith26.fest.ui.components.EventCard
import com.zenith26.fest.ui.components.HeroSection
import com.zenith26.fest.ui.components.LoadingView
import com.zenith26.fest.ui.components.RegistrationModal
import com.zenith26.fest.ui.components.SearchBar
import com.zenith26.fest.ui.theme.Purple300
import com.zenith26.fest.ui.theme.Slate100
import com.zenith26.fest.ui.theme.Slate400
import com.zenith26.fest.ui.theme.Slate700
import com.zenith26.fest.ui.theme.Slate800
import com.zenith26.fest.ui.theme.Slate900
import com.zenith26.fest.ui.theme.Slate950
import com.zenith26.fest.ui.viewmodel.StudentPortalViewModel

@Composable
fun StudentPortalScreen(
    viewModel: StudentPortalViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Section
            item {
                HeroSection()
            }

            // 2. Search & Category Filters Container
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Slate900.copy(alpha = 0.9f))
                        .border(1.dp, Slate800, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SearchBar(
                            query = uiState.searchQuery,
                            onQueryChanged = viewModel::onSearchQueryChanged
                        )

                        CategoryFilters(
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = viewModel::onCategorySelected
                        )
                    }
                }
            }

            // 3. Section Header: Title & Event Count Badge
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Fest Events",
                            color = Slate100,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Slate800)
                                .border(1.dp, Slate700, CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (uiState.isLoading) "Loading..." else "${uiState.events.size} event${if (uiState.events.size == 1) "" else "s"}",
                                color = Purple300,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text(
                        text = "Live availability",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }
            }

            // 4. Content States: Loading, Error, Empty, or Events List
            when {
                uiState.isLoading -> {
                    item {
                        LoadingView(message = "Fetching events from server...")
                    }
                }

                uiState.errorMessage != null -> {
                    item {
                        ErrorView(
                            errorMessage = uiState.errorMessage!!,
                            onRetry = viewModel::fetchEvents
                        )
                    }
                }

                uiState.events.isEmpty() -> {
                    item {
                        EmptyView(
                            title = "No events match your criteria",
                            description = "We couldn't find any events matching your selected category or search term.",
                            onResetFilters = viewModel::resetFilters
                        )
                    }
                }

                else -> {
                    items(uiState.events, key = { it.id }) { event ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            EventCard(
                                event = event,
                                onViewDetailsClicked = viewModel::openEventModal
                            )
                        }
                    }
                }
            }

            // Footer spacer
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // 5. Event Detail & Registration Modal
        uiState.activeEventForModal?.let { activeEvent ->
            RegistrationModal(
                event = activeEvent,
                isSubmitting = uiState.isSubmittingRegistration,
                successMessage = uiState.registrationSuccessMessage,
                serverErrorMessage = uiState.registrationServerError,
                onDismiss = viewModel::closeEventModal,
                onSubmit = viewModel::submitRegistration
            )
        }
    }
}
