package com.zenith26.fest.ui.navigation

sealed class Screen(val route: String) {
    data object StudentPortal : Screen("student_portal")
    data object AdminDashboard : Screen("admin_dashboard")
}
