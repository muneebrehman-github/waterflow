package com.spgg.app.ui.customer.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spgg.app.ui.components.EmptyState
import com.spgg.app.ui.components.InitialsAvatar
import com.spgg.app.ui.components.SpggScaffold
import com.spgg.app.ui.components.SpggSurfaceCard
import com.spgg.app.ui.components.StatusChip

@Composable
fun CustomerProfileScreen(onBack: () -> Unit, viewModel: CustomerProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SpggScaffold(title = "My Profile", onBack = onBack) { padding ->
        val profile = uiState.profile
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            profile == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(title = "Couldn't load profile", message = uiState.error ?: "Please try again shortly.")
            }
            else -> Column(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                InitialsAvatar(name = profile.fullName, size = 88)
                Spacer(Modifier.height(12.dp))
                Text(profile.fullName, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(6.dp))
                StatusChip(profile.status)
                Spacer(Modifier.height(20.dp))
                SpggSurfaceCard {
                    ProfileRow(Icons.Default.Phone, "Phone", profile.phone)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ProfileRow(Icons.Default.Email, "Email", profile.email)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ProfileRow(Icons.Default.Storefront, "Shop", profile.shopName)
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, value: String) {
    Row(Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
