package com.zenith26.fest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith26.fest.ui.components.AddEventModal
import com.zenith26.fest.ui.components.AdminEventsTable
import com.zenith26.fest.ui.components.AdminStatCards
import com.zenith26.fest.ui.components.CategoryDistribution
import com.zenith26.fest.ui.components.DeleteConfirmModal
import com.zenith26.fest.ui.components.ErrorView
import com.zenith26.fest.ui.components.LoadingView
import com.zenith26.fest.ui.components.RegistrationList
import com.zenith26.fest.ui.theme.Indigo600
import com.zenith26.fest.ui.theme.Purple300
import com.zenith26.fest.ui.theme.Purple400
import com.zenith26.fest.ui.theme.Purple600
import com.zenith26.fest.ui.theme.Slate100
import com.zenith26.fest.ui.theme.Slate200
import com.zenith26.fest.ui.theme.Slate300
import com.zenith26.fest.ui.theme.Slate400
import com.zenith26.fest.ui.theme.Slate700
import com.zenith26.fest.ui.theme.Slate800
import com.zenith26.fest.ui.theme.Slate900
import com.zenith26.fest.ui.theme.Slate950
import com.zenith26.fest.ui.viewmodel.AdminTab
import com.zenith26.fest.ui.viewmodel.AdminViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
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
            // 1. Dashboard Sub-header: Live Refresh & Add Event Action Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Live Refresh Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Slate900)
                            .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                            .clickable(enabled = !uiState.isRefreshing) {
                                viewModel.loadDashboardData(isPullRefresh = true)
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.isRefreshing) {
                                CircularProgressIndicator(
                                    color = Purple400,
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = Purple400,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Live Refresh",
                                color = Slate200,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Add Event Button (Stretch Goal 1)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(listOf(Purple600, Indigo600))
                            )
                            .clickable { viewModel.openAddEventModal() }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "+ Add Event",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 2. Loading State
            if (uiState.isLoading) {
                item {
                    LoadingView(message = "Synchronizing Fest Metrics...")
                }
            }

            // 3. Error State
            if (uiState.errorMessage != null && !uiState.isLoading) {
                item {
                    ErrorView(
                        title = "Dashboard Synchronization Error",
                        errorMessage = uiState.errorMessage!!,
                        onRetry = { viewModel.loadDashboardData() }
                    )
                }
            }

            // 4. Content Area
            if (!uiState.isLoading && uiState.stats != null) {
                // Live Stat Cards (Top 4)
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        AdminStatCards(stats = uiState.stats!!)
                    }
                }

                // Category Distribution Breakdown
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        CategoryDistribution(eventsByCategory = uiState.stats!!.eventsByCategory)
                    }
                }

                // Tabs: Events Demand Table vs Student Registrations Log
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Tab 1
                            val isTabEvents = uiState.selectedTab == AdminTab.EVENTS_DEMAND
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isTabEvents) Purple600 else Slate900)
                                    .border(
                                        1.dp,
                                        if (isTabEvents) Purple600 else Slate800,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.selectTab(AdminTab.EVENTS_DEMAND) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🎪 Events Demand",
                                    color = if (isTabEvents) Color.White else Slate300,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Tab 2
                            val isTabRegs = uiState.selectedTab == AdminTab.REGISTRATIONS_LOG
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isTabRegs) Purple600 else Slate900)
                                    .border(
                                        1.dp,
                                        if (isTabRegs) Purple600 else Slate800,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.selectTab(AdminTab.REGISTRATIONS_LOG) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "📝 Student Registrations",
                                    color = if (isTabRegs) Color.White else Slate300,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Subtitle Hint
                        if (uiState.selectedTab == AdminTab.EVENTS_DEMAND) {
                            Text(
                                text = "Sorted by Utilization % descending (high demand first)",
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Tab Content Rendering
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        when (uiState.selectedTab) {
                            AdminTab.EVENTS_DEMAND -> {
                                AdminEventsTable(events = uiState.events)
                            }
                            AdminTab.REGISTRATIONS_LOG -> {
                                RegistrationList(
                                    registrations = uiState.registrations,
                                    allEvents = uiState.events,
                                    selectedEventIdFilter = uiState.selectedEventFilterId,
                                    onFilterChanged = viewModel::setEventFilter,
                                    onCancelRegistrationClicked = viewModel::promptDeleteRegistration
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Modals
        if (uiState.showAddEventModal) {
            AddEventModal(
                isSubmitting = uiState.isCreatingEvent,
                errorMessage = uiState.addEventError,
                onDismiss = viewModel::dismissAddEventModal,
                onSubmit = viewModel::submitCreateEvent
            )
        }

        uiState.activeDeleteRegistration?.let { regToDelete ->
            DeleteConfirmModal(
                registration = regToDelete,
                isDeleting = uiState.isDeletingRegistration,
                onDismiss = viewModel::dismissDeleteDialog,
                onConfirm = viewModel::confirmDeleteRegistration
            )
        }
    }
}
