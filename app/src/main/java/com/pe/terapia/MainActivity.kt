package com.pe.terapia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pe.terapia.core.navigation.NavGraph
import com.pe.terapia.ui.theme.TerapiaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TerapiaTheme {
                NavGraph()
            }
        }
    }
}