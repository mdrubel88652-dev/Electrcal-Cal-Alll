package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ElectricalApp
import com.example.data.calculator.CalculatorRegistry
import com.example.data.database.HistoryEntity
import com.example.ui.screens.calculators.FactoryLoadCalculatorView
import com.example.ui.screens.calculators.HouseWiringCalculatorView
import com.example.ui.screens.calculators.PfiCalculatorView
import com.example.ui.screens.calculators.SolarSystemCalculatorView
import com.example.data.model.CalculationResult
import com.example.pdf.PdfReportGenerator
import com.example.print.PrintManagerHelper
import com.example.ui.theme.ElectricBluePrimary
import com.example.ui.theme.ElectricBlueSecondary
import com.example.ui.theme.LevelAdvanceBg
import com.example.ui.theme.LevelAdvanceColor
import com.example.ui.theme.LevelBasicBg
import com.example.ui.theme.LevelBasicColor
import com.example.ui.theme.LevelLowerBg
import com.example.ui.theme.LevelLowerColor
import com.example.utils.ShareUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    calcId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val app = ElectricalApp.getApp(context)

    val devSettings by app.settingsManager.developerSettingsFlow.collectAsState(initial = com.example.data.datastore.DeveloperReportSettings())
    val pdfSettings by app.settingsManager.pdfPrintSettingsFlow.collectAsState(initial = com.example.data.datastore.PdfPrintSettings())
    val techProfile by app.settingsManager.technicianProfileFlow.collectAsState(initial = com.example.data.datastore.TechnicianReportProfile())

    val calculator = remember(calcId) {
        CalculatorRegistry.getById(calcId)
    }

    if (calculator == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Calculator #$calcId not found")
        }
        return
    }

    // Specialized views for upgraded calculators
    when (calcId) {
        1 -> {
            FactoryLoadCalculatorView(
                calculator = calculator,
                onNavigateBack = onNavigateBack,
                app = app,
                devSettings = devSettings,
                pdfSettings = pdfSettings,
                techProfile = techProfile
            )
            return
        }
        3 -> {
            HouseWiringCalculatorView(
                calculator = calculator,
                onNavigateBack = onNavigateBack,
                app = app,
                devSettings = devSettings,
                pdfSettings = pdfSettings,
                techProfile = techProfile
            )
            return
        }
        4 -> {
            SolarSystemCalculatorView(
                calculator = calculator,
                onNavigateBack = onNavigateBack,
                app = app,
                devSettings = devSettings,
                pdfSettings = pdfSettings,
                techProfile = techProfile
            )
            return
        }
        5 -> {
            PfiCalculatorView(
                calculator = calculator,
                onNavigateBack = onNavigateBack,
                app = app,
                devSettings = devSettings,
                pdfSettings = pdfSettings,
                techProfile = techProfile
            )
            return
        }
    }

    // Input States
    val inputValues = remember(calculator) {
        mutableStateMapOf<String, String>().apply {
            calculator.inputs.forEach { input ->
                put(input.id, input.defaultValue)
            }
        }
    }

    val unitSelections = remember(calculator) {
        mutableStateMapOf<String, String>().apply {
            calculator.inputs.forEach { input ->
                if (input.defaultUnit.isNotEmpty()) {
                    put(input.id, input.defaultUnit)
                }
            }
        }
    }

    var selectedPhase by remember(calculator) {
        mutableStateOf(
            if (calculator.isPhaseSelectable) {
                calculator.defaultPhase
            } else {
                "3-Phase"
            }
        )
    }

    var result by remember { mutableStateOf<CalculationResult?>(null) }
    var isSaved by remember { mutableStateOf(false) }
    var showFormulaSolution by remember { mutableStateOf(false) }

    fun runRecalculation(phaseToUse: String = selectedPhase) {
        try {
            if (calculator.isPhaseSelectable) {
                unitSelections["phase"] = phaseToUse
                unitSelections["is3Phase"] = phaseToUse
            }
            val numVals = inputValues.mapNotNull { (k, v) ->
                v.toDoubleOrNull()?.let { k to it }
            }.toMap()
            result = calculator.calculate(numVals, unitSelections)
            isSaved = false
        } catch (_: Exception) {}
    }

    // Auto-calculate initial state
    LaunchedEffect(calculator, selectedPhase) {
        if (calculator.isPhaseSelectable) {
            unitSelections["phase"] = selectedPhase
            unitSelections["is3Phase"] = selectedPhase
        }
        runRecalculation(selectedPhase)
    }

    val (levelColor, levelBg) = when (calculator.level) {
        com.example.data.model.CalculatorLevel.ADVANCE -> LevelAdvanceColor to LevelAdvanceBg
        com.example.data.model.CalculatorLevel.BASIC -> LevelBasicColor to LevelBasicBg
        com.example.data.model.CalculatorLevel.LOWER -> LevelLowerColor to LevelLowerBg
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("calculator_screen_container")
    ) {
        // 1. Compact Info Row (Title is already shown in the Top Bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = levelBg, shape = RoundedCornerShape(6.dp)) {
                    Text(
                        text = "#${calculator.id} • ${calculator.level.displayName}",
                        color = levelColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = calculator.category.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            if (calculator.standard.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF0284C7).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = calculator.standard,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Input Fields Section Header with Prominent Clear/Reset and Default Sample System
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "বোঝার সুবিধার্থে ডিফল্ট মান দেখানো হয়েছে। নিজের মান বসাতে রিসেট করুন।",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Reset / Clear Inputs button (clears inputs so user types without backspacing)
                    Surface(
                        onClick = {
                            calculator.inputs.forEach { input ->
                                inputValues[input.id] = ""
                            }
                            result = null
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("clear_inputs_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "রিসেট / খালি করুন",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }

                    // Default Example Values button
                    Surface(
                        onClick = {
                            calculator.inputs.forEach { input ->
                                inputValues[input.id] = input.defaultValue
                            }
                            runRecalculation()
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("default_values_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "💡 নমুনা মান লোড",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                        }
                    }
                }
            }
        }

        // Modern Clean Phase Selection Component
        if (calculator.isPhaseSelectable) {
            com.example.ui.components.ModernPhaseSelector(
                selectedPhase = selectedPhase,
                onPhaseSelected = { newPhase ->
                    selectedPhase = newPhase
                    unitSelections["phase"] = newPhase
                    unitSelections["is3Phase"] = newPhase
                    if (newPhase == "1-Phase") {
                        listOf("v", "voltage", "volts", "vlt", "sysV", "v2").forEach { vKey ->
                            if (inputValues[vKey] == "400" || inputValues[vKey] == "415") {
                                inputValues[vKey] = "230"
                            }
                        }
                    } else {
                        listOf("v", "voltage", "volts", "vlt", "sysV", "v2").forEach { vKey ->
                            if (inputValues[vKey] == "230" || inputValues[vKey] == "220") {
                                inputValues[vKey] = "400"
                            }
                        }
                    }
                    runRecalculation(newPhase)
                },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        val visibleInputs = calculator.inputs.filter { it.id != "phase" && it.id != "is3Phase" }

        visibleInputs.forEach { inputConfig ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputValues[inputConfig.id] ?: "",
                    onValueChange = { inputValues[inputConfig.id] = it },
                    label = { Text(inputConfig.label) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_${inputConfig.id}"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )

                if (inputConfig.unitOptions.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        modifier = Modifier
                            .height(56.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        ) {
                            Text(
                                text = unitSelections[inputConfig.id] ?: inputConfig.defaultUnit,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons: Calculate & Reset
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    try {
                        if (calculator.isPhaseSelectable) {
                            unitSelections["phase"] = selectedPhase
                            unitSelections["is3Phase"] = selectedPhase
                        }
                        val numVals = inputValues.mapNotNull { (k, v) ->
                            v.toDoubleOrNull()?.let { k to it }
                        }.toMap()
                        result = calculator.calculate(numVals, unitSelections)
                        isSaved = false
                    } catch (e: Exception) {
                        Toast.makeText(context, "Calculation error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1.3f)
                    .height(48.dp)
                    .testTag("calculate_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Calculate / হিসাব", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
            }

            OutlinedButton(
                onClick = {
                    calculator.inputs.forEach { input ->
                        inputValues[input.id] = input.defaultValue
                    }
                    runRecalculation()
                },
                modifier = Modifier
                    .weight(0.7f)
                    .height(48.dp)
                    .testTag("reset_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // 3. Result Presentation (Exact Match Screen 4 in reference image)
        result?.let { res ->
            Spacer(modifier = Modifier.height(14.dp))

            // Enhanced Modern Result Card (Dynamic text scaling, never breaks on large/long results)
            com.example.ui.components.ModernResultCard(result = res)

            // Compact Input Summary Card (Dynamic theme colors)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "ইনপুট ডাটা সামারি (Input Data)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    inputValues.forEach { (key, value) ->
                        val inputDef = calculator.inputs.find { it.id == key }
                        val label = inputDef?.label ?: key
                        val unit = unitSelections[key] ?: ""
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "$label :", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "$value $unit".trim(), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // Special Toggle Button for Formula & Step-by-Step Solution
            if (res.formulaUsed.isNotBlank() || res.steps.isNotEmpty()) {
                OutlinedButton(
                    onClick = { showFormulaSolution = !showFormulaSolution },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (showFormulaSolution) Color(0xFFEFF6FF) else Color.White
                    ),
                    border = BorderStroke(1.dp, if (showFormulaSolution) Color(0xFF0284C7) else Color(0xFFCBD5E1))
                ) {
                    Icon(
                        imageVector = if (showFormulaSolution) Icons.Default.ExpandLess else Icons.Default.Calculate,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (showFormulaSolution) "সূত্র ও স্টেপ সমাধান বন্ধ করুন" else "📐 Formula ও Step-by-Step Solution দেখুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF0284C7)
                    )
                }

                // Smoothly animated and displayed ONLY when the user clicks the button
                AnimatedVisibility(visible = showFormulaSolution) {
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Section "Formula"
                        if (res.formulaUsed.isNotBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Calculate, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Formula (সূত্র)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    ) {
                                        Text(
                                            text = res.formulaUsed,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Section "Step-by-Step Solution"
                        if (res.steps.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Step-by-Step Solution (ধাপে ধাপে সমাধান)",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    res.steps.forEachIndexed { idx, step ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF0284C7)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${idx + 1}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${step.equation} = ${step.result}",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (res.notes.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    res.notes.forEach { note ->
                        Text(text = "• $note", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Unified 4-Action Buttons Grid (Save, Copy, Share, PDF Report with direct Print)
            com.example.ui.components.CalculatorActionButtons(
                isSaved = isSaved,
                onSave = {
                    coroutineScope.launch {
                        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
                        val entity = HistoryEntity(
                            calculatorId = calculator.id,
                            calculatorName = calculator.name,
                            level = calculator.level.displayName,
                            category = calculator.category.displayName,
                            formattedDate = dateStr,
                            inputSummary = inputValues.entries.joinToString(", ") { "${it.key}: ${it.value}" },
                            primaryResult = res.primaryValue,
                            resultUnit = res.primaryUnit,
                            formula = res.formulaUsed,
                            stepSummary = res.steps.joinToString(" | ") { it.result }
                        )
                        app.historyRepository.insertHistory(entity)
                        isSaved = true
                        Toast.makeText(context, "Saved to history", Toast.LENGTH_SHORT).show()
                    }
                },
                onCopy = {
                    val copyText = "${calculator.name}\nResult: ${res.primaryValue} ${res.primaryUnit}\nFormula: ${res.formulaUsed}"
                    ShareUtils.copyToClipboard(context, calculator.name, copyText)
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                onShare = {
                    val shareText = "${calculator.name}\nResult: ${res.primaryValue} ${res.primaryUnit}\nFormula: ${res.formulaUsed}\nStandard: ${res.standardBasis}\nCalculated with ELECTRICAL CALCULATION ALL"
                    ShareUtils.shareText(context, calculator.name, shareText)
                },
                onPdf = {
                    try {
                        val pdf = PdfReportGenerator.generateCalculationPdf(
                            context,
                            calculator,
                            inputValues,
                            res,
                            devSettings,
                            pdfSettings,
                            techProfile
                        )
                        Toast.makeText(context, "PDF generated: ${pdf.name}", Toast.LENGTH_SHORT).show()
                        ShareUtils.sharePdf(context, pdf, "${calculator.name} Report")
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}
