package dev.faizksen.kursor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import dagger.hilt.android.AndroidEntryPoint
import dev.faizksen.kursor.designsystem.KursorTheme
import dev.faizksen.kursor.ui.KursorApp
import dev.faizksen.kursor.ui.MainViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        viewModel.scheduleSync()
        setContent {
            KursorTheme {
                KursorApp()
            }
        }
    }
}
