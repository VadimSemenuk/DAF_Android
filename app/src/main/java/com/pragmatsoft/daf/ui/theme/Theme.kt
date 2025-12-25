package com.pragmatsoft.daf.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Blue,
    onPrimary = White,

    secondary = Blue,
    secondaryContainer = BlueLight_Night,
    onSecondaryContainer = BlueLight,

    tertiaryContainer = Yellow,
    onTertiaryContainer = YellowDark,

    background = BlueSuperDark_Night,

    surface = BlueDark_Night,
    surfaceContainerLow = BlueSuperDark_Night,
    surfaceContainer = BlueLight_Night,
    surfaceContainerHigh = BlueSuperDark_Night,
    surfaceContainerHighest = BlueDark_Night,
    onSurfaceVariant = BlueLight,

    outlineVariant = BlueLight
)

private val LightColorScheme = lightColorScheme(
    primary = Blue,

    secondary = Blue,
    secondaryContainer = BlueLight,
    onSecondaryContainer = BlueDark,

    tertiaryContainer = Yellow,
    onTertiaryContainer = YellowDark,

    background = BlueSuperLight,

    surface = White,
    surfaceContainerLow = BlueSuperLight,
    surfaceContainer = BlueLight,
    surfaceContainerHigh = BlueSuperLight,
    surfaceContainerHighest = White,
    onSurfaceVariant = BlueDark,

    outlineVariant = BlueLight
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DAFTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}