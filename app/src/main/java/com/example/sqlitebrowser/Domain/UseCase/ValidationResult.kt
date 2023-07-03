package com.example.sqlitebrowser.Domain.UseCase

data class ValidationResult(var success: Boolean, var errorMessage: String? = null)