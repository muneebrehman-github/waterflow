package com.spgg.app.ui.shopowner.team

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spgg.app.domain.StaffSummary

private val TEAM_ROLES = listOf("STAFF", "DELIVERY_BOY")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(onBack: () -> Unit, viewModel: TeamViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Team") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add team member")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } }
    ) { padding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.members.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No staff or delivery boys yet")
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.members, key = { it.id }) { member -> TeamMemberCard(member) }
            }
        }
    }

    if (showAddDialog) {
        AddTeamMemberDialog(
            isSubmitting = uiState.isMutating,
            onDismiss = { showAddDialog = false },
            onConfirm = { fullName, phone, email, password, role ->
                viewModel.createMember(fullName, phone, email, password, role)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TeamMemberCard(member: StaffSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(member.fullName, style = MaterialTheme.typography.titleMedium)
                Text(member.role.replace('_', ' '), style = MaterialTheme.typography.labelMedium)
            }
            Text(member.phone, style = MaterialTheme.typography.bodySmall)
            Text(member.email, style = MaterialTheme.typography.bodySmall)
            Text(member.status, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTeamMemberDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (fullName: String, phone: String, email: String, password: String, role: String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(TEAM_ROLES.first()) }
    var roleExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Team Member") },
        text = {
            Column {
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Box {
                    OutlinedTextField(
                        value = role.replace('_', ' '),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = {
                            IconButton(onClick = { roleExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Choose role")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                        TEAM_ROLES.forEach { option ->
                            DropdownMenuItem(text = { Text(option.replace('_', ' ')) }, onClick = { role = option; roleExpanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(fullName, phone, email, password, role) },
                enabled = !isSubmitting && fullName.isNotBlank() && phone.isNotBlank() && email.isNotBlank() && password.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
