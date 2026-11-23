package com.example.qevin.domain.repository

import android.net.Uri

interface ProfileImageRepository {

    suspend fun saveProfileImage(uri: Uri): String

    suspend fun getSavedProfileImagePath(): String?

    suspend fun removeProfileImage()
}
