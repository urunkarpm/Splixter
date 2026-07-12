package com.example.splixter

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.splixter.data.AppStep
import com.example.splixter.ui.SplitterViewModel
import com.example.splixter.ui.components.bounceClick
import com.example.splixter.ui.components.sliceShatter
import com.example.splixter.ui.screens.ItemAssignmentScreen
import com.example.splixter.ui.screens.PeopleSetupScreen
import com.example.splixter.ui.screens.ReceiptSummaryScreen
import com.example.splixter.ui.screens.ScanBillScreen
import com.example.splixter.ui.screens.SplashScreen

@Composable
fun MainNavigation(splitterViewModel: SplitterViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by splitterViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        splitterViewModel.initStorage(context)
    }

    AnimatedContent(
        targetState = uiState.currentStep,
        transitionSpec = {
            val forward = targetState.ordinal > initialState.ordinal
            if (forward) {
                if (initialState == AppStep.SPLASH) {
                    // Shatter transition: let entering screen fade in while splash shatters
                    fadeIn(
                        animationSpec = tween(durationMillis = 800)
                    ) togetherWith fadeOut(
                        animationSpec = tween(durationMillis = 800)
                    )
                } else {
                    // Expand from the button area (bottom center) with slide + spring scale
                    slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f)
                    ) + scaleIn(
                        initialScale = 0.88f,
                        transformOrigin = TransformOrigin(0.5f, 0.9f),
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f)
                    ) + fadeIn(animationSpec = tween(durationMillis = 300)) togetherWith
                    slideOutVertically(
                        targetOffsetY = { -it / 8 },
                        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                    ) + scaleOut(
                        targetScale = 0.96f,
                        transformOrigin = TransformOrigin(0.5f, 0.5f),
                        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(durationMillis = 200))
                }
            } else {
                // Compress back into the button area (bottom center)
                slideInVertically(
                    initialOffsetY = { -it / 8 },
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                ) + scaleIn(
                    initialScale = 0.96f,
                    transformOrigin = TransformOrigin(0.5f, 0.5f),
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(durationMillis = 200)) togetherWith
                slideOutVertically(
                    targetOffsetY = { it / 4 },
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f)
                ) + scaleOut(
                    targetScale = 0.88f,
                    transformOrigin = TransformOrigin(0.5f, 0.9f),
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f)
                ) + fadeOut(animationSpec = tween(durationMillis = 250))
            }
        },
        label = "stepTransition"
    ) { step ->
        when (step) {
            AppStep.SPLASH -> {
                val progress by transition.animateFloat(
                    transitionSpec = { tween(durationMillis = 800) },
                    label = "sliceProgress"
                ) { state ->
                    if (state == androidx.compose.animation.EnterExitState.Visible) 0f else 1f
                }
                SplashScreen(
                    viewModel = splitterViewModel,
                    modifier = Modifier.sliceShatter(progress = progress, pieceCount = 12)
                )
            }
            AppStep.PEOPLE -> {
                if (step == uiState.currentStep) {
                    BackHandler { splitterViewModel.setStep(AppStep.SPLASH) }
                }
                PeopleSetupScreen(uiState = uiState, viewModel = splitterViewModel)
            }
            AppStep.SCAN -> {
                if (step == uiState.currentStep) {
                    BackHandler { splitterViewModel.setStep(AppStep.PEOPLE) }
                }
                ScanBillScreen(uiState = uiState, viewModel = splitterViewModel)
            }
            AppStep.ASSIGN -> {
                if (step == uiState.currentStep) {
                    BackHandler { splitterViewModel.setStep(AppStep.SCAN) }
                }
                ItemAssignmentScreen(uiState = uiState, viewModel = splitterViewModel)
            }
            AppStep.RECEIPT -> {
                if (step == uiState.currentStep) {
                    BackHandler { splitterViewModel.setStep(AppStep.ASSIGN) }
                }
                ReceiptSummaryScreen(uiState = uiState, viewModel = splitterViewModel)
            }
        }
    }
}

