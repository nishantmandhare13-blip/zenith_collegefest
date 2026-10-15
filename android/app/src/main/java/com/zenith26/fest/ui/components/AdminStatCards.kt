package com.zenith26.fest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith26.fest.data.model.StatsOut
import com.zenith26.fest.ui.theme.Emerald400
import com.zenith26.fest.ui.theme.Indigo600
import com.zenith26.fest.ui.theme.Purple400
import com.zenith26.fest.ui.theme.Purple600
import com.zenith26.fest.ui.theme.Slate100
import com.zenith26.fest.ui.theme.Slate400
import com.zenith26.fest.ui.theme.Slate500
import com.zenith26.fest.ui.theme.Slate800
import com.zenith26.fest.ui.theme.Slate900
import com.zenith26.fest.ui.theme.Slate950

@Composable
fun AdminStatCards(
    stats: StatsOut,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Total Events & Total Registrations
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Total Events
            StatCard(
                title = "TOTAL EVENTS",
                icon = "🎪",
                value = stats.totalEvents.toString(),
                subtext = "Across 5 distinct categories",
                valueColor = Slate100,
                modifier = Modifier.weight(1f)
            )

            // Card 2: Total Registrations
            StatCard(
                title = "TOTAL REGISTRATIONS",
                icon = "🎟️",
                value = stats.totalRegistrations.toString(),
                subtext = "Out of ${stats.totalCapacity} total capacity",
                valueColor = Slate100,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Utilization % & Upcoming Events
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 3: Overall Utilization
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate900.copy(alpha = 0.95f))
                    .border(1.dp, Slate800, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UTILIZATION",
                            color = Slate400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(text = "📈", fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${stats.overallUtilizationPct}%",
                        color = Purple400,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(Slate950)
                            .border(0.5.dp, Slate800, CircleShape)
                    ) {
                        val progressFraction = (stats.overallUtilizationPct / 100f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Purple600, Indigo600)
                                    )
                                )
                        )
                    }
                }
            }

            // Card 4: Upcoming Events
            StatCard(
                title = "UPCOMING EVENTS",
                icon = "📅",
                value = stats.upcomingEventsCount.toString(),
                subtext = "Scheduled Nov 14–15",
                valueColor = Emerald400,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    icon: String,
    value: String,
    subtext: String,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Slate900.copy(alpha = 0.95f))
            .border(1.dp, Slate800, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Slate400,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(text = icon, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                color = valueColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtext,
                color = Slate500,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}
