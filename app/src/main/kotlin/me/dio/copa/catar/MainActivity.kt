package me.dio.copa.catar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import dagger.hilt.android.AndroidEntryPoint
import me.dio.copa.catar.extensions.observe
import me.dio.copa.catar.features.ActionUIMain
import me.dio.copa.catar.features.MainViewModel
import me.dio.copa.catar.notification.scheduler.extensions.NotificationMatchesWorker
import me.dio.copa.catar.ui.MainScreen
import me.dio.copa.catar.ui.theme.Copa2022Theme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeAction()
        setContent {
            Copa2022Theme {
                //Essa função confere o estado
                val state = viewModel.state.collectAsState()
                MainScreen(matches = state.value.matches, viewModel::toggleNotification)
            }
        }
    }

    private fun observeAction() {
        viewModel.action.observe(this){
            when(it){
                is ActionUIMain.DisableNotification -> NotificationMatchesWorker.cancel(applicationContext, it.match)
                is ActionUIMain.EnableNotification -> NotificationMatchesWorker.start(applicationContext, it.match)
                is ActionUIMain.MatchesNotFound -> {}
                ActionUIMain.Unexpexted -> {}
            }
        }
    }

}
