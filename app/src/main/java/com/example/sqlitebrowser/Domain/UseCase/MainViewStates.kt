package com.example.sqlitebrowser.Domain.UseCase

import android.net.Uri
import com.example.sqlitebrowser.Model.TableContent

data class MainViewStates(
    val sqlCode: String = "",
    val sqlCodeError: String? = null,
    val selectedSQLFile: Uri = Uri.EMPTY,
    val selectedSQLFileError: String? = null,
    val queryResult: TableContent? = null,
    val isMenuOpen: Boolean = false,
)