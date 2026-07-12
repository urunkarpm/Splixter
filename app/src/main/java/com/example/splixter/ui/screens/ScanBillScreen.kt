package com.example.splixter.ui.screens

import com.example.splixter.ui.components.bounceClick

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splixter.data.AppStep
import com.example.splixter.data.BillItem
import com.example.splixter.data.ItemCategory
import com.example.splixter.ui.SplitterUiState
import com.example.splixter.ui.SplitterViewModel
import com.example.splixter.ui.components.LiquidGlassBackground
import com.example.splixter.ui.components.glassCardColors
import com.example.splixter.ui.components.glassCardBorder
import com.example.splixter.util.CommonFoodItems
import java.util.Locale

@Composable
fun ScanBillScreen(
    uiState: SplitterUiState,
    viewModel: SplitterViewModel
) {
    val context = LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    var manualItemName by remember { mutableStateOf("") }
    var manualItemPrice by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ItemCategory.FOOD) }
    val itemNameFocusRequester = remember { FocusRequester() }
    val priceFocusRequester = remember { FocusRequester() }

    val suggestions = CommonFoodItems.getSuggestions(manualItemName)

    val onAddManualItem = {
        val price = manualItemPrice.toDoubleOrNull()
        if (manualItemName.isNotBlank() && price != null && price > 0.0) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            viewModel.addItem(manualItemName, price, selectedCategory)
            manualItemName = ""
            manualItemPrice = ""
            itemNameFocusRequester.requestFocus()
        } else {
            Toast.makeText(context, "Enter valid item name and price", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LiquidGlassBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
            Box(modifier = Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 4.dp)) {
                com.example.splixter.ui.components.WorkflowStepHeader(currentStep = AppStep.SCAN)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
            ) {

            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Add Bill Items",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Enter items ordered and their prices",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Manual Add Item Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = glassCardColors(),
                border = glassCardBorder(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualItemName,
                            onValueChange = {
                                manualItemName = it
                                selectedCategory = com.example.splixter.data.ItemCategory.guessFromName(it)
                            },
                            label = { Text("Item Name") },
                            placeholder = { Text("e.g. Pizza, Burger, Coffee") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(itemNameFocusRequester),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = manualItemPrice,
                            onValueChange = { manualItemPrice = it },
                            label = { Text("Price (₹)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .width(110.dp)
                                .focusRequester(priceFocusRequester),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { onAddManualItem() },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Item",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Category:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FoodLiquorToggle(
                            category = selectedCategory,
                            onToggle = {
                                selectedCategory = if (selectedCategory == com.example.splixter.data.ItemCategory.FOOD) {
                                    com.example.splixter.data.ItemCategory.LIQUOR
                                } else {
                                    com.example.splixter.data.ItemCategory.FOOD
                                }
                            }
                        )
                    }

                    // Autocomplete Dishes Suggestion Row
                    if (suggestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(suggestions) { dish ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.clickable {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        manualItemName = dish
                                        selectedCategory = com.example.splixter.data.ItemCategory.guessFromName(dish)
                                        priceFocusRequester.requestFocus()
                                    }
                                ) {
                                    Text(
                                        text = "+ $dish",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtotal Live Summary Row
            if (uiState.items.isNotEmpty()) {
                val totalSum = remember(uiState.items) { uiState.items.sumOf { it.price } }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = glassCardColors(),
                    border = glassCardBorder(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Items Added: ${uiState.items.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Subtotal: ₹${String.format(Locale.US, "%.2f", totalSum)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (uiState.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = glassCardColors(),
                        border = glassCardBorder(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🧾", fontSize = 44.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Items Added Yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Type item details above or tap quick suggestions to start!",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        ScannedItemCard(
                            item = item,
                            onDelete = { viewModel.removeItem(item.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Continue Button
            val scanContinueBtnInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            Button(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    viewModel.setStep(AppStep.ASSIGN)
                },
                enabled = uiState.items.isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(),
                interactionSource = scanContinueBtnInteractionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .bounceClick(scanContinueBtnInteractionSource)
                    .background(
                        if (uiState.items.isNotEmpty()) {
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFF0EA5E9))
                            )
                        } else {
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
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
                        text = "Next: Assign Items",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                }
            }
        }
    }
}
}
}

@Composable
fun ScannedItemCard(
    item: BillItem,
    onDelete: () -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = glassCardColors(),
        border = glassCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "₹${String.format(Locale.US, "%.2f", item.price)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onDelete()
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Item",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
