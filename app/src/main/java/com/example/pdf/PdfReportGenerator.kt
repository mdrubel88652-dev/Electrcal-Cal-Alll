package com.example.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.datastore.DeveloperReportSettings
import com.example.data.datastore.PdfPrintSettings
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

    fun generateCalculationPdf(
        context: Context,
        calculator: CalculatorDefinition,
        inputs: Map<String, String>,
        result: CalculationResult,
        devSettings: DeveloperReportSettings,
        pdfSettings: PdfPrintSettings
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (595x842 pt)
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        var y = 40f

        // 1. Header Banner & Title
        paint.color = Color.rgb(13, 71, 161) // Electric Blue
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ELECTRICAL CALCULATION ALL", 40f, y, paint)
        y += 18f

        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Electrical Engineering Calculation & Estimation", 40f, y, paint)
        y += 15f

        // Divider
        paint.color = Color.rgb(226, 232, 240)
        paint.strokeWidth = 1.5f
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 25f

        // Calculation Title Card
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(40f, y, 555f, y + 65f, 8f, 8f, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(calculator.name, 55f, y + 26f, paint)

        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        canvas.drawText("Level: ${calculator.level.displayName}  |  Category: ${calculator.category.displayName}  |  Date: $dateStr", 55f, y + 48f, paint)
        y += 85f

        // 2. Input Parameters Table
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("INPUT PARAMETERS", 40f, y, paint)
        y += 18f

        paint.color = Color.rgb(51, 65, 85)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        for ((k, v) in inputs) {
            val label = calculator.inputs.firstOrNull { it.id == k }?.label ?: k
            canvas.drawText("• $label: $v", 50f, y, paint)
            y += 16f
        }
        y += 10f

        // 3. Final Result Highlight Box
        paint.color = Color.rgb(227, 242, 253) // Soft electric blue container
        canvas.drawRoundRect(40f, y, 555f, y + 60f, 8f, 8f, paint)

        paint.color = Color.rgb(13, 71, 161)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CALCULATED FINAL RESULT", 55f, y + 20f, paint)

        paint.textSize = 18f
        canvas.drawText("${result.primaryValue} ${result.primaryUnit}", 55f, y + 46f, paint)
        y += 80f

        // 4. Formula & Secondary Metrics
        if (pdfSettings.showFormula && result.formulaUsed.isNotEmpty()) {
            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("FORMULA:", 40f, y, paint)
            y += 16f

            paint.color = Color.rgb(30, 41, 59)
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText(result.formulaUsed, 50f, y, paint)
            y += 24f
        }

        // 5. Step-by-Step Solution
        if (pdfSettings.showSteps && result.steps.isNotEmpty()) {
            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("STEP-BY-STEP SOLUTION:", 40f, y, paint)
            y += 18f

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 10f
            for (step in result.steps) {
                paint.color = Color.rgb(71, 85, 105)
                canvas.drawText("${step.stepNumber}. ${step.title}:  ${step.equation}  =  ${step.result}", 50f, y, paint)
                y += 16f
            }
            y += 15f
        }

        // 6. Notes & Standard
        if (pdfSettings.showNotes) {
            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Standard / Engineering Basis: ${result.standardBasis}", 40f, y, paint)
            y += 16f
            if (result.notes.isNotEmpty()) {
                paint.color = Color.rgb(100, 116, 139)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                for (note in result.notes) {
                    canvas.drawText("Note: $note", 40f, y, paint)
                    y += 14f
                }
            }
        }

        // 7. Footer: Developer Info
        if (pdfSettings.showDeveloperInfo) {
            val footerY = 790f
            paint.color = Color.rgb(226, 232, 240)
            canvas.drawLine(40f, footerY - 15f, 555f, footerY - 15f, paint)

            paint.color = Color.rgb(100, 116, 139)
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Prepared by: ${devSettings.developerName} | ${devSettings.companyName}", 40f, footerY, paint)
            canvas.drawText("Contact: ${devSettings.contactNumber} | Email: ${devSettings.email}", 40f, footerY + 14f, paint)
            canvas.drawText("Page 1 of 1", 500f, footerY, paint)
        }

        document.finishPage(page)

        val outputDir = File(context.cacheDir, "pdf_reports").apply { mkdirs() }
        val outputFile = File(outputDir, "Report_Calc_${calculator.id}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
        return outputFile
    }

    fun generateMaterialListPdf(
        context: Context,
        project: BuildingProjectData,
        materials: List<MaterialItem>,
        devSettings: DeveloperReportSettings
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        var y = 35f

        // Header
        paint.color = Color.rgb(13, 71, 161)
        paint.textSize = 17f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ELECTRICAL MATERIAL REQUIREMENT LIST", 40f, y, paint)
        y += 16f

        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Electrical Engineering Calculation & Estimation", 40f, y, paint)
        y += 14f

        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 1f
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 18f

        // Project Info Box
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(40f, y, 555f, y + 55f, 6f, 6f, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Project: ${project.projectName}", 50f, y + 18f, paint)
        canvas.drawText("Owner: ${project.ownerName}", 320f, y + 18f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("Address: ${project.address}", 50f, y + 36f, paint)
        canvas.drawText("Date: ${project.date}  |  Floors: ${project.floors}, Rooms: ${project.rooms}", 320f, y + 36f, paint)
        y += 70f

        // Table Header (Strictly NO price columns!)
        paint.color = Color.rgb(13, 71, 161)
        canvas.drawRect(40f, y, 555f, y + 22f, paint)

        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("No.", 45f, y + 15f, paint)
        canvas.drawText("Material Name", 75f, y + 15f, paint)
        canvas.drawText("Specification", 215f, y + 15f, paint)
        canvas.drawText("Unit", 365f, y + 15f, paint)
        canvas.drawText("Qty", 415f, y + 15f, paint)
        canvas.drawText("Remarks", 460f, y + 15f, paint)
        y += 24f

        // Table Rows
        paint.textSize = 8.5f
        for ((idx, item) in materials.take(24).withIndex()) {
            if (idx % 2 == 1) {
                paint.color = Color.rgb(248, 250, 252)
                canvas.drawRect(40f, y, 555f, y + 18f, paint)
            }
            paint.color = Color.rgb(15, 23, 42)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(item.no.toString(), 45f, y + 13f, paint)
            canvas.drawText(item.name.take(25), 75f, y + 13f, paint)
            canvas.drawText(item.specification.take(28), 215f, y + 13f, paint)
            canvas.drawText(item.unit.take(10), 365f, y + 13f, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(item.quantity.toString(), 415f, y + 13f, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.rgb(100, 116, 139)
            canvas.drawText(item.remarks.take(18), 460f, y + 13f, paint)
            y += 18f
        }

        // Signatures Footer
        y = 780f
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Prepared By: __________________", 45f, y, paint)
        canvas.drawText("Owner: __________________", 230f, y, paint)
        canvas.drawText("Signature: __________________", 400f, y, paint)

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
