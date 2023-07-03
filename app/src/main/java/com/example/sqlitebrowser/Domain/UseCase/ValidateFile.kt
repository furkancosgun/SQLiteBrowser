package com.example.sqlitebrowser.Domain.UseCase

import android.net.Uri

class ValidateFile {
    fun execute(uri: Uri): ValidationResult {
        if (uri == Uri.EMPTY) {
            return ValidationResult(false, "Please select a sql file")
        }
        
        return ValidationResult(true)
    }
}