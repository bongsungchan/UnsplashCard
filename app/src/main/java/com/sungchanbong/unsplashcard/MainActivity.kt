package com.sungchanbong.unsplashcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.sungchanbong.unsplashcard.navigation.NavigationGraph
import com.sungchanbong.unsplashcard.ui.theme.UnsplashcardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnsplashcardTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavigationGraph()
                }
            }
        }
    }
}
