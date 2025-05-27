package com.t895.switch_test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.android.material.color.DynamicColors
import com.google.android.material.materialswitch.MaterialSwitch
import com.t895.switch_test.ui.theme.MaterialswitchcmpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialswitchcmpTheme {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                    SwitchPreview(Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun SideBySide(
    firstContent: @Composable () -> Unit,
    secondContent: @Composable () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        firstContent()
        secondContent()
    }
}

@Preview
@Composable
fun SwitchPreview(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .defaultMinSize(100.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val context = LocalContext.current
        val checkDrawable = ContextCompat.getDrawable(context, R.drawable.ic_check)
        val crossDrawable = ContextCompat.getDrawable(context, R.drawable.ic_close)
        var enabled by remember { mutableStateOf(true) }
        var checked by remember { mutableStateOf(false) }
        Text("Jetpack Compose Material 3 Expressive Switch")
        SideBySide(
            firstContent = {
                androidx.compose.material3.Switch(
                    enabled = enabled,
                    checked = checked,
                    onCheckedChange = { checked = it },
                    thumbContent = {
                        if (checked) {
                            Icon(
                                painter = rememberDrawablePainter(checkDrawable),
                                contentDescription = null,
                            )
                        } else {
                            Icon(
                                painter = rememberDrawablePainter(crossDrawable),
                                contentDescription = null,
                            )
                        }
                    },
                )
            },
            secondContent = {
                androidx.compose.material3.Switch(
                    enabled = enabled,
                    checked = checked,
                    onCheckedChange = { checked = it },
                )
            }
        )

        Text("Android View Material 3 Switch")
        SideBySide(
            firstContent = {
                AndroidView(
                    modifier = Modifier.size(52.dp, 32.dp),
                    factory = { context ->
                        MaterialSwitch(context).apply {
                            if (checked) {
                                setThumbIconResource(R.drawable.ic_check)
                            } else {
                                setThumbIconResource(R.drawable.ic_close)
                            }

                            setOnCheckedChangeListener { _, newChecked ->
                                checked = newChecked
                                if (checked) {
                                    setThumbIconResource(R.drawable.ic_check)
                                } else {
                                    setThumbIconResource(R.drawable.ic_close)
                                }
                            }
                        }
                    },
                    update = { view ->
                        view.isEnabled = enabled
                        view.isChecked = checked
                    },
                )
            },
            secondContent = {
                AndroidView(
                    modifier = Modifier.size(52.dp, 32.dp),
                    factory = { context ->
                        MaterialSwitch(context).apply {
                            setOnCheckedChangeListener { _, newChecked ->
                                checked = newChecked
                            }
                        }
                    },
                    update = { view ->
                        view.isEnabled = enabled
                        view.isChecked = checked
                    },
                )
            }
        )

        Text("Material Switch")
        SideBySide(
            firstContent = {
                com.t895.materialswitch.MaterialSwitch(
                    enabled = enabled,
                    checked = checked,
                    onCheckedChange = { checked = it },
                    thumbContent = { mostlyEnabled ->
                        if (mostlyEnabled) {
                            Icon(
                                painter = rememberDrawablePainter(checkDrawable),
                                contentDescription = null,
                            )
                        } else {
                            Icon(
                                painter = rememberDrawablePainter(crossDrawable),
                                contentDescription = null,
                            )
                        }
                    },
                )
            },
            secondContent = {
                com.t895.materialswitch.MaterialSwitch(
                    enabled = enabled,
                    checked = checked,
                    onCheckedChange = { checked = it },
                )
            }
        )

        Text("Enabled")
        Checkbox(
            checked = enabled,
            onCheckedChange = { enabled = !enabled },
        )
    }
}
