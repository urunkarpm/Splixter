package com.example.splixter.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splixter.data.AppStep
import com.example.splixter.data.BillItem
import com.example.splixter.data.ItemCategory
import com.example.splixter.ui.SplitterUiState
import com.example.splixter.ui.SplitterViewModel
import com.example.splixter.ui.components.AppBackground
import com.example.splixter.ui.components.EditItemDialog
import com.example.splixter.ui.components.PasteBillDialog
import com.example.splixter.ui.components.WorkflowStepHeader
import com.example.splixter.ui.components.appCardBorder
import com.example.splixter.ui.components.appCardColors
import com.example.splixter.ui.components.bounceClick
import com.example.splixter.ui.theme.PlusJakartaSansFontFamily
import com.example.splixter.util.ReceiptParser
import java.util.Locale

@Composable
fun ScanBillScreen(
    uiState: SplitterUiState,
    viewModel: SplitterViewModel
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Manual input state
    var manualItemName by remember { mutableStateOf("") }
    var manualItemPrice by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableIntStateOf(1) }
    var selectedCategory by remember { mutableStateOf(ItemCategory.FOOD) }

    // List filtering & search state
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var listCategoryFilter by remember { mutableStateOf<ItemCategory?>(null) }

    // Dialog states
    var itemToEdit by remember { mutableStateOf<BillItem?>(null) }
    var showPasteDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val itemNameFocusRequester = remember { FocusRequester() }
    val priceFocusRequester = remember { FocusRequester() }
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            searchFocusRequester.requestFocus()
        }
    }


    // Subtotal Calculations
    val totalSum by remember(uiState.items) {
        derivedStateOf { uiState.items.sumOf { it.price } }
    }
    val foodSum by remember(uiState.items) {
        derivedStateOf { uiState.items.filter { it.category == ItemCategory.FOOD }.sumOf { it.price } }
    }
    val liquorSum by remember(uiState.items) {
        derivedStateOf { uiState.items.filter { it.category == ItemCategory.LIQUOR }.sumOf { it.price } }
    }

    val foodCount = remember(uiState.items) { uiState.items.count { it.category == ItemCategory.FOOD } }
    val liquorCount = remember(uiState.items) { uiState.items.count { it.category == ItemCategory.LIQUOR } }

    // Ratio for visual progress bar
    val foodRatio = if (totalSum > 0) (foodSum / totalSum).toFloat() else 0f
    val liquorRatio = if (totalSum > 0) (liquorSum / totalSum).toFloat() else 0f

    // Filtered items in the list
    val filteredItems = remember(uiState.items, searchQuery, listCategoryFilter, isSearchActive) {
        uiState.items.filter { item ->
            val matchesQuery = !isSearchActive || searchQuery.isBlank() || item.name.contains(searchQuery.trim(), ignoreCase = true)
            val matchesCategory = listCategoryFilter == null || item.category == listCategoryFilter
            matchesQuery && matchesCategory
        }
    }

    val onAddManualItem = {
        val unitPrice = manualItemPrice.toDoubleOrNull()
        if (manualItemName.isNotBlank() && unitPrice != null && unitPrice > 0.0) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            val totalPrice = unitPrice * itemQuantity
            val nameWithQty = if (itemQuantity > 1) {
                "${manualItemName.trim()} (${itemQuantity}x)"
            } else {
                manualItemName.trim()
            }
            viewModel.addItem(nameWithQty, totalPrice, selectedCategory)

            // Reset inputs
            manualItemName = ""
            manualItemPrice = ""
            itemQuantity = 1
            itemNameFocusRequester.requestFocus()
        } else {
            Toast.makeText(context, "Please enter item name and valid price", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AppBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Workflow Step Indicator
                Box(modifier = Modifier.padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 4.dp)) {
                    WorkflowStepHeader(currentStep = AppStep.SCAN)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    // HERO STATUS CARD: Bill Total & Ratio Breakdown
                    HeroBillStatusCard(
                        totalSum = totalSum,
                        itemCount = uiState.items.size,
                        foodSum = foodSum,
                        liquorSum = liquorSum,
                        foodRatio = foodRatio,
                        liquorRatio = liquorRatio,
                        onPasteClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            showPasteDialog = true
                        },
                        onSampleClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            viewModel.addItems(ReceiptParser.getSampleBillItems())
                            Toast.makeText(context, "Loaded sample party bill", Toast.LENGTH_SHORT).show()
                        },
                        onClearClick = {
                            showClearConfirmDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // HIGH-SPEED ENTRY DECK
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = appCardColors(),
                        border = appCardBorder(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Category Segmented Switcher Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Food Tab
                                val isFood = selectedCategory == ItemCategory.FOOD
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isFood) MaterialTheme.colorScheme.primaryContainer
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                            selectedCategory = ItemCategory.FOOD
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🍲", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Food & Dishes",
                                            fontSize = 13.sp,
                                            fontWeight = if (isFood) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isFood) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Liquor Tab
                                val isLiquor = selectedCategory == ItemCategory.LIQUOR
                                val isDarkTheme = isSystemInDarkTheme()
                                val liquorTabColor = if (isDarkTheme) Color(0xFFFBBF24) else Color(0xFFB45309)
                                val liquorTabBg = if (isDarkTheme) Color(0xFFF59E0B).copy(alpha = 0.25f) else Color(0xFFFEF3C7)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isLiquor) liquorTabBg
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                            selectedCategory = ItemCategory.LIQUOR
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🍺", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Drinks & Bar",
                                            fontSize = 13.sp,
                                            fontWeight = if (isLiquor) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isLiquor) liquorTabColor else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Item Name Input
                            OutlinedTextField(
                                value = manualItemName,
                                onValueChange = {
                                    manualItemName = it
                                },
                                label = { Text("Item Name", fontSize = 13.sp) },
                                placeholder = {
                                    Text(
                                        if (selectedCategory == ItemCategory.FOOD) "e.g. Butter Chicken, Pizza, Naan"
                                        else "e.g. Beer Pint, Mojito, Whiskey",
                                        fontSize = 13.sp
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { priceFocusRequester.requestFocus() }),
                                trailingIcon = {
                                    if (manualItemName.isNotEmpty()) {
                                        IconButton(
                                            onClick = { manualItemName = "" },
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear",
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(itemNameFocusRequester),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quantity Stepper + Price Input + Add CTA Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Inline Quantity Stepper
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.height(52.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (itemQuantity > 1) {
                                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                                    itemQuantity--
                                                }
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Minus",
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }

                                        Text(
                                            text = "${itemQuantity}x",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )

                                        IconButton(
                                            onClick = {
                                                if (itemQuantity < 99) {
                                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                                    itemQuantity++
                                                }
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Plus",
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Price Input Field
                                OutlinedTextField(
                                    value = manualItemPrice,
                                    onValueChange = { manualItemPrice = it },
                                    label = { Text("Price (₹)", fontSize = 13.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            onAddManualItem()
                                            keyboardController?.hide()
                                        }
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(priceFocusRequester),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Add Button
                                val addInteraction = remember { MutableInteractionSource() }
                                Button(
                                    onClick = { onAddManualItem() },
                                    shape = RoundedCornerShape(14.dp),
                                    interactionSource = addInteraction,
                                    modifier = Modifier
                                        .height(52.dp)
                                        .bounceClick(addInteraction)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(MaterialTheme.colorScheme.primary, Color(0xFF0EA5E9))
                                            ),
                                            shape = RoundedCornerShape(14.dp)
                                        ),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Add",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // Dynamic Calculation / Price Adder Row
                            val unitPrice = manualItemPrice.toDoubleOrNull()
                            if (itemQuantity > 1 && unitPrice != null && unitPrice > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Subtotal: ₹${String.format(Locale.US, "%.2f", unitPrice * itemQuantity)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${itemQuantity} × ₹${String.format(Locale.US, "%.2f", unitPrice)})",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // LIST CONTROLS & FILTER TABS / SEARCH BAR (When items exist)
                    if (uiState.items.isNotEmpty()) {
                        AnimatedVisibility(
                            visible = isSearchActive,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text(
                                            "Search items in bill...",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(
                                                onClick = { searchQuery = "" },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Clear search",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Search
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onSearch = { keyboardController?.hide() }
                                    ),
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(searchFocusRequester),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        cursorColor = MaterialTheme.colorScheme.primary
                                    )
                                )

                                // Close Search Button
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                        isSearchActive = false
                                        searchQuery = ""
                                        keyboardController?.hide()
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = !isSearchActive,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Segmented Filter Tabs
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                        .padding(2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    FilterSegmentButton(
                                        title = "All (${uiState.items.size})",
                                        selected = listCategoryFilter == null,
                                        onClick = { listCategoryFilter = null }
                                    )
                                    FilterSegmentButton(
                                        title = "🍲 Food ($foodCount)",
                                        selected = listCategoryFilter == ItemCategory.FOOD,
                                        onClick = { listCategoryFilter = ItemCategory.FOOD }
                                    )
                                    FilterSegmentButton(
                                        title = "🍺 Bar ($liquorCount)",
                                        selected = listCategoryFilter == ItemCategory.LIQUOR,
                                        onClick = { listCategoryFilter = ItemCategory.LIQUOR }
                                    )
                                }

                                // Search Trigger
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                        .clickable {
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                            isSearchActive = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ITEMS LIST / EMPTY STATE
                    if (uiState.items.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                shape = RoundedCornerShape(22.dp),
                                colors = appCardColors(),
                                border = appCardBorder(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🧾", fontSize = 42.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Your Bill is Empty",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Add items above, paste a message breakdown, or load a sample party bill.",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { showPasteDialog = true },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("📋 Paste Text", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.addItems(ReceiptParser.getSampleBillItems())
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("✨ Load Sample Bill", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            if (filteredItems.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🔍", fontSize = 28.sp)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = if (isSearchActive && searchQuery.isNotBlank()) "No items matching \"${searchQuery.trim()}\"" else "No items found",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Check spelling or change category filter",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(filteredItems, key = { it.id }) { item ->
                                    ScannedItemCard(
                                        item = item,
                                        onEdit = { itemToEdit = item },
                                        onDuplicate = {
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                            viewModel.duplicateItem(item.id)
                                        },
                                        onDelete = {
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                            viewModel.removeItem(item.id)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // FIXED BOTTOM CTA BUTTON
                    val scanContinueBtnInteractionSource = remember { MutableInteractionSource() }
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            viewModel.setStep(AppStep.ASSIGN)
                        },
                        enabled = uiState.items.isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(),
                        interactionSource = scanContinueBtnInteractionSource,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .bounceClick(scanContinueBtnInteractionSource)
                            .background(
                                if (uiState.items.isNotEmpty()) {
                                    Brush.horizontalGradient(
                                        listOf(MaterialTheme.colorScheme.primary, Color(0xFF0EA5E9))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                }
                            ),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (uiState.items.isNotEmpty()) {
                                    "Next: Assign Items (${uiState.items.size} • ₹${String.format(Locale.US, "%.0f", totalSum)})"
                                } else {
                                    "Next: Assign Items"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }

    // Edit Item Dialog
    itemToEdit?.let { item ->
        EditItemDialog(
            item = item,
            onDismiss = { itemToEdit = null },
            onConfirm = { name, price, category ->
                viewModel.updateItem(item.id, name, price, category)
                itemToEdit = null
            }
        )
    }

    // Paste Text Dialog
    if (showPasteDialog) {
        PasteBillDialog(
            onDismiss = { showPasteDialog = false },
            onAddItems = { items ->
                viewModel.addItems(items)
                showPasteDialog = false
                Toast.makeText(context, "Added ${items.size} items", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Clear All Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear All Items?", fontWeight = FontWeight.Bold) },
            text = { Text("This will remove all ${uiState.items.size} items from the bill.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearItems()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HeroBillStatusCard(
    totalSum: Double,
    itemCount: Int,
    foodSum: Double,
    liquorSum: Double,
    foodRatio: Float,
    liquorRatio: Float,
    onPasteClick: () -> Unit,
    onSampleClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = appCardColors(),
        border = appCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Bill Total & Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "TOTAL BILL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 3.dp, end = 2.dp)
                        )
                        Text(
                            text = String.format(Locale.US, "%.2f", totalSum),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = "$itemCount item${if (itemCount != 1) "s" else ""}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Quick Action Bar
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Paste Pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { onPasteClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Paste",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Sample Pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.clickable { onSampleClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Sample",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Demo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Clear Trash
                    if (itemCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.clickable { onClearClick() }
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Visual Segmented Ratio Bar (when items > 0)
            if (totalSum > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (foodRatio > 0) {
                        Box(
                            modifier = Modifier
                                .weight(foodRatio.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    if (liquorRatio > 0) {
                        Box(
                            modifier = Modifier
                                .weight(liquorRatio.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(Color(0xFFF59E0B))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val isDark = isSystemInDarkTheme()
                val barTextColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)

                // Breakdown Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Food: ₹${String.format(Locale.US, "%.0f", foodSum)} (${(foodRatio * 100).toInt()}%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (liquorSum > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Bar: ₹${String.format(Locale.US, "%.0f", liquorSum)} (${(liquorRatio * 100).toInt()}%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = barTextColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSegmentButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .clickable {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ScannedItemCard(
    item: BillItem,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val barTextColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
    val barBadgeBg = if (isDark) Color(0xFFF59E0B).copy(alpha = 0.25f) else Color(0xFFFEF3C7)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = appCardColors(),
        border = appCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Category Icon Badge
            val isFood = item.category == ItemCategory.FOOD
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFood) MaterialTheme.colorScheme.primaryContainer
                        else barBadgeBg
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isFood) "🍲" else "🍺",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Item Name & Category
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isFood) "Food" else "Drinks / Bar",
                    fontSize = 11.sp,
                    color = if (isFood) MaterialTheme.colorScheme.onSurfaceVariant else barTextColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Price Tag
            Text(
                text = "₹${String.format(Locale.US, "%.2f", item.price)}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Actions Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Edit Button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Duplicate Button
                IconButton(
                    onClick = onDuplicate,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
