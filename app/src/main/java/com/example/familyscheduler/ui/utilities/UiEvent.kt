package com.example.familyscheduler.ui.utilities

sealed class UiEvent {

    data class ShowUndoDelete(
        val message: String = "削除しました",
        val onUndo: () -> Unit
    ) : UiEvent()

    data class ShowUndoProposal(
        val message: String = "提案を実行しました",
        val onUndo: () -> Unit
    ) : UiEvent()
}