package com.example.veggielens.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.veggielens.data.model.ScanHistory
import com.example.veggielens.data.model.VegetableCatalog
import com.example.veggielens.data.model.VegetableEntity
import com.example.veggielens.data.model.VeggieDatabase
import com.example.veggielens.data.repository.VegetableRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 图鉴中的一种蔬菜。
 * 同一种蔬菜在数据库里可能有多条（如 Brinjal / Eggplant 都是“茄子”），按中文名合并，
 * [aliases] 保存这一组的全部英文名（已规范化），用于匹配识别记录。
 */
data class GuideEntry(
    val vegetable: VegetableEntity,
    val aliases: Set<String>,
    val discovered: Boolean,
)

class GuideViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: VegetableRepository
    private val vegetables = MutableStateFlow<List<VegetableEntity>>(emptyList())

    /** 全部识别记录（时间倒序） */
    val history: StateFlow<List<ScanHistory>>

    /** 图鉴条目，顺序与预置数据一致 */
    val entries: StateFlow<List<GuideEntry>>

    init {
        val database = VeggieDatabase.getDatabase(application)
        repository = VegetableRepository(database.vegetableDao(), database.scanHistoryDao())

        history = repository.getAllHistory().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

        entries = combine(vegetables, history) { vegetableList, historyList ->
            val scanned = historyList.mapTo(mutableSetOf()) { VegetableCatalog.normalize(it.vegetableName) }
            vegetableList
                .groupBy { it.chineseName }
                .values
                .map { group ->
                    val aliases = group.mapTo(mutableSetOf()) { VegetableCatalog.normalize(it.name) }
                    GuideEntry(
                        vegetable = group.first(),
                        aliases = aliases,
                        discovered = aliases.any { it in scanned },
                    )
                }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

        viewModelScope.launch(Dispatchers.IO) {
            vegetables.value = repository.getAllVegetables()
        }
    }
}
