package com.pragmatsoft.daf.ui.screens.main.components.record

import android.content.Context
import androidx.annotation.IntRange
import androidx.annotation.OptIn
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.state.ProgressStateWithTickInterval
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberProgressStateWithTickInterval
import com.pragmatsoft.daf.ui.theme.DAFTheme
import kotlinx.coroutines.flow.filter

@Composable
fun rememberAudioPlayer(context: Context, url: String): ExoPlayer {
    val player = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(url))
                prepare()
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            player.stop()
            player.release()
        }
    }

    return player
}

@OptIn(UnstableApi::class)
@Composable
fun AudioPlayerView(
    url: String,
) {
    val context = LocalContext.current

    val player = rememberAudioPlayer(context, url)
    val playPauseButtonState = rememberPlayPauseButtonState(player)
    val progressState = rememberProgressStateWithTickInterval(player, 500)
    val isEnded = player.playbackState == Player.STATE_ENDED

    AudioPlayerView(
        isEnabled = playPauseButtonState.isEnabled,
        isPlaying = !playPauseButtonState.showPlay,
        currentPositionMs = progressState.currentPositionMs,
        duration = progressState.durationMs,
        isEnded = isEnded,
        onStartStop = playPauseButtonState::onClick,
        onSeekTo = player::seekTo
    )
}

@kotlin.OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AudioPlayerView(
    isEnabled: Boolean,
    isPlaying: Boolean,
    currentPositionMs: Long,
    duration: Long,
    isEnded: Boolean,
    onStartStop: () -> Unit,
    onSeekTo: (Long) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Progress(
            value = if (isEnded) duration.toFloat() else currentPositionMs.toFloat(),
            maxValue = duration.toFloat(),
            isActive = isPlaying,
            onChange = { onSeekTo(it.toLong()) }
        )

        FilledIconToggleButton(
            modifier = Modifier.minimumInteractiveComponentSize()
                .size(
                    IconButtonDefaults.largeContainerSize(
                        IconButtonDefaults.IconButtonWidthOption.Wide
                    )
                ),
            onCheckedChange = { onStartStop() },
            checked = isPlaying,
            enabled = isEnabled,
            shapes = IconButtonDefaults.toggleableShapes(),
            colors = IconButtonDefaults.filledIconToggleButtonColors()
        ) {
            if (isPlaying) {
                Icon(Icons.Rounded.Pause, contentDescription = "pause")
            } else {
                Icon(Icons.Rounded.PlayArrow, contentDescription = "play")
            }
        }
    }
}

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Progress(
    value: Float,
    maxValue: Float,
    isActive: Boolean,
    onChange: (Float) -> Unit
) {
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubValue by remember { mutableFloatStateOf(0f) }
    val actualValue = if (isScrubbing) scrubValue else value

    val duration = maxValue.coerceAtLeast(0f)

    val interactionSource = remember { MutableInteractionSource() }

    Slider(
        value = actualValue,
        valueRange = 0f..duration,
        onValueChange = {
            scrubValue = it
            isScrubbing = true
        },
        onValueChangeFinished = {
            onChange(scrubValue)
            isScrubbing = false
        },
        thumb = { null },
        track = { sliderState ->
            SliderDefaults.Track(
                modifier = Modifier.height(4.dp),
                sliderState = sliderState,
                thumbTrackGapSize = 2.dp,
                drawStopIndicator = null
            )
        },
        interactionSource = interactionSource
    )

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(formatTime(actualValue))
        Text(formatTime(duration))
    }
}

fun formatTime(ms: Float): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Preview(showBackground = true)
@Composable
fun AudioPlayerViewPreview() {
    DAFTheme(dynamicColor = false) {
        AudioPlayerView(
            isEnabled = true,
            isPlaying = false,
            currentPositionMs = 500L,
            duration = 1000L,
            isEnded = false,
            onStartStop = { },
            onSeekTo = { }
        )
    }
}