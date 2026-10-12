package com.example.data.repository

import com.example.data.database.ContractProjectDao
import com.example.data.database.ContractProjectEntity
import com.example.data.database.ProjectPaymentDao
import com.example.data.database.ProjectPaymentEntity
import kotlinx.coroutines.flow.Flow

class ProjectRepository(
    private val projectDao: ContractProjectDao,
    private val paymentDao: ProjectPaymentDao
) {
    val allProjects: Flow<List<ContractProjectEntity>> = projectDao.getAllProjects()
    val projectCount: Flow<Int> = projectDao.getProjectCount()

    fun getPaymentsForProject(projectId: Long): Flow<List<ProjectPaymentEntity>> {
        return paymentDao.getPaymentsForProject(projectId)
    }

    suspend fun getPaymentsListForProject(projectId: Long): List<ProjectPaymentEntity> {
        return paymentDao.getPaymentsListForProject(projectId)
    }

    suspend fun getProjectById(id: Long): ContractProjectEntity? {
        return projectDao.getProjectById(id)
    }

    suspend fun saveProject(project: ContractProjectEntity): Long {
        return projectDao.insertOrUpdateProject(project)
    }

    suspend fun deleteProject(projectId: Long) {
        paymentDao.deletePaymentsForProject(projectId)
        projectDao.deleteProject(projectId)
    }

    suspend fun addPayment(payment: ProjectPaymentEntity): Long {
        return paymentDao.insertPayment(payment)
    }

    suspend fun deletePayment(paymentId: Long) {
        paymentDao.deletePayment(paymentId)
    }
}
