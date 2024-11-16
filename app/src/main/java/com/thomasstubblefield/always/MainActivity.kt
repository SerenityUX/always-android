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
import androidx.compose.foundation.layout.width
import coil.compose.SubcomposeAsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.unit.IntOffset
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContentTransitionScope

import com.thomasstubblefield.always.ui.theme.AlwaysTheme
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val tokenManager = TokenManager(this)
        
        setContent {
            AlwaysTheme {
                val navController = rememberNavController()
                
                NavHost(
                    navController = navController, 
                    startDestination = if (tokenManager.getToken() != null) "main" else "onboarding"
                ) {
                    composable(
                        "main",
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            )
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            )
                        }
                    ) {
                        HomeScreen(navController)
                    }
                    
                    composable(
                        "onboarding",
                        enterTransition = {
                            slideIn(tween(300)) { IntOffset(it.width, 0) }
                        },
                        exitTransition = {
                            slideOut(tween(300)) { IntOffset(-it.width, 0) }
                        }
                    ) {
                        OnboardingScreen(navController)
                    }
                    
                    composable(
                        "login",
                        enterTransition = {
                            slideIn(tween(300)) { IntOffset(it.width, 0) }
                        },
                        exitTransition = {
                            slideOut(tween(300)) { IntOffset(-it.width, 0) }
                        }
                    ) {
                        LoginScreen(navController)
                    }
                    
                    composable(
                        "signup",
                        enterTransition = {
                            slideIn(tween(300)) { IntOffset(it.width, 0) }
                        },
                        exitTransition = {
                            slideOut(tween(300)) { IntOffset(-it.width, 0) }
                        }
                    ) {
                        SignupScreen(navController)
                    }
                }
            }
        }
    }
}