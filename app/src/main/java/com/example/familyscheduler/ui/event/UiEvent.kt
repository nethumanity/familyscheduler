package com.example.familyscheduler.ui.event

sealed class UiEvent {

    data class ShowUndoDelete(
        val message: String = "削除しました",
        val onUndo: () -> Unit
    ) : UiEvent()

    data class ShowUndoToggle(
        val message: String = "実行しました",
        val onUndo: () -> Unit
    ) : UiEvent()

    data class ShowUndoProposal(
        val message: String = "提案を実行しました",
        val onUndo: () -> Unit
    ) : UiEvent()
}