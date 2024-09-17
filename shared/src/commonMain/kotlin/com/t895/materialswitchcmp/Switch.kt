package com.t895.materialswitchcmp

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

val MaterialSwitchWidth = 52.dp
val MaterialSwitchHeight = 32.dp
val MaterialSwitchOutlineWidth = 2.dp
val MaterialSwitchHandleEnabledWidth = 24.dp
val MaterialSwitchHandleDisabledWidth = 16.dp
val MaterialSwitchHandlePressedWidth = 28.dp
val MaterialSwitchCornerSize = 50.dp
val MaterialSwitchStateLayerWidth = 40.dp

enum class SwitchState { Pressed, Idle }

@Composable
fun Modifier.swipeable(
    onCheckedChanged: ((Boolean) -> Unit)? = null,
    enabled: Boolean = true,
    checkedState: MutableState<Boolean> = remember { mutableStateOf(true) },
    switchState: MutableState<SwitchState> = remember { mutableStateOf(SwitchState.Idle) },
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) = composed {
    var width = 0f
    var enabledState by remember { mutableStateOf(enabled) }
    enabledState = enabled
    return@composed this
        .pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown()
                if (!enabledState) {
                    return@awaitEachGesture
                }
                down.consume()

                switchState.value = SwitchState.Pressed
                interactionSource.press(currentEvent.changes.first().position)

                val upOrCancel = waitForUpOrCancellation()
                switchState.value = SwitchState.Idle

                if (upOrCancel != null) {
                    upOrCancel.consume()
                    interactionSource.pressRelease(currentEvent.changes.first().position)
                    checkedState.value = !checkedState.value
                    onCheckedChanged?.invoke(checkedState.value)
                } else {
                    interactionSource.pressCancel(currentEvent.changes.first().position)
                }
            }
        }
        .pointerInput(Unit) {
            detectDragGestures(
                onDragEnd = {
                    if (!enabledState) {
                        return@detectDragGestures
                    }

                    switchState.value = SwitchState.Idle
                    interactionSource.dragEnd()
                },
                onDragCancel = {
                    interactionSource.dragCancel()
                },
            ) { change, _ ->
                if (!enabledState) {
                    return@detectDragGestures
                }

                switchState.value = SwitchState.Pressed
                interactionSource.dragStart()
                if (checkedState.value) {
                    if (change.position.x < width / 2.5f) {
                        checkedState.value = false
                        onCheckedChanged?.invoke(checkedState.value)
                    }
                } else {
                    if (change.position.x > width / 1.5f) {
                        checkedState.value = true
                        onCheckedChanged?.invoke(checkedState.value)
                    }
                }
            }
        }
        .graphicsLayer { width = size.width }
}

@Composable
fun MaterialSwitch(
    checked: Boolean = true,
    enabled: Boolean = true,
    onCheckedChanged: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val checkedState = remember { mutableStateOf(checked) }
    val switchState = remember { mutableStateOf(SwitchState.Idle) }
    Box(
        modifier
            .wrapContentSize()
            .requiredSize(height = MaterialSwitchHeight, width = MaterialSwitchWidth)
            .swipeable(
                onCheckedChanged = onCheckedChanged,
                enabled = enabled,
                checkedState = checkedState,
                switchState = switchState,
                interactionSource = interactionSource,
            )
    ) {
        val colorScheme = MaterialTheme.colorScheme

        val handlePaddingStart = MaterialSwitchHeight / 8
        val handlePathLength =
            MaterialSwitchWidth - MaterialSwitchHandleEnabledWidth - handlePaddingStart
        val handleXPosition by animateDpAsState(
            targetValue = if (checkedState.value) {
                handlePathLength
            } else {
                handlePaddingStart
            },
        )
        val handlePositionPercent = (handleXPosition - handlePaddingStart) /
                (handlePathLength - handlePaddingStart)

        val uncheckedTrackBodyColor by animateColorAsState(
            targetValue = if (enabled) {
                colorScheme.onSurface
            } else {
                colorScheme.surfaceContainerHighest
            }.copy(alpha = 0.12f)
        )
        val outlineColor by animateColorAsState(
            targetValue = if (enabled) {
                colorScheme.outline
            } else {
                colorScheme.outline.copy(alpha = 0.12f)
            }
        )
        Box(
            Modifier
                .fillMaxSize()
                .alpha(1f - handlePositionPercent)
                .border(
                    width = MaterialSwitchOutlineWidth,
                    color = outlineColor,
                    shape = RoundedCornerShape(MaterialSwitchCornerSize),
                )
                .drawBehind {
                    val cornerRadius = CornerRadius(MaterialSwitchCornerSize.toPx())
                    drawRoundRect(
                        color = uncheckedTrackBodyColor,
                        cornerRadius = cornerRadius,
                    )
                }
        )

        val selectedTrackBodyColor by animateColorAsState(
            targetValue = if (enabled) {
                colorScheme.primary
            } else {
                colorScheme.onSurface.copy(alpha = 0.12f)
            }
        )
        val selectedTrackBodyColorLerp = lerp(
            start = Color.Transparent,
            stop = selectedTrackBodyColor,
            fraction = handlePositionPercent,
        )
        Box(
            Modifier
                .fillMaxSize()
                .drawBehind {
                    val cornerRadius = CornerRadius(MaterialSwitchCornerSize.toPx())
                    drawRoundRect(
                        color = selectedTrackBodyColorLerp,
                        cornerRadius = cornerRadius,
                    )
                }
        )

        val checkedHandleColor = colorScheme.onPrimary
        val uncheckedHandleColor = colorScheme.outline
        val checkedPressedHandleColor = colorScheme.primaryContainer
        val uncheckedPressedHandleColor = colorScheme.onSurfaceVariant
        val handleColor by animateColorAsState(
            targetValue = when (switchState.value) {
                SwitchState.Pressed -> if (checkedState.value) {
                    checkedPressedHandleColor
                } else {
                    uncheckedPressedHandleColor
                }

                SwitchState.Idle -> if (checkedState.value) {
                    if (enabled) {
                        checkedHandleColor
                    } else {
                        colorScheme.surface.copy(alpha = 0.38f)
                    }
                } else {
                    if (enabled) {
                        uncheckedHandleColor
                    } else {
                        colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                }
            }
        )
        val handleWidth by animateDpAsState(
            targetValue = when (switchState.value) {
                SwitchState.Pressed -> MaterialSwitchHandlePressedWidth
                SwitchState.Idle -> if (checkedState.value) {
                    MaterialSwitchHandleEnabledWidth
                } else {
                    MaterialSwitchHandleDisabledWidth
                }
            },
        )
        Box(
            Modifier
                .requiredSize(MaterialSwitchHandleEnabledWidth)
                .offset {
                    IntOffset(handleXPosition.roundToPx(), handlePaddingStart.roundToPx())
                }
                .drawBehind {
                    drawCircle(
                        color = handleColor,
                        radius = handleWidth.toPx() / 2,
                    )
                }
                .indication(
                    interactionSource = interactionSource,
                    indication = ripple(
                        bounded = false,
                        radius = MaterialSwitchStateLayerWidth / 2,
                    ),
                )
        )
    }
}
