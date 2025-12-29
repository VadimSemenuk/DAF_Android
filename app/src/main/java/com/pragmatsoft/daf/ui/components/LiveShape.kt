package com.pragmatsoft.daf.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.util.fastCoerceIn
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import com.pragmatsoft.daf.ui.theme.Yellow

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun _LiveShape(trigger: Boolean) {

    val isInitialized = remember { mutableStateOf(false) }

    val morphIndex = remember { mutableStateOf(0) }

    val indicatorPolygons = listOf(MaterialShapes.Pill, MaterialShapes.Cookie4Sided, MaterialShapes.Pill)
//    val indicatorPolygons = listOf(MaterialShapes.Pill, MaterialShapes.Slanted, MaterialShapes.Arch, MaterialShapes.Pentagon, MaterialShapes.Cookie4Sided, MaterialShapes.Pill)
    val morphSequence = remember(indicatorPolygons) {
        morphSequence(polygons = indicatorPolygons)
    }
    val morph = morphSequence[morphIndex.value]

    val target = remember { mutableStateOf(0f) }

    val animatedProgress by animateFloatAsState(
        targetValue = target.value,
    )
    val progress = if (target.value == 0f) 0f else animatedProgress + 1 - target.value

    val path = remember { Path() }

    LaunchedEffect(trigger) {
        if (!isInitialized.value) {
            isInitialized.value = true
            return@LaunchedEffect
        }

        target.value += 1

        if (target.value > 1) {
            var nextMorphIndex = morphIndex.value + 1
            if (nextMorphIndex >= morphSequence.size) nextMorphIndex = 0
            morphIndex.value = nextMorphIndex
        }
    }

    Spacer(
        Modifier
            .aspectRatio(ratio = 1f, matchHeightConstraintsFirst = true)
            .drawWithContent {
                drawPath(
                    path =
                        processPath(
                            path =
                                morph.toPath(
                                    progress = progress,
                                    path = path,
                                    startAngle = 0,
                                ),
                            size = size,
                        ),
                    color = Yellow,
                    style = Fill,
                )
            }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LiveShape(progress: Float) {
    val indicatorPolygons = listOf(MaterialShapes.Pill, MaterialShapes.Ghostish, MaterialShapes.Slanted, MaterialShapes.Gem, MaterialShapes.Cookie4Sided)

//    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessVeryLow,
                visibilityThreshold = 1 / 1000f,
            ),
    )

    val coercedProgress = { animatedProgress.fastCoerceIn(0f, 1f) }

    val morphSequence = remember(indicatorPolygons) {
        morphSequence(polygons = indicatorPolygons)
    }

    val progressValue = coercedProgress()
    val activeMorphIndex =
        (morphSequence.size * progressValue)
            .toInt()
            .coerceAtMost(morphSequence.size - 1)

    val adjustedProgressValue =
        if (progressValue == 1f && activeMorphIndex == morphSequence.size - 1) {
            1f
        } else {
            (progressValue * morphSequence.size) % 1f
        }

    val path = remember { Path() }

    Spacer(
        Modifier
            .aspectRatio(ratio = 1f, matchHeightConstraintsFirst = true)
            .drawWithContent {
                drawPath(
                    path =
                        processPath(
                            path =
                                morphSequence[activeMorphIndex].toPath(
                                    // Use the adjusted progress.
                                    progress = adjustedProgressValue,
                                    path = path,
                                    startAngle = 0,
                                ),
                            size = size,
//                                    scaleFactor = morphScaleFactor,
//                                    scaleMatrix = scaleMatrix,
                        ),
                    color = Yellow,
                    style = Fill,
                )
            }
    )
}

private fun morphSequence(polygons: List<RoundedPolygon>): List<Morph> {
    return buildList {
        for (i in polygons.indices) {
            if (i + 1 < polygons.size) {
                add(Morph(polygons[i].normalized(), polygons[i + 1].normalized()))
            }
        }
    }
}

private fun processPath(
    path: Path,
    size: Size,
    scaleMatrix: Matrix = Matrix(),
): Path {
    scaleMatrix.apply { scale(x = size.width, y = size.height) }
    path.transform(scaleMatrix)
    return path
}