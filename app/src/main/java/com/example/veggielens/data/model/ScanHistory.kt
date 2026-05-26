package com.example.veggielens.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "scan_history",
    foreignKeys = [
        ForeignKey(
            entity = VegetableEntity::class,
            parentColumns = ["name"],          // 关联蔬菜表的主键
            childColumns = ["vegetableName"],  // 本表的关联字段
            onDelete = ForeignKey.CASCADE      // 可选：如果蔬菜被删，对应的历史记录也删掉
        )
    ],
)
data class ScanHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vegetableName: String,              // 蔬菜名称
    val confidence: Int,                    // 置信度（%）
    val description: String?,               // AI描述
    val timestamp: Long                     // 时间戳
)