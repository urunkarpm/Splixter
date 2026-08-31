package com.example.splixter.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splixter.data.AppStep
import com.example.splixter.data.TripPersonBalance
import com.example.splixter.data.TripSettlement
import com.example.splixter.ui.SplitterUiState
import com.example.splixter.ui.SplitterViewModel
import com.example.splixter.ui.components.AppBackground
import com.example.splixter.ui.components.MonogramAvatar
import com.example.splixter.ui.components.appCardBorder
import com.example.splixter.ui.components.appCardColors
import com.example.splixter.ui.components.bounceClick
import com.example.splixter.ui.theme.PlusJakartaSansFontFamily
import java.util.Locale

@Composable
fun TripSummaryScreen(
    uiState: SplitterUiState,
    viewModel: SplitterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val balances = remember(uiState.tripExpenses, uiState.people) { viewModel.calculateTripBalances() }
    val settlements = remember(uiState.tripExpenses, uiState.people) { viewModel.calculateTripSettlements() }
    val totalTripAmount = uiState.tripExpenses.sumOf { it.amount }

    val shareText = remember(uiState.tripName, totalTripAmount, uiState.people, settlements, balances) {
        buildString {
            appendLine("🏖️ *${uiState.tripName.uppercase()} — SETTLEMENT SUMMARY*")
            appendLine("💰 Total Group Spend: ₹${String.format(Locale.US, "%.2f", totalTripAmount)}")
            appendLine("👥 Members (${uiState.people.size}): ${uiState.people.joinToString { it.name }}")
            appendLine()
            appendLine("📊 *DIRECT SETTLEMENTS:*")
            if (settlements.isEmpty()) {
                appendLine("🎉 *All expenses are fully balanced and settled!*")
            } else {
                settlements.forEach { s ->
                    val upiInfo = s.toPerson.upiId ?: s.toPerson.phoneNumber ?: ""
                    val upiSuffix = if (upiInfo.isNotBlank()) " [UPI: $upiInfo]" else ""
                    appendLine("👉 *${s.fromPerson.name}* pays *${s.toPerson.name}*: ₹${String.format(Locale.US, "%.2f", s.amount)}$upiSuffix")
                }
            }
            appendLine()
            appendLine("⚡ *INDIVIDUAL BREAKDOWN:*")
            balances.forEach { b ->
                val status = if (b.netBalance > 0.01) "Gets back ₹${String.format(Locale.US, "%.2f", b.netBalance)}"
                else if (b.netBalance < -0.01) "Owes ₹${String.format(Locale.US, "%.2f", -b.netBalance)}"
                else "Settled"
                appendLine("• ${b.person.name}: Paid ₹${String.format(Locale.US, "%.0f", b.totalPaid)} | Share ₹${String.format(Locale.US, "%.0f", b.totalOwed)} ($status)")
            }
            appendLine()
            appendLine("✨ Split smartly using *Splixter*")
        }
    }

    AppBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            viewModel.setStep(AppStep.TRIP_EXPENSES)
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Settlement Statement",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Optimized multi-debt resolution",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        viewModel.clearTripExpenses()
                        viewModel.setStep(AppStep.MODE_SELECTION)
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "New Calculation",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
            ) {
                // EXECUTIVE HERO CARD
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = appCardColors(),
                        border = appCardBorder(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Payments,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "FINANCIAL AUDIT",
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.8.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Surface(
                                    color = if (settlements.isEmpty()) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (settlements.isEmpty()) "Fully Settled ✓" else "${settlements.size} Direct Transfers",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (settlements.isEmpty()) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Total Group Expense",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹${String.format(Locale.US, "%.2f", totalTripAmount)}",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Metric Cards Strip
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val avgShare = if (uiState.people.isNotEmpty()) totalTripAmount / uiState.people.size else 0.0
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)) {
                                        Text("Avg / Person", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("₹${String.format(Locale.US, "%.0f", avgShare)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)) {
                                        Text("Total Spends", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${uiState.tripExpenses.size} Items", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)) {
                                        Text("Members", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${uiState.people.size} People", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }

                // SECTION 1: OPTIMIZED DIRECT SETTLEMENTS ("WHO PAYS WHOM")
                item {
                    Column {
                        Text(
                            text = "💸 Optimized Direct Payments (${settlements.size})",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Mutual debts are consolidated to minimize transfer steps.",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (settlements.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = appCardColors(),
                            border = appCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp).fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "All Debts Balanced",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Everyone has paid their exact share. No pending transfers!",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(settlements) { settlement ->
                        SettlementCard(
                            settlement = settlement,
                            currentUserId = uiState.currentUserId ?: uiState.userProfile?.id ?: "",
                            tripName = uiState.tripName,
                            onRecordSettlement = {
                                viewModel.recordSettlementPayment(settlement)
                            }
                        )
                    }
                }

                // SECTION 2: NET MEMBER BALANCES
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⚡ Member Net Balances",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(balances) { balance ->
                    PersonBalanceCard(balance = balance)
                }

                // SECTION 3: LIVE ACTIVITY FEED (if any)
                if (uiState.tripActivities.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Live Activity & Audit Feed (${uiState.tripActivities.size})",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Real-time changelog of spends, edits, and settlements",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(uiState.tripActivities.take(8)) { act ->
                        ActivityFeedCard(activity = act)
                    }
                }
            }

            // FIXED BOTTOM ACTION BUTTON: WHATSAPP SHARE STATEMENT
            val shareBtnInteraction = remember { MutableInteractionSource() }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Statement on WhatsApp / Message")
                        context.startActivity(shareIntent)
                    },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(),
                    interactionSource = shareBtnInteraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .bounceClick(shareBtnInteraction)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF25D366), Color(0xFF128C7E))
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Share Statement on WhatsApp",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettlementCard(
    settlement: TripSettlement,
    currentUserId: String,
    tripName: String,
    onRecordSettlement: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var isSettled by remember { mutableStateOf(false) }
    var showCustomUpiDialog by remember { mutableStateOf(false) }
    var customUpiInput by remember { mutableStateOf("") }

    val isCurrentDebtor = currentUserId.isNotBlank() && currentUserId == settlement.fromPerson.id
    val isCurrentCreditor = currentUserId.isNotBlank() && currentUserId == settlement.toPerson.id

    val recipientUpi = settlement.toPerson.upiId?.ifBlank { null }
        ?: settlement.toPerson.phoneNumber?.let { if (it.contains("@")) it else "$it@upi" }

    if (showCustomUpiDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCustomUpiDialog = false },
            title = {
                Text(
                    text = "Enter ${settlement.toPerson.name}'s UPI ID",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter the UPI address or VPA for ${settlement.toPerson.name} to proceed with instant UPI app payment.",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = customUpiInput,
                        onValueChange = { customUpiInput = it },
                        placeholder = { Text("e.g. mobile@upi or name@okaxis") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleaned = customUpiInput.trim()
                        if (cleaned.isNotBlank()) {
                            showCustomUpiDialog = false
                            val upiUri = android.net.Uri.parse(
                                "upi://pay?pa=$cleaned&pn=${android.net.Uri.encode(settlement.toPerson.name)}&am=${String.format(Locale.US, "%.2f", settlement.amount)}&cu=INR&tn=${android.net.Uri.encode("Splixter Settlement")}"
                            )
                            val upiIntent = Intent(Intent.ACTION_VIEW, upiUri)
                            try {
                                context.startActivity(upiIntent)
                            } catch (e: Exception) {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("UPI Info", "Pay ₹${String.format(Locale.US, "%.2f", settlement.amount)} to $cleaned")
                                clipboard.setPrimaryClip(clip)
                                android.widget.Toast.makeText(context, "UPI details copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Proceed to Pay", fontFamily = PlusJakartaSansFontFamily, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomUpiDialog = false }) {
                    Text("Cancel", fontFamily = PlusJakartaSansFontFamily)
                }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = appCardColors(),
        border = appCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    MonogramAvatar(
                        name = settlement.fromPerson.name,
                        color = settlement.fromPerson.color,
                        size = 34.dp,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = settlement.fromPerson.name,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isCurrentDebtor) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = Color(0xFFF43F5E).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "You",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF43F5E),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Owes",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFF43F5E)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    MonogramAvatar(
                        name = settlement.toPerson.name,
                        color = settlement.toPerson.color,
                        size = 34.dp,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = settlement.toPerson.name,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isCurrentCreditor) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "You",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Receives",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "₹${String.format(Locale.US, "%.2f", settlement.amount)}",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: UPI & Settle actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCurrentCreditor) {
                    // Creditor: Remind via WhatsApp button
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            val msg = "Hey ${settlement.fromPerson.name}, please settle ₹${String.format(Locale.US, "%.2f", settlement.amount)} for $tripName.${if (!recipientUpi.isNullOrBlank()) " My UPI is: $recipientUpi" else ""}"
                            val waIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, msg)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(waIntent, "Send Payment Reminder"))
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF25D366)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Text(
                            text = "💬 Send Reminder",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Debtor or Group participant: 1-Click UPI Payment
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            if (recipientUpi != null) {
                                val upiUri = android.net.Uri.parse(
                                    "upi://pay?pa=$recipientUpi&pn=${android.net.Uri.encode(settlement.toPerson.name)}&am=${String.format(Locale.US, "%.2f", settlement.amount)}&cu=INR&tn=${android.net.Uri.encode("Splixter Settlement")}"
                                )
                                val upiIntent = Intent(Intent.ACTION_VIEW, upiUri)
                                try {
                                    context.startActivity(upiIntent)
                                } catch (e: Exception) {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("UPI Payment Info", "Pay ₹${String.format(Locale.US, "%.2f", settlement.amount)} to ${settlement.toPerson.name} ($recipientUpi)")
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, "Payment info copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                showCustomUpiDialog = true
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Text(
                            text = if (isCurrentDebtor) "⚡ Pay ₹${String.format(Locale.US, "%.0f", settlement.amount)} (UPI)" else "⚡ Pay via UPI",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Mark Settled / Confirm Received Button
                Surface(
                    color = if (isSettled) Color(0xFF10B981).copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isSettled) Color(0xFF10B981).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .height(38.dp)
                        .clickable {
                            if (!isSettled) {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                isSettled = true
                                onRecordSettlement()
                                android.widget.Toast.makeText(context, "Marked settled & synced across members!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        if (isSettled) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Settled ✓",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        } else {
                            Text(
                                text = if (isCurrentCreditor) "Confirm Received" else "Mark Paid",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityFeedCard(activity: com.example.splixter.data.TripActivity) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = appCardColors(),
        border = appCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                val emoji = when (activity.actionType) {
                    "EXPENSE_ADDED" -> "💸"
                    "EXPENSE_DELETED" -> "🗑️"
                    "SETTLEMENT_PAID" -> "🤝"
                    "MEMBER_JOINED" -> "👋"
                    else -> "📝"
                }
                Text(text = emoji, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = activity.description,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = android.text.format.DateUtils.getRelativeTimeSpanString(activity.timestamp).toString(),
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonBalanceCard(balance: TripPersonBalance) {
    val isCreditor = balance.netBalance > 0.01
    val isDebtor = balance.netBalance < -0.01

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = appCardColors(),
        border = appCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MonogramAvatar(
                    name = balance.person.name,
                    color = balance.person.color,
                    size = 38.dp,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = balance.person.name,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Paid ₹${String.format(Locale.US, "%.0f", balance.totalPaid)} • Share ₹${String.format(Locale.US, "%.0f", balance.totalOwed)}",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val isDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f
            
            // High-contrast, WCAG AAA compliant colors
            val creditorTextColor = if (isDarkTheme) Color(0xFF4ADE80) else Color(0xFF15803D)
            val creditorBgColor = if (isDarkTheme) Color(0xFF064E3B).copy(alpha = 0.65f) else Color(0xFFDCFCE7)
            val creditorBorderColor = if (isDarkTheme) Color(0xFF059669) else Color(0xFF86EFAC)

            val debtorTextColor = if (isDarkTheme) Color(0xFFFB7185) else Color(0xFF9F1239)
            val debtorBgColor = if (isDarkTheme) Color(0xFF4C0519).copy(alpha = 0.65f) else Color(0xFFFFE4E6)
            val debtorBorderColor = if (isDarkTheme) Color(0xFFE11D48) else Color(0xFFFDA4AF)

            val settledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            val settledBgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            val settledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

            Surface(
                color = when {
                    isCreditor -> creditorBgColor
                    isDebtor -> debtorBgColor
                    else -> settledBgColor
                },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    1.dp,
                    when {
                        isCreditor -> creditorBorderColor
                        isDebtor -> debtorBorderColor
                        else -> settledBorderColor
                    }
                )
            ) {
                Text(
                    text = when {
                        isCreditor -> "+₹${String.format(Locale.US, "%.2f", balance.netBalance)}"
                        isDebtor -> "-₹${String.format(Locale.US, "%.2f", -balance.netBalance)}"
                        else -> "Settled"
                    },
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isCreditor -> creditorTextColor
                        isDebtor -> debtorTextColor
                        else -> settledTextColor
                    },
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                )
            }
        }
    }
}
