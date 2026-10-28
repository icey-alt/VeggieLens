package com.example.veggielens.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.veggielens.data.model.ScanHistory
import com.example.veggielens.data.model.VeggieDatabase
import com.example.veggielens.data.repository.VegetableRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application): AndroidViewModel(application){
    private val repository: VegetableRepository
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    val historyList: StateFlow<List<ScanHistory>>

    /** 英文名 → 学名，记录列表里显示学名用 */
    private val _scientificNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val scientificNames: StateFlow<Map<String, String>> = _scientificNames

    init {
        val database = VeggieDatabase.getDatabase(application)
        repository = VegetableRepository(database.vegetableDao(), database.scanHistoryDao())
        viewModelScope.launch(Dispatchers.IO) {
            _scientificNames.value = repository.getAllVegetables()
                .associate { it.name to it.scientificName }
        }

        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        historyList = _searchQuery.flatMapLatest { query ->
            if(query.isBlank()) {
                repository.getAllHistory()
            }else {
                repository.searchHistory(query)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllHistory()
        }
    }
}