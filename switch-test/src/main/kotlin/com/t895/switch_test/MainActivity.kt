package com.t895.switch_test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.t895.materialswitchcmp.MaterialSwitch
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
    Row (
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .defaultMinSize(100.dp, 100.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        var enabled by remember { mutableStateOf(true) }
        var checked by remember { mutableStateOf(false) }
        Switch(
            // modifier = Modifier.size(120.dp),
            enabled = enabled,
            checked = checked,
            onCheckedChange = { checked = it },
        )
        MaterialSwitch(
            enabled = enabled,
            checked = checked,
            onCheckedChange = { checked = it },
        )
    }
}
