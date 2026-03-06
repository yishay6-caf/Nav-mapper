package com.subnavar.app.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnavar.app.domain.repository.BuildingRepository
import com.subnavar.app.util.LocaleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val language: LocaleManager.AppLanguage = LocaleManager.AppLanguage.ENGLISH,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: BuildingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(
            language = LocaleManager.getLanguage(context)
        )
    }

    fun setLanguage(language: LocaleManager.AppLanguage) {
        LocaleManager.setLanguage(context, language)
        _uiState.value = _uiState.value.copy(language = language)
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            try {
                val buildings = repository.getAllBuildings().first()
                if (buildings.isNotEmpty()) {
                    repository.exportBuildingData(buildings.first().id, uri)
                    _uiState.value = _uiState.value.copy(message = "Export successful")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "Export failed: ${e.message}")
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            try {
                val buildingId = repository.insertBuilding(
                    com.subnavar.app.domain.model.Building(name = "Imported Building")
                )
                repository.importBuildingData(uri, buildingId)
                _uiState.value = _uiState.value.copy(message = "Import successful")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "Import failed: ${e.message}")
            }
        }
    }
}
