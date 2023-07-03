package com.example.sqlitebrowser.Model

data class TableContent(
    var columns: MutableList<String> = mutableListOf(),
    var cells: MutableList<String> = mutableListOf()
)