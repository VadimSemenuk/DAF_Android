package com.pragmatsoft.daf.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.pragmatsoft.daf.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

object PreferenceKeys {
    val DELAY = intPreferencesKey("DELAY")
    val GAIN = intPreferencesKey("GAIN")
    val INPUT_DEVICE_ID = intPreferencesKey("INPUT_DEVICE_ID")
    val OUTPUT_DEVICE_ID = intPreferencesKey("OUTPUT_DEVICE_ID")
    val IS_AD_VISIBLE = booleanPreferencesKey("IS_AD_VISIBLE")
    val IS_INITIAL_HELP_SHOWN = booleanPreferencesKey("IS_INITIAL_HELP_SHOWN")
    val IS_NOISE_CANCELLATION_ON = booleanPreferencesKey("IS_NOISE_CANCELLATION_ON")
    val IS_MINIMAL_DELAY_DIALOG_SHOWN = booleanPreferencesKey("IS_MINIMAL_DELAY_DIALOG_SHOWN")
}

class DataStoreRepository @Inject constructor(context: Context) {

    private val dataStore = context.dataStore

    // region pitch
    val delayFlow: Flow<Int> = dataStore.data
        .map { it[PreferenceKeys.DELAY] ?: 50 }
        .distinctUntilChanged()

    val delay = runBlocking { delayFlow.first() }

    suspend fun saveDelay(value: Int) {
        dataStore.edit { it[PreferenceKeys.DELAY] = value }
    }
    // endregion

    // region gain
    val gainFlow: Flow<Int> = dataStore.data
        .map { it[PreferenceKeys.GAIN] ?: 3 }
        .distinctUntilChanged()

    val gain = runBlocking { gainFlow.first() }

    suspend fun saveGain(value: Int) {
        dataStore.edit { it[PreferenceKeys.GAIN] = value }
    }
    // endregion

    // region input device
    val inputDeviceIdFlow: Flow<Int?> = dataStore.data
        .map { it[PreferenceKeys.INPUT_DEVICE_ID] }
        .distinctUntilChanged()

    fun getInputDeviceId() = runBlocking {
        inputDeviceIdFlow.first()
    }

    suspend fun saveInputDeviceId(value: Int?) {
        if (value == null) {
            dataStore.edit { it.remove(PreferenceKeys.INPUT_DEVICE_ID) }
        } else {
            dataStore.edit { it[PreferenceKeys.INPUT_DEVICE_ID] = value }
        }
    }
    // endregion

    // region output device
    val outputDeviceIdFlow: Flow<Int?> = dataStore.data
        .map { it[PreferenceKeys.OUTPUT_DEVICE_ID] }
        .distinctUntilChanged()

    fun getOutputDeviceId() = runBlocking {
        outputDeviceIdFlow.first()
    }

    suspend fun saveOutputDeviceId(value: Int?) {
        if (value == null) {
            dataStore.edit { it.remove(PreferenceKeys.OUTPUT_DEVICE_ID) }
        } else {
            dataStore.edit { it[PreferenceKeys.OUTPUT_DEVICE_ID] = value }
        }
    }
    // endregion

    // region ad
    val isAdVisible: Flow<Boolean> = dataStore.data
        .map { it[PreferenceKeys.IS_AD_VISIBLE] ?: false }
        .distinctUntilChanged()

    suspend fun setIsAdVisible(value: Boolean) {
        dataStore.edit { it[PreferenceKeys.IS_AD_VISIBLE] = value }
    }
    // endregion

    // region initial help
    val isInitialHelpShown: Flow<Boolean> = dataStore.data
        .map { it[PreferenceKeys.IS_INITIAL_HELP_SHOWN] ?: false }
        .distinctUntilChanged()

    suspend fun setIsInitialHelpShown(value: Boolean) {
        dataStore.edit { it[PreferenceKeys.IS_INITIAL_HELP_SHOWN] = value }
    }
    // endregion

    // region min delay dialog
    val isMinimalDelayDialogShown: Flow<Boolean> = dataStore.data
        .map { it[PreferenceKeys.IS_MINIMAL_DELAY_DIALOG_SHOWN] ?: false }
        .distinctUntilChanged()

    suspend fun saveIsMinimalDelayDialogShown(value: Boolean) {
        dataStore.edit { it[PreferenceKeys.IS_MINIMAL_DELAY_DIALOG_SHOWN] = value }
    }
    // endregion

    // region noise cancellation
    val isNoiseCancellationOn: Flow<Boolean> = dataStore.data
        .map { it[PreferenceKeys.IS_NOISE_CANCELLATION_ON] ?: false }
        .distinctUntilChanged()

    suspend fun setIsNoiseCancellationOn(value: Boolean) {
        dataStore.edit { it[PreferenceKeys.IS_NOISE_CANCELLATION_ON] = value }
    }
    // endregion
}