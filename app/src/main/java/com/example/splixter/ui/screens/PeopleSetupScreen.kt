package com.example.splixter.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.splixter.data.AppStep
import com.example.splixter.data.BillHistoryRecord
import com.example.splixter.data.Person
import com.example.splixter.data.SavedGroup
import com.example.splixter.ui.SplitterUiState
import com.example.splixter.ui.SplitterViewModel
import com.example.splixter.ui.components.LiquidGlassBackground
import com.example.splixter.ui.components.glassCardColors
import com.example.splixter.ui.components.glassCardBorder
import com.example.splixter.ui.components.bounceClick
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PeopleSetupScreen(
    uiState: SplitterUiState,
    viewModel: SplitterViewModel
) {
    val context = LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var nameInput by remember { mutableStateOf("") }
    
    val contactPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let {
            val details = getContactDetails(context, it)
            if (details != null) {
                val (name, phone) = details
                viewModel.addPerson(name, phone)
            }
        }
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            contactPickerLauncher.launch(null)
        } else {
            Toast.makeText(context, "Permission denied to read contacts.", Toast.LENGTH_SHORT).show()
        }
    }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showInsightsDialog by remember { mutableStateOf(false) }
    var showSaveGroupDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var groupNameInput by remember { mutableStateOf("") }

    // Recent names from history, excluding already-added people
    val quickAddNames = remember(uiState.history, uiState.people) {
        val currentNames = uiState.people.map { it.name }.toSet()
        uiState.history
            .flatMap { it.people }
            .map { it.name }
            .distinct()
            .filter { it !in currentNames }
            .take(6)
    }

    val onAddPerson = {
        if (nameInput.isNotBlank()) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            viewModel.addPerson(nameInput.trim())
            nameInput = ""
        }
    }

    val isAnyDialogOpen = showSaveGroupDialog || showHistoryDialog || showInsightsDialog || showMoreMenu

    if (showHistoryDialog) {
        HistoryDialog(
            history = uiState.history,
            onLoadRecord = { recordId ->
                viewModel.loadBillFromHistory(recordId)
                showHistoryDialog = false
            },
            onDeleteRecord = { viewModel.deleteHistoryRecord(it) },
            onNewBill = { viewModel.clearAllData(); showHistoryDialog = false },
            onDismiss = { showHistoryDialog = false }
        )
    }

    if (showInsightsDialog) {
        InsightsDialog(history = uiState.history, onDismiss = { showInsightsDialog = false })
    }

    if (showSaveGroupDialog) {
        SaveGroupDialog(
            groupNameInput = groupNameInput,
            onGroupNameChange = { groupNameInput = it },
            onSave = {
                if (groupNameInput.isNotBlank()) {
                    viewModel.saveCurrentGroup(groupNameInput)
                    groupNameInput = ""
                    showSaveGroupDialog = false
                }
            },
            onDismiss = { showSaveGroupDialog = false }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LiquidGlassBackground {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(if (isAnyDialogOpen) 20.dp else 0.dp)
            ) {
                Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(bottom = 84.dp)
            ) {
                // Workflow step indicator
                Box(modifier = Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 4.dp)) {
                    com.example.splixter.ui.components.WorkflowStepHeader(currentStep = AppStep.PEOPLE)
                }

                // Header row: title + overflow menu
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Who's Splitting?",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Add everyone joining the bill",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                        )
                    }
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            shape = RoundedCornerShape(16.dp),
                            containerColor = MaterialTheme.colorScheme.surface,
                            shadowElevation = 3.dp,
                            modifier = Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(16.dp)
                            )
                        ) {
                            DropdownMenuItem(
                                text = { Text("New Bill", fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = { viewModel.clearAllData(); showMoreMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Bill History") },
                                leadingIcon = { Icon(Icons.Default.History, null) },
                                onClick = { showHistoryDialog = true; showMoreMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Spending Insights") },
                                leadingIcon = { Icon(Icons.Default.Analytics, null) },
                                onClick = { showInsightsDialog = true; showMoreMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text(if (uiState.isDarkMode) "Light Mode" else "Dark Mode") },
                                leadingIcon = {
                                    Icon(
                                        if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        null
                                    )
                                },
                                onClick = { viewModel.toggleDarkMode(); showMoreMenu = false }
                            )
                        }
                    }
                }

                // Name input bar - Redesigned as a unified Glassmorphic capsule
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = glassCardColors(isPayee = false),
                    border = glassCardBorder(isPayee = false),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            modifier = Modifier.size(22.dp)
                        )
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            placeholder = { Text("Enter a name...", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { onAddPerson() }),
                            modifier = Modifier.weight(1f),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        
                        // Contacts picker button
                        IconButton(
                            onClick = {
                                val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.READ_CONTACTS
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                
                                if (hasPermission) {
                                    contactPickerLauncher.launch(null)
                                } else {
                                    permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Contacts,
                                contentDescription = "Pick from contacts",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        val isInputBlank = nameInput.isBlank()
                        val scale by androidx.compose.animation.core.animateFloatAsState(
                            targetValue = if (isInputBlank) 0.85f else 1f,
                            label = "addButtonScale"
                        )
                        val buttonColor = if (isInputBlank) {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(
                                    if (isInputBlank) {
                                        Brush.linearGradient(listOf(buttonColor, buttonColor))
                                    } else {
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                Color(0xFF0EA5E9)
                                            )
                                        )
                                    }
                                )
                                .clickable(enabled = !isInputBlank) { onAddPerson() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add person",
                                tint = if (isInputBlank) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Quick-add suggestions from history
                if (quickAddNames.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickAddNames) { name ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.45f))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        viewModel.addPerson(name)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Saved group presets
                if (uiState.savedGroups.isNotEmpty() || uiState.people.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Saved Groups",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (uiState.people.isNotEmpty()) {
                            TextButton(
                                onClick = { showSaveGroupDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.BookmarkAdd, null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    if (uiState.savedGroups.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.savedGroups) { group ->
                                GroupPresetCard(
                                    group = group,
                                    onClick = {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        viewModel.loadSavedGroup(group.id)
                                    },
                                    onDelete = {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        viewModel.deleteSavedGroup(group.id)
                                    }
                                )
                            }
                        }
                    }
                }

                // Members section header
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.people.isEmpty()) "Members" else "Members (${uiState.people.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    val payer = uiState.people.find { it.id == uiState.paidByPersonId }
                    if (payer != null) {
                        Text(
                            text = "👑 ${payer.name} paid",
                            fontSize = 12.sp,
                            color = Color(0xFFFFB703),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (uiState.people.isNotEmpty() && uiState.paidByPersonId == null) {
                    Text(
                        text = "Tap 👑 on a card to mark who paid",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }

                // Avatar grid / empty state
                if (uiState.people.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val outlineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .drawBehind {
                                    val stroke = Stroke(
                                        width = 1.5.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                                    )
                                    drawRoundRect(
                                        color = outlineColor,
                                        style = stroke,
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx())
                                    )
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("👥", fontSize = 32.sp)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Your split group is empty",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Add participants by entering a name above\nor load a saved group preset",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(uiState.people) { person ->
                            PersonAvatarCard(
                                person = person,
                                isPayee = person.id == uiState.paidByPersonId,
                                hasPayeeSelected = uiState.paidByPersonId != null,
                                onRemove = { viewModel.removePerson(person.id) },
                                onSetPayee = { viewModel.setPaidByPerson(person.id) }
                            )
                        }
                    }
                }
            }

            // Sticky CTA
            if (uiState.people.isNotEmpty()) {
                val continueBtnInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                Button(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        if (uiState.people.size < 2) {
                            Toast.makeText(context, "Add at least 2 people to split!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.setStep(AppStep.SCAN)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(),
                    interactionSource = continueBtnInteractionSource,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .bounceClick(continueBtnInteractionSource)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    Color(0xFF0EA5E9)
                                )
                            )
                        ),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Continue with ${uiState.people.size} ${if (uiState.people.size == 1) "person" else "people"}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
}


@Composable
fun PersonAvatarCard(
    person: Person,
    isPayee: Boolean,
    hasPayeeSelected: Boolean,
    onRemove: () -> Unit,
    onSetPayee: () -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val personColor = Color(person.color)

    val activeBorder = if (isPayee) {
        BorderStroke(2.dp, Color(0xFF1DB954).copy(alpha = 0.8f))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }
    
    val activeColors = CardDefaults.cardColors(
        containerColor = if (isPayee) {
            Color(0xFF1DB954).copy(alpha = 0.08f).compositeOver(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f))
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
        }
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = activeColors,
        border = activeBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar circle with border and crown overlay
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(personColor.copy(alpha = 0.2f))
                        .border(1.5.dp, personColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = person.name.take(1).uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = personColor
                    )
                }
                if (isPayee) {
                    Text(
                        text = "👑",
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name + status
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = person.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (person.phoneNumber != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "📞",
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (isPayee) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1DB954).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "👑 Payer",
                                fontSize = 11.sp,
                                color = Color(0xFF1DB954),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                } else {
                    Text(
                        text = if (!hasPayeeSelected) "Tap check to mark as payer" else "Participant",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Payer selection toggle button on the right
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPayee) Color(0xFF1DB954)
                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )
                    .border(
                        width = if (isPayee) 0.dp else 1.5.dp,
                        color = if (isPayee) Color.Transparent else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                    .clickable {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onSetPayee()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = if (isPayee) "Payer" else "Mark as payer",
                    tint = if (isPayee) Color.White else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Remove button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onRemove()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}



@Composable
fun GroupPresetCard(
    group: SavedGroup,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .width(170.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📁", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = group.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(100.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onDelete()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete Group",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Stack of overlapping avatar emojis
                Box(
                    modifier = Modifier.height(24.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    val displayMembers = group.members.take(4)
                    displayMembers.forEachIndexed { index, person ->
                        Box(
                            modifier = Modifier
                                .offset(x = (index * 14).dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(person.color).copy(alpha = 0.35f))
                                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = person.name.take(1).uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(person.color)
                            )
                        }
                    }
                }

                // Count tag
                Text(
                    text = "${group.members.size} ppl",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SaveGroupDialog(
    groupNameInput: String,
    onGroupNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Save Current Group",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Save members as a reusable preset",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = groupNameInput,
                    onValueChange = onGroupNameChange,
                    label = { Text("Group Name") },
                    placeholder = { Text("e.g. Flatmates, Office Squad") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onSave,
                        enabled = groupNameInput.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Group", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun InsightsDialog(
    history: List<BillHistoryRecord>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Spending Insights",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val totalSpent = remember(history) { history.sumOf { it.totalAmount } }
                val avgSpent = remember(history) { if (history.isNotEmpty()) totalSpent / history.size else 0.0 }
                val topCompanion = remember(history) {
                    val counts = mutableMapOf<String, Int>()
                    history.forEach { rec ->
                        rec.people.forEach { p ->
                            counts[p.name] = (counts[p.name] ?: 0) + 1
                        }
                    }
                    counts.maxByOrNull { it.value }?.key ?: "N/A"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Total Spent", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("₹${String.format(Locale.US, "%.2f", totalSpent)}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Avg per Bill", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("₹${String.format(Locale.US, "%.2f", avgSpent)}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB703), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Top Dining Companion", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(topCompanion, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Split History Summary", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(history) { rec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(rec.timestamp)),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text("${rec.people.size} people • ${rec.items.size} items", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("₹${String.format(Locale.US, "%.2f", rec.totalAmount)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryDialog(
    history: List<BillHistoryRecord>,
    onLoadRecord: (String) -> Unit,
    onDeleteRecord: (String) -> Unit,
    onNewBill: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bill History",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = onNewBill,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = "New Bill",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No saved bills yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(history) { rec ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    val dateStr = remember(rec.timestamp) {
                                        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(rec.timestamp))
                                    }
                                    val paidPerson = rec.people.find { it.id == rec.paidByPersonId }
                                    val paidName = paidPerson?.name ?: "N/A"

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = dateStr,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "₹${String.format(Locale.US, "%.2f", rec.totalAmount)}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Paid by: $paidName • ${rec.people.size} people • ${rec.items.size} items",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(
                                            onClick = { onDeleteRecord(rec.id) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { onLoadRecord(rec.id) },
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = ButtonDefaults.ContentPadding
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Edit / Load", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getContactDetails(context: android.content.Context, contactUri: android.net.Uri): Pair<String, String?>? {
    var name: String? = null
    var phoneNumber: String? = null
    val contentResolver = context.contentResolver
    
    val cursor = contentResolver.query(contactUri, null, null, null, null)
    cursor?.use { c ->
        if (c.moveToFirst()) {
            val idIndex = c.getColumnIndex(android.provider.ContactsContract.Contacts._ID)
            val nameIndex = c.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME)
            
            if (idIndex >= 0 && nameIndex >= 0) {
                val contactId = c.getString(idIndex)
                name = c.getString(nameIndex)
                
                val hasPhoneIndex = c.getColumnIndex(android.provider.ContactsContract.Contacts.HAS_PHONE_NUMBER)
                val hasPhone = if (hasPhoneIndex >= 0) c.getString(hasPhoneIndex) else "0"
                
                if (hasPhone == "1") {
                    val phoneCursor = contentResolver.query(
                        android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(contactId),
                        null
                    )
                    phoneCursor?.use { pc ->
                        if (pc.moveToFirst()) {
                            val numberIndex = pc.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (numberIndex >= 0) {
                                phoneNumber = pc.getString(numberIndex)
                            }
                        }
                    }
                }
            }
        }
    }
    return if (name != null) Pair(name, phoneNumber) else null
}
