package com.worksync.app.presentation.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.worksync.app.domain.model.enums.UserRole
import com.worksync.app.presentation.ui.common.components.CustomButton
import com.worksync.app.presentation.ui.common.components.CustomTextField
import com.worksync.app.presentation.viewmodel.AuthViewModel
import com.worksync.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.EMPLOYEE) }

    val uiState by viewModel.uiState.collectAsState()

    // Navigate on successful registration
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onRegisterSuccess()
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
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Create Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Join WorkSync to manage your tasks efficiently",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Name Field
        CustomTextField(
            value = name,
            onValueChange = { name = it },
            label = "Full Name",
            leadingIcon = Icons.Default.Person,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password Field
        CustomTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm Password",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Role Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Your Role",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Employee Role
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedRole == UserRole.EMPLOYEE,
                            onClick = { selectedRole = UserRole.EMPLOYEE },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedRole == UserRole.EMPLOYEE,
                        onClick = { selectedRole = UserRole.EMPLOYEE }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Employee",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Can view and update assigned tasks",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Admin Role
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedRole == UserRole.ADMIN,
                            onClick = { selectedRole = UserRole.ADMIN },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedRole == UserRole.ADMIN,
                        onClick = { selectedRole = UserRole.ADMIN }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Admin",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Can create, assign, and manage all tasks",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Register Button
        CustomButton(
            text = "Create Account",
            onClick = {
                viewModel.clearMessages()
                viewModel.register(email, password, confirmPassword, name, selectedRole)
            },
            isLoading = uiState.isLoading,
            enabled = name.isNotBlank() && email.isNotBlank() &&
                    password.isNotBlank() && confirmPassword.isNotBlank()
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

        Spacer(modifier = Modifier.height(24.dp))

        // Login Navigation
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = "Sign In",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
