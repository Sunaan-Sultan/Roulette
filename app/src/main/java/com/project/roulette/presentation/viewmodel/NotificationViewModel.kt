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
