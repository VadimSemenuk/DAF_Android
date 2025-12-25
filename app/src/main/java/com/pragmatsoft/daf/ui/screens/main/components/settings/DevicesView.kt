package com.pragmatsoft.daf.ui.screens.main.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pragmatsoft.daf.R
import com.pragmatsoft.daf.ui.theme.DAFTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DevicesView(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InputDevicePickerView(
            uiState.inputDeviceOptions,
            uiState.inputDeviceId,
            uiState.autoSelectOption,
            { onAction(SettingsAction.SetInputDeviceId(it)) }
        )

        OutputDevicePickerView(
            uiState.outputDeviceOptions,
            uiState.outputDeviceId,
            uiState.autoSelectOption,
            { onAction(SettingsAction.SetOutputDeviceId(it)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InputDevicePickerView(
    inputDeviceOptions: List<Pair<Int?, String>>,
    inputDeviceId: Int?,
    autoSelectOption: Pair<Int?, String>,
    onChange: (Int?) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val selectedOption = inputDeviceOptions.firstOrNull { it.first == inputDeviceId }
        ?:  autoSelectOption

    ExposedDropdownMenuBox(
        modifier = Modifier.fillMaxWidth(),
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            value = selectedOption.second,
            onValueChange = { },
            readOnly = true,
            label = {
                Text(text = stringResource(R.string.mic))
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            leadingIcon = {
                Icon(imageVector = Icons.Rounded.Mic, contentDescription = "mic")
            },
            shape = MaterialTheme.shapes.large,
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            shape = MenuDefaults.standaloneGroupShape,
        ) {
            val optionCount = inputDeviceOptions.size
            inputDeviceOptions.forEachIndexed { index, option ->
                DropdownMenuItem(
                    shapes = MenuDefaults.itemShape(index, optionCount),
                    text = {
                        Text(text = option.second)
                    },
                    selected = option.first == selectedOption.first,
                    onClick = {
                        isExpanded = false
                        onChange(option.first)
                    },
                    checkedLeadingIcon = {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = null)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
//                    colors = MenuDefaults.selectableItemColors()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OutputDevicePickerView(
    outputDeviceOptions: List<Pair<Int?, String>>,
    outputDeviceId: Int?,
    autoSelectOption: Pair<Int?, String>,
    onChange: (Int?) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val selectedOption = outputDeviceOptions.firstOrNull { it.first == outputDeviceId }
        ?: autoSelectOption

    ExposedDropdownMenuBox(
        modifier = Modifier.fillMaxWidth(),
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            value = selectedOption.second,
            onValueChange = { },
            readOnly = true,
            label = {
                Text(text = stringResource(R.string.output))
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            leadingIcon = {
                Icon(imageVector = Icons.Rounded.Headphones, contentDescription = "output")
            },
            shape = MaterialTheme.shapes.large,
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            shape = MenuDefaults.standaloneGroupShape,
        ) {
            val optionCount = outputDeviceOptions.size
            outputDeviceOptions.forEachIndexed { index, option ->
                DropdownMenuItem(
                    shapes = MenuDefaults.itemShape(index, optionCount),
                    text = { Text(text = option.second) },
                    selected = option.first == selectedOption.first,
                    onClick = {
                        isExpanded = false
                        onChange(option.first)
                    },
                    checkedLeadingIcon = {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = null)
                     },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Preview()
@Composable
fun DropdownMenuItemPreview() {
    DropdownMenuItemPreview(false)
}

@Preview()
@Composable
fun DropdownMenuItemNightPreview() {
    DropdownMenuItemPreview(true)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuItemPreview(darkTheme: Boolean) {
    DAFTheme(
        dynamicColor = false,
        darkTheme = darkTheme
    ) {
        Column {
            DropdownMenuItem(
                shapes = MenuDefaults.itemShape(0, 1),
                text = { Text(text = "Микрофон") },
                selected = false,
                onClick = { },
                checkedLeadingIcon = {
                    Icon(imageVector = Icons.Filled.Check, contentDescription = null)
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
            )

            DropdownMenuItem(
                shapes = MenuDefaults.itemShape(0, 1),
                text = { Text(text = "Микрофон") },
                selected = true,
                onClick = { },
                checkedLeadingIcon = {
                    Icon(imageVector = Icons.Filled.Check, contentDescription = null)
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
            )
        }
    }
}