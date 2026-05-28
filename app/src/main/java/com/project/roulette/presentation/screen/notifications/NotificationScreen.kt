package com.project.roulette.presentation.screen.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.domain.model.Notification
import com.project.roulette.domain.model.NotificationType
import com.project.roulette.presentation.model.NotificationFilter
import com.project.roulette.presentation.model.NotificationUiState
import com.project.roulette.presentation.viewmodel.NotificationViewModel
import com.project.roulette.ui.theme.*
import com.project.roulette.util.TimeUtils
import com.project.roulette.presentation.screen.home.BannerAd
import com.project.roulette.presentation.screen.wheels.WheelsFilterChipItem
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentFilter by viewModel.filter.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            BannerAd(modifier = Modifier.fillMaxWidth())
        },
        containerColor = DeepNavyBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "UPDATES",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
//                TextButton(onClick = { viewModel.markAllAsRead() }) {
//                    Text("Mark all read", color = PrimaryPurple, fontWeight = FontWeight.Bold)
//                }
            }

            Spacer(Modifier.height(20.dp))

            // Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                WheelsFilterChipItem(
                    label = "All",
                    isSelected = currentFilter == NotificationFilter.ALL,
                    onClick = { viewModel.setFilter(NotificationFilter.ALL) }
                )
                WheelsFilterChipItem(
                    label = "Unread",
                    isSelected = currentFilter == NotificationFilter.UNREAD,
                    onClick = { viewModel.setFilter(NotificationFilter.UNREAD) }
                )
                WheelsFilterChipItem(
                    label = "Activity",
                    isSelected = currentFilter == NotificationFilter.ACTIVITY,
                    onClick = { viewModel.setFilter(NotificationFilter.ACTIVITY) }
                )
            }

            Spacer(Modifier.height(24.dp))

            when (val state = uiState) {
                is NotificationUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryPurple)
                    }
                }

                is NotificationUiState.Success -> {
                    if (state.notifications.isEmpty()) {
                        EmptyNotificationsState()
                    } else {
                        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                        val groups = state.notifications.groupBy { 
                            if (it.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date == today) "TODAY" else "EARLIER"
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            groups.forEach { (header, notifications) ->
                                item {
                                    Text(
                                        text = header,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(notifications) { notification ->
                                    NotificationItem(
                                        notification = notification,
                                        onClick = { viewModel.markAsRead(notification.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                is NotificationUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val (icon, color) = when (notification.type) {
        NotificationType.SPIN_RESULT -> Icons.Filled.RadioButtonChecked to TilePurple
        NotificationType.MILESTONE -> Icons.Filled.EmojiEvents to Color(0xFFFFA000) // Amber
        NotificationType.STREAK -> Icons.Filled.Whatshot to TileCoral
        NotificationType.REMINDER -> Icons.Filled.Notifications to Color.Gray
        NotificationType.TIP -> Icons.Filled.Lightbulb to Color.Yellow
        NotificationType.UPDATE -> Icons.Filled.Campaign to TileBlue
        NotificationType.ANNOUNCEMENT -> Icons.Filled.Info to TileTeal
        NotificationType.FAVOURITE -> Icons.Filled.Star to TilePink
    }

    val finalColor = if (notification.isRead) Color.Gray.copy(alpha = 0.5f) else color
    val backgroundColor = if (notification.isRead) SurfaceDarker else SurfaceDark
    val borderAlpha = if (notification.isRead) 0.1f else 0.5f
    val textColor = if (notification.isRead) TextSecondary else Color.White

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, finalColor.copy(alpha = borderAlpha))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(finalColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = finalColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = TimeUtils.getRelativeTime(notification.timestamp),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    color = if (notification.isRead) TextSecondary.copy(alpha = 0.7f) else TextSecondary,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmptyNotificationsState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.NotificationsNone,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("No notifications yet", color = TextSecondary)
        }
    }
}
