package com.offlinelearninghub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.offlinelearninghub.data.preferences.UserPreferences
import com.offlinelearninghub.ui.navigation.MainNavGraph
import com.offlinelearninghub.ui.theme.OfflineLearningHubTheme
import com.offlinelearninghub.util.LocalizationUtils
import com.offlinelearninghub.viewmodel.DashboardViewModel
import com.offlinelearninghub.viewmodel.LibraryViewModel
import com.offlinelearninghub.viewmodel.PDFReaderViewModel
import com.offlinelearninghub.viewmodel.PlayerViewModel
import com.offlinelearninghub.viewmodel.SettingsViewModel
import com.offlinelearninghub.viewmodel.TimerViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userPreferences = UserPreferences(this)

        enableEdgeToEdge()

        setContent {
            val themeMode by userPreferences.themeMode.collectAsState(initial = 0)

            OfflineLearningHubTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val dashboardViewModel: DashboardViewModel = viewModel()
                    val libraryViewModel: LibraryViewModel = viewModel()
                    val timerViewModel: TimerViewModel = viewModel()
                    val settingsViewModel: SettingsViewModel = viewModel()
                    val playerViewModel: PlayerViewModel = viewModel()
                    val pdfReaderViewModel: PDFReaderViewModel = viewModel()

                    MainNavGraph(
                        dashboardViewModel = dashboardViewModel,
                        libraryViewModel = libraryViewModel,
                        timerViewModel = timerViewModel,
                        settingsViewModel = settingsViewModel,
                        playerViewModel = playerViewModel,
                        pdfReaderViewModel = pdfReaderViewModel,
                        onLanguageChanged = {
                            recreate()
                        }
                    )
                }
            }
        }
    }

    override fun attachBaseContext(newBase: android.content.Context) {
        val language = try {
            runBlocking { UserPreferences(newBase).language.first() }
        } catch (e: Exception) {
            "en"
        }
        super.attachBaseContext(LocalizationUtils.applyLanguage(newBase, language))
    }
}
