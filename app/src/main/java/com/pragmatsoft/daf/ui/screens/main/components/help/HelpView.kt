package com.pragmatsoft.daf.ui.screens.main.components.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.pragmatsoft.daf.R
import com.pragmatsoft.daf.ui.components.EmailLink
import com.pragmatsoft.daf.ui.theme.Blue
import com.pragmatsoft.daf.ui.theme.DAFTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HelpDialog(
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.fillMaxWidth(),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                contentDescription = "help"
            )
        },
        title = {
            Text(text = stringResource(R.string.help_and_support))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.delay),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(text = stringResource(R.string.delay_description))
                }
                Column {
                    Text(
                        text = stringResource(R.string.no_sound),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(text = stringResource(R.string.no_sound_description))
                }
                Column {
                    Text(
                        text = stringResource(R.string.support),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = buildAnnotatedString {
                            withLink(
                                LinkAnnotation.Url(
                                    "https://t.me/VadimSemenyuk/",
                                    TextLinkStyles(style = SpanStyle(color = Blue))
                                )
                            ) {
                                append("t.me/VadimSemenyuk")
                            }
                        },
                    )
                    EmailLink(
                        emailAddress = "mamindeveloper@gmail.com",
                        linkText = "mamindeveloper@gmail.com"
                    )
                }

                HorizontalDivider(thickness = 1.dp)

                Column {
                    Text(
                        text = stringResource(R.string.consultation),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = buildAnnotatedString {
                            withLink(
                                LinkAnnotation.Url(
                                    "https://logoadult.by/",
                                    TextLinkStyles(style = SpanStyle(color = Blue))
                                )
                            ) {
                                append("logoadult.by")
                            }
                        },
                    )
                }
            }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {

        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(R.string.close))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun HelpViewPreview() {
    DAFTheme(dynamicColor = false) {
        HelpDialog(onDismissRequest = { })
    }
}