package com.pragmatsoft.daf.utils

import android.Manifest
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.pragmatsoft.daf.R

// region mic
@Composable
fun MicPermissionRationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(Icons.Rounded.Info, contentDescription = "warning")
        },
        title = {
            Text(text = stringResource(R.string.give_mic_permission))
        },
        text = {
            Text(text = stringResource(R.string.give_mic_permission_description))
        },
        onDismissRequest = {
            onDismiss()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                }
            ) {
                Text(text = stringResource(R.string.go_to_settings))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun rememberMicPermissionHandler(onGranted: (() -> Unit)? = null): Pair<Boolean, () -> Unit> {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    var isPermissionGranted by remember { mutableStateOf(false) }
    val isNotGrantedDialogOpen = remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            if (it) {
                onGranted?.invoke()
            } else {
                isNotGrantedDialogOpen.value = true
            }
        }
    )

    if (isNotGrantedDialogOpen.value) {
        MicPermissionRationaleDialog(
            onConfirm = {
                isNotGrantedDialogOpen.value = false

                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivity(intent)
            },
            onDismiss = {
                isNotGrantedDialogOpen.value = false
            }
        )
    }

    LifecycleResumeEffect(Unit) {
        isPermissionGranted = if (isPreview) {
            false
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PermissionChecker.PERMISSION_GRANTED
        }

        onPauseOrDispose { }
    }

    return Pair(
        isPermissionGranted,
        {
            if (isPermissionGranted) {
                onGranted?.invoke()
            } else {
                launcher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    )
}
// endregion

// region notification
@Composable
fun NotificationPermissionRationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(Icons.Rounded.Info, contentDescription = "warning")
        },
        title = {
            Text(text = stringResource(R.string.allow_notifications))
        },
        text = {
            Text(
                text = stringResource(R.string.allow_notifications_description)
            )
        },
        onDismissRequest = {
            onDismiss()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                }
            ) {
                Text(text = stringResource(R.string.accept))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun rememberNotificationPermissionHandler(onGranted: (() -> Unit)? = null): Pair<Boolean, () -> Unit> {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    var isPermissionGranted by remember { mutableStateOf(false) }
    val isNotGrantedDialogOpen = remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            if (it) {
                onGranted?.invoke()
            } else {
                isNotGrantedDialogOpen.value = true
            }
        }
    )

    if (isNotGrantedDialogOpen.value) {
        NotificationPermissionRationaleDialog(
            onConfirm = {
                isNotGrantedDialogOpen.value = false

                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                context.startActivity(intent)
            },
            onDismiss = {
                isNotGrantedDialogOpen.value = false
            }
        )
    }

    LifecycleResumeEffect(Unit) {
        isPermissionGranted = if (isPreview) {
            false
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PermissionChecker.PERMISSION_GRANTED
        }

        onPauseOrDispose { }
    }

    return Pair(
        isPermissionGranted,
        {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    )
}
// endregion

// region battery
@Composable
fun BatteryPermissionRationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(Icons.Rounded.Info, contentDescription = "warning")
        },
        title = {
            Text(text = stringResource(R.string.cancel_battery_optimization))
        },
        text = {
            Text(text = stringResource(R.string.cancel_battery_optimization_description))
        },
        onDismissRequest = {
            onDismiss()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                }
            ) {
                Text(text = stringResource(R.string.accept))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun rememberBatteryPermissionHandler(): Pair<Boolean, () -> Unit> {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    val isDialogOpen = remember { mutableStateOf(false) }
    if (isDialogOpen.value) {
        BatteryPermissionRationaleDialog(
            onConfirm = {
                isDialogOpen.value = false
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:${context.requireActivity().packageName}")
                context.requireActivity().startActivity(intent)
            },
            onDismiss = {
                isDialogOpen.value = false
            }
        )
    }

    var isPermissionGranted by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        isPermissionGranted = if (isPreview) {
            false
        } else {
            val pm = context.getSystemService(POWER_SERVICE) as PowerManager
            pm.isIgnoringBatteryOptimizations(context.packageName)
        }

        onPauseOrDispose { }
    }

    return Pair(
        isPermissionGranted,
        { isDialogOpen.value = true }
    )
}
// endregion