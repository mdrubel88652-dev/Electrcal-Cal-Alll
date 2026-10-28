package com.example.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.R
import com.example.data.database.ContractProjectEntity
import com.example.data.database.ProjectPaymentEntity
import com.example.data.datastore.DeveloperReportSettings
import com.example.data.datastore.EngineerReportProfile
import com.example.data.datastore.PdfPrintSettings
import com.example.data.datastore.TechnicianReportProfile
import com.example.data.model.BuildingProjectData
import com.example.data.model.CalculationResult
import com.example.data.model.CalculatorDefinition
import com.example.data.model.MaterialItem
import com.example.data.model.MaterialEstimateRow
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    // Standard ISO A4 Dimensions in points (72 DPI)
    private const val A4_PORTRAIT_WIDTH = 595
    private const val A4_PORTRAIT_HEIGHT = 842

    private const val A4_LANDSCAPE_WIDTH = 842
    private const val A4_LANDSCAPE_HEIGHT = 595

    // Balanced Margins (approx 17 mm ~ 48 pt, within 15-20 mm)
    private const val MARGIN_LEFT = 48f
    private const val MARGIN_RIGHT = 48f
    private const val MARGIN_TOP = 42f
    private const val MARGIN_BOTTOM = 42f

    fun generateCalculationPdf(
        context: Context,
        calculator: CalculatorDefinition,
        inputs: Map<String, String>,
        result: CalculationResult,
        devSettings: DeveloperReportSettings = DeveloperReportSettings(),
        pdfSettings: PdfPrintSettings = PdfPrintSettings(),
        techProfile: TechnicianReportProfile = TechnicianReportProfile()
    ): File {
        val document = PdfDocument()

        val pageWidth = A4_PORTRAIT_WIDTH
        val pageHeight = A4_PORTRAIT_HEIGHT

        val contentStartX = MARGIN_LEFT
        val contentEndX = pageWidth - MARGIN_RIGHT
        val contentWidth = contentEndX - contentStartX

        val logoBitmap: Bitmap? = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_app_logo)
                ?: BitmapFactory.decodeResource(context.resources, R.drawable.img_app_icon)
        } catch (_: Exception) {
            null
        }

        val paint = Paint().apply { isAntiAlias = true }

        // We check if content requires 1 or 2 pages
        // Estimate height needed
        val hasTechProfile = techProfile.isNotEmpty()
        val techHeight = if (hasTechProfile) 75f else 45f
        val inputCount = inputs.size
        val inputsHeight = 25f + (inputCount * 16f)
        val stepsCount = result.steps.size
        val stepsHeight = if (pdfSettings.showSteps && stepsCount > 0) 25f + (stepsCount * 18f) else 0f
        val formulaHeight = if (pdfSettings.showFormula && result.formulaUsed.isNotEmpty()) 40f else 0f
        val notesHeight = if (pdfSettings.showNotes && result.notes.isNotEmpty()) 20f + (result.notes.size * 14f) else 0f

        val estimatedTotalHeight = techHeight + 65f + inputsHeight + formulaHeight + stepsHeight + 70f + notesHeight + 45f
        val totalPages = if (estimatedTotalHeight > (pageHeight - MARGIN_BOTTOM - 45f)) 2 else 1

        if (totalPages == 1) {
            // --- Single Page Layout ---
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var y = MARGIN_TOP

            // 1. UNIVERSAL TECHNICIAN HEADER (Picture, Name, Designation, License, Contact, Email, Address; No Apple Logo)
            y = drawUniversalTechnicianHeader(canvas, paint, techProfile, contentStartX, contentEndX, y)

            // 2. CALCULATION REPORT TITLE CARD
            y = drawCalculationHeaderCard(canvas, paint, calculator, contentStartX, contentEndX, y)

            // 3. INPUT DATA
            y = drawInputParameters(canvas, paint, calculator, inputs, contentStartX, contentEndX, y)

            // 4. FORMULA
            if (pdfSettings.showFormula && result.formulaUsed.isNotEmpty()) {
                y = drawFormulaSection(canvas, paint, result.formulaUsed, contentStartX, contentEndX, y)
            }

            // 5. STEP-BY-STEP SOLUTION
            if (pdfSettings.showSteps && result.steps.isNotEmpty()) {
                y = drawStepsSection(canvas, paint, result, contentStartX, contentEndX, y)
            }

            // 6. FINAL RESULT
            y = drawFinalResultBox(canvas, paint, result, contentStartX, contentEndX, y)

            // 7. STANDARD & NOTES
            y = drawStandardAndNotes(canvas, paint, result, pdfSettings, contentStartX, contentEndX, y)

            // 8. UNIVERSAL DEVELOPER FOOTER (Apple Logo, App Name, Developer Email, Page X of Y)
            drawUniversalDeveloperFooter(
                canvas = canvas,
                paint = paint,
                devSettings = devSettings,
                logoBitmap = logoBitmap,
                contentStartX = contentStartX,
                contentEndX = contentEndX,
                pageHeight = pageHeight.toFloat(),
                pageNumber = 1,
                totalPages = 1
            )

            document.finishPage(page)
        } else {
            // --- Multi-Page Layout (2 Pages) ---
            // Page 1: Universal Technician Header + Calculation Header + Inputs + Formula
            val page1Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page1 = document.startPage(page1Info)
            val canvas1 = page1.canvas

            var y1 = MARGIN_TOP
            y1 = drawUniversalTechnicianHeader(canvas1, paint, techProfile, contentStartX, contentEndX, y1)

            y1 = drawCalculationHeaderCard(canvas1, paint, calculator, contentStartX, contentEndX, y1)
            y1 = drawInputParameters(canvas1, paint, calculator, inputs, contentStartX, contentEndX, y1)

            if (pdfSettings.showFormula && result.formulaUsed.isNotEmpty()) {
                drawFormulaSection(canvas1, paint, result.formulaUsed, contentStartX, contentEndX, y1)
            }

            // Universal Developer Footer on Page 1
            drawUniversalDeveloperFooter(
                canvas = canvas1,
                paint = paint,
                devSettings = devSettings,
                logoBitmap = logoBitmap,
                contentStartX = contentStartX,
                contentEndX = contentEndX,
                pageHeight = pageHeight.toFloat(),
                pageNumber = 1,
                totalPages = 2
            )
            document.finishPage(page1)

            // Page 2: Header continuation + Steps + Final Result + Standard/Notes + Universal Developer Footer
            val page2Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
            val page2 = document.startPage(page2Info)
            val canvas2 = page2.canvas

            var y2 = MARGIN_TOP
            y2 = drawContinuationHeader(canvas2, paint, calculator, contentStartX, contentEndX, y2)

            if (pdfSettings.showSteps && result.steps.isNotEmpty()) {
                y2 = drawStepsSection(canvas2, paint, result, contentStartX, contentEndX, y2)
            }

            y2 = drawFinalResultBox(canvas2, paint, result, contentStartX, contentEndX, y2)
            drawStandardAndNotes(canvas2, paint, result, pdfSettings, contentStartX, contentEndX, y2)

            // Universal Developer Footer on Page 2
            drawUniversalDeveloperFooter(
                canvas = canvas2,
                paint = paint,
                devSettings = devSettings,
                logoBitmap = logoBitmap,
                contentStartX = contentStartX,
                contentEndX = contentEndX,
                pageHeight = pageHeight.toFloat(),
                pageNumber = 2,
                totalPages = 2
            )
            document.finishPage(page2)
        }

        val outputDir = File(context.cacheDir, "pdf_reports").apply { mkdirs() }
        val outputFile = File(outputDir, "Report_Calc_${calculator.id}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
        return outputFile
    }

    // 1. Draw Universal Technician Header (Technician picture, name, designation, govt license, contact, email, address)
    // CRITICAL: Does NOT display the developer's Apple logo in this header
    private fun drawUniversalTechnicianHeader(
        canvas: Canvas,
        paint: Paint,
        profile: TechnicianReportProfile,
        startX: Float,
        endX: Float,
        topY: Float
    ): Float {
        var y = topY

        val photoBitmap: Bitmap? = if (profile.photoUri.isNotBlank()) {
            try {
                if (profile.photoUri.startsWith("/")) {
                    BitmapFactory.decodeFile(profile.photoUri)
                } else null
            } catch (_: Exception) {
                null
            }
        } else null

        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())

        if (profile.isEmpty()) {
            // Clean general header if technician profile is not filled
            paint.color = Color.rgb(13, 71, 161)
            paint.textSize = 15f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("ELECTRICAL CALCULATION REPORT", startX, y + 14f, paint)

            paint.color = Color.rgb(100, 116, 139)
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Standard Engineering Report & Calculation Summary", startX, y + 28f, paint)

            val dateWidth = paint.measureText("Date: $dateStr")
            canvas.drawText("Date: $dateStr", endX - dateWidth, y + 14f, paint)

            y += 36f
            paint.color = Color.rgb(203, 213, 225)
            paint.strokeWidth = 1f
            canvas.drawLine(startX, y, endX, y, paint)
            return y + 12f
        }

        // Available profile information
        val hasPhoto = photoBitmap != null
        val photoSize = 48f
        val textStartX = if (hasPhoto) startX + photoSize + 12f else startX

        // Draw photo if present
        if (hasPhoto && photoBitmap != null) {
            val photoRect = RectF(startX, y, startX + photoSize, y + photoSize)
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRoundRect(photoRect, 6f, 6f, paint)

            val destRect = Rect(startX.toInt(), y.toInt(), (startX + photoSize).toInt(), (y + photoSize).toInt())
            canvas.drawBitmap(photoBitmap, null, destRect, paint)

            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(2, 132, 199)
            paint.strokeWidth = 1f
            canvas.drawRoundRect(photoRect, 6f, 6f, paint)
            paint.style = Paint.Style.FILL
        }

        // Technician details
        var currentY = y + 13f

        // 1. Technician Name
        if (profile.name.isNotBlank()) {
            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 13.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(profile.name, textStartX, currentY, paint)
            currentY += 14f
        }

        // 2. Designation / Position
        if (profile.designation.isNotBlank()) {
            paint.color = Color.rgb(2, 132, 199)
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(profile.designation, textStartX, currentY, paint)
            currentY += 12f
        }

        // 3. Government License if present
        if (profile.licenseName.isNotBlank() || profile.licenseNumber.isNotBlank()) {
            paint.color = Color.rgb(71, 85, 105)
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val licText = buildString {
                append("Govt License: ")
                if (profile.licenseName.isNotBlank()) append(profile.licenseName)
                if (profile.licenseNumber.isNotBlank()) append(" #${profile.licenseNumber}")
            }
            canvas.drawText(licText, textStartX, currentY, paint)
            currentY += 11f
        }

        // 4. Contact and Email (in one row if both present)
        val contactItems = mutableListOf<String>()
        if (profile.contactPhone.isNotBlank()) contactItems.add("Mob: ${profile.contactPhone}")
        if (profile.email.isNotBlank()) contactItems.add("Email: ${profile.email}")
        if (contactItems.isNotEmpty()) {
            paint.color = Color.rgb(71, 85, 105)
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(contactItems.joinToString("   |   "), textStartX, currentY, paint)
            currentY += 11f
        }

        // 5. Official Address
        if (profile.officialAddress.isNotBlank()) {
            paint.color = Color.rgb(100, 116, 139)
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val firstLine = profile.officialAddress.lines().firstOrNull { it.isNotBlank() } ?: profile.officialAddress
            canvas.drawText("Address: $firstLine", textStartX, currentY, paint)
            currentY += 11f
        }

        // Right side badge: "TECHNICIAN REPORT" + Date
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val titleText = "TECHNICIAN REPORT"
        val titleWidth = paint.measureText(titleText)
        canvas.drawText(titleText, endX - titleWidth, y + 13f, paint)

        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val dateWidth = paint.measureText("Date: $dateStr")
        canvas.drawText("Date: $dateStr", endX - dateWidth, y + 26f, paint)

        val headerBottom = maxOf(y + photoSize, currentY) + 6f
        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 1f
        canvas.drawLine(startX, headerBottom, endX, headerBottom, paint)

        return headerBottom + 12f
    }

    // 3. Draw Calculation Report Header Card
    private fun drawCalculationHeaderCard(
        canvas: Canvas,
        paint: Paint,
        calculator: CalculatorDefinition,
        startX: Float,
        endX: Float,
        topY: Float
    ): Float {
        val boxHeight = 56f

        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(startX, topY, endX, topY + boxHeight, 6f, 6f, paint)

        paint.color = Color.rgb(203, 213, 225)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(startX, topY, endX, topY + boxHeight, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // Title
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 13.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(calculator.name, startX + 12f, topY + 22f, paint)

        // Metadata
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        canvas.drawText(
            "Calculation ID: #${calculator.id}   |   Level: ${calculator.level.displayName}   |   Category: ${calculator.category.displayName}   |   Date: $dateStr",
            startX + 12f,
            topY + 42f,
            paint
        )

        return topY + boxHeight + 14f
    }

    // 4. Draw Input Parameters Table
    private fun drawInputParameters(
        canvas: Canvas,
        paint: Paint,
        calculator: CalculatorDefinition,
        inputs: Map<String, String>,
        startX: Float,
        endX: Float,
        topY: Float
    ): Float {
        var y = topY

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("INPUT DATA", startX, y + 10f, paint)
        y += 18f

        paint.color = Color.rgb(226, 232, 240)
        paint.strokeWidth = 1f
        canvas.drawLine(startX, y, endX, y, paint)
        y += 12f

        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        // Draw in 2 columns if many inputs
        val entries = inputs.entries.toList()
        val mid = (entries.size + 1) / 2
        val col2X = startX + ((endX - startX) * 0.52f)

        if (entries.size > 4) {
            for (i in 0 until mid) {
                val e1 = entries[i]
                val label1 = calculator.inputs.firstOrNull { it.id == e1.key }?.label ?: e1.key
                paint.color = Color.rgb(51, 65, 85)
                canvas.drawText("• $label1:  ${e1.value}", startX + 6f, y, paint)

                if (i + mid < entries.size) {
                    val e2 = entries[i + mid]
                    val label2 = calculator.inputs.firstOrNull { it.id == e2.key }?.label ?: e2.key
                    canvas.drawText("• $label2:  ${e2.value}", col2X, y, paint)
                }
                y += 15f
            }
        } else {
            for (e in entries) {
                val label = calculator.inputs.firstOrNull { it.id == e.key }?.label ?: e.key
                paint.color = Color.rgb(51, 65, 85)
                canvas.drawText("• $label:  ${e.value}", startX + 6f, y, paint)
                y += 15f
            }
        }

        return y + 6f
    }

    // 5. Draw Formula Section
    private fun drawFormulaSection(
        canvas: Canvas,
        paint: Paint,
        formula: String,
        startX: Float,
        endX: Float,
        topY: Float
    ): Float {
        var y = topY

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("FORMULA", startX, y + 10f, paint)
        y += 18f

        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(startX, y, endX, y + 26f, 4f, 4f, paint)

        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText(formula, startX + 10f, y + 17f, paint)

        return y + 36f
    }

    // 6. Draw Steps Section
    private fun drawStepsSection(
        canvas: Canvas,
        paint: Paint,
        result: CalculationResult,
        startX: Float,
        endX: Float,
        topY: Float
    ): Float {
        var y = topY

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("STEP-BY-STEP SOLUTION", startX, y + 10f, paint)
        y += 18f

        paint.color = Color.rgb(226, 232, 240)
        paint.strokeWidth = 1f
        canvas.drawLine(startX, y, endX, y, paint)
        y += 12f

        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        for (step in result.steps) {
            paint.color = Color.rgb(71, 85, 105)
            val stepText = "${step.stepNumber}. ${step.title}:   ${step.equation}  =  ${step.result}"
            canvas.drawText(stepText, startX + 6f, y, paint)
            y += 16f
        }

        return y + 6f
    }

    // 7. Draw Final Result Box
    private fun drawFinalResultBox(
        canvas: Canvas,
        paint: Paint,
        result: CalculationResult,
        startX: Float,
        endX: Float,
        topY: Float
    ): Float {
        val boxHeight = 56f

        // Soft electric blue fill with border
        paint.color = Color.rgb(227, 242, 253)
        canvas.drawRoundRect(startX, topY, endX, topY + boxHeight, 6f, 6f, paint)

        paint.color = Color.rgb(13, 71, 161)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f
        canvas.drawRoundRect(startX, topY, endX, topY + boxHeight, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(13, 71, 161)
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("FINAL RESULT", startX + 14f, topY + 18f, paint)

        paint.textSize = 17f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val resultString = "${result.primaryValue} ${result.primaryUnit}"
        canvas.drawText(resultString, startX + 14f, topY + 42f, paint)

        // Draw secondary result if available
        if (result.secondaryResults.isNotEmpty()) {
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.rgb(30, 41, 59)
            val secText = result.secondaryResults.joinToString("  |  ") { "${it.first}: ${it.second}" }
            canvas.drawText(secText.take(65), startX + ((endX - startX) * 0.45f), topY + 42f, paint)
        }

        return topY + boxHeight + 14f
    }

    // 8. Draw Standard & Notes
    private fun drawStandardAndNotes(
        canvas: Canvas,
        paint: Paint,
        result: CalculationResult,
        pdfSettings: PdfPrintSettings,
        startX: Float,
        endX: Float,
        topY: Float
    ): Float {
        var y = topY

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("STANDARD / ENGINEERING BASIS:  ${result.standardBasis}", startX, y + 10f, paint)
        y += 18f

        if (pdfSettings.showNotes && result.notes.isNotEmpty()) {
            paint.color = Color.rgb(100, 116, 139)
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            for (note in result.notes) {
                canvas.drawText("• Note: $note", startX, y + 8f, paint)
                y += 13f
            }
        }

        return y + 10f
    }

    // Draw Universal Developer Footer (on every page of all 167 calculators and material estimates)
    private fun drawUniversalDeveloperFooter(
        canvas: Canvas,
        paint: Paint,
        devSettings: DeveloperReportSettings,
        logoBitmap: Bitmap?,
        contentStartX: Float,
        contentEndX: Float,
        pageHeight: Float,
        pageNumber: Int,
        totalPages: Int
    ) {
        val footerHeight = 34f
        val footerY = pageHeight - MARGIN_BOTTOM - footerHeight

        // Top divider
        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 1f
        canvas.drawLine(contentStartX, footerY, contentEndX, footerY, paint)

        // Draw Apple logo asset / app icon beside app name
        val logoSize = 22f
        val logoY = footerY + 8f

        if (logoBitmap != null) {
            val srcRect = Rect(0, 0, logoBitmap.width, logoBitmap.height)
            val dstRect = RectF(contentStartX, logoY, contentStartX + logoSize, logoY + logoSize)
            canvas.drawBitmap(logoBitmap, srcRect, dstRect, paint)
        }

        val textStartX = if (logoBitmap != null) contentStartX + logoSize + 8f else contentStartX

        // App Name
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Electrical Calculation All", textStartX, footerY + 16f, paint)

        // Developer Email retrieved from Developer Profile
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val devEmail = devSettings.email.ifBlank { "mdrubel88652@gmail.com" }
        canvas.drawText("Developer: $devEmail", textStartX, footerY + 27f, paint)

        // Page Numbering: Page X of Y (aligned right)
        val pageText = "Page $pageNumber of $totalPages"
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val textWidth = paint.measureText(pageText)
        canvas.drawText(pageText, contentEndX - textWidth, footerY + 20f, paint)
    }

    private fun drawContinuationHeader(
        canvas: Canvas,
        paint: Paint,
        calculator: CalculatorDefinition,
        startX: Float,
        endX: Float,
        topY: Float
    ): Float {
        var y = topY
        paint.color = Color.rgb(13, 71, 161)
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ELECTRICAL CALCULATION ALL — ${calculator.name} (Continued)", startX, y + 12f, paint)
        y += 18f

        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 1f
        canvas.drawLine(startX, y, endX, y, paint)

        return y + 14f
    }

    // Universal Material Estimate Multi-Page PDF (A4 Portrait, Unlimited Rows, Universal Header & Footer)
    fun generateMaterialEstimatePdf(
        context: Context,
        estimateTitle: String,
        projectName: String,
        clientName: String,
        location: String,
        dateStr: String,
        materials: List<MaterialEstimateRow>,
        techProfile: TechnicianReportProfile = TechnicianReportProfile(),
        devSettings: DeveloperReportSettings = DeveloperReportSettings()
    ): File {
        val document = PdfDocument()

        val pageWidth = A4_PORTRAIT_WIDTH
        val pageHeight = A4_PORTRAIT_HEIGHT

        val contentStartX = MARGIN_LEFT
        val contentEndX = pageWidth - MARGIN_RIGHT

        val logoBitmap: Bitmap? = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_app_logo)
                ?: BitmapFactory.decodeResource(context.resources, R.drawable.img_app_icon)
        } catch (_: Exception) {
            null
        }

        val paint = Paint().apply { isAntiAlias = true }

        val footerHeight = 34f
        val maxContentY = pageHeight - MARGIN_BOTTOM - footerHeight - 12f
        val rowHeight = 20f

        // Paginate materials
        val pages = mutableListOf<List<MaterialEstimateRow>>()
        var currentIndex = 0
        var isFirstPage = true

        while (currentIndex < materials.size || (materials.isEmpty() && isFirstPage)) {
            val availableHeight = if (isFirstPage) {
                // Top header ~75f + Project Info ~54f + Table Header 22f
                maxContentY - (MARGIN_TOP + 75f + 54f + 22f)
            } else {
                // Continuation Header ~32f + Table Header 22f
                maxContentY - (MARGIN_TOP + 32f + 22f)
            }

            val rowsThisPage = maxOf(1, (availableHeight / rowHeight).toInt())
            val endIndex = minOf(currentIndex + rowsThisPage, materials.size)
            pages.add(materials.subList(currentIndex, endIndex))
            currentIndex = endIndex
            isFirstPage = false
            if (materials.isEmpty()) break
        }

        val totalPages = maxOf(1, pages.size)

        for ((pageIndex, pageItems) in pages.withIndex()) {
            val pageNumber = pageIndex + 1
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var y = MARGIN_TOP

            if (pageNumber == 1) {
                // 1. Universal Technician Header
                y = drawUniversalTechnicianHeader(canvas, paint, techProfile, contentStartX, contentEndX, y)

                // 2. Project Information Card
                val infoHeight = 48f
                paint.color = Color.rgb(248, 250, 252)
                canvas.drawRoundRect(contentStartX, y, contentEndX, y + infoHeight, 6f, 6f, paint)

                paint.color = Color.rgb(226, 232, 240)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f
                canvas.drawRoundRect(contentStartX, y, contentEndX, y + infoHeight, 6f, 6f, paint)
                paint.style = Paint.Style.FILL

                paint.color = Color.rgb(15, 23, 42)
                paint.textSize = 9.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("ESTIMATE: ${estimateTitle.ifBlank { "Electrical Material List" }}", contentStartX + 10f, y + 16f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.rgb(71, 85, 105)
                paint.textSize = 8.5f
                canvas.drawText("Project: ${projectName.ifBlank { "General Work" }}   |   Client: ${clientName.ifBlank { "Direct Client" }}", contentStartX + 10f, y + 30f, paint)
                canvas.drawText("Location: ${location.ifBlank { "-" }}   |   Date: ${dateStr.ifBlank { SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date()) }}", contentStartX + 10f, y + 42f, paint)

                y += infoHeight + 12f
            } else {
                // Continuation Header on subsequent pages
                paint.color = Color.rgb(13, 71, 161)
                paint.textSize = 11f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("ESTIMATE: ${estimateTitle.ifBlank { "Electrical Material List" }} (Continued)", contentStartX, y + 12f, paint)

                paint.color = Color.rgb(100, 116, 139)
                paint.textSize = 8.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("Project: $projectName   |   Client: $clientName", contentStartX, y + 24f, paint)

                y += 32f
                paint.color = Color.rgb(203, 213, 225)
                paint.strokeWidth = 1f
                canvas.drawLine(contentStartX, y, contentEndX, y, paint)
                y += 10f
            }

            // Table Columns Definition (A4 Portrait width: contentEndX - contentStartX = 499f)
            val colSl = contentStartX + 8f
            val colName = contentStartX + 36f
            val colSpec = contentStartX + 230f
            val colQty = contentStartX + 390f
            val colUnit = contentStartX + 445f

            // Table Header Bar (Repeated on every page!)
            val tableHeaderHeight = 22f
            paint.color = Color.rgb(13, 71, 161)
            canvas.drawRoundRect(contentStartX, y, contentEndX, y + tableHeaderHeight, 4f, 4f, paint)

            paint.color = Color.WHITE
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            canvas.drawText("SL", colSl, y + 15f, paint)
            canvas.drawText("Material Name", colName, y + 15f, paint)
            canvas.drawText("Size / Specification", colSpec, y + 15f, paint)
            canvas.drawText("Quantity", colQty, y + 15f, paint)
            canvas.drawText("Unit", colUnit, y + 15f, paint)

            y += tableHeaderHeight + 2f

            // Table Rows
            paint.textSize = 8.5f
            for (item in pageItems) {
                if (item.sl % 2 == 0) {
                    paint.color = Color.rgb(248, 250, 252)
                    canvas.drawRect(contentStartX, y, contentEndX, y + rowHeight, paint)
                }

                paint.color = Color.rgb(241, 245, 249)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.8f
                canvas.drawLine(contentStartX, y + rowHeight, contentEndX, y + rowHeight, paint)
                paint.style = Paint.Style.FILL

                paint.color = Color.rgb(15, 23, 42)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(item.sl.toString(), colSl, y + 14f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(item.name.take(36), colName, y + 14f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.rgb(71, 85, 105)
                canvas.drawText(item.spec.take(28), colSpec, y + 14f, paint)

                paint.color = Color.rgb(2, 132, 199)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val qtyStr = if (item.qty % 1.0 == 0.0) item.qty.toInt().toString() else String.format(Locale.US, "%.1f", item.qty)
                canvas.drawText(qtyStr, colQty, y + 14f, paint)

                paint.color = Color.rgb(71, 85, 105)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(item.unit.take(12), colUnit, y + 14f, paint)

                y += rowHeight
            }

            // Draw Universal Developer Footer on every page
            drawUniversalDeveloperFooter(
                canvas = canvas,
                paint = paint,
                devSettings = devSettings,
                logoBitmap = logoBitmap,
                contentStartX = contentStartX,
                contentEndX = contentEndX,
                pageHeight = pageHeight.toFloat(),
                pageNumber = pageNumber,
                totalPages = totalPages
            )

            document.finishPage(page)
        }

        val outputDir = File(context.cacheDir, "material_estimates").apply { mkdirs() }
        val outputFile = File(outputDir, "Material_Estimate_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
        return outputFile
    }

    // Material Estimate PDF — Centered on A4 Landscape (842 x 595 pt)
    fun generateMaterialListPdf(
        context: Context,
        project: BuildingProjectData,
        materials: List<MaterialItem>,
        devSettings: DeveloperReportSettings = DeveloperReportSettings()
    ): File {
        val document = PdfDocument()

        val pageWidth = A4_LANDSCAPE_WIDTH
        val pageHeight = A4_LANDSCAPE_HEIGHT

        val contentStartX = MARGIN_LEFT
        val contentEndX = pageWidth - MARGIN_RIGHT
        val contentWidth = contentEndX - contentStartX

        val logoBitmap: Bitmap? = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_app_logo)
                ?: BitmapFactory.decodeResource(context.resources, R.drawable.img_app_icon)
        } catch (_: Exception) {
            null
        }

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        var y = MARGIN_TOP

        // Top Header
        val logoSize = 34f
        if (logoBitmap != null) {
            val srcRect = Rect(0, 0, logoBitmap.width, logoBitmap.height)
            val dstRect = RectF(contentStartX, y, contentStartX + logoSize, y + logoSize)
            canvas.drawBitmap(logoBitmap, srcRect, dstRect, paint)
        }

        val textStartX = if (logoBitmap != null) contentStartX + logoSize + 12f else contentStartX

        paint.color = Color.rgb(13, 71, 161)
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ELECTRICAL MATERIAL REQUIREMENT LIST", textStartX, y + 14f, paint)

        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Electrical Engineering Calculation & Estimation", textStartX, y + 28f, paint)

        y += logoSize + 8f

        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 1.2f
        canvas.drawLine(contentStartX, y, contentEndX, y, paint)
        y += 14f

        // Project Info Card
        val infoHeight = 44f
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(contentStartX, y, contentEndX, y + infoHeight, 4f, 4f, paint)

        paint.color = Color.rgb(226, 232, 240)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(contentStartX, y, contentEndX, y + infoHeight, 4f, 4f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Project: ${project.projectName}", contentStartX + 10f, y + 16f, paint)
        canvas.drawText("Owner: ${project.ownerName}", contentStartX + 260f, y + 16f, paint)
        canvas.drawText("Prepared By: ${project.preparedBy}", contentStartX + 520f, y + 16f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("Address: ${project.address}", contentStartX + 10f, y + 32f, paint)
        canvas.drawText("Date: ${project.date}   |   Floors: ${project.floors}, Rooms: ${project.rooms}", contentStartX + 260f, y + 32f, paint)
        y += infoHeight + 14f

        // Table Header (Width = 752pt)
        paint.color = Color.rgb(13, 71, 161)
        canvas.drawRect(contentStartX, y, contentEndX, y + 20f, paint)

        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val colNo = contentStartX + 6f
        val colName = contentStartX + 40f
        val colSpec = contentStartX + 220f
        val colUnit = contentStartX + 460f
        val colQty = contentStartX + 540f
        val colRem = contentStartX + 610f

        canvas.drawText("No.", colNo, y + 14f, paint)
        canvas.drawText("Material Name", colName, y + 14f, paint)
        canvas.drawText("Specification", colSpec, y + 14f, paint)
        canvas.drawText("Unit", colUnit, y + 14f, paint)
        canvas.drawText("Qty", colQty, y + 14f, paint)
        canvas.drawText("Remarks", colRem, y + 14f, paint)
        y += 22f

        // Table Rows
        paint.textSize = 8.5f
        for ((idx, item) in materials.take(18).withIndex()) {
            if (idx % 2 == 1) {
                paint.color = Color.rgb(248, 250, 252)
                canvas.drawRect(contentStartX, y, contentEndX, y + 16f, paint)
            }
            paint.color = Color.rgb(15, 23, 42)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            canvas.drawText(item.no.toString(), colNo, y + 12f, paint)
            canvas.drawText(item.name.take(30), colName, y + 12f, paint)
            canvas.drawText(item.specification.take(40), colSpec, y + 12f, paint)
            canvas.drawText(item.unit.take(12), colUnit, y + 12f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(item.quantity.toString(), colQty, y + 12f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.rgb(100, 116, 139)
            canvas.drawText(item.remarks.take(24), colRem, y + 12f, paint)

            y += 16f
        }

        // Bottom Signature and Fixed Engineer Profile
        val footerY = pageHeight - MARGIN_BOTTOM - 45f
        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 1f
        canvas.drawLine(contentStartX, footerY, contentEndX, footerY, paint)

        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Prepared By: ${project.preparedBy}", contentStartX + 10f, footerY + 16f, paint)
        canvas.drawText("Owner Signature: ________________________", contentStartX + 220f, footerY + 16f, paint)
        canvas.drawText("Engineer: ${EngineerReportProfile.DEFAULT.name}", contentStartX + 460f, footerY + 16f, paint)
        canvas.drawText("Page 1 of 1", contentEndX - 55f, footerY + 16f, paint)

        document.finishPage(page)

        val outputDir = File(context.cacheDir, "material_reports").apply { mkdirs() }
        val outputFile = File(outputDir, "Material_List_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
        return outputFile
    }

    fun generateProjectContractPdf(
        context: Context,
        project: ContractProjectEntity,
        payments: List<ProjectPaymentEntity>,
        techProfile: TechnicianReportProfile = TechnicianReportProfile()
    ): File {
        val document = PdfDocument()
        val pageWidth = A4_PORTRAIT_WIDTH
        val pageHeight = A4_PORTRAIT_HEIGHT

        val contentStartX = MARGIN_LEFT
        val contentEndX = pageWidth - MARGIN_RIGHT

        val logoBitmap: Bitmap? = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_app_logo)
                ?: BitmapFactory.decodeResource(context.resources, R.drawable.img_app_icon)
        } catch (_: Exception) {
            null
        }

        val paint = Paint().apply { isAntiAlias = true }

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var y = MARGIN_TOP

        // Universal Technician Header (Picture, Name, Designation, License, Contact, Email, Address; No Apple Logo)
        y = drawUniversalTechnicianHeader(canvas, paint, techProfile, contentStartX, contentEndX, y)

        // Project Title Banner
        y += 8f
        paint.color = Color.rgb(2, 132, 199)
        paint.strokeWidth = 2.5f
        canvas.drawLine(contentStartX, y, contentEndX, y, paint)

        y += 16f
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ELECTRICAL CONTRACT & PAYMENT STATEMENT", contentStartX, y, paint)

        y += 14f
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val dateStr = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        canvas.drawText("Generated on: $dateStr • Contract ID: #PRJ-${project.id}", contentStartX, y, paint)

        y += 16f
        // Project Overview Box
        val boxHeight = 72f
        paint.color = Color.rgb(241, 245, 249)
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(contentStartX, y, contentEndX, y + boxHeight), 6f, 6f, paint)

        paint.color = Color.rgb(203, 213, 225)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(contentStartX, y, contentEndX, y + boxHeight), 6f, 6f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Project: ${project.projectName}", contentStartX + 12f, y + 18f, paint)

        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("Client: ${if (project.clientName.isNotBlank()) project.clientName else "N/A"}", contentStartX + 12f, y + 34f, paint)
        canvas.drawText("Location: ${if (project.location.isNotBlank()) project.location else "N/A"}", contentStartX + 12f, y + 48f, paint)
        canvas.drawText("Contact: ${if (project.contactPhone.isNotBlank()) project.contactPhone else "N/A"}  |  Email: ${if (project.emailAddress.isNotBlank()) project.emailAddress else "N/A"}", contentStartX + 12f, y + 62f, paint)

        // Financial Summary on Right Side of Box
        val totalReceived = payments.sumOf { it.amount }
        val remainingDue = project.contractAmount - totalReceived
        val isPaidInFull = remainingDue <= 0

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10f
        canvas.drawText("Contract Total: BDT ${String.format(Locale.US, "%,.2f", project.contractAmount)}", contentStartX + 290f, y + 18f, paint)

        paint.color = Color.rgb(16, 185, 129)
        canvas.drawText("Total Received: BDT ${String.format(Locale.US, "%,.2f", totalReceived)}", contentStartX + 290f, y + 34f, paint)

        paint.color = if (isPaidInFull) Color.rgb(16, 185, 129) else Color.rgb(220, 38, 38)
        canvas.drawText(
            if (isPaidInFull) "Status: COMPLETED (FULLY PAID)" else "Due Balance: BDT ${String.format(Locale.US, "%,.2f", remainingDue)}",
            contentStartX + 290f,
            y + 50f,
            paint
        )

        y += boxHeight + 18f

        // Payment History Section
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PAYMENT & INSTALLMENT HISTORY", contentStartX, y, paint)

        y += 10f
        // Table Header
        paint.color = Color.rgb(15, 23, 42)
        paint.style = Paint.Style.FILL
        canvas.drawRect(RectF(contentStartX, y, contentEndX, y + 20f), paint)

        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val colSl = contentStartX + 8f
        val colDate = contentStartX + 35f
        val colNote = contentStartX + 115f
        val colAmount = contentStartX + 330f
        val colBal = contentStartX + 410f

        canvas.drawText("SL", colSl, y + 14f, paint)
        canvas.drawText("DATE", colDate, y + 14f, paint)
        canvas.drawText("DESCRIPTION / PURPOSE", colNote, y + 14f, paint)
        canvas.drawText("RECEIVED (BDT)", colAmount, y + 14f, paint)
        canvas.drawText("RUNNING DUE", colBal, y + 14f, paint)

        y += 20f

        // Rows
        var runningBal = project.contractAmount
        val sortedPayments = payments.sortedBy { it.timestamp }

        if (sortedPayments.isEmpty()) {
            paint.color = Color.rgb(148, 163, 184)
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("No installment payments recorded yet.", contentStartX + 16f, y + 18f, paint)
            y += 26f
        } else {
            sortedPayments.forEachIndexed { index, pmt ->
                runningBal -= pmt.amount
                paint.color = if (index % 2 == 0) Color.rgb(248, 250, 252) else Color.WHITE
                paint.style = Paint.Style.FILL
                canvas.drawRect(RectF(contentStartX, y, contentEndX, y + 18f), paint)

                paint.color = Color.rgb(71, 85, 105)
                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

                canvas.drawText("${index + 1}", colSl, y + 13f, paint)
                canvas.drawText(pmt.paymentDate, colDate, y + 13f, paint)
                canvas.drawText(pmt.note.take(36), colNote, y + 13f, paint)

                paint.color = Color.rgb(5, 150, 105)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(String.format(Locale.US, "%,.2f", pmt.amount), colAmount, y + 13f, paint)

                paint.color = if (runningBal <= 0) Color.rgb(16, 185, 129) else Color.rgb(220, 38, 38)
                canvas.drawText(String.format(Locale.US, "%,.2f", maxOf(0.0, runningBal)), colBal, y + 13f, paint)

                y += 18f
            }
        }

        // Table bottom border
        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 1f
        canvas.drawLine(contentStartX, y, contentEndX, y, paint)

        // Signatures Block at bottom
        val sigY = pageHeight - MARGIN_BOTTOM - 60f
        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 1f
        canvas.drawLine(contentStartX, sigY, contentEndX, sigY, paint)

        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Client / Homeowner Signature", contentStartX + 10f, sigY + 36f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawLine(contentStartX + 10f, sigY + 22f, contentStartX + 180f, sigY + 22f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Technician / Contractor Signature", contentEndX - 190f, sigY + 36f, paint)
        canvas.drawLine(contentEndX - 190f, sigY + 22f, contentEndX - 10f, sigY + 22f, paint)

        // Footer note
        paint.textSize = 8f
        paint.color = Color.rgb(148, 163, 184)
        canvas.drawText("Electrical Pro Suite • Official Contract & Payment Record • 100% Verified", contentStartX, pageHeight - MARGIN_BOTTOM + 15f, paint)

        document.finishPage(page)

        val outputDir = File(context.cacheDir, "project_reports").apply { mkdirs() }
        val outputFile = File(outputDir, "Project_Contract_${project.id}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
        return outputFile
    }
}
