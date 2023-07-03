package com.example.sqlitebrowser.ViewModels

import DBHelper
import SQLiteFileManager
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sqlitebrowser.Domain.UseCase.MainViewEvent
import com.example.sqlitebrowser.Domain.UseCase.MainViewStates
import com.example.sqlitebrowser.Domain.UseCase.ValidateCode
import com.example.sqlitebrowser.Domain.UseCase.ValidateFile
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val application: Application,
    private val validateCode: ValidateCode = ValidateCode(),
    private val validateFile: ValidateFile = ValidateFile()
) : AndroidViewModel(application) {
    var state by mutableStateOf(MainViewStates())
        private set

    private val uiEventChannel = Channel<UiEvents>()
    val events = uiEventChannel.receiveAsFlow()

    private lateinit var db: DBHelper
    private lateinit var sqlFileManager: SQLiteFileManager

    fun onEvent(event: MainViewEvent) {
        when (event) {
            is MainViewEvent.SQLCodeChanged -> handleSQLCodeChanged(event)
            is MainViewEvent.FileSelected -> handleFileSelected(event)
            is MainViewEvent.OpenFileSelector -> openFileSelector()
            is MainViewEvent.Execute -> execute()
            is MainViewEvent.DropDownExportDatabaseSelected -> dropDownExportDatabaseSelected()
            is MainViewEvent.DropDrownOpen -> dropDownOpen(event)
        }
    }

    private fun handleSQLCodeChanged(event: MainViewEvent.SQLCodeChanged) {
        state = state.copy(sqlCode = event.sqlCode)
    }

    private fun handleFileSelected(event: MainViewEvent.FileSelected) {
        state = state.copy(selectedSQLFile = event.uri)
    }

    private fun openFileSelector() {
        viewModelScope.launch {
            uiEventChannel.send(UiEvents.SelectFile)
        }
    }

    private fun execute() {
        val sqlCodeResult = validateCode.execute(state.sqlCode)
        val fileResult = validateFile.execute(state.selectedSQLFile)

        state = state.copy(
            selectedSQLFileError = fileResult.errorMessage,
            sqlCodeError = sqlCodeResult.errorMessage
        )

        val hasError = listOf(sqlCodeResult, fileResult).any {
            !it.success
        }

        if (hasError) return

        initializeFileManagerAndDB()

        val lastValidate = db.validateQuery(state.sqlCode)

        if (!lastValidate.success) {
            state = state.copy(sqlCodeError = lastValidate.errorMessage)
            return
        }

        viewModelScope.launch {
            state = state.copy(queryResult = db.executeSQL(state.sqlCode))
        }
    }

    private fun dropDownExportDatabaseSelected() {
        val fileResult = validateFile.execute(state.selectedSQLFile)

        state = state.copy(selectedSQLFileError = fileResult.errorMessage)

        if (!fileResult.success) return

        initializeFileManagerAndDB()
        sqlFileManager.downloadDatabase()
        viewModelScope.launch {
            uiEventChannel.send(UiEvents.SqlExportedMessage)
        }
    }

    private fun dropDownOpen(event: MainViewEvent.DropDrownOpen) {
        state = state.copy(isMenuOpen = event.isMenuOpen)
    }

    private fun initializeFileManagerAndDB() {
        sqlFileManager = SQLiteFileManager(application, state.selectedSQLFile)
        db = DBHelper(application, sqlFileManager.generateSqlFile())
    }

    sealed class UiEvents {
        object SelectFile : UiEvents()
        object SqlExportedMessage : UiEvents()
    }
}
