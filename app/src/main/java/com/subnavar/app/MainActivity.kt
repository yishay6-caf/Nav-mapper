package com.subnavar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.subnavar.app.ui.SubNavAppRoot
import com.subnavar.app.ui.theme.SubNavARTheme
import com.subnavar.app.util.FileLogger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onResume() {
        super.onResume()
        FileLogger.log("ACTIVITY", "MainActivity.onResume")
    }

    override fun onPause() {
        super.onPause()
        FileLogger.log("ACTIVITY", "MainActivity.onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        FileLogger.log("ACTIVITY", "MainActivity.onDestroy")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FileLogger.log("ACTIVITY", "MainActivity.onCreate")
        enableEdgeToEdge()
        setContent {
            SubNavARTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SubNavAppRoot()
                }
            }
        }
    }
}
