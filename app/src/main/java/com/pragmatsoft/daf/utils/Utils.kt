package com.pragmatsoft.daf.utils

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun animateShapeAsState(shape: CornerBasedShape): CornerBasedShape {
    val density = LocalDensity.current

    val topStart by animateDpAsState(
        shape.topStart.toDp(density),
        motionScheme.fastSpatialSpec()
    )
    val topEnd by animateDpAsState(
        shape.topEnd.toDp(density),
        motionScheme.fastSpatialSpec()
    )
    val bottomStart by animateDpAsState(
        shape.bottomStart.toDp(density),
        motionScheme.fastSpatialSpec()
    )
    val bottomEnd by animateDpAsState(
        shape.bottomEnd.toDp(density),
        motionScheme.fastSpatialSpec()
    )

    return RoundedCornerShape(
        topStart = topStart.coerceAtLeast(0.dp),
        topEnd = topEnd.coerceAtLeast(0.dp),
        bottomStart = bottomStart.coerceAtLeast(0.dp),
        bottomEnd = bottomEnd.coerceAtLeast(0.dp),
    )
}

fun <T1, T2, T3, T4, T5, T6, R> combine(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    transform: suspend (T1, T2, T3, T4, T5, T6) -> R
): Flow<R> = combine(
    combine(flow1, flow2, flow3) { a, b, c -> Triple(a, b, c) },
    combine(flow4, flow5, flow6) { d, e, f -> Triple(d, e, f) }
) { first, second ->
    transform(first.first, first.second, first.third, second.first, second.second, second.third)
}

//public fun <T1, T2, T3, T4, T5, R> combine(
//    flow: Flow<T1>,
//    flow2: Flow<T2>,
//    flow3: Flow<T3>,
//    flow4: Flow<T4>,
//    flow5: Flow<T5>,
//    transform: suspend (T1, T2, T3, T4, T5) -> R
//): Flow<R> = combineUnsafe(flow, flow2, flow3, flow4, flow5) { args: Array<*> ->
//    transform(
//        args[0] as T1,
//        args[1] as T2,
//        args[2] as T3,
//        args[3] as T4,
//        args[4] as T5
//    )
//}