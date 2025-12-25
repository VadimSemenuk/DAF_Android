package com.pragmatsoft.daf.ui.screens.main

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Environment
import android.os.IBinder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pragmatsoft.daf.R
import com.pragmatsoft.daf.services.audio.AudioService
import com.pragmatsoft.daf.ui.components.ChronometerView
import com.pragmatsoft.daf.ui.components._LiveShape
import com.pragmatsoft.daf.ui.screens.main.components.help.HelpDialog
import com.pragmatsoft.daf.ui.screens.main.components.permissions.PermissionsModalBottomSheet
import com.pragmatsoft.daf.ui.screens.main.components.record.RecordModalBottomSheet
import com.pragmatsoft.daf.ui.screens.main.components.settings.SettingsModalBottomSheet
import com.pragmatsoft.daf.ui.theme.DAFTheme
import com.pragmatsoft.daf.ui.theme.Green
import com.pragmatsoft.daf.ui.theme.ShapeDefaults.bottomListItemShape
import com.pragmatsoft.daf.ui.theme.ShapeDefaults.topListItemShape
import com.pragmatsoft.daf.ui.theme.YellowDark
import com.pragmatsoft.daf.utils.rememberBatteryPermissionHandler
import com.pragmatsoft.daf.utils.rememberMicPermissionHandler
import com.pragmatsoft.daf.utils.rememberNotificationPermissionHandler
import kotlinx.coroutines.flow.combine
import java.io.File
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    MainScreen(
        uiState,
        viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainUiState,
    onAction: (value: MainAction) -> Unit,
) {
    val context = LocalContext.current

    val audioService = rememberAudioServiceBound()
    val audioServiceState = produceAudioServiceState(audioService)

    val (isMicPermissionGranted, requestMicPermissionIfNeedAndStart) = rememberMicPermissionHandler {
        onAction(
            MainAction.StartAudioProcessing {
                audioService?.start()
                return@StartAudioProcessing true
            }
        )
    }

    fun start() {
        onAction(
            MainAction.StartAudioProcessing {
                if (!isMicPermissionGranted) {
                    requestMicPermissionIfNeedAndStart()
                    return@StartAudioProcessing false
                }

                audioService?.start()
                return@StartAudioProcessing true
            }
        )
    }

    fun stop() {
        audioService?.stop()
    }

    if (uiState.isSettingsVisible) {
        SettingsModalBottomSheet { onAction(MainAction.SetIsSettingsVisible(false)) }
    }

    if (uiState.isRecordingVisible) {
        RecordModalBottomSheet(
            filePath = "${context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)}/recording.m4a"
        ) {
            onAction(MainAction.SetIsRecordingVisible(false))
        }
    }

    if (uiState.isHeadphonesNotificationVisible) {
        HeadphonesNotificationDialog { onAction(MainAction.SetIsHeadphonesNotificationVisible(false)) }
    }

    if (uiState.isPermissionsVisible) {
        PermissionsModalBottomSheet { onAction(MainAction.SetIsPermissionsVisible(false)) }
    }

    if (uiState.isHelpVisible) {
        HelpDialog { onAction(MainAction.SetIsHelpVisible(false)) }
    }

    if (uiState.isInitialHelpVisible) {
        InitialHelpDialog { onAction(MainAction.SetIsInitialHelpVisible(false)) }
    }

    if (uiState.isMinimalDelayDialogVisible) {
        MinimalDelayDialog {
            onAction(MainAction.SetIsMinimalDelayDialogVisible(false))
        }
    }

    Column(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
    ) {
        AppBar(onAction = onAction)

        if (uiState.isAdVisible) {
            Box(
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Ad(onAction = onAction)
            }
        }

        Card(
            shape = topListItemShape,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, true)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                _LiveShape(audioServiceState.isActive)

                Column(
                    modifier = Modifier
                        .animateContentSize(animationSpec = tween(durationMillis = 300)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ChronometerView(
                        isActive = audioServiceState.isActive,
                        baseTime = audioServiceState.activatedTimestamp,
                        content = {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.displayLargeEmphasized,
                                fontWeight = FontWeight.Bold,
                                color = YellowDark
                            )
                        }
                    )

                    AnimatedVisibility(
                        visible = audioServiceState.isWriting,
                        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 300))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                modifier = Modifier.padding(end = 3.dp),
                                imageVector = Icons.Rounded.RadioButtonChecked,
                                contentDescription = "write",
                                tint = YellowDark
                            )

                            ChronometerView(
                                isActive = audioServiceState.isWriting,
                                baseTime = audioServiceState.activatedWritingTimestamp,
                                content = {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = YellowDark
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        Card(
            shape = bottomListItemShape,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                DelaySelector(
                    value = uiState.delay,
                    onChange = { onAction(MainAction.SetDelay(it)) }
                )

                GainSelector(
                    value = uiState.gain,
                    onChange = { onAction(MainAction.SetGain(it)) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) { }

            FilledIconToggleButton(
                modifier = Modifier
                    .size(
                        IconButtonDefaults.extraLargeContainerSize(
                            IconButtonDefaults.IconButtonWidthOption.Wide
                        )
                    ),
                onCheckedChange = {
                    if (audioServiceState.isActive) {
                        stop()
                    } else {
                        start()
                    }
                },
                checked = audioServiceState.isActive,
                shapes = IconButtonDefaults.toggleableShapes(),
                colors = IconButtonDefaults.filledIconToggleButtonColors()
            ) {
                if (audioServiceState.isActive) {
                    Icon(
                        modifier = Modifier
                            .size(IconButtonDefaults.largeIconSize),
                        imageVector = Icons.Rounded.Stop,
                        contentDescription = "stop"
                    )
                } else {
                    Icon(
                        modifier = Modifier
                            .size(IconButtonDefaults.largeIconSize),
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "play"
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (audioServiceState.isActive) {
                    OutlinedIconButton(
                        modifier =
                            Modifier
                                .size(
                                    IconButtonDefaults.largeContainerSize(
                                        IconButtonDefaults.IconButtonWidthOption.Narrow
                                    )
                                ),
                        border = IconButtonDefaults.outlinedIconButtonVibrantBorder(true),
                        colors = IconButtonDefaults.outlinedIconButtonVibrantColors(
                            contentColor = if (audioServiceState.isWriting) Green else MaterialTheme.colorScheme.error
                        ),
                        onClick = {
                            if (audioService?.audioProcessor == null) {
                                return@OutlinedIconButton
                            }

                            val audioProcessor = audioService.audioProcessor
                            if (audioServiceState.isWriting) {
                                audioProcessor.writingManager.stop()
                                onAction(MainAction.SetIsRecordingVisible(true))
                            } else {
                                val filePath = "${context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)}/recording.m4a"
                                val file = File(filePath)
                                if (file.exists()) {
                                    file.delete()
                                }
                                audioProcessor.writingManager.start(filePath)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RadioButtonChecked,
                            contentDescription = "write",
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable()
fun Ad(
    onAction: (value: MainAction) -> Unit,
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row {
                Text(
                    text = stringResource(R.string.created_with) + " ",
                    fontSize = 12.sp,
                    lineHeight = 1.1.sp
                )
                Text(
                    text = "LOGO ADULT",
                    fontSize = 12.sp,
                    color = YellowDark,
                    lineHeight = 1.1.sp,
                    fontWeight = FontWeight(500)
                )
            }
            Text(
                text = stringResource(R.string.speach_therapist),
                fontSize = 16.sp,
                lineHeight = 1.1.sp
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            FilledIconButton(
                modifier = Modifier
                    .size(IconButtonDefaults.smallContainerSize(
                        IconButtonDefaults.IconButtonWidthOption.Narrow
                    )),
                onClick = {
                    val browserIntent = Intent(Intent.ACTION_VIEW,
                        "https://logoadult.by/".toUri())
                    context.startActivity(browserIntent)
                },
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowOutward,
                    contentDescription = "go",
                )
            }
            FilledIconButton(
                modifier = Modifier
                    .size(IconButtonDefaults.smallContainerSize(
                        IconButtonDefaults.IconButtonWidthOption.Narrow
                    )),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
//                    colors = IconButtonDefaults.filledTonalIconButtonColors(
//                        containerColor = MaterialTheme.colorScheme.errorContainer,
//                        contentColor = MaterialTheme.colorScheme.onErrorContainer
//                    ),
                onClick = {
                    onAction(MainAction.SetIsAdVisible(false))
                },
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "close",
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable()
fun AppBar(
    onAction: (value: MainAction) -> Unit
) {
    val (isMicPermissionGranted) = rememberMicPermissionHandler()
    val (isNotificationPermissionGranted) = rememberNotificationPermissionHandler()
    val (isBatteryPermissionGranted) = rememberBatteryPermissionHandler()

    Row(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1F))

        if (!isMicPermissionGranted
            || !isNotificationPermissionGranted
            || !isBatteryPermissionGranted) {
            FilledTonalIconButton(
                modifier = Modifier
                    .size(IconButtonDefaults.smallContainerSize(
                        IconButtonDefaults.IconButtonWidthOption.Wide
                    )),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                onClick = {
                    onAction(MainAction.SetIsPermissionsVisible(true))
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.PriorityHigh,
                    contentDescription = "attention",
                )
            }
        }

        FilledTonalIconButton(
            modifier = Modifier
                .size(IconButtonDefaults.smallContainerSize(
                    IconButtonDefaults.IconButtonWidthOption.Wide
                )),
//            colors = IconButtonDefaults.outlinedIconButtonVibrantColors(),
//            border = IconButtonDefaults.outlinedIconButtonVibrantBorder(true),
            onClick = {
                onAction(MainAction.SetIsHelpVisible(true))
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                contentDescription = "help",
            )
        }

        FilledTonalIconButton(
            modifier = Modifier
                .size(IconButtonDefaults.smallContainerSize(
                    IconButtonDefaults.IconButtonWidthOption.Wide
                )),
//            colors = IconButtonDefaults.outlinedIconButtonVibrantColors(),
//            border = IconButtonDefaults.outlinedIconButtonVibrantBorder(true),
            onClick = {
                onAction(MainAction.SetIsSettingsVisible(true))
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "settings",
            )
        }
    }
}

@Composable
fun rememberAudioServiceBound(): AudioService? {
    val context = LocalContext.current

    var service: AudioService? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val b = binder as AudioService.LocalBinder
                service = b.getService()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                service = null
            }
        }

        AudioService.bind(context, connection)

        onDispose {
            AudioService.unbind(context, connection)
        }
    }

    return service
}

data class AudioServiceState(
    val isActive: Boolean = false,
    val activatedTimestamp: Long? = null,
    val isWriting: Boolean = false,
    val activatedWritingTimestamp: Long? = null
)

@Composable
fun produceAudioServiceState(service: AudioService?): AudioServiceState {
    val state by produceState(initialValue = AudioServiceState(), key1 = service) {
        service?.let {
            combine(
                it.isActiveFlow,
                it.audioProcessor.writingManager.isActiveFlow,
            ) { isActive, isWriting ->
                AudioServiceState(
                    isActive = isActive,
                    activatedTimestamp = service.activatedTimestamp,
                    isWriting = isWriting,
                    activatedWritingTimestamp = service.audioProcessor.writingManager.activatedTimestamp
                )
            }
                .collect { nextState ->
                    this.value = nextState
                }
        }
    }

    return state
}

@Composable
fun HeadphonesNotificationDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(Icons.Rounded.Headphones, contentDescription = "warning")
        },
        title = {
            Text(text = stringResource(R.string.connect_headphones))
        },
        text = {
            Text(text = stringResource(R.string.connect_headphones_long))
        },
        onDismissRequest = {
            onDismiss()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(text = stringResource(R.string.close))
            }
        },
    )
}

@Composable
fun InitialHelpDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(Icons.Rounded.Info, contentDescription = "info")
        },
        title = {
            Text(text = stringResource(R.string.initial_help_title))
        },
        text = {
            Text(text = stringResource(R.string.initial_help_description))
        },
        onDismissRequest = {
            onDismiss()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(text = stringResource(R.string.ok))
            }
        },
    )
}

@Composable
fun MinimalDelayDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = "help"
            )
        },
        title = {
            Text(text = stringResource(R.string.min_delay))
        },
        text = {
            Text(text = stringResource(R.string.min_delay_long))
        },
        onDismissRequest = {
            onDismiss()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(text = stringResource(R.string.ok))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DelaySelector(
    value: Int,
    onChange: (value: Int) -> Unit
) {
    val minValue = 0f
    val maxValue = 200f

    var _value by remember(value) { mutableFloatStateOf(value.toFloat()) }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (!isEditing) {
            _value = value.toFloat()
        }
    }

    val getCorrectValue = remember { {
        value: Float ->
        (value - (value % 10)).coerceIn(minValue, maxValue)
    } }

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.delay),
                style = MaterialTheme.typography.labelLargeEmphasized
            )

            Text(
                text = "${if (_value == 0f) "≈" else ""} ${_value.toInt() + 100} ${stringResource(R.string.ms)}",
                style = MaterialTheme.typography.labelLargeEmphasized
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedIconButton(
                modifier = Modifier
                    .size(IconButtonDefaults.smallContainerSize(
                        IconButtonDefaults.IconButtonWidthOption.Narrow
                    )),
                border = IconButtonDefaults.outlinedIconButtonVibrantBorder(true),
                colors = IconButtonDefaults.outlinedIconButtonVibrantColors(),
                onClick = {
                    onChange(getCorrectValue(_value - 10f).toInt())
                },
            ) {
                Icon(
                    imageVector = Icons.Rounded.Remove,
                    contentDescription = "lower",
                )
            }

            Slider(
                modifier = Modifier.weight(1f),
                value = _value,
                valueRange = minValue..maxValue,
                onValueChange = {
                    isEditing = true
                    _value = getCorrectValue(it)
                },
                onValueChangeFinished = {
                    isEditing = false
                    onChange(_value.toInt())
                }
            )

            OutlinedIconButton(
                modifier = Modifier
                    .size(IconButtonDefaults.smallContainerSize(
                        IconButtonDefaults.IconButtonWidthOption.Narrow
                    )),
                border = IconButtonDefaults.outlinedIconButtonVibrantBorder(true),
                colors = IconButtonDefaults.outlinedIconButtonVibrantColors(),
                onClick = {
                    onChange(getCorrectValue(_value + 10f).toInt())
                },
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "higher",
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GainSelector(
    value: Int,
    onChange: (value: Int) -> Unit
) {
    val minValue = 1f
    val maxValue = 10f

    var _value by remember(value) { mutableFloatStateOf(value.toFloat()) }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (!isEditing) {
            _value = value.toFloat()
        }
    }

    val getCorrectValue = remember { {
            value: Float ->
        value.coerceIn(minValue, maxValue)
    } }

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.gain),
                style = MaterialTheme.typography.labelLargeEmphasized
            )

            Text(
                text = _value.toInt().toString(),
                style = MaterialTheme.typography.labelLargeEmphasized
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedIconButton(
                modifier = Modifier
                    .size(IconButtonDefaults.smallContainerSize(
                        IconButtonDefaults.IconButtonWidthOption.Narrow
                    )),
                border = IconButtonDefaults.outlinedIconButtonVibrantBorder(true),
                colors = IconButtonDefaults.outlinedIconButtonVibrantColors(),
                onClick = {
                    onChange(getCorrectValue(_value - 1).toInt())
                },
            ) {
                Icon(
                    imageVector = Icons.Rounded.Remove,
                    contentDescription = "lower",
                )
            }

            Slider(
                modifier = Modifier.weight(1f),
                value = _value,
                valueRange = 1f..10f,
                onValueChange = {
                    isEditing = true
                    _value = it.roundToInt().toFloat()
                },
                onValueChangeFinished = {
                    isEditing = false
                    onChange(_value.roundToInt())
                }
            )

            OutlinedIconButton(
                modifier = Modifier
                    .size(IconButtonDefaults.smallContainerSize(
                        IconButtonDefaults.IconButtonWidthOption.Narrow
                    )),
                border = IconButtonDefaults.outlinedIconButtonVibrantBorder(true),
                colors = IconButtonDefaults.outlinedIconButtonVibrantColors(),
                onClick = {
                    onChange(getCorrectValue(_value + 1).toInt())
                },
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "higher",
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Preview()
@Composable
fun MainScreenPreview() {
    MainScreenPreview(false)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Preview()
@Composable
fun MainScreenNightPreview() {
    MainScreenPreview(true)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreenPreview(darkTheme: Boolean) {
    DAFTheme(
        dynamicColor = false,
        darkTheme = darkTheme
    ) {
        Box(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            MainScreen(
                MainUiState(),
                { },
            )
        }
    }
}