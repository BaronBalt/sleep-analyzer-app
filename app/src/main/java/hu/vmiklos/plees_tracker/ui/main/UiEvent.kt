package hu.vmiklos.plees_tracker.ui.main

sealed class UiEvent {
    data class ShowToast(val resId: Int) : UiEvent()
    data class ShowError(val message: String) : UiEvent()
    data class ImportSuccess(val count: Int) : UiEvent()
    data class ExportSuccess(val count: Int) : UiEvent()
}