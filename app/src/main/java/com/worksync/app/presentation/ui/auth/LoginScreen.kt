package com.worksync.app.presentation.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.worksync.app.presentation.ui.common.components.CustomButton
import com.worksync.app.presentation.ui.common.components.CustomTextField
import com.worksync.app.presentation.viewmodel.AuthViewModel
import com.worksync.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    // Navigate on successful login
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    // Show success/error messages
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // You can show a snackbar here if needed
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Logo and Title
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "WorkSync Logo",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "WorkSync",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Welcome back! Please sign in to continue.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Email Field
        CustomTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email Address",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        CustomTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Forgot Password
        TextButton(
            onClick = { /* TODO: Implement forgot password */ },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                text = "Forgot Password?",
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        CustomButton(
            text = "Sign In",
            onClick = {
                viewModel.clearMessages()
                viewModel.login(email, password)
            },
            isLoading = uiState.isLoading,
            enabled = email.isNotBlank() && password.isNotBlank()
        )

        // Error Message
        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Register Navigation
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account? ",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = "Sign Up",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
