package com.example.aurascan

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScanResultViewModel : ViewModel() {
    private val _payload = MutableStateFlow("")
    private val _formatLabel = MutableStateFlow("")
    private val _richPayloadActions = MutableStateFlow(false)
    val payload: StateFlow<String> = _payload.asStateFlow()
    val formatLabel: StateFlow<String> = _formatLabel.asStateFlow()
    val richPayloadActions: StateFlow<Boolean> = _richPayloadActions.asStateFlow()

    fun setResult(payload: String, formatLabel: String, richPayloadActions: Boolean) {
        _payload.value = payload
        _formatLabel.value = formatLabel
        _richPayloadActions.value = richPayloadActions
    }
}
