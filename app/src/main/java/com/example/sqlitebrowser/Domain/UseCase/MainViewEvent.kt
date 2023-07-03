package com.example.sqlitebrowser.Domain.UseCase

import android.net.Uri

sealed class MainViewEvent {
    data class SQLCodeChanged(val sqlCode: String) : MainViewEvent()
    data class FileSelected(val uri: Uri) : MainViewEvent()
    data class DropDrownOpen(val isMenuOpen: Boolean) : MainViewEvent()
    object Execute : MainViewEvent()
    object OpenFileSelector : MainViewEvent()
    object DropDownExportDatabaseSelected : MainViewEvent()
}