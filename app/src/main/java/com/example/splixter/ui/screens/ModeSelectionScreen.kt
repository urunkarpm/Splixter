package com.example.splixter.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.splixter.data.AppStep
import com.example.splixter.data.CalculationMode
import com.example.splixter.ui.SplitterUiState
import com.example.splixter.ui.SplitterViewModel
import com.example.splixter.ui.components.AppBackground
import com.example.splixter.ui.components.MonogramAvatar
import com.example.splixter.ui.components.appCardBorder
import com.example.splixter.ui.components.appCardColors
import com.example.splixter.ui.components.bounceClick
import com.example.splixter.ui.theme.PlusJakartaSansFontFamily
import com.example.splixter.util.AppUpdateInfo
import com.example.splixter.util.AppUpdateResult

import androidx.compose.material.icons.filled.QrCodeScanner
import com.example.splixter.data.LobbySession
import com.example.splixter.ui.components.ClaimProfileDialog
import com.example.splixter.ui.components.QrScannerDialog

@Composable
fun ModeSelectionScreen(
    uiState: SplitterUiState,
    viewModel: SplitterViewModel,
    modifier: Modifier = Modifier
) {
    val profile = uiState.userProfile
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    // Settings local state
    var nameInput by remember { mutableStateOf(profile?.name ?: "") }
    var upiInput by remember { mutableStateOf(profile?.upiId ?: "") }
    var isSettingsSaved by remember { mutableStateOf(false) }

    // Direct QR Scanner & Member Claiming state
    var showQrScanner by remember { mutableStateOf(false) }
    var pendingClaimSession by remember { mutableStateOf<LobbySession?>(null) }
    var pendingJoinCode by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(profile) {
        if (profile != null) {
            nameInput = profile.name
            upiInput = profile.upiId ?: ""
        }
    }

    if (showQrScanner) {
        QrScannerDialog(
            onDismiss = { showQrScanner = false },
            onCodeScanned = { rawPayload ->
                val extracted = viewModel.extractLobbyCode(rawPayload)
                showQrScanner = false
                if (extracted.isNotBlank()) {
                    val memberName = profile?.name?.ifBlank { "Guest" } ?: "Guest"
                    Toast.makeText(context, "Checking group $extracted...", Toast.LENGTH_SHORT).show()
                    viewModel.fetchLobbyForJoin(extracted) { session ->
                        if (session != null && session.members.isNotEmpty()) {
                            pendingClaimSession = session
                            pendingJoinCode = extracted
                        } else {
                            viewModel.joinLobby(extracted, memberName, null)
                        }
                    }
                }
            }
        )
    }

    if (pendingClaimSession != null && pendingJoinCode != null) {
        ClaimProfileDialog(
            session = pendingClaimSession!!,
            joinMemberName = profile?.name?.ifBlank { "Guest" } ?: "Guest",
            onDismiss = {
                pendingClaimSession = null
                pendingJoinCode = null
            },
            onClaimMember = { personId ->
                val code = pendingJoinCode!!
                val name = profile?.name?.ifBlank { "Guest" } ?: "Guest"
                pendingClaimSession = null
                pendingJoinCode = null
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                Toast.makeText(context, "Claimed profile & joined trip!", Toast.LENGTH_SHORT).show()
                viewModel.joinLobby(code, name, claimPersonId = personId)
            },
            onJoinAsNew = {
                val code = pendingJoinCode!!
                val name = profile?.name?.ifBlank { "Guest" } ?: "Guest"
                pendingClaimSession = null
                pendingJoinCode = null
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                Toast.makeText(context, "Joining as new member...", Toast.LENGTH_SHORT).show()
                viewModel.joinLobby(code, name, claimPersonId = null)
            }
        )
    }

    AppBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Splixter",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile Pill
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setStep(AppStep.USER_PROFILE_SETUP) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            if (profile != null && profile.name.isNotBlank()) {
                                MonogramAvatar(
                                    name = profile.name,
                                    color = profile.color,
                                    size = 20.dp,
                                    fontSize = 9.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = profile.name,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else {
                                Text(
                                    text = "Set Profile",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // QR Scan & Join Button
                    IconButton(
                        onClick = { showQrScanner = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Group QR Code",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark Mode",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Heading Title
            Text(
                text = "Select Mode",
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Choose how you would like to track and settle expenses.",
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Card 1: Single Bill / Itemized Splitter
            ExecutiveModeCard(
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                iconAccentColor = Color(0xFF6366F1),
                title = "Single Bill Splitter",
                subtitle = "Itemized dining & restaurant receipts",
                description = "Scan receipts, assign individual dishes to specific participants, and accurately calculate food GST, liquor VAT, tip & discounts.",
                features = listOf("OCR Receipt Scanner", "Food & Liquor Taxes", "Itemized Settlement"),
                buttonText = "Start Single Bill Split",
                onClick = {
                    viewModel.setCalculationMode(CalculationMode.SINGLE_BILL)
                    viewModel.setStep(AppStep.PEOPLE)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Card 2: Group / Trip Expense Log
            ExecutiveModeCard(
                icon = Icons.Default.FlightTakeoff,
                iconAccentColor = Color(0xFF0284C7),
                title = "Trip & Group Expenses",
                subtitle = "Multi-person sequential spends for trips",
                description = "Log ongoing expenses for trips, shared flats, or group events. Automatically resolves mutual debts into minimal settlement transactions.",
                features = listOf("Multi-Expense Ledger", "Mutual Debt Settlement", "Live Multi-User Sync"),
                buttonText = "Open Trip Ledger",
                onClick = {
                    viewModel.setCalculationMode(CalculationMode.TRIP_EXPENSE)
                    viewModel.setStep(AppStep.LOBBY_HUB)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SETTINGS MODULE
            SettingsSectionCard(
                nameInput = nameInput,
                onNameChange = {
                    nameInput = it
                    isSettingsSaved = false
                },
                upiInput = upiInput,
                onUpiChange = {
                    upiInput = it
                    isSettingsSaved = false
                },
                isSaved = isSettingsSaved,
                isCheckingUpdate = uiState.isCheckingUpdate,
                onSave = {
                    if (nameInput.isNotBlank()) {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        viewModel.saveUserProfile(
                            name = nameInput.trim(),
                            upiId = upiInput.trim(),
                            emoji = profile?.emoji ?: "😎",
                            color = profile?.color ?: 0xFF6C5CE7,
                            phone = profile?.phoneNumber
                        )
                        isSettingsSaved = true
                        Toast.makeText(context, "Settings saved successfully!", Toast.LENGTH_SHORT).show()
                    }
                },
                onCheckUpdates = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    viewModel.checkForAppUpdates(isManual = true) { result ->
                        when (result) {
                            is AppUpdateResult.Success -> {
                                // Dialog is shown automatically
                            }
                            is AppUpdateResult.NoUpdate -> {
                                Toast.makeText(context, "You're using the latest version of Splixter (${result.currentVersion})! ✓", Toast.LENGTH_LONG).show()
                            }
                            is AppUpdateResult.Error -> {
                                Toast.makeText(context, "Checking GitHub releases...", Toast.LENGTH_SHORT).show()
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(result.fallbackUrl))
                                try {
                                    context.startActivity(browserIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Unable to open browser", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // APP UPDATE AVAILABLE DIALOG
        if (uiState.showUpdateDialog && uiState.availableAppUpdate != null) {
            AppUpdateDialog(
                updateInfo = uiState.availableAppUpdate,
                onDismiss = { viewModel.dismissUpdateDialog() },
                onDownload = {
                    val targetUrl = uiState.availableAppUpdate.apkDownloadUrl ?: uiState.availableAppUpdate.htmlUrl
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Unable to open browser", Toast.LENGTH_SHORT).show()
                    }
                    viewModel.dismissUpdateDialog()
                }
            )
        }
    }
}

@Composable
private fun AppUpdateDialog(
    updateInfo: AppUpdateInfo,
    onDismiss: () -> Unit,
    onDownload: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = appCardColors(),
            border = appCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Update Available!",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "New: ${updateInfo.latestVersion}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "• Current: ${updateInfo.currentVersion}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "What's New in this release:",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = updateInfo.releaseNotes.ifBlank { "Bug fixes, design improvements, and performance optimizations." },
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                val downloadBtnInteraction = remember { MutableInteractionSource() }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onDownload()
                    },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(),
                    interactionSource = downloadBtnInteraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .bounceClick(downloadBtnInteraction)
                        .background(
                            Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, Color(0xFF0EA5E9))),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Download & Install Update",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Remind Me Later",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    nameInput: String,
    onNameChange: (String) -> Unit,
    upiInput: String,
    onUpiChange: (String) -> Unit,
    isSaved: Boolean,
    isCheckingUpdate: Boolean,
    onSave: () -> Unit,
    onCheckUpdates: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current

    val upiSuffixes = listOf("@okhdfcbank", "@okaxis", "@oksbi", "@paytm", "@ybl", "@upi")

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = appCardColors(),
        border = appCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Profile & App Settings",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Manage your identity, UPI ID & app updates",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. NAME FIELD
            Text(
                text = "Display Name",
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = nameInput,
                onValueChange = onNameChange,
                placeholder = { Text("Your Name (e.g. Alex)", fontSize = 13.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. UPI ADDRESS FIELD
            Text(
                text = "UPI Address (For 1-Click Payments)",
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = upiInput,
                onValueChange = onUpiChange,
                placeholder = { Text("e.g. mobile@upi or name@okaxis", fontSize = 13.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick UPI handle chips
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                upiSuffixes.forEach { suffix ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            val prefix = if (upiInput.contains("@")) upiInput.substringBefore("@") else upiInput.ifBlank { nameInput.lowercase().replace("\\s+".toRegex(), "") }
                            onUpiChange("$prefix$suffix")
                        }
                    ) {
                        Text(
                            text = suffix,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SAVE BUTTON
            val saveBtnInteraction = remember { MutableInteractionSource() }
            val canSave = nameInput.isNotBlank()

            Button(
                onClick = {
                    keyboardController?.hide()
                    onSave()
                },
                enabled = canSave,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(),
                interactionSource = saveBtnInteraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .bounceClick(saveBtnInteraction)
                    .background(
                        if (canSave) {
                            Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                        } else {
                            Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                        },
                        shape = RoundedCornerShape(14.dp)
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Save,
                        contentDescription = null,
                        tint = if (canSave) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSaved) "Settings Saved ✓" else "Save Settings",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canSave) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. CHECK FOR UPDATES ON GITHUB
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(enabled = !isCheckingUpdate, onClick = onCheckUpdates)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            if (isCheckingUpdate) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "GitHub Releases",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isCheckingUpdate) "Checking GitHub for updates..." else "Splixter v2.0.0 • GitHub Channel",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isCheckingUpdate) "Checking..." else "Check Updates",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExecutiveModeCard(
    icon: ImageVector,
    iconAccentColor: Color,
    title: String,
    subtitle: String,
    description: String,
    features: List<String>,
    buttonText: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = appCardColors(),
        border = appCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .bounceClick(interactionSource)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconAccentColor.copy(alpha = 0.15f))
                        .border(1.dp, iconAccentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconAccentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = description,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Feature Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                features.forEach { feature ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = feature,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconAccentColor)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = buttonText,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
