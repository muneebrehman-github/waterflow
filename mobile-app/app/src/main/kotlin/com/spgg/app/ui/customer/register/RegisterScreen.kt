package com.spgg.app.ui.customer.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spgg.app.domain.ShopDirectoryEntry
import com.spgg.app.ui.auth.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onBackToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    var selectedShop by remember { mutableStateOf<ShopDirectoryEntry?>(null) }
    var shopMenuExpanded by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Create your account", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))

            if (uiState.isLoadingShops) {
                CircularProgressIndicator()
            } else {
                Box(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedShop?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Choose your shop") },
                        trailingIcon = {
                            IconButton(onClick = { shopMenuExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Choose shop")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = shopMenuExpanded, onDismissRequest = { shopMenuExpanded = false }) {
                        uiState.shops.forEach { shop ->
                            DropdownMenuItem(
                                text = { Text(shop.name) },
                                onClick = {
                                    selectedShop = shop
                                    shopMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            (formError ?: uiState.error)?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(24.dp))
            FilledTonalButton(
                onClick = {
                    val shop = selectedShop
                    if (shop == null || fullName.isBlank() || phone.isBlank() || email.isBlank() || password.isBlank()) {
                        formError = "Fill in every field and choose a shop"
                        return@FilledTonalButton
                    }
                    formError = null
                    coroutineScope.launch {
                        val ok = viewModel.register(shop.id, fullName.trim(), phone.trim(), email.trim(), password)
                        if (ok) {
                            authViewModel.login(email.trim(), password)
                        }
                    }
                },
                enabled = !uiState.isRegistering,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isRegistering) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Sign Up")
                }
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onBackToLogin) {
                Text("Already have an account? Log in")
            }
        }
    }
}
