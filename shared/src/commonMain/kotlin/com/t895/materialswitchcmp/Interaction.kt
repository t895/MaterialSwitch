package com.t895.materialswitchcmp

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.geometry.Offset

fun MutableInteractionSource.press(offset: Offset = Offset.Unspecified) =
    tryEmit(PressInteraction.Press(offset))

fun MutableInteractionSource.pressRelease(offset: Offset = Offset.Unspecified) =
    tryEmit(PressInteraction.Release(PressInteraction.Press(offset)))

fun MutableInteractionSource.pressCancel(offset: Offset = Offset.Unspecified) =
    tryEmit(PressInteraction.Cancel(PressInteraction.Press(offset)))

fun MutableInteractionSource.dragStart() = tryEmit(DragInteraction.Start())

fun MutableInteractionSource.dragEnd() =
    tryEmit(DragInteraction.Stop(DragInteraction.Start()))

fun MutableInteractionSource.dragCancel() =
    tryEmit(DragInteraction.Cancel(DragInteraction.Start()))
