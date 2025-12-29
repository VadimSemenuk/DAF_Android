package com.pragmatsoft.daf.ui.screens.main.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pragmatsoft.daf.R
import com.pragmatsoft.daf.ui.theme.DAFTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsModalBottomSheet(
    viewModel: SettingsViewModel = viewModel(),
    onCloseRequest: () -> Unit
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onCloseRequest,
        sheetState = bottomSheetState
    ) {
        SettingsView(
            uiState = uiState,
            onAction = viewModel::onAction
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsView(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
    ) {
        Text(
            modifier = Modifier.padding(bottom = 30.dp),
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineSmall
        )

        DevicesView(
            uiState = uiState,
            onAction = onAction
        )
    }
}

@Composable
fun NoiseCancellationView(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.noise_cancellation)
        )

        Switch(
            checked = uiState.isNoiseCancellationOn,
            onCheckedChange = {
                onAction(SettingsAction.SetIsNoiseCancellationOn(it))
            }
        )
    }
}

@Preview()
@Composable
fun SettingsViewPreview() {
    SettingsViewPreview(false)
}

@Preview()
@Composable
fun SettingsViewNightPreview() {
    SettingsViewPreview(true)
}

@Composable
fun SettingsViewPreview(darkTheme: Boolean) {
    DAFTheme(
        dynamicColor = false,
        darkTheme = darkTheme
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
        ) {
            SettingsView(
                uiState = SettingsUiState(),
                onAction = { }
            )
        }
    }
}