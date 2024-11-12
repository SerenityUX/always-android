package com.thomasstubblefield.always

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import android.content.Context
import com.thomasstubblefield.always.TokenManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var showMenu by remember { mutableStateOf(false) }

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
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Box(
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                SubcomposeAsyncImage(
                    model = "https://kodan-cdn.s3.amazonaws.com/profile-pictures/b999a181b8a630382adb68ff8c745d3a02047f976bcd825841c10593baba321f-1730239520033.jpeg",
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = false),
                            onClick = { showMenu = true }
                        ),
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
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .width(200.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("Swap Event") },
                        onClick = { 
                            println("Swap Event clicked")
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Update Avatar") },
                        onClick = { 
                            println("Update Avatar clicked")
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Logout") },
                        onClick = { 
                            tokenManager.deleteToken()
                            navController.navigate("onboarding") {
                                popUpTo("main") { inclusive = true }
                            }
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Account", color = Color.Red) },
                        onClick = { 
                            println("Delete Account clicked")
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
} 