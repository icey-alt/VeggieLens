package com.example.veggielens.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.veggielens.data.model.VeggieDatabase
import com.example.veggielens.data.repository.VegetableRepository
import com.example.veggielens.security.ApiKeyStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    init {
        val database = VeggieDatabase.getDatabase(application)
        repository = VegetableRepository(database.vegetableDao(), database.scanHistoryDao())
        _apiKey.value = apiKeyStore.read()
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