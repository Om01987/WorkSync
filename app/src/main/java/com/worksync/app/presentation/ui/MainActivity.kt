package com.worksync.app.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.worksync.app.domain.model.enums.UserRole
import com.worksync.app.presentation.navigation.NavigationRoutes
import com.worksync.app.presentation.ui.admin.AdminDashboardScreen
import com.worksync.app.presentation.ui.admin.CreateTaskScreen
import com.worksync.app.presentation.ui.admin.TaskDetailsScreen
import com.worksync.app.presentation.ui.auth.LoginScreen
import com.worksync.app.presentation.ui.auth.RegisterScreen
import com.worksync.app.presentation.ui.employee.EmployeeDashboardScreen
import com.worksync.app.presentation.ui.splash.SplashScreen
import com.worksync.app.presentation.viewmodel.AuthViewModel
import com.worksync.app.ui.theme.WorkSyncTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkSyncTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        WorkSyncApp()
                    }
                }
            }
        }
    }
}

@Composable
fun WorkSyncApp(authViewModel: AuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.SPLASH
    ) {
        // Splash Screen - determines where to navigate based on auth state
        composable(NavigationRoutes.SPLASH) {
            SplashScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // Login Screen
        composable(NavigationRoutes.LOGIN) {
            val uiState by authViewModel.uiState.collectAsState()
            LoginScreen(
                onNavigateToRegister = { navController.navigate(NavigationRoutes.REGISTER) },
                onLoginSuccess = {
                    val destination = when (uiState.currentUser?.role) {
                        UserRole.ADMIN -> NavigationRoutes.ADMIN_DASHBOARD
                        UserRole.EMPLOYEE -> NavigationRoutes.EMPLOYEE_DASHBOARD
                        else -> NavigationRoutes.LOGIN
                    }
                    navController.navigate(destination) {
                        popUpTo(NavigationRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Register Screen
        composable(NavigationRoutes.REGISTER) {
            val uiState by authViewModel.uiState.collectAsState()
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(NavigationRoutes.LOGIN) {
                        popUpTo(NavigationRoutes.REGISTER) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    val destination = when (uiState.currentUser?.role) {
                        UserRole.ADMIN -> NavigationRoutes.ADMIN_DASHBOARD
                        UserRole.EMPLOYEE -> NavigationRoutes.EMPLOYEE_DASHBOARD
                        else -> NavigationRoutes.LOGIN
                    }
                    navController.navigate(destination) {
                        popUpTo(NavigationRoutes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        // Admin Dashboard
        composable(NavigationRoutes.ADMIN_DASHBOARD) {
            val uiState by authViewModel.uiState.collectAsState()
            uiState.currentUser?.let { user ->
                AdminDashboardScreen(
                    currentUser = user,
                    authViewModel = authViewModel,
                    taskViewModel = hiltViewModel(),
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(NavigationRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onCreateTask = {
                        navController.navigate(NavigationRoutes.CREATE_TASK)
                    },
                    onTaskClick = { task ->
                        navController.navigate("${NavigationRoutes.TASK_DETAILS}/${task.id}")
                    }
                )
            }
        }

        // Employee Dashboard
        composable(NavigationRoutes.EMPLOYEE_DASHBOARD) {
            val uiState by authViewModel.uiState.collectAsState()
            uiState.currentUser?.let { user ->
                EmployeeDashboardScreen(
                    currentUser = user,
                    taskViewModel = hiltViewModel(),
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(NavigationRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onTaskClick = { task ->
                        navController.navigate("${NavigationRoutes.TASK_DETAILS}/${task.id}")
                    }
                )
            }
        }

        // Create Task Screen (Admin only)
        composable(NavigationRoutes.CREATE_TASK) {
            val uiState by authViewModel.uiState.collectAsState()
            uiState.currentUser?.let { user ->
                CreateTaskScreen(
                    currentUser = user,
                    taskViewModel = hiltViewModel(),
                    userViewModel = hiltViewModel(),
                    onNavigateBack = { navController.popBackStack() },
                    onTaskCreated = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // Task Details Screen
        composable(
            route = "${NavigationRoutes.TASK_DETAILS}/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            val uiState by authViewModel.uiState.collectAsState()
            uiState.currentUser?.let { user ->
                TaskDetailsScreen(
                    taskId = taskId,
                    currentUser = user,
                    taskViewModel = hiltViewModel(),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
