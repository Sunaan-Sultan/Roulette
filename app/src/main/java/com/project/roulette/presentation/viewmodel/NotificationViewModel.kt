package com.project.roulette.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.roulette.domain.model.Result
import com.project.roulette.domain.repository.NotificationRepository
import com.project.roulette.presentation.model.NotificationFilter
import com.project.roulette.presentation.model.NotificationUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.days

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    init {
        // Add mock data for demonstration purposes if empty
        viewModelScope.launch {
            repository.getAllNotifications().first().let { result ->
                if (result is Result.Success && result.data.isEmpty()) {
                    val now = Clock.System.now()
                    val mocks = listOf(
                        com.project.roulette.domain.model.Notification(
                            id = "1",
                            title = "Spin Result",
                            message = "Your wheel asdasdas landed on Alex! 🎯",
                            type = com.project.roulette.domain.model.NotificationType.SPIN_RESULT,
                            timestamp = now - 2.minutes,
                            isRead = false
                        ),
                        com.project.roulette.domain.model.Notification(
                            id = "2",
                            title = "Milestone 🎉",
                            message = "You've made 25 spins total. Keep the momentum going!",
                            type = com.project.roulette.domain.model.NotificationType.MILESTONE,
                            timestamp = now - 1.hours,
                            isRead = false
                        ),
                        com.project.roulette.domain.model.Notification(
                            id = "3",
                            title = "Streak 🔥",
                            message = "You're on a 3-day spin streak. Don't break it!",
                            type = com.project.roulette.domain.model.NotificationType.STREAK,
                            timestamp = now - 3.hours,
                            isRead = false
                        ),
                        com.project.roulette.domain.model.Notification(
                            id = "4",
                            title = "Reminder",
                            message = "You haven't spun Class Wheel in 3 days.",
                            type = com.project.roulette.domain.model.NotificationType.REMINDER,
                            timestamp = now - 1.days,
                            isRead = true
                        ),
                        com.project.roulette.domain.model.Notification(
                            id = "5",
                            title = "Tip 💡",
                            message = "Try adding more segments to New Wheel for more variety!",
                            type = com.project.roulette.domain.model.NotificationType.TIP,
                            timestamp = now - 2.days,
                            isRead = true
                        )
                    )
                    mocks.forEach { repository.addNotification(it) }
                }
            }
        }
    }

    private val _filter = MutableStateFlow(NotificationFilter.ALL)
    val filter: StateFlow<NotificationFilter> = _filter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<NotificationUiState> = combine(
        repository.getAllNotifications(),
        repository.getUnreadCount(),
        _filter
    ) { result, unreadCount, filter ->
        when (result) {
            is Result.Success -> {
                val filtered = when (filter) {
                    NotificationFilter.ALL -> result.data
                    NotificationFilter.UNREAD -> result.data.filter { !it.isRead }
                    NotificationFilter.ACTIVITY -> result.data.filter { 
                        it.type == com.project.roulette.domain.model.NotificationType.SPIN_RESULT ||
                        it.type == com.project.roulette.domain.model.NotificationType.FAVOURITE ||
                        it.type == com.project.roulette.domain.model.NotificationType.STREAK
                    }
                }
                NotificationUiState.Success(
                    notifications = filtered,
                    unreadCount = unreadCount,
                    currentFilter = filter
                )
            }
            is Result.Error -> NotificationUiState.Error(result.exception.message ?: "Unknown error")
            is Result.Loading -> NotificationUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotificationUiState.Loading
    )

    fun setFilter(filter: NotificationFilter) {
        _filter.value = filter
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }
}
