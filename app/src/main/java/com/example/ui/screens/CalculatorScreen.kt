package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
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
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("calculator_screen_container")
    ) {
        // 1. Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = levelBg, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = "#${calculator.id} • ${calculator.level.displayName}",
                            color = levelColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = calculator.category.displayName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = calculator.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = calculator.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Input Fields Section
        Text(
            text = "Input Parameters",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Phase Selection Component (Only for Single Phase / Three Phase Calculators)
        if (calculator.isPhaseSelectable) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("phase_selection_card"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, ElectricBluePrimary.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = ElectricBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "System Phase Selection / ফেজ নির্বাচন",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = ElectricBluePrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = selectedPhase,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBluePrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Phase Toggle Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Single Phase Option
                        val isSingleSelected = selectedPhase == "1-Phase"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSingleSelected) ElectricBluePrimary else Color.Transparent)
                                .clickable {
                                    selectedPhase = "1-Phase"
                                    unitSelections["phase"] = "1-Phase"
                                    unitSelections["is3Phase"] = "1-Phase"
                                    listOf("v", "voltage", "volts", "vlt", "sysV", "v2").forEach { vKey ->
                                        if (inputValues[vKey] == "400" || inputValues[vKey] == "415") {
                                            inputValues[vKey] = "230"
                                        }
                                    }
                                    runRecalculation("1-Phase")
                                }
                                .testTag("phase_single"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isSingleSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = "Single Phase (1φ • 230V)",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSingleSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSingleSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Three Phase Option
                        val isThreeSelected = selectedPhase == "3-Phase"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isThreeSelected) ElectricBluePrimary else Color.Transparent)
                                .clickable {
                                    selectedPhase = "3-Phase"
                                    unitSelections["phase"] = "3-Phase"
                                    unitSelections["is3Phase"] = "3-Phase"
                                    listOf("v", "voltage", "volts", "vlt", "sysV", "v2").forEach { vKey ->
                                        if (inputValues[vKey] == "230" || inputValues[vKey] == "220") {
                                            inputValues[vKey] = "400"
                                        }
                                    }
                                    runRecalculation("3-Phase")
                                }
                                .testTag("phase_three"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isThreeSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = "Three Phase (3φ • 400V)",
                                    fontSize = 12.sp,
                                    fontWeight = if (isThreeSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isThreeSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Strict Separation Banner
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(top = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "গুরুত্বপূর্ণ শর্ত: Single Phase ও Three Phase লোড একই ক্যালকুলেশনে মিশিয়ে হিসাব করা যাবে না।",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (selectedPhase == "1-Phase") {
                                        "বর্তমানে Single Phase (1φ Line-to-Neutral 230V) সূত্রে সম্পূর্ণ হিসাব সম্পন্ন হচ্ছে। সূত্র: I = P / (V × PF), P = V × I × PF, ΔV = 2×L×I×R/1000"
                                    } else {
                                        "বর্তমানে Three Phase (3φ Line-to-Line 400V) সূত্রে সম্পূর্ণ হিসাব সম্পন্ন হচ্ছে। সূত্র: I = P / (√3 × V × PF), P = √3 × V × I × PF, ΔV = √3×L×I×R/1000"
                                    },
                                    fontSize = 10.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }
                }
            }
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
                    .weight(1f)
                    .height(48.dp)
                    .testTag("calculate_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Calculate", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    if (calculator.isPhaseSelectable) {
                        selectedPhase = calculator.defaultPhase
                        unitSelections["phase"] = calculator.defaultPhase
                        unitSelections["is3Phase"] = calculator.defaultPhase
                    }
                    calculator.inputs.forEach { input ->
                        inputValues[input.id] = input.defaultValue
                    }
                    runRecalculation()
                },
                modifier = Modifier
                    .weight(0.6f)
                    .height(48.dp)
                    .testTag("reset_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset")
            }
        }

        // 3. Result Presentation Card
        result?.let { res ->
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("result_card"),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "FINAL RESULT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlueSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = res.primaryValue,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = res.primaryUnit,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    // Secondary results
                    if (res.secondaryResults.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        for ((label, value) in res.secondaryResults) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = value,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Formula
                    if (res.formulaUsed.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Text(
                            text = "Formula:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Text(
                                text = res.formulaUsed,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Steps
                    if (res.steps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Step-by-Step Solution:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            res.steps.forEach { step ->
                                Text(
                                    text = "${step.stepNumber}. ${step.title}: ${step.equation} = ${step.result}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Standard & Notes
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Standard / Basis: ${res.standardBasis}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    res.notes.forEach { note ->
                        Text(
                            text = "• $note",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Action Buttons Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Save to History
                    IconButton(
                        onClick = {
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
                        modifier = Modifier.testTag("action_save")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Check else Icons.Default.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (isSaved) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Copy
                    IconButton(
                        onClick = {
                            val copyText = "${calculator.name}\nResult: ${res.primaryValue} ${res.primaryUnit}\nFormula: ${res.formulaUsed}"
                            ShareUtils.copyToClipboard(context, calculator.name, copyText)
                        },
                        modifier = Modifier.testTag("action_copy")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }

                    // Share
                    IconButton(
                        onClick = {
                            val shareText = "${calculator.name}\nResult: ${res.primaryValue} ${res.primaryUnit}\nFormula: ${res.formulaUsed}\nStandard: ${res.standardBasis}\nCalculated with ELECTRICAL CALCULATION ALL"
                            ShareUtils.shareText(context, calculator.name, shareText)
                        },
                        modifier = Modifier.testTag("action_share")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }

                    // PDF
                    IconButton(
                        onClick = {
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
                                ShareUtils.sharePdf(context, pdf, "${calculator.name} Report")
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("action_pdf")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Report", tint = Color(0xFFC62828))
                    }

                    // Print
                    IconButton(
                        onClick = {
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
                                PrintManagerHelper.printPdfFile(context, pdf, calculator.name)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Print error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("action_print")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print", tint = ElectricBluePrimary)
                    }
                }
            }
        }
    }
}
