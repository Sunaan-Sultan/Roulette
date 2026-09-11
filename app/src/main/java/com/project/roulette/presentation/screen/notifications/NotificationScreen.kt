package com.project.roulette.presentation.screen.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
import com.project.roulette.presentation.screen.wheels.WheelsFilterChipItem
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.project.roulette.presentation.component.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentFilter by viewModel.filter.collectAsStateWithLifecycle()

    NotificationContent(
        uiState = uiState,
        currentFilter = currentFilter,
        onFilterSelected = viewModel::setFilter,
        onMarkAllRead = viewModel::markAllAsRead,
        onNotificationClick = viewModel::markAsRead,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationContent(
    uiState: NotificationUiState,
    currentFilter: NotificationFilter,
    onFilterSelected: (NotificationFilter) -> Unit,
    onMarkAllRead: () -> Unit,
    onNotificationClick: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = RouletteTheme.colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = "UPDATES",
                style = MaterialTheme.typography.labelLarge,
                color = RouletteTheme.colors.textSecondary,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = RouletteTheme.colors.textPrimary
                )

                TextButton(
                    onClick = onMarkAllRead,
                    contentPadding = PaddingValues(start = 6.dp, top = 8.dp)
                ) {
                    Text(
                        text = "Mark all read",
                        color = RouletteTheme.colors.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
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
                    onClick = { onFilterSelected(NotificationFilter.ALL) }
                )
                WheelsFilterChipItem(
                    label = "Unread",
                    isSelected = currentFilter == NotificationFilter.UNREAD,
                    onClick = { onFilterSelected(NotificationFilter.UNREAD) }
                )
                WheelsFilterChipItem(
                    label = "Activity",
                    isSelected = currentFilter == NotificationFilter.ACTIVITY,
                    onClick = { onFilterSelected(NotificationFilter.ACTIVITY) }
                )
            }

            Spacer(Modifier.height(24.dp))

            when (val state = uiState) {
                is NotificationUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = RouletteTheme.colors.primary)
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
                                        color = RouletteTheme.colors.textSecondary,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(notifications) { notification ->
                                    NotificationItem(
                                        notification = notification,
                                        onClick = { onNotificationClick(notification.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                is NotificationUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = RouletteTheme.colors.danger)
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
        NotificationType.SPIN_RESULT -> AppIcons.RadioButtonChecked to TilePurple
        NotificationType.MILESTONE -> AppIcons.Trophy to RouletteTheme.colors.warning // Amber
        NotificationType.STREAK -> AppIcons.Whatshot to TileCoral
        NotificationType.REMINDER -> AppIcons.Notifications to RouletteTheme.colors.textSecondary
        NotificationType.TIP -> AppIcons.Lightbulb to RouletteTheme.colors.warning
        NotificationType.UPDATE -> AppIcons.Campaign to TileBlue
        NotificationType.ANNOUNCEMENT -> AppIcons.Info to TileTeal
        NotificationType.FAVOURITE -> AppIcons.Star to TilePink
    }

    val finalColor = if (notification.isRead) RouletteTheme.colors.textSecondary.copy(alpha = 0.5f) else color
    val backgroundColor = if (notification.isRead) RouletteTheme.colors.surfaceElevated else RouletteTheme.colors.surface
    val borderAlpha = if (notification.isRead) 0.1f else 0.5f
    val borderColor = if (notification.isRead) finalColor else RouletteTheme.colors.primary
    val textColor = if (notification.isRead) RouletteTheme.colors.textSecondary else RouletteTheme.colors.textPrimary

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RouletteTheme.shapes.card,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor.copy(alpha = borderAlpha))
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
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = TimeUtils.getRelativeTime(notification.timestamp),
                        color = RouletteTheme.colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    color = if (notification.isRead) RouletteTheme.colors.textSecondary.copy(alpha = 0.7f) else RouletteTheme.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
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
                AppIcons.Notifications,
                contentDescription = null,
                tint = RouletteTheme.colors.textSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("No notifications yet", color = RouletteTheme.colors.textSecondary)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050B18)
@Composable
fun NotificationScreenPreview() {
    RouletteTheme {
        NotificationContent(
            uiState = NotificationUiState.Success(
                notifications = listOf(
                    Notification(
                        id = "1",
                        title = "Spin Result",
                        message = "Your wheel Luck landed on Red! 🎯",
                        type = NotificationType.SPIN_RESULT,
                        timestamp = Clock.System.now(),
                        isRead = false
                    ),
                    Notification(
                        id = "2",
                        title = "Milestone 🎉",
                        message = "You've made 50 spins total! Keep the momentum going!",
                        type = NotificationType.MILESTONE,
                        timestamp = Clock.System.now(),
                        isRead = true
                    )
                )
            ),
            currentFilter = NotificationFilter.ALL,
            onFilterSelected = {},
            onMarkAllRead = {},
            onNotificationClick = {},
            onNavigateBack = {}
        )
    }
}
