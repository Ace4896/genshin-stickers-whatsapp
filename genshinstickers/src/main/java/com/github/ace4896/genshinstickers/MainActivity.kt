package com.github.ace4896.genshinstickers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage
import com.github.ace4896.genshinstickers.ui.theme.GenshinStickersTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GenshinStickersTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column {
                        Text(text = "Hello world!")
                        StickerImage("file:///android_asset/1/Icon_Emoji_Paimon's_Paintings_01_Aether_1.webp")
                        StickerImage("file:///android_asset/1/Icon_Emoji_Paimon's_Paintings_01_Aether_2.webp")
                    }
                }
            }
        }
    }
}

@Composable
fun StickerImage(assetUri: String) {
    AsyncImage(
        model = assetUri,
        contentDescription = null,
    )
}
