package com.example.veggielens.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.veggielens.data.model.VeggieDatabase
import com.example.veggielens.data.repository.VegetableRepository
import com.example.veggielens.security.ApiKeyStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class ProfileStats(
    val totalScans: Int = 0,
    val uniqueVegetables: Int = 0,
    val monthlyScans: Int = 0,
    val streakDays: Int = 0
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: VegetableRepository
    private val apiKeyStore = ApiKeyStore(application)
    private val _apiKey = MutableStateFlow("")
    val apiKey : StateFlow<String> = _apiKey
    private val _stats = MutableStateFlow(ProfileStats())
    val stats: StateFlow<ProfileStats> = _stats

    /** 图鉴里的蔬菜总数，用于“已发现 x / N 种”的收集进度 */
    private val _totalVegetables = MutableStateFlow(0)
    val totalVegetables: StateFlow<Int> = _totalVegetables

    init {
        val database = VeggieDatabase.getDatabase(application)
        repository = VegetableRepository(database.vegetableDao(), database.scanHistoryDao())
        _apiKey.value = apiKeyStore.read()
        viewModelScope.launch(Dispatchers.IO) {
            // 按中文名去重（Brinjal / Eggplant 都是“茄子”），与图鉴一致
            _totalVegetables.value = repository.getAllVegetables().map { it.chineseName }.distinct().size
        }
        viewModelScope.launch {
            repository.getAllHistory().collect { historyList ->
                _stats.value = ProfileStatsCalculator.calculate(historyList)
            }
        }
    }

    fun updateApiKey(key: String) {
        _apiKey.value = key
        apiKeyStore.write(key)
    }
}