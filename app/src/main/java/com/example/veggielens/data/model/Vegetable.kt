package com.example.veggielens.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vegetable")
data class VegetableEntity(
    @PrimaryKey val name: String,            // 蔬菜英文名
    val chineseName: String,
    val scientificName: String,              // 学名
    val calories: String,                   // 热量
    val protein: String,                    // 蛋白质
    val carbs: String,                      // 碳水
    val fiber: String,                      // 纤维
    val assetImagePath: String              // "vegetables/broccoli.jpg"
)