package com.example.firebaseapptest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.firebaseapptest.ui.theme.FirebaseApptestTheme
import com.example.firebaseapptest.ui.view.AppViewModel
import com.example.firebaseapptest.ui.view.screen.AppScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirebaseApptestTheme {
                val viewModel = hiltViewModel<AppViewModel>()
                val state = viewModel.state.collectAsState().value
                AppScreen(state, viewModel::onEvent)
            }
        }
    }
}

