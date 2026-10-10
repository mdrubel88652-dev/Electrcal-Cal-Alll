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
import com.example.data.datastore.DeveloperReportSettings
import com.example.data.datastore.EngineerReportProfile
import com.example.data.datastore.PdfPrintSettings
import com.example.data.datastore.TechnicianReportProfile
import com.example.data.model.BuildingProjectData
import com.example.data.model.CalculationResult
import com.example.data.model.CalculatorDefinition
import com.example.data.model.MaterialItem
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
        val addressLineCount = if (hasTechProfile) maxOf(1, techProfile.officialAddress.lines().count { it.isNotBlank() }) else 0
        val techHeight = if (hasTechProfile) 65f + (addressLineCount * 14f) else 0f
        val inputCount = inputs.size
        val inputsHeight = 25f + (inputCount * 16f)
        val stepsCount = result.steps.size
        val stepsHeight = if (pdfSettings.showSteps && stepsCount > 0) 25f + (stepsCount * 18f) else 0f
        val formulaHeight = if (pdfSettings.showFormula && result.formulaUsed.isNotEmpty()) 40f else 0f
        val notesHeight = if (pdfSettings.showNotes && result.notes.isNotEmpty()) 20f + (result.notes.size * 14f) else 0f

        val estimatedTotalHeight = 120f + techHeight + 65f + inputsHeight + formulaHeight + stepsHeight + 70f + notesHeight + 110f
        val totalPages = if (estimatedTotalHeight > (pageHeight - MARGIN_BOTTOM - 20f)) 2 else 1

        if (totalPages == 1) {
            // --- Single Page Layout ---
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var y = MARGIN_TOP

            // 1. TOP HEADER: APP NAME + APP LOGO
            y = drawTopHeader(canvas, paint, logoBitmap, contentStartX, contentEndX, y)

            // 2. TECHNICIAN PROFILE (Immediate below header, hidden if empty)
            if (hasTechProfile) {
                y = drawTechnicianProfile(canvas, paint, techProfile, contentStartX, contentEndX, y)
            }

            // 3. CALCULATION REPORT TITLE CARD
            y = drawCalculationHeaderCard(canvas, paint, calculator, contentStartX, contentEndX, y)

            // 4. INPUT DATA
            y = drawInputParameters(canvas, paint, calculator, inputs, contentStartX, contentEndX, y)

            // 5. FORMULA
            if (pdfSettings.showFormula && result.formulaUsed.isNotEmpty()) {
                y = drawFormulaSection(canvas, paint, result.formulaUsed, contentStartX, contentEndX, y)
            }

            // 6. STEP-BY-STEP SOLUTION
            if (pdfSettings.showSteps && result.steps.isNotEmpty()) {
                y = drawStepsSection(canvas, paint, result, contentStartX, contentEndX, y)
            }

            // 7. FINAL RESULT
            y = drawFinalResultBox(canvas, paint, result, contentStartX, contentEndX, y)

            // 8. STANDARD & NOTES
            y = drawStandardAndNotes(canvas, paint, result, pdfSettings, contentStartX, contentEndX, y)

            // 9. BOTTOM OF A4 PAPER: ENGINEER & REPORT PROFILE + APP LOGO + PAGE NUMBER
            drawEngineerFooter(
                canvas = canvas,
                paint = paint,
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
            // Page 1: Top Header + Technician Profile + Calculation Header + Inputs + Formula
            val page1Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page1 = document.startPage(page1Info)
            val canvas1 = page1.canvas

            var y1 = MARGIN_TOP
            y1 = drawTopHeader(canvas1, paint, logoBitmap, contentStartX, contentEndX, y1)

            if (hasTechProfile) {
                y1 = drawTechnicianProfile(canvas1, paint, techProfile, contentStartX, contentEndX, y1)
            }

            y1 = drawCalculationHeaderCard(canvas1, paint, calculator, contentStartX, contentEndX, y1)
            y1 = drawInputParameters(canvas1, paint, calculator, inputs, contentStartX, contentEndX, y1)

            if (pdfSettings.showFormula && result.formulaUsed.isNotEmpty()) {
                drawFormulaSection(canvas1, paint, result.formulaUsed, contentStartX, contentEndX, y1)
            }

            // Draw interim footer on Page 1
            drawInterimPageFooter(canvas1, paint, contentStartX, contentEndX, pageHeight.toFloat(), 1, 2)
            document.finishPage(page1)

            // Page 2: Header continuation + Steps + Final Result + Standard/Notes + Bottom Engineer Profile
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

            drawEngineerFooter(
                canvas = canvas2,
                paint = paint,
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

    // 1. Draw Top Header: App Name + App Logo
    private fun drawTopHeader(
        canvas: Canvas,
        paint: Paint,
        logoBitmap: Bitmap?,
        startX: Float,
        endX: Float,
        topY: Float
    ): Float {
        var y = topY
        val logoSize = 36f

        if (logoBitmap != null) {
            val srcRect = Rect(0, 0, logoBitmap.width, logoBitmap.height)
            val dstRect = RectF(startX, y, startX + logoSize, y + logoSize)
            canvas.drawBitmap(logoBitmap, srcRect, dstRect, paint)
        }

        val textStartX = if (logoBitmap != null) startX + logoSize + 12f else startX

        // App Name
        paint.color = Color.rgb(13, 71, 161) // Electric Navy / Blue
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ELECTRICAL CALCULATION ALL", textStartX, y + 15f, paint)

        // Subtitle
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Electrical Engineering Calculation & Estimation", textStartX, y + 30f, paint)

        y += logoSize + 10f

        // Balanced Divider
        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 1.2f
        canvas.drawLine(startX, y, endX, y, paint)

        return y + 14f
    }

    // 2. Draw Technician Profile (Immediate below header, strictly hidden if empty)
    private fun drawTechnicianProfile(
        canvas: Canvas,
        paint: Paint,
        profile: TechnicianReportProfile,
        startX: Float,
        endX: Float,
        topY: Float
    ): Float {
        var y = topY

        val addressLines = profile.officialAddress.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val addressLineCount = maxOf(1, addressLines.size)
        val boxHeight = 56f + (addressLineCount * 14f)

        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(startX, y, endX, y + boxHeight, 6f, 6f, paint)

        paint.color = Color.rgb(226, 232, 240)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(startX, y, endX, y + boxHeight, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // Header Title
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TECHNICIAN PROFILE", startX + 12f, y + 15f, paint)

        val dividerY = y + 21f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawLine(startX + 12f, dividerY, endX - 12f, dividerY, paint)

        // Two columns of technician details
        val col1X = startX + 12f
        val col2X = startX + ((endX - startX) * 0.52f)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = Color.rgb(51, 65, 85)

        val row1Y = y + 34f
        val row2Y = y + 48f
        var currentY = y + 62f

        if (profile.name.isNotBlank()) {
            canvas.drawText("Name: ${profile.name}", col1X, row1Y, paint)
        }
        if (profile.company.isNotBlank()) {
            canvas.drawText("Company: ${profile.company}", col2X, row1Y, paint)
        }

        if (profile.contactPhone.isNotBlank()) {
            canvas.drawText("Contact Phone: ${profile.contactPhone}", col1X, row2Y, paint)
        }
        if (profile.email.isNotBlank()) {
            canvas.drawText("Email: ${profile.email}", col2X, row2Y, paint)
        }

        if (addressLines.isNotEmpty()) {
            for ((idx, line) in addressLines.withIndex()) {
                val prefix = if (idx == 0) "Official Address: " else "                  "
                canvas.drawText("$prefix$line", col1X, currentY, paint)
                currentY += 14f
            }
        }

        return y + boxHeight + 14f
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

    // 9. Draw Engineer Footer (BOTTOM OF FINAL PAGE): Fixed Rubel profile + App Logo beside it
    private fun drawEngineerFooter(
        canvas: Canvas,
        paint: Paint,
        logoBitmap: Bitmap?,
        contentStartX: Float,
        contentEndX: Float,
        pageHeight: Float,
        pageNumber: Int,
        totalPages: Int
    ) {
        val footerHeight = 78f
        val footerY = pageHeight - MARGIN_BOTTOM - footerHeight

        // Top divider
        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 1f
        canvas.drawLine(contentStartX, footerY, contentEndX, footerY, paint)

        val logoSize = 34f
        val logoY = footerY + 12f

        if (logoBitmap != null) {
            val srcRect = Rect(0, 0, logoBitmap.width, logoBitmap.height)
            val dstRect = RectF(contentStartX, logoY, contentStartX + logoSize, logoY + logoSize)
            canvas.drawBitmap(logoBitmap, srcRect, dstRect, paint)
        }

        val textStartX = if (logoBitmap != null) contentStartX + logoSize + 10f else contentStartX

        // Section Title: ENGINEER & REPORT PROFILE
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ENGINEER & REPORT PROFILE", textStartX, footerY + 14f, paint)

        val profile = EngineerReportProfile.DEFAULT

        // Fixed Details
        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Name: ${profile.name}", textStartX, footerY + 27f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("Company: ${profile.company}", textStartX, footerY + 39f, paint)
        canvas.drawText("Contact: ${profile.contactPhone}   |   Email: ${profile.email}", textStartX, footerY + 51f, paint)
        canvas.drawText("Address: ${profile.officialAddress}", textStartX, footerY + 63f, paint)

        // Page Numbering: Page X of Y (aligned right)
        val pageText = "Page $pageNumber of $totalPages"
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val textWidth = paint.measureText(pageText)
        canvas.drawText(pageText, contentEndX - textWidth, footerY + 40f, paint)
    }

    private fun drawInterimPageFooter(
        canvas: Canvas,
        paint: Paint,
        contentStartX: Float,
        contentEndX: Float,
        pageHeight: Float,
        pageNumber: Int,
        totalPages: Int
    ) {
        val footerY = pageHeight - MARGIN_BOTTOM - 20f
        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 1f
        canvas.drawLine(contentStartX, footerY, contentEndX, footerY, paint)

        val pageText = "Page $pageNumber of $totalPages"
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val textWidth = paint.measureText(pageText)
        canvas.drawText(pageText, contentEndX - textWidth, footerY + 14f, paint)
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
}
