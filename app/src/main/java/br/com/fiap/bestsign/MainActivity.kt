package br.com.fiap.bestsign

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.fiap.bestsign.ui.BestsignApp
import br.com.fiap.bestsign.ui.tema.TemaBestsign

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TemaBestsign {
                BestsignApp()
            }
        }
    }
}
