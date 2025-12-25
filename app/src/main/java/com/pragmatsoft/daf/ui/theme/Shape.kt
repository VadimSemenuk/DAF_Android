package com.pragmatsoft.daf.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.ui.unit.dp

object ShapeDefaults {
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    val topListItemShape: RoundedCornerShape
        @Composable get() =
            RoundedCornerShape(
                topStart = shapes.largeIncreased.topStart,
                topEnd = shapes.largeIncreased.topEnd,
//                bottomStart = shapes.small.bottomStart,
//                bottomEnd = shapes.small.bottomEnd,
                bottomStart = CornerSize(0.dp),
                bottomEnd = CornerSize(0.dp),
            )

//    val middleListItemShape: RoundedCornerShape
//        @Composable get() = RoundedCornerShape(shapes.small.topStart)

    val middleListItemShape: RoundedCornerShape
        @Composable get() = RoundedCornerShape(CornerSize(0.dp))

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    val bottomListItemShape: RoundedCornerShape
        @Composable get() =
            RoundedCornerShape(
                topStart = CornerSize(0.dp),
                topEnd = CornerSize(0.dp),
//                topStart = shapes.small.topStart,
//                topEnd = shapes.small.topEnd,
                bottomStart = shapes.largeIncreased.bottomStart,
                bottomEnd = shapes.largeIncreased.bottomEnd
            )

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    val cardShape: CornerBasedShape
        @Composable get() = shapes.largeIncreased
}