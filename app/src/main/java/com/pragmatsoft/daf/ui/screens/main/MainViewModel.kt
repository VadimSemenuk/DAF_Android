package com.pragmatsoft.daf.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pragmatsoft.daf.data.local.DataStoreRepository
import com.pragmatsoft.daf.utils.AudioHelper
import com.pragmatsoft.daf.utils.combine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val delay: Int = 0,
    val gain: Int = 0,
    val isOutputDeviceAvailable: Boolean = false,
    val isSettingsVisible: Boolean = false,
    val isRecordingVisible: Boolean = false,
    val isHeadphonesNotificationVisible: Boolean = false,
    val isPermissionsVisible: Boolean = false,
    val isHelpVisible: Boolean = false,
    val isAdVisible: Boolean = false,
    val isInitialHelpShown: Boolean = false,
    val isInitialHelpVisible: Boolean = false,
    val isMinimalDelayDialogShown: Boolean = false,
    val isMinimalDelayDialogVisible: Boolean = false,
)

sealed interface MainAction {
    data class StartAudioProcessing(val requestServiceStart: () -> Boolean) : MainAction
    data class SetDelay(val value: Int) : MainAction
    data class SetGain(val value: Int) : MainAction
    data class SetIsSettingsVisible(val value: Boolean) : MainAction
    data class SetIsRecordingVisible(val value: Boolean) : MainAction
    data class SetIsHeadphonesNotificationVisible(val value: Boolean) : MainAction
    data class SetIsPermissionsVisible(val value: Boolean) : MainAction
    data class SetIsHelpVisible(val value: Boolean) : MainAction
    data class SetIsAdVisible(val value: Boolean) : MainAction
    data class SetIsInitialHelpShown(val value: Boolean) : MainAction
    data class SetIsInitialHelpVisible(val value: Boolean) : MainAction
    data class SetIsMinimalDelayDialogVisible(val value: Boolean) : MainAction
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val audioHelper: AudioHelper,
) : ViewModel() {

    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState>
        get() = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dataStoreRepository.delayFlow,
                dataStoreRepository.gainFlow,
                audioHelper.outputDevicesFlow,
                dataStoreRepository.isAdVisible,
                dataStoreRepository.isInitialHelpShown,
                dataStoreRepository.isMinimalDelayDialogShown,
            ) {
              delay, gain, outputDevices, isAdVisible, isInitialHelpShown, isMinimalDelayDialogShown ->
                _state.value.copy(
                    delay = delay,
                    gain = gain,
//                    isOutputDeviceAvailable = true,
                    isOutputDeviceAvailable = outputDevices.isNotEmpty(),
                    isAdVisible = isAdVisible,
                    isInitialHelpShown = isInitialHelpShown,
                    isMinimalDelayDialogShown = isMinimalDelayDialogShown
                )
            }
                .collect {
                    _state.value = it
                }
        }
    }

    fun onAction(action: MainAction) {
        when (action) {
            is MainAction.StartAudioProcessing -> startAudioProcessing(action.requestServiceStart)
            is MainAction.SetDelay -> setDelay(action.value)
            is MainAction.SetGain -> setGain(action.value)
            is MainAction.SetIsSettingsVisible -> setIsSettingsVisible(action.value)
            is MainAction.SetIsRecordingVisible -> setIsRecordingVisible(action.value)
            is MainAction.SetIsHeadphonesNotificationVisible -> setIsHeadphonesNotificationVisible(action.value)
            is MainAction.SetIsPermissionsVisible -> setIsPermissionsVisible(action.value)
            is MainAction.SetIsHelpVisible -> setIsHelpVisible(action.value)
            is MainAction.SetIsAdVisible -> setIsAdVisible(action.value)
            is MainAction.SetIsInitialHelpShown -> setIsInitialHelpShown(action.value)
            is MainAction.SetIsInitialHelpVisible -> setIsInitialHelpVisible(action.value)
            is MainAction.SetIsMinimalDelayDialogVisible -> setIsMinimalDelayDialogVisible(action.value)
        }
    }

    private fun startAudioProcessing(requestServiceStart: () -> Boolean) {
        if (!_state.value.isOutputDeviceAvailable) {
            setIsHeadphonesNotificationVisible(true)
            return
        }

        val isStarted = requestServiceStart()
        if (!isStarted) return

        if (!_state.value.isInitialHelpShown) {
            viewModelScope.launch {
                delay(1000)
                setIsInitialHelpVisible(true)
                setIsInitialHelpShown(true)
            }
        }
    }

    private fun setDelay(value: Int) = viewModelScope.launch {
        if (value == 0 && !_state.value.isMinimalDelayDialogShown) {
            setIsMinimalDelayDialogShown(true)
            setIsMinimalDelayDialogVisible(true)
        }

        dataStoreRepository.saveDelay(value)
    }

    private fun setGain(value: Int) = viewModelScope.launch {
        dataStoreRepository.saveGain(value)
    }

    fun setIsSettingsVisible(value: Boolean) {
        _state.update { it.copy(isSettingsVisible = value) }
    }

    private fun setIsRecordingVisible(value: Boolean) {
        _state.update { it.copy(isRecordingVisible = value) }
    }

    private fun setIsHeadphonesNotificationVisible(value: Boolean) {
        _state.update { it.copy(isHeadphonesNotificationVisible = value) }
    }

    private fun setIsPermissionsVisible(value: Boolean) {
        _state.update { it.copy(isPermissionsVisible = value) }
    }

    private fun setIsHelpVisible(value: Boolean) {
        _state.update { it.copy(isHelpVisible = value) }
    }

    private fun setIsAdVisible(value: Boolean) = viewModelScope.launch {
        dataStoreRepository.setIsAdVisible(value)
    }

    private fun setIsInitialHelpShown(value: Boolean) = viewModelScope.launch {
        dataStoreRepository.setIsInitialHelpShown(value)
    }

    private fun setIsMinimalDelayDialogShown(value: Boolean) = viewModelScope.launch {
        dataStoreRepository.saveIsMinimalDelayDialogShown(value)
    }

    private fun setIsInitialHelpVisible(value: Boolean) {
        _state.update { it.copy(isInitialHelpVisible = value) }
    }

    private fun setIsMinimalDelayDialogVisible(value: Boolean) {
        _state.update { it.copy(isMinimalDelayDialogVisible = value) }
    }
}