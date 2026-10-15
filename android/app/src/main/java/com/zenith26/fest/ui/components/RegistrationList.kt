package com.zenith26.fest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith26.fest.data.model.EventOut
import com.zenith26.fest.data.model.RegistrationDetail
import com.zenith26.fest.ui.theme.Purple300
import com.zenith26.fest.ui.theme.Purple400
import com.zenith26.fest.ui.theme.Rose400
import com.zenith26.fest.ui.theme.RoseBg
import com.zenith26.fest.ui.theme.RoseBorder
import com.zenith26.fest.ui.theme.Slate100
import com.zenith26.fest.ui.theme.Slate300
import com.zenith26.fest.ui.theme.Slate400
import com.zenith26.fest.ui.theme.Slate500
import com.zenith26.fest.ui.theme.Slate700
import com.zenith26.fest.ui.theme.Slate800
import com.zenith26.fest.ui.theme.Slate900
import com.zenith26.fest.ui.theme.Slate950

@Composable
fun RegistrationList(
    registrations: List<RegistrationDetail>,
    allEvents: List<EventOut>,
    selectedEventIdFilter: Int?,
    onFilterChanged: (Int?) -> Unit,
    onCancelRegistrationClicked: (RegistrationDetail) -> Unit,
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    val filterLabel = if (selectedEventIdFilter == null) {
        "All Events"
    } else {
        val found = allEvents.find { it.id == selectedEventIdFilter }
        if (found != null) "[#${found.id}] ${found.title}" else "Event #${selectedEventIdFilter}"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dropdown Event Filter Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filter by Event:",
                color = Slate300,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate950)
                        .border(1.dp, Slate700, RoundedCornerShape(10.dp))
                        .clickable { dropdownExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = filterLabel,
                            color = Slate100,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Expand",
                            tint = Slate400
                        )
                    }
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.background(Slate900)
                ) {
                    DropdownMenuItem(
                        text = { Text("All Events", color = Slate100, fontSize = 12.sp) },
                        onClick = {
                            dropdownExpanded = false
                            onFilterChanged(null)
                        }
                    )
                    allEvents.forEach { ev ->
                        DropdownMenuItem(
                            text = { Text("[#${ev.id}] ${ev.title}", color = Slate100, fontSize = 12.sp) },
                            onClick = {
                                dropdownExpanded = false
                                onFilterChanged(ev.id)
                            }
                        )
                    }
                }
            }
        }

        // List of registrations
        if (registrations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate900)
                    .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No registrations found for the selected criteria.",
                    color = Slate400,
                    fontSize = 13.sp
                )
            }
        } else {
            registrations.forEach { reg ->
                RegistrationItemCard(
                    reg = reg,
                    onCancelClicked = { onCancelRegistrationClicked(reg) }
                )
            }
        }
    }
}

@Composable
private fun RegistrationItemCard(
    reg: RegistrationDetail,
    onCancelClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Slate900.copy(alpha = 0.95f))
            .border(1.dp, Slate800, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Row 1: Reg ID, Student Name, Cancel Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${reg.id}",
                        color = Purple400,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = reg.studentName,
                        color = Slate100,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Cancel Button (Stretch Goal)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RoseBg.copy(alpha = 0.4f))
                        .border(1.dp, RoseBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .clickable { onCancelClicked() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Cancel",
                        color = Rose400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Roll Number & Event Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Roll: ${reg.rollNumber}",
                    color = Slate400,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = reg.eventTitle,
                        color = Purple300,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(#${reg.eventId})",
                        color = Slate500,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 3: Email & Phone, Registered Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = reg.email,
                        color = Slate300,
                        fontSize = 11.sp
                    )
                    if (!reg.phone.isNullOrBlank()) {
                        Text(
                            text = reg.phone,
                            color = Slate500,
                            fontSize = 10.sp
                        )
                    }
                }

                Text(
                    text = reg.registeredAt,
                    color = Slate500,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
