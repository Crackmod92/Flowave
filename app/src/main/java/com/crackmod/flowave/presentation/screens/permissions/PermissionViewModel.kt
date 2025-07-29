package com.crackmod.flowave.presentation.screens.permissions

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crackmod.flowave.presentation.permissions.PermissionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val permissionHandler: PermissionHandler
) : ViewModel() {

    private val _state = MutableStateFlow(PermissionState())
    val state: StateFlow<PermissionState> = _state.asStateFlow()

    fun checkPermissions() {
        viewModelScope.launch {
            val hasPermissions = permissionHandler.hasRequiredPermissions()
            _state.value = _state.value.copy(
                hasPermissions = hasPermissions,
                isLegacyPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.R,
                initialCheckCompleted = true // Устанавливаем флаг, что проверка завершена
            )
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            // После успешного предоставления разрешений обновляем состояние
            checkPermissions()
        }
    }

    fun getLegacyPermissions(): Array<String> {
        return permissionHandler.getLegacyPermissions()
    }
}

data class PermissionState(
    val hasPermissions: Boolean = false,
    val isLegacyPermission: Boolean = false,
    val initialCheckCompleted: Boolean = false // НОВЫЙ ФЛАГ
)