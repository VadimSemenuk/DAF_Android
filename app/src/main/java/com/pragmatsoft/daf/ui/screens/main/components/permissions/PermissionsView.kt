package com.pragmatsoft.daf.ui.screens.main.components.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.pragmatsoft.daf.R
import com.pragmatsoft.daf.ui.theme.DAFTheme
import com.pragmatsoft.daf.utils.rememberBatteryPermissionHandler
import com.pragmatsoft.daf.utils.rememberMicPermissionHandler
import com.pragmatsoft.daf.utils.rememberNotificationPermissionHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsModalBottomSheet(onCloseRequest: () -> Unit) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onCloseRequest,
        sheetState = bottomSheetState
    ) {
        PermissionsView(onAllPermissionsGranted = onCloseRequest)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PermissionsView(onAllPermissionsGranted: () -> Unit) {
    val (isMicPermissionGranted, requestMicPermission) = rememberMicPermissionHandler { }
    val (isNotificationPermissionGranted, requestNotificationPermission) = rememberNotificationPermissionHandler()
    val (isBatteryPermissionGranted, requestBatteryPermission) = rememberBatteryPermissionHandler()

    LifecycleResumeEffect(
        isMicPermissionGranted,
        isNotificationPermissionGranted,
        isBatteryPermissionGranted
        ) {
        if (isMicPermissionGranted
            && isNotificationPermissionGranted
            && isBatteryPermissionGranted) {
            onAllPermissionsGranted()
        }

        onPauseOrDispose { }
    }

    val items = listOf(
        Triple(
            isMicPermissionGranted,
            stringResource(R.string.mic_using),
            requestMicPermission
        ),
        Triple(
            isNotificationPermissionGranted,
            stringResource(R.string.notification_show),
            requestNotificationPermission
        ),
        Triple(
            isBatteryPermissionGranted,
            stringResource(R.string.battery_optimization),
            requestBatteryPermission
        )
    )
        .filter { !it.first }

    Column(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Text(
            modifier = Modifier.padding(bottom = 30.dp),
            text = stringResource(R.string.permissions),
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            modifier = Modifier.padding(bottom = 10.dp),
            text = stringResource(R.string.permissions_description),
        )

        Card(
            shape = shapes.largeIncreased,
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent,
            ),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items.mapIndexed { index, item ->
                    ListItem(
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "go"
                            )
                        },
                        content = { Text(text = item.second) },
                        onClick = { item.third() },
                    )
                }
            }
        }
    }
}

@Preview()
@Composable
fun PermissionsViewPreview() {
    PermissionsViewPreview(false)
}

@Preview()
@Composable
fun PermissionsViewNightPreview() {
    PermissionsViewPreview(true)
}

@Composable
fun PermissionsViewPreview(darkTheme: Boolean) {
    DAFTheme(
        dynamicColor = false,
        darkTheme = darkTheme
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
        ) {
            PermissionsView(onAllPermissionsGranted = { })
        }
    }
}