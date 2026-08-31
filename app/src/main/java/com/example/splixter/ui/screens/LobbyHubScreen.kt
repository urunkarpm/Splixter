package com.example.splixter.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.splixter.data.AppStep
import com.example.splixter.data.LobbySession
import com.example.splixter.data.Person
import com.example.splixter.data.SavedGroup
import com.example.splixter.ui.SplitterUiState
import com.example.splixter.ui.SplitterViewModel
import com.example.splixter.ui.components.AppBackground
import com.example.splixter.ui.components.MonogramAvatar
import com.example.splixter.ui.components.appCardBorder
import com.example.splixter.ui.components.appCardColors
import com.example.splixter.ui.components.bounceClick
import com.example.splixter.ui.theme.PlusJakartaSansFontFamily
import java.util.Locale

private enum class LobbyAction {
    CREATE,
    JOIN
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LobbyHubScreen(
    uiState: SplitterUiState,
    viewModel: SplitterViewModel,
    modifier: Modifier = Modifier
) {
    var activeAction by remember { mutableStateOf<LobbyAction?>(null) }

    var createTripName by remember { mutableStateOf("") }
    var createHostName by remember { mutableStateOf(uiState.userProfile?.name ?: "") }
    var selectedSavedGroup by remember { mutableStateOf<SavedGroup?>(null) }

    var joinCode by remember { mutableStateOf("") }
    var joinMemberName by remember { mutableStateOf(uiState.userProfile?.name ?: "") }
    var showQrScanner by remember { mutableStateOf(false) }

    // Claim member state
    var pendingClaimSession by remember { mutableStateOf<LobbySession?>(null) }
    var pendingJoinCode by remember { mutableStateOf<String?>(null) }
    var isCheckingLobby by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(uiState.userProfile) {
        if (createHostName.isBlank() && uiState.userProfile != null) {
            createHostName = uiState.userProfile.name
        }
        if (joinMemberName.isBlank() && uiState.userProfile != null) {
            joinMemberName = uiState.userProfile.name
        }
    }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    fun startJoinFlow(code: String, rawName: String) {
        val cleanCode = viewModel.extractLobbyCode(code)
        if (cleanCode.isBlank()) return
        val memberName = rawName.ifBlank { uiState.userProfile?.name ?: "Guest" }.trim()

        isCheckingLobby = true
        Toast.makeText(context, "Checking group $cleanCode...", Toast.LENGTH_SHORT).show()
        
        viewModel.fetchLobbyForJoin(cleanCode) { session ->
            isCheckingLobby = false
            if (session != null && session.members.isNotEmpty()) {
                pendingClaimSession = session
                pendingJoinCode = cleanCode
            } else {
                viewModel.joinLobby(cleanCode, memberName, null)
            }
        }
    }

    if (showQrScanner) {
        com.example.splixter.ui.components.QrScannerDialog(
            onDismiss = { showQrScanner = false },
            onCodeScanned = { rawPayload ->
                val extracted = viewModel.extractLobbyCode(rawPayload)
                showQrScanner = false
                if (extracted.isNotBlank()) {
                    startJoinFlow(extracted, joinMemberName)
                }
            }
        )
    }

    // CLAIM MEMBER MODAL DIALOG
    if (pendingClaimSession != null && pendingJoinCode != null) {
        ClaimProfileDialog(
            session = pendingClaimSession!!,
            joinMemberName = joinMemberName.ifBlank { uiState.userProfile?.name ?: "Guest" },
            onDismiss = {
                pendingClaimSession = null
                pendingJoinCode = null
            },
            onClaimMember = { personId ->
                val code = pendingJoinCode!!
                val name = joinMemberName.ifBlank { uiState.userProfile?.name ?: "Guest" }
                pendingClaimSession = null
                pendingJoinCode = null
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                Toast.makeText(context, "Claimed profile & joined trip!", Toast.LENGTH_SHORT).show()
                viewModel.joinLobby(code, name, claimPersonId = personId)
            },
            onJoinAsNew = {
                val code = pendingJoinCode!!
                val name = joinMemberName.ifBlank { uiState.userProfile?.name ?: "Guest" }
                pendingClaimSession = null
                pendingJoinCode = null
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                Toast.makeText(context, "Joining as new member...", Toast.LENGTH_SHORT).show()
                viewModel.joinLobby(code, name, claimPersonId = null)
            }
        )
    }

    if (activeAction != null) {
        BackHandler { activeAction = null }
    }

    AppBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (activeAction != null) {
                                activeAction = null
                            } else {
                                viewModel.setStep(AppStep.MODE_SELECTION)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (activeAction) {
                            LobbyAction.CREATE -> "Create Trip Room"
                            LobbyAction.JOIN -> "Join Trip Room"
                            else -> "Trip Hub"
                        },
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (activeAction) {
                null -> {
                    // MAIN LOBBY OPTIONS SELECTION
                    Text(
                        text = "Trip Sessions",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Create a new trip ledger or join an existing session via code.",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Option 1 Button Card: Create a Lobby
                    ExecutiveLobbyOptionCard(
                        icon = Icons.Default.Add,
                        iconColor = Color(0xFF6366F1),
                        title = "Create a Trip Ledger",
                        subtitle = "Host a new trip & generate a shareable 6-digit code",
                        buttonText = "Create Ledger",
                        onClick = {
                            selectedSavedGroup = null
                            activeAction = LobbyAction.CREATE
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Option 2 Button Card: Direct QR Scan to Join
                    ExecutiveLobbyOptionCard(
                        icon = Icons.Default.QrCodeScanner,
                        iconColor = Color(0xFF10B981),
                        title = "Scan QR to Join Instantly",
                        subtitle = "Point camera at host's QR code to enter ledger",
                        buttonText = "Scan QR",
                        onClick = { showQrScanner = true }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Option 3 Button Card: Join via Code
                    ExecutiveLobbyOptionCard(
                        icon = Icons.Default.Key,
                        iconColor = Color(0xFF0284C7),
                        title = "Join with 6-Digit Code",
                        subtitle = "Enter room code shared by your friend or host",
                        buttonText = "Enter Code",
                        onClick = { activeAction = LobbyAction.JOIN }
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // SAVED / RECENT SESSIONS SECTION
                    if (uiState.savedLobbies.isNotEmpty()) {
                        Text(
                            text = "Recent Trip Ledgers (${uiState.savedLobbies.size})",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        uiState.savedLobbies.forEach { session ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = appCardColors(),
                                border = appCardBorder(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = session.name,
                                                fontFamily = PlusJakartaSansFontFamily,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = session.code,
                                                    fontFamily = PlusJakartaSansFontFamily,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(3.dp))

                                        val totalLobbyAmount = session.expenses.sumOf { it.amount }
                                        Text(
                                            text = "${session.members.size} members • ${session.expenses.size} spends • ₹${String.format(Locale.US, "%.2f", totalLobbyAmount)}",
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { viewModel.deleteLobby(session.code) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Lobby",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))

                                        Button(
                                            onClick = { viewModel.loadLobby(session.code) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Open", fontFamily = PlusJakartaSansFontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                LobbyAction.CREATE -> {
                    // DETAILS PAGE: CREATE LOBBY
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = appCardColors(),
                        border = appCardBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF6366F1).copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color(0xFF6366F1),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "New Trip Room",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Enter details to generate your Lobby Code",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            OutlinedTextField(
                                value = createTripName,
                                onValueChange = { createTripName = it },
                                label = { Text("Trip / Event Name") },
                                placeholder = { Text("e.g. Goa Vacation, Flat Spends") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = createHostName,
                                onValueChange = { createHostName = it },
                                label = { Text("Your Name (Host)") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Quick Group Presets
                            if (uiState.savedGroups.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Import Initial Members from Saved Group:",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    uiState.savedGroups.forEach { group ->
                                        val isSelected = selectedSavedGroup == group
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedSavedGroup = if (isSelected) null else group
                                            },
                                            label = {
                                                Text(
                                                    text = "${group.name} (${group.members.size})",
                                                    fontFamily = PlusJakartaSansFontFamily,
                                                    fontSize = 12.sp
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Groups,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            val canCreate = createTripName.isNotBlank() && createHostName.isNotBlank()
                            val createBtnInteraction = remember { MutableInteractionSource() }

                            Button(
                                onClick = {
                                    if (canCreate) {
                                        viewModel.createLobby(
                                            tripName = createTripName,
                                            hostName = createHostName,
                                            initialMembers = selectedSavedGroup?.members ?: emptyList()
                                        )
                                    }
                                },
                                enabled = canCreate,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(),
                                interactionSource = createBtnInteraction,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .bounceClick(createBtnInteraction)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Create Room & Open Ledger",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                LobbyAction.JOIN -> {
                    // DETAILS PAGE: JOIN LOBBY
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = appCardColors(),
                        border = appCardBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF0284C7).copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Join Trip Room",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Enter the 6-digit Lobby Code provided by the host",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // SCAN QR CODE BUTTON
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showQrScanner = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Scan Host's QR Code",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = joinCode,
                                onValueChange = { joinCode = viewModel.extractLobbyCode(it) },
                                label = { Text("Or Enter 6-Digit Code") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = joinMemberName,
                                onValueChange = { joinMemberName = it },
                                label = { Text("Your Participant Name") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            val canJoin = joinCode.isNotBlank() && joinMemberName.isNotBlank()
                            val joinBtnInteraction = remember { MutableInteractionSource() }

                            Button(
                                onClick = {
                                    if (canJoin) {
                                        startJoinFlow(joinCode, joinMemberName)
                                    }
                                },
                                enabled = canJoin && !isCheckingLobby,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(),
                                interactionSource = joinBtnInteraction,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .bounceClick(joinBtnInteraction)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isCheckingLobby) "Checking Room..." else "Join Room",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ClaimProfileDialog(
    session: LobbySession,
    joinMemberName: String,
    onDismiss: () -> Unit,
    onClaimMember: (String) -> Unit,
    onJoinAsNew: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = appCardColors(),
            border = appCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6366F1).copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.HowToReg,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Claim Your Profile",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Were you already added to \"${session.name}\"? Select your name to take over and manage your expenses:",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                // List of existing members to claim
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    session.members.forEach { member ->
                        val paidCount = session.expenses.count { it.paidByPersonId == member.id }
                        val splitCount = session.expenses.count { it.splitWithPersonIds.contains(member.id) }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onClaimMember(member.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    MonogramAvatar(
                                        name = member.name,
                                        color = member.color,
                                        size = 36.dp,
                                        fontSize = 13.sp
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = member.name,
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "$paidCount spends paid • in $splitCount splits",
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    color = Color(0xFF6366F1).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Claim ➔",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6366F1),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Option: Join as New Member
                OutlinedButton(
                    onClick = onJoinAsNew,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "I'm not listed (Join as '$joinMemberName')",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ExecutiveLobbyOptionCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    buttonText: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = appCardColors(),
        border = appCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .bounceClick(interactionSource)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconColor.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = iconColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = buttonText,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = iconColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
