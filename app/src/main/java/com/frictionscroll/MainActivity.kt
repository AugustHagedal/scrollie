package com.frictionscroll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.frictionscroll.ui.navigation.NavGraph
import com.frictionscroll.ui.theme.FrictionScrollTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FrictionScrollTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
