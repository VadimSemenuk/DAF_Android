package com.pragmatsoft.daf.ui.screens.main.components.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pragmatsoft.daf.R
import com.pragmatsoft.daf.data.local.DataStoreRepository
import com.pragmatsoft.daf.utils.AndroidStringProvider
import com.pragmatsoft.daf.utils.AudioHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val inputDeviceOptions: List<Pair<Int?, String>> = listOf(),
    val outputDeviceOptions: List<Pair<Int?, String>> = listOf(),
    val inputDeviceId: Int? = null,
    val outputDeviceId: Int? = null,
    val autoSelectOption: Pair<Int?, String> = Pair(null, ""),
    val isNoiseCancellationOn: Boolean = false
)

sealed interface SettingsAction {
    data class SetInputDeviceId(val value: Int?) : SettingsAction
    data class SetOutputDeviceId(val value: Int?) : SettingsAction
    data class SetIsNoiseCancellationOn(val value: Boolean) : SettingsAction
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val audioHelper: AudioHelper,
    private val dataStoreRepository: DataStoreRepository,
    androidStringProvider: AndroidStringProvider
) : ViewModel() {

    val autoSelectOption = Pair(null, androidStringProvider.getString(R.string.auto_select))

    private val _state = MutableStateFlow(SettingsUiState(autoSelectOption = autoSelectOption))
    val state: StateFlow<SettingsUiState>
        get() = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                audioHelper.inputDevicesFlow,
                audioHelper.outputDevicesFlow,
                dataStoreRepository.inputDeviceIdFlow,
                dataStoreRepository.outputDeviceIdFlow,
                dataStoreRepository.isNoiseCancellationOn
            ) {
                inputDevices, outputDevices, inputDeviceId, outputDeviceId, isNoiseCancellationOn ->

                val inputDeviceOptions = inputDevices
                    .map { Pair(it.id, audioHelper.toString(it)) }

                val outputDeviceOptions = outputDevices
                    .map { Pair(it.id, audioHelper.toString(it)) }

                _state.value.copy(
                    inputDeviceOptions = listOf(autoSelectOption) + inputDeviceOptions,
                    outputDeviceOptions = listOf(autoSelectOption) + outputDeviceOptions,
                    inputDeviceId = inputDeviceId,
                    outputDeviceId = outputDeviceId,
                    isNoiseCancellationOn = isNoiseCancellationOn
                )
            }
                .collect {
                    _state.value = it
                }
        }
    }


    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SetInputDeviceId -> setInputDeviceId(action.value)
            is SettingsAction.SetOutputDeviceId -> setOutputDeviceId(action.value)
            is SettingsAction.SetIsNoiseCancellationOn -> setIsNoiseCancellationOn(action.value)
        }
    }

    private fun setInputDeviceId(value: Int?) = viewModelScope.launch {
        dataStoreRepository.saveInputDeviceId(value)
    }

    private fun setOutputDeviceId(value: Int?) = viewModelScope.launch {
        dataStoreRepository.saveOutputDeviceId(value)
    }

    private fun setIsNoiseCancellationOn(value: Boolean) = viewModelScope.launch {
        dataStoreRepository.setIsNoiseCancellationOn(value)
    }
}