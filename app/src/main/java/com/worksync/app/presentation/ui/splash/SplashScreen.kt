package com.worksync.app.presentation.ui.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.worksync.app.domain.model.enums.UserRole
import com.worksync.app.presentation.navigation.NavigationRoutes
import com.worksync.app.presentation.viewmodel.AuthViewModel

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedIn, uiState.currentUser?.role) {
        val destination = if (uiState.isLoggedIn) {
            when (uiState.currentUser?.role) {
                UserRole.ADMIN -> NavigationRoutes.ADMIN_DASHBOARD
                UserRole.EMPLOYEE -> NavigationRoutes.EMPLOYEE_DASHBOARD
                else -> NavigationRoutes.LOGIN
            }
        } else {
            NavigationRoutes.LOGIN
        }
        navController.navigate(destination) {
            popUpTo(0) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
