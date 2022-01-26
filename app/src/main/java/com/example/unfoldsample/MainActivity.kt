package com.example.unfoldsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.unfoldsample.ui.theme.UnfoldSampleTheme
import com.microsoft.device.dualscreen.windowstate.WindowState
import com.microsoft.device.dualscreen.windowstate.rememberWindowState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val windowState = rememberWindowState()
            UnfoldSampleTheme {
                UnfoldSample(windowState)
            }
        }
    }
}

@Composable
fun UnfoldSample(windowState: WindowState) {
    Surface(color = MaterialTheme.colors.background) {
        if (windowState.hasFold)
            Text("${if (windowState.isFoldHorizontal) "Horizontal" else "Vertical"} fold present")
        else
            Text("No fold present")
    }
}