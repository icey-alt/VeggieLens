package com.example.veggielens.data.repository

import com.example.veggielens.data.local.ScanHistoryDao
import com.example.veggielens.data.local.VegetableDao
import com.example.veggielens.data.model.ScanHistory
import com.example.veggielens.data.model.VegetableEntity
import kotlinx.coroutines.flow.Flow

class VegetableRepository(
    private val vegetableDao: VegetableDao,
    private val scanHistoryDao: ScanHistoryDao
) {
    suspend fun getVegetableByName(name: String): VegetableEntity? {
        return vegetableDao.getVegetableByName(name)
    }

    suspend fun getAllVegetables(): List<VegetableEntity> {
        return vegetableDao.getAllVegetables()
    }

    suspend fun insertScanHistory(history: ScanHistory) {
        scanHistoryDao.insert(history)
    }

    fun getAllHistory(): Flow<List<ScanHistory>> {
        return scanHistoryDao.getAllHistory()
    }

    fun searchHistory(query: String): Flow<List<ScanHistory>> {
        return scanHistoryDao.searchHistory(query)
    }

    suspend fun deleteAllHistory() {
        scanHistoryDao.deleteAll(
    }
}