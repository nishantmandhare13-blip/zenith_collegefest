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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith26.fest.data.model.EventOut
import com.zenith26.fest.ui.theme.Amber400
import com.zenith26.fest.ui.theme.Amber500
import com.zenith26.fest.ui.theme.AmberBg
import com.zenith26.fest.ui.theme.AmberBorder
import com.zenith26.fest.ui.theme.Emerald400
import com.zenith26.fest.ui.theme.Purple500
import com.zenith26.fest.ui.theme.Rose400
import com.zenith26.fest.ui.theme.Rose500
import com.zenith26.fest.ui.theme.RoseBg
import com.zenith26.fest.ui.theme.RoseBorder
import com.zenith26.fest.ui.theme.Slate100
import com.zenith26.fest.ui.theme.Slate300
import com.zenith26.fest.ui.theme.Slate400
import com.zenith26.fest.ui.theme.Slate500
import com.zenith26.fest.ui.theme.Slate800
import com.zenith26.fest.ui.theme.Slate900
import com.zenith26.fest.ui.theme.Slate950
import com.zenith26.fest.ui.theme.getCategoryStyle

data class EnrichedEvent(
    val event: EventOut,
    val registeredCount: Int,
    val utilizationPct: Int
)

@Composable
fun AdminEventsTable(
    events: List<EventOut>,
    modifier: Modifier = Modifier
) {
    // Enrich with utilization % and sort descending by default
    val sortedEnriched = events.map { ev ->
        val regCount = (ev.capacity - ev.availableSeats).coerceAtLeast(0)
        val utilPct = if (ev.capacity > 0) ((regCount.toFloat() / ev.capacity.toFloat()) * 100).toInt() else 0
        EnrichedEvent(ev, regCount, utilPct)
    }.sortedByDescending { it.utilizationPct }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (sortedEnriched.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate900)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No events found in database.",
                    color = Slate400,
                    fontSize = 13.sp
                )
            }
        } else {
            sortedEnriched.forEach { item ->
                AdminEventItemCard(enriched = item)
            }
        }
    }
}

@Composable
private fun AdminEventItemCard(
    enriched: EnrichedEvent
) {
    val ev = enriched.event
    val isFull = ev.availableSeats <= 0
    val isOver90 = enriched.utilizationPct >= 90
    val catStyle = getCategoryStyle(ev.category)

    val borderLeftColor = when {
        isFull -> Rose500
        isOver90 -> Amber500
        else -> Slate800
    }

    val cardBg = when {
        isFull -> RoseBg.copy(alpha = 0.25f)
        isOver90 -> AmberBg.copy(alpha = 0.25f)
        else -> Slate900.copy(alpha = 0.95f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, Slate800, RoundedCornerShape(14.dp))
            .padding(start = 4.dp) // highlight bar space
    ) {
        // Colored left accent bar
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(4.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(borderLeftColor)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Icon, Title, ID, Category, Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = ev.icon ?: "🎉", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = ev.title,
                            color = Slate100,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "ID: #${ev.id} · ${if (ev.fee > 0) "Fee: ₹${ev.fee}" else "Free"}",
                            color = Slate500,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Badge
                if (isFull) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(RoseBg)
                            .border(1.dp, RoseBorder, CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "FULL (100%)",
                            color = Rose400,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else if (isOver90) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AmberBg)
                            .border(1.dp, AmberBorder, CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Filling Fast",
                            color = Amber400,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0x2610B981))
                            .border(1.dp, Color(0x4D10B981), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${ev.availableSeats} Open",
                            color = Emerald400,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category & Date & Venue
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(catStyle.background)
                        .border(1.dp, catStyle.borderColor, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = ev.category.uppercase(),
                        color = catStyle.textColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${ev.eventDate} · ${ev.eventTime} · ${ev.venue}",
                    color = Slate400,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Metrics: Registered / Capacity & Utilization %
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${enriched.registeredCount}",
                        color = Slate100,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = " / ${ev.capacity} reg",
                        color = Slate500,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${enriched.utilizationPct}%",
                        color = if (isOver90) Amber400 else Slate300,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (isOver90) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "⚠️", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Mini progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Slate950)
                    .border(0.5.dp, Slate800, CircleShape)
            ) {
                val fraction = (enriched.utilizationPct / 100f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(if (isOver90) Amber500 else Purple500)
                )
            }
        }
    }
}
