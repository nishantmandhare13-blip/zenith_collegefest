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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.zenith26.fest.data.model.EventOut
import com.zenith26.fest.data.model.RegistrationIn
import com.zenith26.fest.ui.theme.Amber400
import com.zenith26.fest.ui.theme.AmberBg
import com.zenith26.fest.ui.theme.AmberBorder
import com.zenith26.fest.ui.theme.Emerald400
import com.zenith26.fest.ui.theme.EmeraldBg
import com.zenith26.fest.ui.theme.EmeraldBorder
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
import com.zenith26.fest.ui.theme.getCategoryStyle
import kotlinx.coroutines.delay

@Composable
fun RegistrationModal(
    event: EventOut,
    isSubmitting: Boolean,
    successMessage: String?,
    serverErrorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (eventId: Int, reg: RegistrationIn) -> Unit
) {
    var studentName by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var rollError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }

    val isFull = event.availableSeats <= 0
    val catStyle = getCategoryStyle(event.category)
    val feeText = if (event.fee > 0) "₹${event.fee}" else "Free"

    // If registration succeeds, auto-dismiss modal after 2.5 seconds
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            delay(2500)
            onDismiss()
        }
    }

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
            ) {
                // Header Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate900)
                        .border(width = 0.5.dp, color = Slate800)
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = event.icon ?: "🎉", fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(catStyle.background)
                                        .border(1.dp, catStyle.borderColor, CircleShape)
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = event.category.uppercase(),
                                        color = catStyle.textColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

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

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = event.title,
                            color = Slate100,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = event.description ?: "No description provided.",
                            color = Slate300,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Details Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Date & Time Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Slate950)
                                    .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(text = "🗓️ Date & Time", color = Slate500, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "${event.eventDate}\n${event.eventTime}", color = Slate100, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Venue Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Slate950)
                                    .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(text = "📍 Venue", color = Slate500, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = event.venue, color = Slate100, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Seats & Fee Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Slate950)
                                    .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(text = "🎟️ Seats / Fee", color = Slate500, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isFull) "0 left · $feeText" else "${event.availableSeats} left · $feeText",
                                        color = if (isFull) Rose400 else Emerald400,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Body & Form Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Full Event Warning Banner
                    if (isFull) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AmberBg)
                                .border(1.dp, AmberBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🚫", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Registrations Closed: This event is at maximum capacity.",
                                    color = Amber400,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Success Banner
                    if (successMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(EmeraldBg)
                                .border(1.dp, EmeraldBorder, RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "✅", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Registration Confirmed!",
                                        color = Slate100,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = successMessage,
                                    color = Emerald400,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Server Error Banner
                    if (serverErrorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(RoseBg)
                                .border(1.dp, RoseBorder, RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "⚠️", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "REGISTRATION ERROR",
                                        color = Rose400,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = serverErrorMessage,
                                    color = Slate200,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Form Fields
                    // 1. Student Name
                    Text(
                        text = "Student Full Name *",
                        color = Slate300,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = studentName,
                        onValueChange = {
                            studentName = it
                            if (nameError) nameError = false
                        },
                        placeholder = { Text(text = "e.g. Nishant Mandhare", color = Slate500, fontSize = 12.sp) },
                        isError = nameError,
                        enabled = !isFull && !isSubmitting,
                        singleLine = true,
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (nameError) {
                        Text(
                            text = "Please enter your name.",
                            color = Rose400,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Roll Number & Phone in a Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Roll Number
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Roll Number *",
                                color = Slate300,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = rollNumber,
                                onValueChange = {
                                    rollNumber = it
                                    if (rollError) rollError = false
                                },
                                placeholder = { Text(text = "24PM-082", color = Slate500, fontSize = 12.sp) },
                                isError = rollError,
                                enabled = !isFull && !isSubmitting,
                                singleLine = true,
                                colors = outlinedFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (rollError) {
                                Text(
                                    text = "Enter roll number.",
                                    color = Rose400,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        // Phone (Optional)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Phone (Optional)",
                                color = Slate300,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                placeholder = { Text(text = "+91 9876543210", color = Slate500, fontSize = 12.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                enabled = !isFull && !isSubmitting,
                                singleLine = true,
                                colors = outlinedFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Email Address
                    Text(
                        text = "Email Address *",
                        color = Slate300,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (emailError) emailError = false
                        },
                        placeholder = { Text(text = "student@college.edu", color = Slate500, fontSize = 12.sp) },
                        isError = emailError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        enabled = !isFull && !isSubmitting,
                        singleLine = true,
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (emailError) {
                        Text(
                            text = "Please enter a valid email address with '@'.",
                            color = Rose400,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
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
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = !isSubmitting) { onDismiss() }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                color = Slate400,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isFull || isSubmitting) Slate800 else Purple600
                                )
                                .clickable(enabled = !isFull && !isSubmitting) {
                                    // Client validation checks
                                    var hasError = false
                                    if (studentName.trim().isEmpty()) {
                                        nameError = true
                                        hasError = true
                                    }
                                    if (rollNumber.trim().isEmpty()) {
                                        rollError = true
                                        hasError = true
                                    }
                                    val trimmedEmail = email.trim()
                                    if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@") || trimmedEmail.startsWith("@") || trimmedEmail.endsWith("@")) {
                                        emailError = true
                                        hasError = true
                                    }

                                    if (!hasError) {
                                        onSubmit(
                                            event.id,
                                            RegistrationIn(
                                                studentName = studentName.trim(),
                                                rollNumber = rollNumber.trim(),
                                                email = trimmedEmail,
                                                phone = phone.trim().ifEmpty { null }
                                            )
                                        )
                                    }
                                }
                                .padding(horizontal = 18.dp, vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Registering...",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "Confirm Registration",
                                        color = if (isFull) Slate500 else Color.White,
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
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Slate100,
    unfocusedTextColor = Slate100,
    disabledTextColor = Slate500,
    focusedContainerColor = Slate950,
    unfocusedContainerColor = Slate950,
    disabledContainerColor = Slate950,
    focusedBorderColor = Purple500,
    unfocusedBorderColor = Slate700,
    disabledBorderColor = Slate800,
    errorBorderColor = Rose500,
    cursorColor = Purple500
)
