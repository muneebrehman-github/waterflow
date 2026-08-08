package com.spgg.app.ui.shopowner.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.spgg.app.domain.DayHours
import com.spgg.app.domain.DayOfWeek

private enum class ActiveEditor { NONE, BASIC_INFO, BUSINESS_HOURS, DELIVERY_HOURS, DELIVERY_SETTINGS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopProfileScreen(onBack: () -> Unit, viewModel: ShopProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var activeEditor by remember { mutableStateOf(ActiveEditor.NONE) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shop Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } }
    ) { padding ->
        val profile = uiState.profile
        when {
            uiState.isLoading && profile == null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            profile == null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Couldn't load shop profile")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        ProfileSectionCard(
                            title = "Basic info",
                            onEdit = { activeEditor = ActiveEditor.BASIC_INFO }
                        ) {
                            Text(profile.name, style = MaterialTheme.typography.titleMedium)
                            profile.description?.let { Text(it) }
                            profile.address?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                    item {
                        ProfileSectionCard(
                            title = "Business hours",
                            onEdit = { activeEditor = ActiveEditor.BUSINESS_HOURS }
                        ) {
                            HoursSummary(profile.businessHours)
                        }
                    }
                    item {
                        ProfileSectionCard(
                            title = "Delivery hours",
                            onEdit = { activeEditor = ActiveEditor.DELIVERY_HOURS }
                        ) {
                            HoursSummary(profile.deliveryHours)
                        }
                    }
                    item {
                        ProfileSectionCard(
                            title = "Delivery settings",
                            onEdit = { activeEditor = ActiveEditor.DELIVERY_SETTINGS }
                        ) {
                            Text("Radius: ${profile.deliveryRadiusKm ?: "not set"} km")
                            Text("Minimum order: ${profile.minOrderValue ?: "not set"}")
                            if (profile.deliveryChargeTiers.isEmpty()) {
                                Text("No delivery charge tiers configured yet")
                            } else {
                                profile.deliveryChargeTiers.forEach { tier ->
                                    val range = if (tier.maxOrderValue == null) {
                                        "${tier.minOrderValue}+"
                                    } else {
                                        "${tier.minOrderValue}–${tier.maxOrderValue}"
                                    }
                                    Text("$range: ${tier.charge}")
                                }
                            }
                        }
                    }
                }

                when (activeEditor) {
                    ActiveEditor.BASIC_INFO -> BasicInfoEditorDialog(
                        profile = profile,
                        onDismiss = { activeEditor = ActiveEditor.NONE },
                        onSave = { name, description, address ->
                            viewModel.updateBasicInfo(name, description, address)
                            activeEditor = ActiveEditor.NONE
                        }
                    )
                    ActiveEditor.BUSINESS_HOURS -> HoursEditorDialog(
                        title = "Business hours",
                        initialHours = profile.businessHours,
                        onDismiss = { activeEditor = ActiveEditor.NONE },
                        onSave = { hours ->
                            viewModel.updateBusinessHours(hours)
                            activeEditor = ActiveEditor.NONE
                        }
                    )
                    ActiveEditor.DELIVERY_HOURS -> HoursEditorDialog(
                        title = "Delivery hours",
                        initialHours = profile.deliveryHours,
                        onDismiss = { activeEditor = ActiveEditor.NONE },
                        onSave = { hours ->
                            viewModel.updateDeliveryHours(hours)
                            activeEditor = ActiveEditor.NONE
                        }
                    )
                    ActiveEditor.DELIVERY_SETTINGS -> DeliverySettingsEditorDialog(
                        initialRadiusKm = profile.deliveryRadiusKm,
                        initialMinOrderValue = profile.minOrderValue,
                        initialTiers = profile.deliveryChargeTiers,
                        onDismiss = { activeEditor = ActiveEditor.NONE },
                        onSave = { radius, minOrder, tiers ->
                            viewModel.updateDeliverySettings(radius, minOrder, tiers)
                            activeEditor = ActiveEditor.NONE
                        }
                    )
                    ActiveEditor.NONE -> {}
                }
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(title: String, onEdit: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit $title")
                }
            }
            content()
        }
    }
}

@Composable
private fun HoursSummary(hours: List<DayHours>) {
    if (hours.isEmpty()) {
        Text("Not configured yet")
        return
    }
    val byDay = hours.associateBy { it.day }
    DayOfWeek.WEEK_ORDER.forEach { day ->
        val entry = byDay[day]
        val text = when {
            entry == null -> "${day.label}: not set"
            entry.closed -> "${day.label}: Closed"
            else -> "${day.label}: ${entry.openTime} – ${entry.closeTime}"
        }
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}
