package com.thomasstubblefield.always

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.error
import androidx.compose.foundation.layout.fillMaxSize
import coil.compose.SubcomposeAsyncImage

import com.thomasstubblefield.always.ui.theme.AlwaysTheme
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlwaysTheme {
                Scaffold { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp)
                            .height(56.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Always",
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.align(alignment = androidx.compose.ui.Alignment.CenterVertically)
                        )

                        SubcomposeAsyncImage(
                            model = "https://kodan-cdn.s3.amazonaws.com/profile-pictures/b999a181b8a630382adb68ff8c745d3a02047f976bcd825841c10593baba321f-1730239520033.jpeg",
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .align(androidx.compose.ui.Alignment.CenterVertically),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.LightGray)
                                )
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.LightGray)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}