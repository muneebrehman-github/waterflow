package com.spgg.app.ui.shopowner.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spgg.app.domain.DayHours
import com.spgg.app.domain.DayOfWeek

private data class EditableDayHours(
    val day: DayOfWeek,
    val openTime: String,
    val closeTime: String,
    val closed: Boolean
)

@Composable
fun HoursEditorDialog(
    title: String,
    initialHours: List<DayHours>,
    onDismiss: () -> Unit,
    onSave: (List<DayHours>) -> Unit
) {
    val rows = remember {
        mutableStateListOf(*DayOfWeek.WEEK_ORDER.map { day ->
            val existing = initialHours.find { it.day == day }
            EditableDayHours(
                day = day,
                openTime = existing?.openTime ?: "08:00",
                closeTime = existing?.closeTime ?: "20:00",
                closed = existing?.closed ?: false
            )
        }.toTypedArray())
    }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                rows.forEachIndexed { index, row ->
                    DayHoursRow(row = row, onChanged = { updated -> rows[index] = updated })
                    if (index != rows.lastIndex) HorizontalDivider(Modifier.padding(vertical = 4.dp))
                }
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val timePattern = Regex("""^\d{2}:\d{2}$""")
                val invalid = rows.any {
                    !it.closed && (!timePattern.matches(it.openTime) || !timePattern.matches(it.closeTime))
                }
                if (invalid) {
                    error = "Times must be in HH:mm format (e.g. 08:00)"
                    return@TextButton
                }
                onSave(rows.map {
                    DayHours(
                        day = it.day,
                        openTime = if (it.closed) null else it.openTime,
                        closeTime = if (it.closed) null else it.closeTime,
                        closed = it.closed
                    )
                })
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun DayHoursRow(row: EditableDayHours, onChanged: (EditableDayHours) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(row.day.label, modifier = Modifier.weight(1f))
            Text(if (row.closed) "Closed" else "Open", modifier = Modifier.padding(end = 8.dp))
            Switch(
                checked = !row.closed,
                onCheckedChange = { isOpen -> onChanged(row.copy(closed = !isOpen)) }
            )
        }
        if (!row.closed) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = row.openTime,
                    onValueChange = { onChanged(row.copy(openTime = it)) },
                    label = { Text("Open") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = row.closeTime,
                    onValueChange = { onChanged(row.copy(closeTime = it)) },
                    label = { Text("Close") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
