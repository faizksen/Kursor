package dev.faizksen.kursor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import dev.faizksen.kursor.designsystem.KursorTheme
import dev.faizksen.kursor.ui.KursorApp
import dev.faizksen.kursor.ui.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

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
