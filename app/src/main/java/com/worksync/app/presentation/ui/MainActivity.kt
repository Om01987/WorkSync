package com.worksync.app.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel // <-- fixed import
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.worksync.app.domain.model.enums.UserRole
import com.worksync.app.presentation.navigation.NavigationRoutes
import com.worksync.app.presentation.ui.auth.LoginScreen
import com.worksync.app.presentation.ui.auth.RegisterScreen
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
    val uiState by authViewModel.uiState.collectAsState()

    val startDestination = if (uiState.isLoggedIn) {
        when (uiState.currentUser?.role) {
            UserRole.ADMIN -> NavigationRoutes.ADMIN_DASHBOARD
            UserRole.EMPLOYEE -> NavigationRoutes.EMPLOYEE_DASHBOARD
            else -> NavigationRoutes.LOGIN
        }
    } else {
        NavigationRoutes.LOGIN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavigationRoutes.LOGIN) {
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

        composable(NavigationRoutes.REGISTER) {
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

        composable(NavigationRoutes.ADMIN_DASHBOARD) {
            AdminDashboardPlaceholder(
                user = uiState.currentUser,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(NavigationRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(NavigationRoutes.EMPLOYEE_DASHBOARD) {
            EmployeeDashboardPlaceholder(
                user = uiState.currentUser,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(NavigationRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun AdminDashboardPlaceholder(
    user: com.worksync.app.domain.model.User?,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Admin Dashboard",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Welcome, ${user?.name ?: "Admin"}!",
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "✅ Phase 1: App Setup Complete",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "✅ Phase 2: Database Setup Complete",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "✅ Phase 3: Authentication Complete",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Button(
                onClick = onLogout,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("Logout")
            }
        }
    }
}

@Composable
fun EmployeeDashboardPlaceholder(
    user: com.worksync.app.domain.model.User?,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Employee Dashboard",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Welcome, ${user?.name ?: "Employee"}!",
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "✅ Phase 1: App Setup Complete",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "✅ Phase 2: Database Setup Complete",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "✅ Phase 3: Authentication Complete",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Button(
                onClick = onLogout,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("Logout")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkSyncAppPreview() {
    WorkSyncTheme { }
}
