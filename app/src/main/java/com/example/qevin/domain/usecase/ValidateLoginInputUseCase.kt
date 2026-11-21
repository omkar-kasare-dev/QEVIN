package com.example.qevin.domain.usecase

import javax.inject.Inject

class ValidateLoginInputUseCase @Inject constructor() {

    operator fun invoke(
        identifier: String,
        password: String
    ): String? {
        val value = identifier.trim()

        if (value.isBlank()) {
            return "Enter your email or mobile number."
        }

        val isEmail = Regex(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        ).matches(value)

        val isPhone = Regex(
            "^\\+?[0-9]{10,15}$"
        ).matches(value)

        if (!isEmail && !isPhone) {
            return "Enter a valid email or mobile number."
        }

        if (password.isBlank()) {
            return "Enter your password."
        }

        return null
    }
}