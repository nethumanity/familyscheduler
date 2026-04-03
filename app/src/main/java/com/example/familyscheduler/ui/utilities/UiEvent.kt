package com.example.familyscheduler.ui.utilities

sealed class UiEvent {

    data class ShowUndoDelete(
        val message: String,
        val onUndo: () -> Unit
    ) : UiEvent()
}