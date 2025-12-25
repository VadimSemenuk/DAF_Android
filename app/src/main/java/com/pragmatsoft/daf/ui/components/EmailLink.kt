package com.pragmatsoft.daf.ui.components

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.pragmatsoft.daf.ui.theme.Blue

@Composable
fun EmailLink(
    modifier: Modifier = Modifier,
    emailAddress: String,
    linkText: String,
) {
    val context = LocalContext.current

    Text(
        text = linkText,
        modifier = modifier
            .clickable {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:$emailAddress".toUri()
                }
                context.startActivity(intent)
            },
        color = Blue
    )
}