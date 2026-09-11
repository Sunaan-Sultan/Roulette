package com.project.roulette.presentation.screen.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.roulette.domain.model.Notification
import com.project.roulette.domain.model.NotificationType
import com.project.roulette.presentation.component.AppIcons
import com.project.roulette.presentation.component.design.AppFilterChip
import com.project.roulette.presentation.component.design.AppLargeHeader
import com.project.roulette.presentation.component.design.AppScaffold
import com.project.roulette.presentation.component.design.EmptyState
import com.project.roulette.presentation.component.design.SectionHeader
import com.project.roulette.presentation.model.NotificationFilter
import com.project.roulette.presentation.model.NotificationUiState
import com.project.roulette.presentation.viewmodel.NotificationViewModel
import com.project.roulette.ui.theme.RouletteTheme
import com.project.roulette.ui.theme.TileBlue
import com.project.roulette.ui.theme.TileCoral
import com.project.roulette.ui.theme.TilePink
import com.project.roulette.ui.theme.TilePurple
import com.project.roulette.ui.theme.TileTeal
import com.project.roulette.ui.theme.rememberAccentOnSurface
import com.project.roulette.util.TimeUtils
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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

@Composable
fun NotificationContent(
    uiState: NotificationUiState,
    currentFilter: NotificationFilter,
    onFilterSelected: (NotificationFilter) -> Unit,
    onMarkAllRead: () -> Unit,
    onNotificationClick: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    AppScaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AppLargeHeader(
                eyebrow = "Updates",
                title = "Notifications",
                trailing = {
                    TextButton(onClick = onMarkAllRead) {
                        Text(
                            text = "Mark all read",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.primary
                        )
                    }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.screenPadding),
                horizontalArrangement = Arrangement.spacedBy(dimens.space8)
            ) {
                AppFilterChip(
                    label = "All",
                    selected = currentFilter == NotificationFilter.ALL,
                    onClick = { onFilterSelected(NotificationFilter.ALL) }
                )
                AppFilterChip(
                    label = "Unread",
                    selected = currentFilter == NotificationFilter.UNREAD,
                    onClick = { onFilterSelected(NotificationFilter.UNREAD) }
                )
                AppFilterChip(
                    label = "Activity",
                    selected = currentFilter == NotificationFilter.ACTIVITY,
                    onClick = { onFilterSelected(NotificationFilter.ACTIVITY) }
                )
            }

            Spacer(Modifier.size(dimens.space16))

            Box(modifier = Modifier.weight(1f)) {
                when (uiState) {
                    is NotificationUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = colors.primary)
                        }
                    }

                    is NotificationUiState.Success -> {
                        if (uiState.notifications.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                EmptyState(
                                    icon = AppIcons.Notifications,
                                    title = "Nothing here yet",
                                    message = "Spin results, milestones and tips will land here."
                                )
                            }
                        } else {
                            val today = Clock.System.now()
                                .toLocalDateTime(TimeZone.currentSystemDefault()).date
                            val groups = uiState.notifications.groupBy {
                                val date = it.timestamp
                                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
                                if (date == today) "Today" else "Earlier"
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(dimens.space8),
                                contentPadding = PaddingValues(
                                    start = dimens.screenPadding,
                                    end = dimens.screenPadding,
                                    bottom = dimens.listBottomPadding
                                )
                            ) {
                                groups.forEach { (header, notifications) ->
                                    item(key = "header-$header") {
                                        Spacer(Modifier.size(dimens.space8))
                                        SectionHeader(header)
                                    }
                                    items(notifications, key = { it.id }) { notification ->
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
                            Text(
                                text = uiState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.danger
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val colors = RouletteTheme.colors
    val dimens = RouletteTheme.dimens

    val (icon, baseColor) = when (notification.type) {
        NotificationType.SPIN_RESULT -> AppIcons.RadioButtonChecked to TilePurple
        NotificationType.MILESTONE -> AppIcons.Trophy to colors.warning
        NotificationType.STREAK -> AppIcons.Whatshot to TileCoral
        NotificationType.REMINDER -> AppIcons.Notifications to colors.textSecondary
        NotificationType.TIP -> AppIcons.Lightbulb to colors.warning
        NotificationType.UPDATE -> AppIcons.Campaign to TileBlue
        NotificationType.ANNOUNCEMENT -> AppIcons.Info to TileTeal
        NotificationType.FAVOURITE -> AppIcons.Star to TilePink
    }

    val accent = rememberAccentOnSurface(baseColor)
    val iconTint = if (notification.isRead) colors.textSecondary else accent
    val titleColor = if (notification.isRead) colors.textSecondary else colors.textPrimary

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RouletteTheme.shapes.card,
        color = colors.surface,
        border = BorderStroke(
            dimens.borderWidth,
            if (notification.isRead) colors.divider else colors.primaryBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.space16),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RouletteTheme.shapes.iconTile)
                    .background(iconTint.copy(alpha = if (colors.isLight) 0.10f else 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(dimens.iconSizeSmall)
                )
            }

            Spacer(Modifier.width(dimens.rowIconGap))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = titleColor,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(dimens.space8))
                    Text(
                        text = TimeUtils.getRelativeTime(notification.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary
                    )
                }
                Spacer(Modifier.size(dimens.space2))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationScreenPreview() {
    RouletteTheme {
        NotificationContent(
            uiState = NotificationUiState.Success(
                notifications = listOf(
                    Notification(
                        id = "1",
                        title = "Spin Result",
                        message = "Your wheel Luck landed on Red!",
                        type = NotificationType.SPIN_RESULT,
                        timestamp = Clock.System.now(),
                        isRead = false
                    ),
                    Notification(
                        id = "2",
                        title = "Milestone",
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
