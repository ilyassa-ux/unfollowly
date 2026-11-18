package com.unfollowly.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unfollowly.app.ui.UnfollowlyApp
import com.unfollowly.app.ui.UnfollowlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { UnfollowlyTheme { UnfollowlyApp(viewModel()) } }
    }
}
