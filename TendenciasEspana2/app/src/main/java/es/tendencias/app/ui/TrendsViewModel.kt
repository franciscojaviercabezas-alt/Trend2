package es.tendencias.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.tendencias.app.data.TrendsRepository
import es.tendencias.app.model.Trend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TrendsUiState {
    data object Loading : TrendsUiState
    data class Success(
        val trends: List<Trend>,
        val lastUpdated: Long = System.currentTimeMillis()
    ) : TrendsUiState
    data class Error(val message: String) : TrendsUiState
}

class TrendsViewModel(
    private val repository: TrendsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TrendsUiState>(TrendsUiState.Loading)
    val uiState: StateFlow<TrendsUiState> = _uiState.asStateFlow()

    init {
        observeRepository()
        refresh()
    }

    private fun observeRepository() {
        viewModelScope.launch {
            repository.getTrendsStream().collect { trends ->
                if (trends.isNotEmpty()) {
                    _uiState.value = TrendsUiState.Success(trends)
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = TrendsUiState.Loading
            val result = repository.refreshTrends()
            result.onFailure { error ->
                _uiState.value = TrendsUiState.Error(
                    error.localizedMessage ?: "Error al conectar con Google Trends RSS España"
                )
            }
        }
    }
}
