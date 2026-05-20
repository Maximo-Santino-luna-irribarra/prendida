package com.maximosantino.prendida

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.maximosantino.prendida.interfaz.PrendidaApp
import com.maximosantino.prendida.ui.theme.PrendidaTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PrendidaTheme {
                PrendidaApp()
            }
        }
    }
}