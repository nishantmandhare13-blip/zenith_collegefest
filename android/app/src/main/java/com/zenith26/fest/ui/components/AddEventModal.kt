package com.zenith26.fest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zenith26.fest.data.model.EventCreateIn
import com.zenith26.fest.ui.theme.Purple500
import com.zenith26.fest.ui.theme.Purple600
import com.zenith26.fest.ui.theme.Rose400
import com.zenith26.fest.ui.theme.Rose500
import com.zenith26.fest.ui.theme.RoseBg
import com.zenith26.fest.ui.theme.RoseBorder
import com.zenith26.fest.ui.theme.Slate100
import com.zenith26.fest.ui.theme.Slate200
import com.zenith26.fest.ui.theme.Slate300
import com.zenith26.fest.ui.theme.Slate400
import com.zenith26.fest.ui.theme.Slate500
import com.zenith26.fest.ui.theme.Slate700
import com.zenith26.fest.ui.theme.Slate800
import com.zenith26.fest.ui.theme.Slate900
import com.zenith26.fest.ui.theme.Slate950

@Composable
fun AddEventModal(
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (EventCreateIn) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Technical") }
    var icon by remember { mutableStateOf("🎉") }
    var description by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("2026-11-14") }
    var eventTime by remember { mutableStateOf("10:00 AM") }
    var venue by remember { mutableStateOf("") }
    var capacityText by remember { mutableStateOf("50") }
    var feeText by remember { mutableStateOf("0") }

    var localError by remember { mutableStateOf<String?>(null) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Technical", "Cultural", "Sports", "Gaming", "Workshop")

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Slate900,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✨ Add New Fest Event",
                        color = Slate100,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Error Message Banner
                val displayError = localError ?: errorMessage
                if (displayError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(RoseBg)
                            .border(1.dp, RoseBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = displayError,
                            color = Rose400,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Title
                FieldLabel(label = "Title *")
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; localError = null },
                    placeholder = { Text("e.g. AI Hackathon", color = Slate500, fontSize = 12.sp) },
                    singleLine = true,
                    colors = customFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category & Icon Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FieldLabel(label = "Category *")
                        Box {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Slate950)
                                    .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                                    .clickable { categoryMenuExpanded = true }
                                    .padding(horizontal = 12.dp, vertical = 14.dp)
                            ) {
                                Text(text = category, color = Slate100, fontSize = 12.sp)
                            }
                            DropdownMenu(
                                expanded = categoryMenuExpanded,
                                onDismissRequest = { categoryMenuExpanded = false },
                                modifier = Modifier.background(Slate900)
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, color = Slate100, fontSize = 12.sp) },
                                        onClick = {
                                            category = cat
                                            categoryMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        FieldLabel(label = "Icon (Emoji) *")
                        OutlinedTextField(
                            value = icon,
                            onValueChange = { icon = it },
                            singleLine = true,
                            colors = customFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                FieldLabel(label = "Description")
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Brief description...", color = Slate500, fontSize = 12.sp) },
                    colors = customFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Date & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FieldLabel(label = "Date (YYYY-MM-DD) *")
                        OutlinedTextField(
                            value = eventDate,
                            onValueChange = { eventDate = it },
                            singleLine = true,
                            colors = customFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FieldLabel(label = "Time (e.g. 10:00 AM) *")
                        OutlinedTextField(
                            value = eventTime,
                            onValueChange = { eventTime = it },
                            singleLine = true,
                            colors = customFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Venue, Capacity, Fee
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FieldLabel(label = "Venue *")
                        OutlinedTextField(
                            value = venue,
                            onValueChange = { venue = it },
                            placeholder = { Text("Lab 1", color = Slate500, fontSize = 11.sp) },
                            singleLine = true,
                            colors = customFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Column(modifier = Modifier.weight(0.9f)) {
                        FieldLabel(label = "Capacity *")
                        OutlinedTextField(
                            value = capacityText,
                            onValueChange = { capacityText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = customFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Column(modifier = Modifier.weight(0.8f)) {
                        FieldLabel(label = "Fee (₹)")
                        OutlinedTextField(
                            value = feeText,
                            onValueChange = { feeText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = customFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = !isSubmitting) { onDismiss() }
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Text(text = "Cancel", color = Slate400, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSubmitting) Slate800 else Purple600)
                            .clickable(enabled = !isSubmitting) {
                                val cap = capacityText.toIntOrNull() ?: 0
                                val fee = feeText.toIntOrNull() ?: 0
                                if (title.trim().isEmpty() || venue.trim().isEmpty() || eventDate.trim().isEmpty() || eventTime.trim().isEmpty()) {
                                    localError = "Title, venue, date, and time are required."
                                } else if (cap <= 0) {
                                    localError = "Capacity must be greater than 0."
                                } else {
                                    onSubmit(
                                        EventCreateIn(
                                            title = title.trim(),
                                            category = category,
                                            icon = icon.trim().ifEmpty { "🎉" },
                                            description = description.trim().ifEmpty { null },
                                            eventDate = eventDate.trim(),
                                            eventTime = eventTime.trim(),
                                            venue = venue.trim(),
                                            capacity = cap,
                                            fee = fee
                                        )
                                    )
                                }
                            }
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = if (isSubmitting) "Creating..." else "Create Event",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(label: String) {
    Text(
        text = label,
        color = Slate300,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 3.dp)
    )
}

@Composable
private fun customFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Slate100,
    unfocusedTextColor = Slate100,
    focusedContainerColor = Slate950,
    unfocusedContainerColor = Slate950,
    focusedBorderColor = Purple500,
    unfocusedBorderColor = Slate700,
    errorBorderColor = Rose500,
    cursorColor = Purple500
)
