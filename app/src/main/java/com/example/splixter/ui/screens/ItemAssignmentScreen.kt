package com.example.splixter.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splixter.data.AppStep
import com.example.splixter.data.BillItem
import com.example.splixter.data.ItemCategory
import com.example.splixter.data.Person
import com.example.splixter.ui.SplitterUiState
import com.example.splixter.ui.SplitterViewModel
import java.util.Locale

@Composable
fun ItemAssignmentScreen(
    uiState: SplitterUiState,
    viewModel: SplitterViewModel
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var isTaxPercentage by remember { mutableStateOf(uiState.taxAndTip.isTaxPercentage) }
    var isDiscountPercentage by remember { mutableStateOf(uiState.taxAndTip.isDiscountPercentage) }
    
    var taxInput by remember { mutableStateOf(if (isTaxPercentage) (if (uiState.taxAndTip.taxPercentage == 0.0) "" else uiState.taxAndTip.taxPercentage.toString()) else (if (uiState.taxAndTip.taxAmount == 0.0) "" else uiState.taxAndTip.taxAmount.toString())) }
    var tipInput by remember { mutableStateOf(if (uiState.taxAndTip.tipAmount == 0.0) "" else uiState.taxAndTip.tipAmount.toString()) }
    var discountInput by remember { mutableStateOf(if (isDiscountPercentage) (if (uiState.taxAndTip.discountPercentage == 0.0) "" else uiState.taxAndTip.discountPercentage.toString()) else (if (uiState.taxAndTip.discountAmount == 0.0) "" else uiState.taxAndTip.discountAmount.toString())) }

    val updateAllValues = {
        val tax = taxInput.toDoubleOrNull() ?: 0.0
        val tip = tipInput.toDoubleOrNull() ?: 0.0
        val disc = discountInput.toDoubleOrNull() ?: 0.0
        viewModel.updateTaxAndTip(
            taxAmount = if (isTaxPercentage) 0.0 else tax,
            tipAmount = tip,
            discountAmount = if (isDiscountPercentage) 0.0 else disc,
            isTaxPercentage = isTaxPercentage,
            taxPercentage = if (isTaxPercentage) tax else 0.0,
            isDiscountPercentage = isDiscountPercentage,
            discountPercentage = if (isDiscountPercentage) disc else 0.0
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Box(modifier = Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 4.dp)) {
                com.example.splixter.ui.components.WorkflowStepHeader(currentStep = AppStep.ASSIGN)
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
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Who Ordered What?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Assign items and enter discounts",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }


            // Tax, Tip & Discounts Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tax", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                TypeToggle(isPercentage = isTaxPercentage, onToggleChange = { isTaxPercentage = it; updateAllValues() })
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = taxInput,
                                onValueChange = {
                                    taxInput = it
                                    updateAllValues()
                                },
                                label = { Text(if (isTaxPercentage) "Tax (%)" else "Tax (₹)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = tipInput,
                                onValueChange = {
                                    tipInput = it
                                    updateAllValues()
                                },
                                label = { Text("Tip (₹)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Discount", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                TypeToggle(isPercentage = isDiscountPercentage, onToggleChange = { isDiscountPercentage = it; updateAllValues() })
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = discountInput,
                                onValueChange = {
                                    discountInput = it
                                    updateAllValues()
                                },
                                label = { Text(if (isDiscountPercentage) "Discount (%)" else "Discount (₹)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Item Assignment List
            val onToggleItem: (String, String) -> Unit = remember(viewModel) {
                { itemId, personId -> viewModel.toggleItemAssignment(itemId, personId) }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(uiState.items, key = { it.id }) { item ->
                    AssignItemCard(
                        item = item,
                        people = uiState.people,
                        onToggleAssign = onToggleItem
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // View Split Bill Button
            Button(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    viewModel.setStep(AppStep.RECEIPT)
                },
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Calculate Final Bill",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssignItemCard(
    item: BillItem,
    people: List<Person>,
    onToggleAssign: (String, String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (item.category == ItemCategory.FOOD) "🍕" else "🍺",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "₹${String.format(Locale.US, "%.2f", item.price)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                people.forEach { person ->
                    val isAssigned = item.assignedPersonIds.contains(person.id)
                    PersonAssignChip(
                        person = person,
                        isAssigned = isAssigned,
                        onClick = { onToggleAssign(item.id, person.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonAssignChip(
    person: Person,
    isAssigned: Boolean,
    onClick: () -> Unit
) {
    val personColor = remember(person.color) { Color(person.color) }
    val backgroundColor = if (isAssigned) personColor else personColor.copy(alpha = 0.12f)
    val borderColor = if (isAssigned) personColor else personColor.copy(alpha = 0.3f)
    val textColor = if (isAssigned) Color.White else MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = "${person.emoji} ${person.name}",
                fontSize = 14.sp,
                fontWeight = if (isAssigned) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Composable
fun TypeToggle(
    isPercentage: Boolean,
    onToggleChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(50.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = if (!isPercentage) MaterialTheme.colorScheme.primary else Color.Transparent,
            shadowElevation = if (!isPercentage) 3.dp else 0.dp,
            modifier = Modifier.clip(RoundedCornerShape(50.dp)).clickable { onToggleChange(false) }
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "₹",
                    color = if (!isPercentage) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = if (isPercentage) MaterialTheme.colorScheme.primary else Color.Transparent,
            shadowElevation = if (isPercentage) 3.dp else 0.dp,
            modifier = Modifier.clip(RoundedCornerShape(50.dp)).clickable { onToggleChange(true) }
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "%",
                    color = if (isPercentage) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
