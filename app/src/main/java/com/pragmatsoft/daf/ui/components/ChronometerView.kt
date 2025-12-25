package com.pragmatsoft.daf.ui.components

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChronometerView(
    isActive: Boolean,
    baseTime: Long?,
    content: @Composable() (elapsedTime: String) -> Unit
) {
    var elapsedTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isActive) {
        val baseTime = baseTime ?: System.currentTimeMillis()

        if (isActive) {
            while (currentCoroutineContext().isActive) {
                elapsedTime = System.currentTimeMillis() - baseTime
                delay(500)
            }
        } else {
            elapsedTime = 0
        }
    }

    content(formatElapsed(elapsedTime))
}

private fun formatElapsed(ms: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(hours)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms))

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}