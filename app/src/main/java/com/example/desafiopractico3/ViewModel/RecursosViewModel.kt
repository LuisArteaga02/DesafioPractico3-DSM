package com.example.desafiopractico3.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.desafiopractico3.Interfaces.RetrofitClient
import com.example.desafiopractico3.models.Recurso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class RecursosViewModel : ViewModel() {


    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    fun cargarRecursos() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = RetrofitClient.apiService.getRecursos()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = UiState.Success(response.body()!!)
                } else {
                    _uiState.value = UiState.Error("Error en la respuesta del servidor")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error de conexión")
            }
        }
    }

    sealed class UiState {
        object Loading : UiState()
        data class Success(val recursos: List<Recurso>) : UiState()
        data class Error(val message: String) : UiState()
    }
}