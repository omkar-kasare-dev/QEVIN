package com.example.qevin.domain.usecase

import javax.inject.Inject

class ValidateRegistrationInputUseCase @Inject constructor() {

    //
    operator fun invoke(
        displayName: String,
        phoneNumber: String,
        password: String,
        requirePhoneNumber: Boolean = true
    ): String? {

        if (displayName.trim().length < 2) {
            return "Name must contain at least 2 characters."
        }

        if (
            requirePhoneNumber &&
            !Regex("^\\+?[0-9]{10,15}$").matches(phoneNumber.trim())
        ) {
            return "Enter a valid mobile number."
        }

        if (password.length < 8) {
            return "Password must contain at least 8 characters."
        }

        if (!password.any(Char::isLetter) ||
            !password.any(Char::isDigit)
        ) {
            return "Password must contain letters and numbers."
        }

        return null
    }
}