package com.spgg.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Formatting + building blocks for the customer experience.

private val moneyFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
    minimumFractionDigits = 0
    maximumFractionDigits = 2
}

/** "Rs 1,250" / "Rs 99.5" — amounts are PKR as stored by the backend. */
fun formatRs(amount: Double): String =
    (if (amount < 0) "-Rs " else "Rs ") + moneyFormat.format(kotlin.math.abs(amount))

private val friendlyDate = DateTimeFormatter.ofPattern("d MMM, h:mm a", Locale.US)

/** Best-effort pretty date for backend timestamps; falls back to the raw text. */
fun formatTimestamp(raw: String): String {
    val zone = ZoneId.systemDefault()
    val parsed = runCatching { OffsetDateTime.parse(raw).atZoneSameInstant(zone).toLocalDateTime() }
        .recoverCatching { Instant.parse(raw).atZone(zone).toLocalDateTime() }
        .recoverCatching { LocalDateTime.parse(raw) }
        .getOrNull()
    return parsed?.format(friendlyDate) ?: raw
}

fun statusLabel(status: String): String =
    status.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

/** Orders in one of these states are still on their way to the customer. */
val ActiveOrderStatuses = setOf("PENDING", "ACCEPTED", "PREPARING", "OUT_FOR_DELIVERY")

private val trackerSteps = listOf(
    "PENDING" to "Placed",
    "ACCEPTED" to "Accepted",
    "PREPARING" to "Preparing",
    "OUT_FOR_DELIVERY" to "On the way",
    "DELIVERED" to "Delivered"
)

/** Horizontal progress tracker for the happy-path order lifecycle. */
@Composable
fun OrderStatusTracker(status: String, modifier: Modifier = Modifier) {
    val current = trackerSteps.indexOfFirst { it.first == status.uppercase() }
    if (current < 0) {
        // Rejected / cancelled orders don't sit on the delivery timeline.
        StatusChip(status = status, modifier = modifier)
        return
    }
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.outlineVariant
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            trackerSteps.forEachIndexed { index, _ ->
                val done = index <= current
                val dotColor by animateColorAsState(if (done) primary else track, label = "trackerDot")
                Box(
                    modifier = Modifier
                        .size(if (index == current) 22.dp else 18.dp)
                        .clip(CircleShape)
                        .background(dotColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (done) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
                if (index < trackerSteps.lastIndex) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .padding(horizontal = 2.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (index < current) primary else track)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            trackerSteps.forEachIndexed { index, (_, label) ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index <= current) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (index == current) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun IconBadge(icon: ImageVector, tint: Color, background: Color, modifier: Modifier = Modifier, size: Int = 44) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size * 0.32f).dp))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size((size * 0.52f).dp))
    }
}

/** Compact KPI tile: icon, big value, caption. */
@Composable
fun StatTile(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    soft: Color,
    modifier: Modifier = Modifier,
    caption: String? = null,
    onClick: (() -> Unit)? = null
) {
    val body: @Composable () -> Unit = {
        Column(Modifier.padding(16.dp)) {
            IconBadge(icon = icon, tint = accent, background = soft, size = 38)
            Spacer(Modifier.height(12.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (caption != null) {
                Text(
                    caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, border = border) { body() }
    } else {
        Surface(modifier = modifier, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, border = border) { body() }
    }
}

/** Tappable settings-style row used in the account tab. */
@Composable
fun ActionRow(
    title: String,
    icon: ImageVector,
    accent: Color,
    soft: Color,
    onClick: () -> Unit,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(onClick = onClick, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon = icon, tint = accent, background = soft, size = 40)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = titleColor)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AmountRow(label: String, amount: Double, emphasize: Boolean = false, modifier: Modifier = Modifier) {
    val style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Text(
            label,
            style = style,
            color = if (emphasize) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(formatRs(amount), style = style, fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium)
    }
}

/** Initials avatar for people. */
@Composable
fun InitialsAvatar(name: String, modifier: Modifier = Modifier, size: Int = 44) {
    val initials = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        .take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initials,
            style = if (size >= 64) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
