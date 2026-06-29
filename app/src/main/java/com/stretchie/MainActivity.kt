package com.stretchie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.stretchie.presentation.navigation.AppNavigation
import com.stretchie.ui.theme.StretchieTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StretchieTheme {
                AppNavigation()
            }
        }
    }
}
