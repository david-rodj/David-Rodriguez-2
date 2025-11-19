package edu.javeriana.david_rodriguez_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import edu.javeriana.david_rodriguez_2.ui.screens.MapScreen
import edu.javeriana.david_rodriguez_2.ui.theme.DavidRodriguez2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DavidRodriguez2Theme {
                MapScreen()
            }
        }
    }
}