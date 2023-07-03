package com.example.sqlitebrowser.Domain.UseCase

class ValidateCode {
    fun execute(code: String): ValidationResult {
        if (code.isBlank()) {
            return ValidationResult(success = false, errorMessage = "SQL Command are can't blank")
        }
        return ValidationResult(success = true)
    }
}