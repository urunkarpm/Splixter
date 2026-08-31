package com.example.splixter.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splixter.data.AppStep
import com.example.splixter.data.BillItem
import com.example.splixter.data.ItemCategory
import com.example.splixter.data.Person
import com.example.splixter.ui.SplitterUiState
import com.example.splixter.ui.SplitterViewModel
import com.example.splixter.ui.components.AppBackground
import com.example.splixter.ui.components.WorkflowStepHeader
import com.example.splixter.ui.components.appCardBorder
import com.example.splixter.ui.components.appCardColors
import com.example.splixter.ui.components.bounceClick
import com.example.splixter.ui.theme.PlusJakartaSansFontFamily
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemAssignmentScreen(
    uiState: SplitterUiState,
    viewModel: SplitterViewModel
) {
    val haptic = LocalHapticFeedback.current
    var isTaxPercentage by remember { mutableStateOf(uiState.taxAndTip.isTaxPercentage) }
    var isDiscountPercentage by remember { mutableStateOf(uiState.taxAndTip.isDiscountPercentage) }
    var isVatPercentage by remember { mutableStateOf(uiState.taxAndTip.isVatPercentage) }

    var taxInput by remember { mutableStateOf(if (isTaxPercentage) (if (uiState.taxAndTip.taxPercentage == 0.0) "" else uiState.taxAndTip.taxPercentage.toString()) else (if (uiState.taxAndTip.taxAmount == 0.0) "" else uiState.taxAndTip.taxAmount.toString())) }
    var tipInput by remember { mutableStateOf(if (uiState.taxAndTip.tipAmount == 0.0) "" else uiState.taxAndTip.tipAmount.toString()) }
    var discountInput by remember { mutableStateOf(if (isDiscountPercentage) (if (uiState.taxAndTip.discountPercentage == 0.0) "" else uiState.taxAndTip.discountPercentage.toString()) else (if (uiState.taxAndTip.discountAmount == 0.0) "" else uiState.taxAndTip.discountAmount.toString())) }
    var vatInput by remember { mutableStateOf(if (isVatPercentage) (if (uiState.taxAndTip.vatPercentage == 0.0) "" else uiState.taxAndTip.vatPercentage.toString()) else (if (uiState.taxAndTip.vatAmount == 0.0) "" else uiState.taxAndTip.vatAmount.toString())) }

    var isTaxesExpanded by remember { mutableStateOf(false) }

    val unassignedCount by remember(uiState.items) {
        derivedStateOf { uiState.items.count { it.assignedPersonIds.isEmpty() } }
    }

    val updateAllValues = {
        val tax = taxInput.toDoubleOrNull() ?: 0.0
        val tip = tipInput.toDoubleOrNull() ?: 0.0
        val disc = discountInput.toDoubleOrNull() ?: 0.0
        val vat = vatInput.toDoubleOrNull() ?: 0.0
        viewModel.updateTaxAndTip(
            taxAmount = if (isTaxPercentage) 0.0 else tax,
            tipAmount = tip,
            discountAmount = if (isDiscountPercentage) 0.0 else disc,
            isTaxPercentage = isTaxPercentage,
            taxPercentage = if (isTaxPercentage) tax else 0.0,
            isDiscountPercentage = isDiscountPercentage,
            discountPercentage = if (isDiscountPercentage) disc else 0.0,
            vatAmount = if (isVatPercentage) 0.0 else vat,
            isVatPercentage = isVatPercentage,
            vatPercentage = if (isVatPercentage) vat else 0.0
        )
    }

    // Build tax/tip summary pills
    val activeTaxesSummary = remember(taxInput, tipInput, discountInput, vatInput, isTaxPercentage, isDiscountPercentage, isVatPercentage) {
        val list = mutableListOf<String>()
        val taxVal = taxInput.toDoubleOrNull() ?: 0.0
        if (taxVal > 0.0) list.add(if (isTaxPercentage) "GST: $taxInput%" else "GST: ₹$taxInput")
        val tipVal = tipInput.toDoubleOrNull() ?: 0.0
        if (tipVal > 0.0) list.add("Tip: ₹$tipInput")
        val discVal = discountInput.toDoubleOrNull() ?: 0.0
        if (discVal > 0.0) list.add(if (isDiscountPercentage) "Disc: $discountInput%" else "Disc: ₹$discountInput")
        val vatVal = vatInput.toDoubleOrNull() ?: 0.0
        if (vatVal > 0.0) list.add(if (isVatPercentage) "VAT: $vatInput%" else "VAT: ₹$vatInput")
        list
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
            ) {
                // Header Bar with Back Button and Step Progress
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { viewModel.setStep(AppStep.SCAN) },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Scan",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                        WorkflowStepHeader(currentStep = AppStep.ASSIGN)
                    }

                    Spacer(modifier = Modifier.width(38.dp)) // Visual balance
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp)
                ) {
                    // Screen Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestaurantMenu,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Who Ordered What?",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Tap participants or 'All' to assign dishes",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Collapsible Tax, Tip & Discounts Accordion Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = appCardColors(),
                        border = appCardBorder(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Header Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                        isTaxesExpanded = !isTaxesExpanded
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Taxes, Tips & Discounts",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!isTaxesExpanded && activeTaxesSummary.isNotEmpty()) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.padding(end = 4.dp)
                                        ) {
                                            Text(
                                                text = activeTaxesSummary.joinToString(" • "),
                                                fontFamily = PlusJakartaSansFontFamily,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = if (isTaxesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (isTaxesExpanded) "Collapse" else "Expand",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Expanded Form Inputs
                            AnimatedVisibility(
                                visible = isTaxesExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Food GST", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                TypeToggle(isPercentage = isTaxPercentage, onToggleChange = { isTaxPercentage = it; updateAllValues() })
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedTextField(
                                                value = taxInput,
                                                onValueChange = {
                                                    taxInput = it
                                                    updateAllValues()
                                                },
                                                placeholder = { Text(if (isTaxPercentage) "GST %" else "GST ₹", fontSize = 12.sp) },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                                )
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Tip (₹)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedTextField(
                                                value = tipInput,
                                                onValueChange = {
                                                    tipInput = it
                                                    updateAllValues()
                                                },
                                                placeholder = { Text("Tip (₹)", fontSize = 12.sp) },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Discount", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                TypeToggle(isPercentage = isDiscountPercentage, onToggleChange = { isDiscountPercentage = it; updateAllValues() })
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedTextField(
                                                value = discountInput,
                                                onValueChange = {
                                                    discountInput = it
                                                    updateAllValues()
                                                },
                                                placeholder = { Text(if (isDiscountPercentage) "Disc %" else "Disc ₹", fontSize = 12.sp) },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                                )
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Liquor VAT", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                TypeToggle(isPercentage = isVatPercentage, onToggleChange = { isVatPercentage = it; updateAllValues() })
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedTextField(
                                                value = vatInput,
                                                onValueChange = {
                                                    vatInput = it
                                                    updateAllValues()
                                                },
                                                placeholder = { Text(if (isVatPercentage) "VAT %" else "VAT ₹", fontSize = 12.sp) },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Unassigned Items Warning Banner
                    AnimatedVisibility(
                        visible = unassignedCount > 0,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$unassignedCount item${if (unassignedCount > 1) "s" else ""} unassigned",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFD97706)
                                    )
                                }

                                Surface(
                                    color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                                    modifier = Modifier.clickable {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        viewModel.assignAllItemsToEveryone()
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Groups,
                                            contentDescription = null,
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Split All",
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Item Assignment List
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(uiState.items, key = { it.id }) { item ->
                            AssignItemCard(
                                item = item,
                                people = uiState.people,
                                onToggleAssign = { itemId, personId ->
                                    viewModel.toggleItemAssignment(itemId, personId)
                                },
                                onAssignAll = { itemId ->
                                    viewModel.assignAllToItem(itemId)
                                },
                                onClearItem = { itemId ->
                                    viewModel.clearItemAssignments(itemId)
                                },
                                onToggleCategory = { itemId ->
                                    viewModel.toggleItemCategory(itemId)
                                }
                            )
                        }
                    }

                    // Calculate Final Bill CTA Button
                    val assignContinueBtnInteractionSource = remember { MutableInteractionSource() }
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            viewModel.setStep(AppStep.RECEIPT)
                        },
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(),
                        interactionSource = assignContinueBtnInteractionSource,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .bounceClick(assignContinueBtnInteractionSource)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFF0EA5E9))
                                )
                            ),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (unassignedCount > 0) "Calculate Final Bill ($unassignedCount unassigned)" else "Calculate Final Bill",
                                fontFamily = PlusJakartaSansFontFamily,
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
    onToggleAssign: (String, String) -> Unit,
    onAssignAll: (String) -> Unit,
    onClearItem: (String) -> Unit,
    onToggleCategory: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isUnassigned = item.assignedPersonIds.isEmpty()
    val isAllAssigned = people.isNotEmpty() && item.assignedPersonIds.size == people.size

    val cardBorder = if (isUnassigned) {
        BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
    } else {
        appCardBorder()
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = appCardColors(),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Item Name, Food/Liquor toggle, Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FoodLiquorToggle(
                        category = item.category,
                        onToggle = { onToggleCategory(item.id) }
                    )
                }

                Text(
                    text = "₹${String.format(Locale.US, "%.2f", item.price)}",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Actions & Person Chips Row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // "All" Quick Toggle Chip
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        if (isAllAssigned) onClearItem(item.id) else onAssignAll(item.id)
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isAllAssigned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, if (isAllAssigned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isAllAssigned) Icons.Default.Check else Icons.Default.Groups,
                            contentDescription = null,
                            tint = if (isAllAssigned) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAllAssigned) "All ✓" else "All",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAllAssigned) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Individual Person Chips
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
    val haptic = LocalHapticFeedback.current
    val personColor = remember(person.color) { Color(person.color) }
    val backgroundColor = if (isAssigned) personColor else personColor.copy(alpha = 0.12f)
    val borderColor = if (isAssigned) personColor else personColor.copy(alpha = 0.3f)
    val textColor = if (isAssigned) Color.White else MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            onClick()
        },
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor,
        border = BorderStroke(1.2.dp, borderColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = person.name,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 12.sp,
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
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val outerBg = if (isDark) Color.Black.copy(alpha = 0.35f) else Color(0xFFE2E8F0)
    val outerBorderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFCBD5E1).copy(alpha = 0.6f)
    val activeBg = if (isDark) Color.White.copy(alpha = 0.25f) else Color.White
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(outerBg)
            .border(1.dp, outerBorderColor, RoundedCornerShape(50.dp))
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = if (!isPercentage) activeBg else Color.Transparent,
            shadowElevation = if (!isPercentage && !isDark) 2.dp else 0.dp,
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .clickable {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onToggleChange(false)
                }
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "₹",
                    color = if (!isPercentage) {
                        if (isDark) Color.White else MaterialTheme.colorScheme.primary
                    } else {
                        if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = if (isPercentage) activeBg else Color.Transparent,
            shadowElevation = if (isPercentage && !isDark) 2.dp else 0.dp,
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .clickable {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onToggleChange(true)
                }
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "%",
                    color = if (isPercentage) {
                        if (isDark) Color.White else MaterialTheme.colorScheme.primary
                    } else {
                        if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun FoodLiquorToggle(
    category: ItemCategory,
    onToggle: () -> Unit
) {
    val isLiquor = category == ItemCategory.LIQUOR
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val outerBg = if (isDark) Color.Black.copy(alpha = 0.35f) else Color(0xFFE2E8F0)
    val outerBorderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFCBD5E1).copy(alpha = 0.6f)
    val activeBg = if (isDark) Color.White.copy(alpha = 0.25f) else Color.White

    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(outerBg)
            .border(1.dp, outerBorderColor, RoundedCornerShape(50.dp))
            .clickable {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onToggle()
            }
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = if (!isLiquor) activeBg else Color.Transparent,
            shadowElevation = if (!isLiquor && !isDark) 2.dp else 0.dp,
            modifier = Modifier.clip(RoundedCornerShape(50.dp))
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Food",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 10.sp,
                    fontWeight = if (!isLiquor) FontWeight.Bold else FontWeight.Normal,
                    color = if (!isLiquor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = if (isLiquor) activeBg else Color.Transparent,
            shadowElevation = if (isLiquor && !isDark) 2.dp else 0.dp,
            modifier = Modifier.clip(RoundedCornerShape(50.dp))
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Liquor",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 10.sp,
                    fontWeight = if (isLiquor) FontWeight.Bold else FontWeight.Normal,
                    color = if (isLiquor) Color(0xFFE11D48) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
