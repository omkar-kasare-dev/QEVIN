package com.example.qevin.domain.usecase

import javax.inject.Inject

class ValidateRegistrationInputUseCase @Inject constructor() {

    operator fun invoke(
        displayName: String,
        email:String,
        phoneNumber: String,
        password: String
    ): String? {
        if (displayName.trim().length < 2) {
            return "Enter a valid name."
        }

        if (!phoneNumber.matches(Regex("^\\+?[0-9]{10,15}$"))) {
            return "Enter a valid mobile number."
        }

        if (password.length < 8) {
            return "Password must contain at least 8 characters."
        }

        if (password.none(Char::isLetter) ||
            password.none(Char::isDigit)
        ) {
            return "Password must contain letters and numbers."
        }

        return null
    }
}