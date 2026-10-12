package dev.faizksen.kursor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import dagger.hilt.android.AndroidEntryPoint
import dev.faizksen.kursor.designsystem.KursorTheme
import dev.faizksen.kursor.ui.KursorApp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            KursorTheme {
                KursorApp()
            }
        }
    }
}
