package com.pragmatsoft.daf.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun Context.requireActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw Error("activity not found")
}

fun ShortArray.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(size * 2).order(ByteOrder.LITTLE_ENDIAN)
    for (s in this) buffer.putShort(s)
    return buffer.array()
}

fun CornerSize.toDp(density: Density): Dp {
    return with(density) { toPx(Size.Unspecified, this).toDp() }
}