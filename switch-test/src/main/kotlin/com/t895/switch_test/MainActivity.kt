package com.t895.switch_test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.material.materialswitch.MaterialSwitch
import com.t895.switch_test.ui.theme.MaterialswitchcmpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialswitchcmpTheme {
                SwitchPreview(Modifier.fillMaxSize())
            }
        }
    }
}

@Preview
@Composable
fun SwitchPreview(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .defaultMinSize(100.dp, 100.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        var enabled by remember { mutableStateOf(true) }
        var checked by remember { mutableStateOf(false) }
        Text("Jetpack Compose Material 3 Switch")
        androidx.compose.material3.Switch(
            enabled = enabled,
            checked = checked,
            onCheckedChange = { checked = it },
        )
        Text("materialswitchcmp Replacement")
        com.t895.materialswitch.Switch(
            enabled = enabled,
            checked = checked,
            onCheckedChange = { checked = it },
        )
        Text("Android View Material 3 Switch")
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
        Text("Enabled")
        Checkbox(
            checked = enabled,
            onCheckedChange = { enabled = !enabled },
        )
    }
}
