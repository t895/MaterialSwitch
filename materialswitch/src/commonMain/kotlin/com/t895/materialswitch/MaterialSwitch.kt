/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.t895.materialswitch

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

private fun lerp(start: Color, end: Color, fraction: Float): Color {
    return Color(
        alpha = lerp(start.alpha, end.alpha, fraction),
        red = lerp(start.red, end.red, fraction),
        green = lerp(start.green, end.green, fraction),
        blue = lerp(start.blue, end.blue, fraction),
    )
}

internal val SwitchCornerSize = 128.dp
internal val SwitchWidth = 52.dp
internal val SwitchHeight = 32.dp
internal val SwitchOutlineWidth = 2.dp
internal val SwitchThumbPressedSize = 28.dp
internal val SwitchThumbPressedRadius = SwitchThumbPressedSize / 2
internal val SwitchThumbCheckedSize = 24.dp
internal val SwitchThumbCheckedRadius = SwitchThumbCheckedSize / 2
internal val SwitchThumbUncheckedSize = 16.dp
internal val SwitchThumbUncheckedRadius = SwitchThumbUncheckedSize / 2
internal val SwitchStateLayerSize = 40.dp

/**
 * <a href="https://m3.material.io/components/switch" class="external" target="_blank">Material
 * Design Switch</a>.
 *
 * Switches toggle the state of a single item on or off.
 *
 * @param checked whether or not this switch is checked
 * @param onCheckedChange called when this switch is clicked. If `null`, then this switch will not
 *   be interactable, unless something else handles its input events and updates its state.
 * @param modifier the [Modifier] to be applied to this switch
 * @param thumbContent content that will be drawn inside the thumb, expected to measure
 *   [SwitchDefaults.IconSize]. The parameter "mostlyEnabled" is a hint to the user to indicate
 *   when to change the thumb content between a checked and unchecked state.
 * @param enabled controls the enabled state of this switch. When `false`, this component will not
 *   respond to user input, and it will appear visually disabled and disabled to accessibility
 *   services.
 * @param colors [MaterialSwitchColors] that will be used to resolve the colors used for this switch in
 *   different states. See [SwitchDefaults.colors].
 * @param interactionSource an optional hoisted [MutableInteractionSource] for observing and
 *   emitting [Interaction]s for this switch. You can use this to change the switch's appearance or
 *   preview the switch in different states. Note that if `null` is provided, interactions will
 *   still happen internally.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MaterialSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    thumbContent: (@Composable (mostlyEnabled: Boolean) -> Unit)? = null,
    enabled: Boolean = true,
    colors: MaterialSwitchColors = MaterialSwitchColors(
        MaterialTheme.colorScheme,
        SwitchDefaults.colors()
    ),
    interactionSource: MutableInteractionSource? = null,
) {
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    val thumbMinPositionXPx = remember {
        var position = SwitchOutlineWidth
        if (layoutDirection == LayoutDirection.Rtl) {
            position *= -1
        }

        density.density * position.value
    }
    val thumbMaxPositionXPx = remember {
        var position = SwitchWidth / 2 - SwitchOutlineWidth * 2
        if (layoutDirection == LayoutDirection.Rtl) {
            position *= -1
        }

        density.density * position.value
    }
    val trackWidthPx = remember { thumbMaxPositionXPx - thumbMinPositionXPx }

    val internalInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by internalInteractionSource.collectIsPressedAsState()
    val isDragging by internalInteractionSource.collectIsDraggedAsState()
    var dragFloat by remember { mutableFloatStateOf(if (checked) 1f else 0f) }
    val onCheckedChangeInternal = { newChecked: Boolean ->
        onCheckedChange?.invoke(newChecked)
        dragFloat = if (newChecked) 1f else 0f
    }

    // This is a little hacky because we can't change the current value at some point
    // during the animation to cancel the animation from 0 -> 1 or vice versa when we've
    // already dragged the thumb to say, 0.65. Here, I just actively animate to the drag
    // position while the drag is happening with an instant animation. Then once the drag
    // finishes, we relinquish control over the thumb to this animation.
    val targetProgressValue = remember(isDragging, dragFloat, checked) {
        if (isDragging) {
            dragFloat
        } else if (checked) {
            1f
        } else {
            0f
        }
    }
    val thumbProgressAnimated by animateFloatAsState(
        targetValue = targetProgressValue,
        animationSpec = if (isDragging) {
            tween(durationMillis = 0)
        } else {
            spring(dampingRatio = Spring.DampingRatioLowBouncy)
        },
    )
    val colorProgressAnimated by animateFloatAsState(
        targetValue = targetProgressValue,
        animationSpec = if (isDragging) {
            tween(durationMillis = 0)
        } else {
            tween(durationMillis = 200)
        },
    )

    val thumbProgress by remember {
        derivedStateOf {
            if (isDragging) {
                dragFloat
            } else {
                thumbProgressAnimated
            }
        }
    }

    val trackColor by remember(colorProgressAnimated, enabled) {
        derivedStateOf {
            lerp(
                start = colors.trackColor(enabled, false),
                end = colors.trackColor(enabled, true),
                fraction = colorProgressAnimated,
            )
        }
    }

    val borderColor by remember(colorProgressAnimated, enabled) {
        derivedStateOf {
            lerp(
                start = colors.borderColor(enabled, false),
                end = colors.borderColor(enabled, true),
                fraction = colorProgressAnimated,
            )
        }
    }

    val interactable = enabled && onCheckedChange != null

    val cornerSizePx = remember { density.density * SwitchCornerSize.value }
    val outlineWidthPx = remember { density.density * SwitchOutlineWidth.value }
    val halfOutlineWidthPx = remember { outlineWidthPx / 2 }
    val cornerRadius = remember { CornerRadius(cornerSizePx, cornerSizePx) }
    val outlineRightBoundPx =
        remember { (density.density * SwitchWidth.value) - halfOutlineWidthPx }
    val outlineBottomBoundPx =
        remember { (density.density * SwitchHeight.value) - halfOutlineWidthPx }
    Box(
        modifier = modifier
            .requiredSize(width = SwitchWidth, height = SwitchHeight)
            .drawBehind {
                drawRoundRect(
                    color = trackColor,
                    cornerRadius = CornerRadius(cornerSizePx),
                )

                val outline = Outline.Rounded(
                    RoundRect(
                        left = halfOutlineWidthPx,
                        top = halfOutlineWidthPx,
                        right = outlineRightBoundPx,
                        bottom = outlineBottomBoundPx,
                        topLeftCornerRadius = cornerRadius,
                        topRightCornerRadius = cornerRadius,
                        bottomLeftCornerRadius = cornerRadius,
                        bottomRightCornerRadius = cornerRadius,
                    )
                )
                drawOutline(
                    outline = outline,
                    color = borderColor,
                    style = Stroke(width = outlineWidthPx)
                )
            }
            .draggable2D(
                state = rememberDraggable2DState { offset ->
                    dragFloat = (dragFloat + (offset.x / trackWidthPx)).coerceIn(0f, 1f)
                },
                enabled = interactable,
                interactionSource = internalInteractionSource,
                onDragStarted = {
                    dragFloat = if (checked) 1f else 0f
                },
                onDragStopped = {
                    if (checked) {
                        if (dragFloat < 0.5f) {
                            onCheckedChangeInternal(false)
                        }
                    } else {
                        if (dragFloat > 0.5f) {
                            onCheckedChangeInternal(true)
                        }
                    }
                },
            )
            .toggleable(
                value = checked,
                interactionSource = internalInteractionSource,
                indication = null,
                enabled = interactable,
                onValueChange = {
                    onCheckedChangeInternal(!checked)
                },
                role = Role.Switch,
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        val thumbPressed by remember { derivedStateOf { isPressed || isDragging } }
        val thumbColor by remember(enabled, thumbPressed, thumbProgress) {
            derivedStateOf {
                lerp(
                    start = colors.thumbColor(enabled, false, thumbPressed),
                    end = colors.thumbColor(enabled, true, thumbPressed),
                    fraction = thumbProgress,
                )
            }
        }

        val thumbPressedRadiusPx = remember { density.density * SwitchThumbPressedRadius.value }
        val thumbCheckedRadiusPx = remember { density.density * SwitchThumbCheckedRadius.value }
        val thumbUncheckedRadiusPx = remember { density.density * SwitchThumbUncheckedRadius.value }
        val thumbRadiusPx by animateFloatAsState(
            targetValue = if (thumbPressed) {
                thumbPressedRadiusPx
            } else if (checked) {
                thumbCheckedRadiusPx
            } else {
                if (thumbContent != null) {
                    thumbCheckedRadiusPx
                } else {
                    thumbUncheckedRadiusPx
                }
            },
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        )

        val thumbPositionXPx by remember {
            derivedStateOf {
                lerp(thumbMinPositionXPx, thumbMaxPositionXPx, thumbProgress)
            }
        }

        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = thumbPositionXPx
                }
                .size(SwitchThumbPressedSize)
                .drawBehind {
                    drawCircle(
                        color = thumbColor,
                        radius = thumbRadiusPx,
                    )
                }
                .indication(
                    interactionSource = internalInteractionSource,
                    indication = ripple(
                        bounded = false,
                        radius = SwitchStateLayerSize / 2,
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier.size(SwitchDefaults.IconSize),
                contentAlignment = Alignment.Center,
            ) {
                if (thumbContent != null) {
                    val iconColor by remember(thumbPressed, enabled) {
                        derivedStateOf {
                            lerp(
                                start = colors.iconColor(enabled, false),
                                end = colors.iconColor(enabled, true),
                                fraction = thumbProgress,
                            )
                        }
                    }
                    CompositionLocalProvider(LocalContentColor provides iconColor) {
                        thumbContent(thumbProgress > 0.5f)
                    }
                }
            }
        }
    }
}

/**
 * Represents the colors used by a [MaterialSwitch] in different states
 *
 * @param checkedThumbColor the color used for the thumb when enabled and checked
 * @param checkedPressedThumbColor the color used for the thumb when checked and pressed
 * @param checkedTrackColor the color used for the track when enabled and checked
 * @param checkedBorderColor the color used for the border when enabled and checked
 * @param checkedIconColor the color used for the icon when enabled and checked
 * @param uncheckedThumbColor the color used for the thumb when enabled and unchecked
 * @param uncheckedPressedThumbColor the color used for the thumb when unchecked and pressed
 * @param uncheckedTrackColor the color used for the track when enabled and unchecked
 * @param uncheckedBorderColor the color used for the border when enabled and unchecked
 * @param uncheckedIconColor the color used for the icon when enabled and unchecked
 * @param disabledCheckedThumbColor the color used for the thumb when disabled and checked
 * @param disabledCheckedTrackColor the color used for the track when disabled and checked
 * @param disabledCheckedBorderColor the color used for the border when disabled and checked
 * @param disabledCheckedIconColor the color used for the icon when disabled and checked
 * @param disabledUncheckedThumbColor the color used for the thumb when disabled and unchecked
 * @param disabledUncheckedTrackColor the color used for the track when disabled and unchecked
 * @param disabledUncheckedBorderColor the color used for the border when disabled and unchecked
 * @param disabledUncheckedIconColor the color used for the icon when disabled and unchecked
 * @constructor create an instance with arbitrary colors. See [SwitchDefaults.colors] for the
 *   default implementation that follows Material specifications.
 */
@Immutable
class MaterialSwitchColors(
    val checkedThumbColor: Color,
    val checkedPressedThumbColor: Color,
    val checkedTrackColor: Color,
    val checkedBorderColor: Color,
    val checkedIconColor: Color,
    val uncheckedThumbColor: Color,
    val uncheckedPressedThumbColor: Color,
    val uncheckedTrackColor: Color,
    val uncheckedBorderColor: Color,
    val uncheckedIconColor: Color,
    val disabledCheckedThumbColor: Color,
    val disabledCheckedTrackColor: Color,
    val disabledCheckedBorderColor: Color,
    val disabledCheckedIconColor: Color,
    val disabledUncheckedThumbColor: Color,
    val disabledUncheckedTrackColor: Color,
    val disabledUncheckedBorderColor: Color,
    val disabledUncheckedIconColor: Color
) {
    constructor(
        colorScheme: ColorScheme,
        colors: androidx.compose.material3.SwitchColors,
    ) : this(
        colors.checkedThumbColor,
        colorScheme.primaryContainer,
        colors.checkedTrackColor,
        colors.checkedBorderColor,
        colors.checkedIconColor,
        colors.uncheckedThumbColor,
        colorScheme.onSurfaceVariant,
        colors.uncheckedTrackColor,
        colors.uncheckedBorderColor,
        colors.uncheckedIconColor,
        colors.disabledCheckedThumbColor,
        colors.disabledCheckedTrackColor,
        colors.disabledCheckedBorderColor,
        colors.disabledCheckedIconColor,
        colors.disabledUncheckedThumbColor,
        colors.disabledUncheckedTrackColor,
        colors.disabledUncheckedBorderColor,
        colors.disabledUncheckedIconColor,
    )

    /**
     * Returns a copy of this SwitchColors, optionally overriding some of the values. This uses the
     * Color.Unspecified to mean “use the value from the source”
     */
    fun copy(
        checkedThumbColor: Color = this.checkedThumbColor,
        checkedPressedThumbColor: Color = this.checkedPressedThumbColor,
        checkedTrackColor: Color = this.checkedTrackColor,
        checkedBorderColor: Color = this.checkedBorderColor,
        checkedIconColor: Color = this.checkedIconColor,
        uncheckedThumbColor: Color = this.uncheckedThumbColor,
        uncheckedPressedThumbColor: Color = this.uncheckedPressedThumbColor,
        uncheckedTrackColor: Color = this.uncheckedTrackColor,
        uncheckedBorderColor: Color = this.uncheckedBorderColor,
        uncheckedIconColor: Color = this.uncheckedIconColor,
        disabledCheckedThumbColor: Color = this.disabledCheckedThumbColor,
        disabledCheckedTrackColor: Color = this.disabledCheckedTrackColor,
        disabledCheckedBorderColor: Color = this.disabledCheckedBorderColor,
        disabledCheckedIconColor: Color = this.disabledCheckedIconColor,
        disabledUncheckedThumbColor: Color = this.disabledUncheckedThumbColor,
        disabledUncheckedTrackColor: Color = this.disabledUncheckedTrackColor,
        disabledUncheckedBorderColor: Color = this.disabledUncheckedBorderColor,
        disabledUncheckedIconColor: Color = this.disabledUncheckedIconColor,
    ) =
        MaterialSwitchColors(
            checkedThumbColor.takeOrElse { this.checkedThumbColor },
            checkedPressedThumbColor.takeOrElse { this.checkedPressedThumbColor },
            checkedTrackColor.takeOrElse { this.checkedTrackColor },
            checkedBorderColor.takeOrElse { this.checkedBorderColor },
            checkedIconColor.takeOrElse { this.checkedIconColor },
            uncheckedThumbColor.takeOrElse { this.uncheckedThumbColor },
            uncheckedPressedThumbColor.takeOrElse { this.uncheckedPressedThumbColor },
            uncheckedTrackColor.takeOrElse { this.uncheckedTrackColor },
            uncheckedBorderColor.takeOrElse { this.uncheckedBorderColor },
            uncheckedIconColor.takeOrElse { this.uncheckedIconColor },
            disabledCheckedThumbColor.takeOrElse { this.disabledCheckedThumbColor },
            disabledCheckedTrackColor.takeOrElse { this.disabledCheckedTrackColor },
            disabledCheckedBorderColor.takeOrElse { this.disabledCheckedBorderColor },
            disabledCheckedIconColor.takeOrElse { this.disabledCheckedIconColor },
            disabledUncheckedThumbColor.takeOrElse { this.disabledUncheckedThumbColor },
            disabledUncheckedTrackColor.takeOrElse { this.disabledUncheckedTrackColor },
            disabledUncheckedBorderColor.takeOrElse { this.disabledUncheckedBorderColor },
            disabledUncheckedIconColor.takeOrElse { this.disabledUncheckedIconColor },
        )

    /**
     * Represents the color used for the switch's thumb, depending on [enabled] and [checked].
     *
     * @param enabled whether the [MaterialSwitch] is enabled or not
     * @param checked whether the [MaterialSwitch] is checked or not
     */
    @Stable
    internal fun thumbColor(enabled: Boolean, checked: Boolean, pressed: Boolean): Color {
        if (pressed) {
            return if (checked) {
                checkedPressedThumbColor
            } else {
                uncheckedPressedThumbColor
            }
        }

        return if (enabled) {
            if (checked) checkedThumbColor else uncheckedThumbColor
        } else {
            if (checked) disabledCheckedThumbColor else disabledUncheckedThumbColor
        }
    }

    /**
     * Represents the color used for the switch's track, depending on [enabled] and [checked].
     *
     * @param enabled whether the [MaterialSwitch] is enabled or not
     * @param checked whether the [MaterialSwitch] is checked or not
     */
    @Stable
    internal fun trackColor(enabled: Boolean, checked: Boolean): Color =
        if (enabled) {
            if (checked) checkedTrackColor else uncheckedTrackColor
        } else {
            if (checked) disabledCheckedTrackColor else disabledUncheckedTrackColor
        }

    /**
     * Represents the color used for the switch's border, depending on [enabled] and [checked].
     *
     * @param enabled whether the [MaterialSwitch] is enabled or not
     * @param checked whether the [MaterialSwitch] is checked or not
     */
    @Stable
    internal fun borderColor(enabled: Boolean, checked: Boolean): Color =
        if (enabled) {
            if (checked) checkedBorderColor else uncheckedBorderColor
        } else {
            if (checked) disabledCheckedBorderColor else disabledUncheckedBorderColor
        }

    /**
     * Represents the content color passed to the icon if used
     *
     * @param enabled whether the [MaterialSwitch] is enabled or not
     * @param checked whether the [MaterialSwitch] is checked or not
     */
    @Stable
    internal fun iconColor(enabled: Boolean, checked: Boolean): Color =
        if (enabled) {
            if (checked) checkedIconColor else uncheckedIconColor
        } else {
            if (checked) disabledCheckedIconColor else disabledUncheckedIconColor
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is MaterialSwitchColors) return false

        if (checkedThumbColor != other.checkedThumbColor) return false
        if (checkedPressedThumbColor != other.uncheckedPressedThumbColor) return false
        if (checkedTrackColor != other.checkedTrackColor) return false
        if (checkedBorderColor != other.checkedBorderColor) return false
        if (checkedIconColor != other.checkedIconColor) return false
        if (uncheckedThumbColor != other.uncheckedThumbColor) return false
        if (uncheckedPressedThumbColor != other.uncheckedPressedThumbColor) return false
        if (uncheckedTrackColor != other.uncheckedTrackColor) return false
        if (uncheckedBorderColor != other.uncheckedBorderColor) return false
        if (uncheckedIconColor != other.uncheckedIconColor) return false
        if (disabledCheckedThumbColor != other.disabledCheckedThumbColor) return false
        if (disabledCheckedTrackColor != other.disabledCheckedTrackColor) return false
        if (disabledCheckedBorderColor != other.disabledCheckedBorderColor) return false
        if (disabledCheckedIconColor != other.disabledCheckedIconColor) return false
        if (disabledUncheckedThumbColor != other.disabledUncheckedThumbColor) return false
        if (disabledUncheckedTrackColor != other.disabledUncheckedTrackColor) return false
        if (disabledUncheckedBorderColor != other.disabledUncheckedBorderColor) return false
        if (disabledUncheckedIconColor != other.disabledUncheckedIconColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = checkedThumbColor.hashCode()
        result = 31 * result + checkedTrackColor.hashCode()
        result = 31 * result + checkedPressedThumbColor.hashCode()
        result = 31 * result + checkedBorderColor.hashCode()
        result = 31 * result + checkedIconColor.hashCode()
        result = 31 * result + uncheckedThumbColor.hashCode()
        result = 31 * result + uncheckedPressedThumbColor.hashCode()
        result = 31 * result + uncheckedTrackColor.hashCode()
        result = 31 * result + uncheckedBorderColor.hashCode()
        result = 31 * result + uncheckedIconColor.hashCode()
        result = 31 * result + disabledCheckedThumbColor.hashCode()
        result = 31 * result + disabledCheckedTrackColor.hashCode()
        result = 31 * result + disabledCheckedBorderColor.hashCode()
        result = 31 * result + disabledCheckedIconColor.hashCode()
        result = 31 * result + disabledUncheckedThumbColor.hashCode()
        result = 31 * result + disabledUncheckedTrackColor.hashCode()
        result = 31 * result + disabledUncheckedBorderColor.hashCode()
        result = 31 * result + disabledUncheckedIconColor.hashCode()
        return result
    }
}
