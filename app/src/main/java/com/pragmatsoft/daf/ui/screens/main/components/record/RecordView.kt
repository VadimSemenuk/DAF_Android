package com.pragmatsoft.daf.ui.screens.main.components.record

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.pragmatsoft.daf.R
import com.pragmatsoft.daf.ui.theme.DAFTheme
import com.pragmatsoft.daf.ui.theme.GreenDark
import com.pragmatsoft.daf.ui.theme.GreenLight
import com.pragmatsoft.daf.ui.theme.ShapeDefaults.cardShape
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordModalBottomSheet(
    filePath: String,
    onCloseRequest: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val deleteFile = {
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            deleteFile()
            onCloseRequest()
        },
        sheetState = bottomSheetState
    ) {
        RecordView(filePath = filePath)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RecordView(filePath: String) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        File(filePath)
                            .inputStream()
                            .use { input ->
                                input.copyTo(output)
                            }
                    }
                }
            }
        }
    )

    val onSave = {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/mp4"
            putExtra(Intent.EXTRA_TITLE, "recording.m4a")
        }
        saveLauncher.launch(intent)
    }

    val onShare = {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            File(filePath)
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)))
    }

    Column(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.record),
                style = MaterialTheme.typography.headlineSmall
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalIconButton(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(IconButtonDefaults.smallContainerSize(
                            IconButtonDefaults.IconButtonWidthOption.Wide
                        )),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = GreenLight,
                        contentColor = GreenDark
                    ),
                    onClick = onSave
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Save,
                        contentDescription = "save",
                    )
                }

                FilledTonalIconButton(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(IconButtonDefaults.smallContainerSize(
                            IconButtonDefaults.IconButtonWidthOption.Wide
                        )),
                    onClick = onShare
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "share",
                    )
                }
            }
        }

        Card(shape = cardShape) {
            Box(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                if (isPreview) {
                    AudioPlayerViewPreview()
                } else {
                    AudioPlayerView(filePath)
                }
            }
        }
    }
}

@Preview()
@Composable
fun RecordViewPreview() {
    RecordViewPreview(false)
}

@Preview()
@Composable
fun RecordViewNightPreview() {
    RecordViewPreview(true)
}

@Composable
fun RecordViewPreview(darkTheme: Boolean) {
    DAFTheme(
        dynamicColor = false,
        darkTheme = darkTheme
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
        ) {
            RecordView(filePath = "")
        }
    }
}