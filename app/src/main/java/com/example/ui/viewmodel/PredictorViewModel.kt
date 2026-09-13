package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Prediction
import com.example.data.model.PredictionStatus
import com.example.data.remote.TelegramResult
import com.example.data.repository.PredictionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface BotConnectionState {
    data object Idle : BotConnectionState
    data object Checking : BotConnectionState
    data class Connected(val botName: String, val username: String?) : BotConnectionState
    data class Error(val message: String) : BotConnectionState
}

data class UiStats(
    val totalSignals: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val pending: Int = 0,
    val winRate: Float = 0f
)

class PredictorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PredictionRepository(application.applicationContext)

    val predictions: StateFlow<List<Prediction>> = repository.getPredictions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _currentPeriod = MutableStateFlow(repository.currentPeriod)
    val currentPeriod: StateFlow<Long> = _currentPeriod.asStateFlow()

    private val _lastPostedPeriod = MutableStateFlow(repository.lastPostedPeriod)
    val lastPostedPeriod: StateFlow<Long> = _lastPostedPeriod.asStateFlow()

    private val _countdownSeconds = MutableStateFlow(60)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _botConnectionState = MutableStateFlow<BotConnectionState>(BotConnectionState.Idle)
    val botConnectionState: StateFlow<BotConnectionState> = _botConnectionState.asStateFlow()

    private val _botToken = MutableStateFlow(repository.botToken)
    val botToken: StateFlow<String> = _botToken.asStateFlow()

    private val _chatId = MutableStateFlow(repository.chatId)
    val chatId: StateFlow<String> = _chatId.asStateFlow()

    private val _officialTag = MutableStateFlow(repository.officialTag)
    val officialTag: StateFlow<String> = _officialTag.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    private var autoLoopJob: Job? = null

    init {
        testBotConnection()
    }

    fun startAutoSignal() {
        if (_isRunning.value) {
            viewModelScope.launch {
                _snackbarEvent.emit("⚠️ Auto signal is already running!")
            }
            return
        }

        _isRunning.value = true
        viewModelScope.launch {
            _snackbarEvent.emit("🚀 Auto signal loop started!")
        }

        autoLoopJob?.cancel()
        autoLoopJob = viewModelScope.launch {
            while (_isRunning.value) {
                // Post signal for current period
                postSignalInternal(_currentPeriod.value)

                // 60-second countdown
                for (sec in 60 downTo 1) {
                    if (!_isRunning.value) break
                    _countdownSeconds.value = sec
                    delay(1000)
                }
                _countdownSeconds.value = 60
            }
        }
    }

    fun stopAutoSignal() {
        if (!_isRunning.value) {
            viewModelScope.launch {
                _snackbarEvent.emit("⚠️ Auto signal service is already stopped!")
            }
            return
        }

        _isRunning.value = false
        autoLoopJob?.cancel()
        autoLoopJob = null
        _countdownSeconds.value = 60

        viewModelScope.launch {
            _snackbarEvent.emit("🛑 Auto signal stopped.")
        }
    }

    fun sendSingleSignal() {
        if (_isBusy.value) return
        viewModelScope.launch {
            postSignalInternal(_currentPeriod.value)
        }
    }

    private suspend fun postSignalInternal(periodToPost: Long) {
        _isBusy.value = true
        val result = repository.postSignal(periodToPost)
        _isBusy.value = false

        result.fold(
            onSuccess = { pred ->
                _lastPostedPeriod.value = pred.period
                _currentPeriod.value = pred.period + 1
                _snackbarEvent.emit("✅ Period ${pred.period} signal posted to Telegram!")
            },
            onFailure = { error ->
                _snackbarEvent.emit("❌ Signal Error: ${error.message}")
            }
        )
    }

    fun markWin() {
        if (_isBusy.value) return
        val targetPeriod = _lastPostedPeriod.value
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.postWin(targetPeriod)
            _isBusy.value = false

            result.fold(
                onSuccess = {
                    _snackbarEvent.emit("✅ Period $targetPeriod - Win posted to Telegram!")
                },
                onFailure = { error ->
                    _snackbarEvent.emit("❌ Win Post Error: ${error.message}")
                }
            )
        }
    }

    fun markLoss() {
        if (_isBusy.value) return
        val targetPeriod = _lastPostedPeriod.value
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.postLoss(targetPeriod)
            _isBusy.value = false

            result.fold(
                onSuccess = {
                    _snackbarEvent.emit("❌ Period $targetPeriod - Loss posted to Telegram!")
                },
                onFailure = { error ->
                    _snackbarEvent.emit("❌ Loss Post Error: ${error.message}")
                }
            )
        }
    }

    fun markPredictionStatus(period: Long, status: PredictionStatus) {
        viewModelScope.launch {
            if (status == PredictionStatus.WIN) {
                repository.postWin(period)
            } else if (status == PredictionStatus.LOSS) {
                repository.postLoss(period)
            }
        }
    }

    fun setPeriod(newPeriod: Long) {
        repository.currentPeriod = newPeriod
        _currentPeriod.value = newPeriod
        viewModelScope.launch {
            _snackbarEvent.emit("✅ Period updated to $newPeriod")
        }
    }

    fun incrementPeriod() {
        val updated = _currentPeriod.value + 1
        setPeriod(updated)
    }

    fun updateConfig(newToken: String, newChatId: String, newOfficialTag: String) {
        repository.botToken = newToken
        repository.chatId = newChatId
        repository.officialTag = newOfficialTag

        _botToken.value = repository.botToken
        _chatId.value = repository.chatId
        _officialTag.value = repository.officialTag

        viewModelScope.launch {
            _snackbarEvent.emit("⚙️ Bot configuration saved!")
            testBotConnection()
        }
    }

    fun testBotConnection() {
        viewModelScope.launch {
            _botConnectionState.value = BotConnectionState.Checking
            when (val res = repository.testBot()) {
                is TelegramResult.Success -> {
                    _botConnectionState.value = BotConnectionState.Connected(
                        botName = res.data.firstName,
                        username = res.data.username
                    )
                }
                is TelegramResult.Error -> {
                    _botConnectionState.value = BotConnectionState.Error(res.message)
                }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _snackbarEvent.emit("🗑️ Prediction history cleared.")
        }
    }

    fun computeStats(list: List<Prediction>): UiStats {
        val total = list.size
        val wins = list.count { it.status == PredictionStatus.WIN }
        val losses = list.count { it.status == PredictionStatus.LOSS }
        val pending = list.count { it.status == PredictionStatus.PENDING }
        val decided = wins + losses
        val winRate = if (decided > 0) (wins.toFloat() / decided) * 100f else 0f
        return UiStats(
            totalSignals = total,
            wins = wins,
            losses = losses,
            pending = pending,
            winRate = winRate
        )
    }

    override fun onCleared() {
        super.onCleared()
        autoLoopJob?.cancel()
    }
}
