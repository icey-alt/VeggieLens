package com.example.veggielens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.veggielens.navigation.VeggieLensApp
import com.example.veggielens.ui.theme.VeggieLensTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VeggieLensTheme {
                VeggieLensApp()
            }
        }
    }
}
