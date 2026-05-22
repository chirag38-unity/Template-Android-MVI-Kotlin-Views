package com.myapp.core.common.validation

object Validators {

    private val emailRegex = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")

    fun validateEmail(email: String): ValidationResult {
        return if (email.matches(emailRegex)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(listOf("Invalid email address"))
        }
    }

    fun validateNotEmpty(value: String, fieldName: String = "Field"): ValidationResult {
        return if (value.isNotBlank()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(listOf("$fieldName must not be empty"))
        }
    }

    fun validateMinLength(value: String, minLength: Int): ValidationResult {
        return if (value.length >= minLength) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(listOf("Must be at least $minLength characters long"))
        }
    }

    fun validateMaxLength(value: String, maxLength: Int): ValidationResult {
        return if (value.length <= maxLength) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(listOf("Must be at most $maxLength characters long"))
        }
    }

    fun validateRange(value: Int, min: Int, max: Int): ValidationResult {
        return if (value in min..max) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(listOf("Value must be between $min and $max"))
        }
    }
}
