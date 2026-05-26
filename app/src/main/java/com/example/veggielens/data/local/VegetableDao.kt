package com.example.veggielens.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.veggielens.data.model.VegetableEntity

@Dao
interface VegetableDao {
    @Query("SELECT * FROM vegetable WHERE name = :name")
    suspend fun getVegetableByName(name: String): VegetableEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vegetables: List<VegetableEntity>)

    @Query("SELECT * FROM vegetable")
    suspend fun getAllVegetables(): List<VegetableEntity>
}