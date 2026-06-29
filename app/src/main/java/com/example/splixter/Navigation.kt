package com.example.splixter

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.splixter.data.AppStep
import com.example.splixter.ui.SplitterViewModel
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

    when (uiState.currentStep) {
        AppStep.SPLASH -> {
            SplashScreen(viewModel = splitterViewModel)
        }
        AppStep.PEOPLE -> {
            BackHandler { splitterViewModel.setStep(AppStep.SPLASH) }
            PeopleSetupScreen(uiState = uiState, viewModel = splitterViewModel)
        }
        AppStep.SCAN -> {
            BackHandler { splitterViewModel.setStep(AppStep.PEOPLE) }
            ScanBillScreen(uiState = uiState, viewModel = splitterViewModel)
        }
        AppStep.ASSIGN -> {
            BackHandler { splitterViewModel.setStep(AppStep.SCAN) }
            ItemAssignmentScreen(uiState = uiState, viewModel = splitterViewModel)
        }
        AppStep.RECEIPT -> {
            BackHandler { splitterViewModel.setStep(AppStep.ASSIGN) }
            ReceiptSummaryScreen(uiState = uiState, viewModel = splitterViewModel)
        }
    }
}
