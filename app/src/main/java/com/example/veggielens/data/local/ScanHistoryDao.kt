package com.example.veggielens.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.veggielens.data.model.ScanHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
    @Insert
    suspend fun insert(history: ScanHistory)

    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ScanHistory>>

    @Query("""
        SELECT h.* FROM scan_history h 
        INNER JOIN vegetable v ON h.vegetableName = v.name 
        WHERE h.vegetableName LIKE '%' || :query || '%' 
           OR v.chineseName LIKE '%' || :query || '%' 
        ORDER BY h.timestamp DESC
    """)
    fun searchHistory(query: String): Flow<List<ScanHistory>>

    @Query("DELETE FROM scan_history")
    suspend fun deleteAll()
}