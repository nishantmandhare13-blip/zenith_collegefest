package com.zenith26.fest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.zenith26.fest.data.api.ApiClient
import com.zenith26.fest.ui.components.ConfigBaseUrlDialog
import com.zenith26.fest.ui.components.TopNavBar
import com.zenith26.fest.ui.navigation.Screen
import com.zenith26.fest.ui.screens.AdminDashboardScreen
import com.zenith26.fest.ui.screens.StudentPortalScreen
import com.zenith26.fest.ui.theme.Slate950
import com.zenith26.fest.ui.theme.ZenithTheme
import com.zenith26.fest.ui.viewmodel.AdminViewModel
import com.zenith26.fest.ui.viewmodel.StudentPortalViewModel

class MainActivity : ComponentActivity() {

    private val studentPortalViewModel: StudentPortalViewModel by viewModels()
    private val adminViewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZenithTheme {
                ZenithApp(
                    studentPortalViewModel = studentPortalViewModel,
                    adminViewModel = adminViewModel
                )
            }
        }
    }
}

@Composable
fun ZenithApp(
    studentPortalViewModel: StudentPortalViewModel,
    adminViewModel: AdminViewModel
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.StudentPortal) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            TopNavBar(
                isAdminScreen = currentScreen == Screen.AdminDashboard,
                onNavigatePortal = { currentScreen = Screen.StudentPortal },
                onNavigateAdmin = {
                    currentScreen = Screen.AdminDashboard
                    adminViewModel.loadDashboardData()
                },
                onOpenSettings = { showSettingsDialog = true }
            )

            // Current Screen Content
            Box(modifier = Modifier.weight(1f)) {
                when (currentScreen) {
                    Screen.StudentPortal -> {
                        StudentPortalScreen(viewModel = studentPortalViewModel)
                    }
                    Screen.AdminDashboard -> {
                        AdminDashboardScreen(viewModel = adminViewModel)
                    }
                }
            }
        }

        // Settings Dialog for API Base URL Configuration
        if (showSettingsDialog) {
            ConfigBaseUrlDialog(
                currentUrl = ApiClient.getBaseUrl(),
                onDismiss = { showSettingsDialog = false },
                onSaveUrl = { newUrl ->
                    ApiClient.updateBaseUrl(newUrl)
                    showSettingsDialog = false
                    // Reload both ViewModels with the new endpoint
                    studentPortalViewModel.fetchEvents()
                    adminViewModel.loadDashboardData()
                }
            )
        }
    }
}
