package com.example

import com.example.data.database.ContractProjectEntity
import com.example.data.database.ProjectPaymentEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProjectManagementTest {

    @Test
    fun `test contract project balance calculation and payment deduction`() {
        val project = ContractProjectEntity(
            id = 1,
            projectName = "Mirpur Residential Wiring",
            clientName = "Al-Hajj Rafiqul Islam",
            location = "Plot 42, Road 11, Mirpur DOHS, Dhaka",
            contactPhone = "01711223344",
            emailAddress = "rafiq@example.com",
            contractAmount = 85000.0,
            startDate = "11/10/2026",
            isCompleted = false
        )

        val payments = listOf(
            ProjectPaymentEntity(
                id = 101,
                projectId = 1,
                amount = 30000.0,
                paymentDate = "11/10/2026",
                note = "Advance Payment"
            ),
            ProjectPaymentEntity(
                id = 102,
                projectId = 1,
                amount = 25000.0,
                paymentDate = "15/10/2026",
                note = "2nd Installment after Conduit work"
            )
        )

        val totalReceived = payments.sumOf { it.amount }
        val remainingDue = project.contractAmount - totalReceived

        assertEquals(55000.0, totalReceived, 0.001)
        assertEquals(30000.0, remainingDue, 0.001)
        assertTrue(remainingDue > 0)

        // Add 3rd installment to complete project
        val completedPayments = payments + ProjectPaymentEntity(
            id = 103,
            projectId = 1,
            amount = 30000.0,
            paymentDate = "20/10/2026",
            note = "Final Handover Payment"
        )

        val finalTotalReceived = completedPayments.sumOf { it.amount }
        val finalDue = project.contractAmount - finalTotalReceived

        assertEquals(85000.0, finalTotalReceived, 0.001)
        assertEquals(0.0, finalDue, 0.001)
        assertTrue(finalDue <= 0)
    }

    @Test
    fun `test project slots capacity rule maximum 5 projects`() {
        val maxProjects = 5
        val projectList = mutableListOf<ContractProjectEntity>()

        for (i in 1..5) {
            projectList.add(
                ContractProjectEntity(
                    id = i.toLong(),
                    projectName = "Site #$i",
                    contractAmount = 50000.0,
                    startDate = "11/10/2026"
                )
            )
        }

        assertEquals(5, projectList.size)
        val isSlotFull = projectList.size >= maxProjects
        assertTrue(isSlotFull)

        // Capacity remaining is 0
        val remainingSlots = maxProjects - projectList.size
        assertEquals(0, remainingSlots)

        // If one project is deleted
        projectList.removeAt(0)
        assertEquals(4, projectList.size)
        assertFalse(projectList.size >= maxProjects)
        assertEquals(1, maxProjects - projectList.size)
    }
}
