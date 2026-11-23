package com.example.qevin.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.qevin.domain.repository.ProfileImageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import java.util.UUID

class InternalProfileImageRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : ProfileImageRepository {

    companion object {
        private const val PREF_NAME = "qevin_profile_image"
        private const val KEY_IMAGE_PATH = "profile_image_path"
        private const val DIRECTORY_NAME = "profile_images"
    }

    private val preferences by lazy {
        context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
    }


    override suspend fun saveProfileImage(uri: Uri): String =
        withContext(Dispatchers.IO) {

            val mimeType = context.contentResolver.getType(uri)

            val extension = android.webkit.MimeTypeMap
                .getSingleton()
                .getExtensionFromMimeType(mimeType)
                ?.lowercase()
                ?: "img"

            val directory = File(
                context.filesDir,
                DIRECTORY_NAME
            )

            if (!directory.exists() && !directory.mkdirs()) {
                throw IOException("Unable to create profile image directory")
            }

            val destination = File(
                directory,
                "profile_image.$extension"
            )

            val temporaryFile = File(
                directory,
                "profile_${UUID.randomUUID()}.tmp"
            )

            val backupFile = File(
                directory,
                "backup_${UUID.randomUUID()}.tmp"
            )

            val oldPath = preferences.getString(
                KEY_IMAGE_PATH,
                null
            )

            var previousImageBackedUp = false

            try {
                val inputStream = context.contentResolver
                    .openInputStream(uri)
                    ?: throw IOException("Unable to open selected image")

                inputStream.use { input ->
                    temporaryFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                if (!temporaryFile.exists() || temporaryFile.length() == 0L) {
                    throw IOException("Selected image is empty")
                }

                // Back up the existing destination before replacement.
                if (destination.exists()) {
                    if (!destination.renameTo(backupFile)) {
                        throw IOException("Unable to back up existing image")
                    }
                    previousImageBackedUp = true
                }

                // Move the new image into its permanent location.
                if (!temporaryFile.renameTo(destination)) {
                    throw IOException("Unable to save new profile image")
                }

                val newPath = destination.absolutePath

                // Persist the new image path.
                val saved = preferences.edit()
                    .putString(KEY_IMAGE_PATH, newPath)
                    .commit()

                if (!saved) {
                    destination.delete()
                    throw IOException("Unable to persist image path")
                }

                // Replacement succeeded. Remove previous image.
                if (oldPath != null && oldPath != newPath) {
                    File(oldPath).delete()
                }

                backupFile.delete()

                newPath

            } catch (exception: Exception) {

                // Restore the previous destination if replacement failed.
                if (previousImageBackedUp && backupFile.exists()) {
                    destination.delete()
                    backupFile.renameTo(destination)
                }

                throw exception

            } finally {
                temporaryFile.delete()
            }
        }


    override suspend fun getSavedProfileImagePath(): String? =
        withContext(Dispatchers.IO) {

            val path = preferences.getString(
                KEY_IMAGE_PATH,
                null
            ) ?: return@withContext null

            val imageFile = File(path)

            if (imageFile.exists() && imageFile.isFile) {
                path
            } else {
                preferences.edit()
                    .remove(KEY_IMAGE_PATH)
                    .apply()

                null
            }
        }



    override suspend fun removeProfileImage() {
        withContext(Dispatchers.IO) {

            val savedPath = preferences.getString(
                KEY_IMAGE_PATH,
                null
            )

            // Remove the persisted reference first.
            val cleared = preferences.edit()
                .remove(KEY_IMAGE_PATH)
                .commit()

            if (!cleared) {
                throw IOException("Unable to remove saved profile image reference")
            }

            // Delete the physical image.
            savedPath?.let { path ->
                val imageFile = File(path)

                if (imageFile.exists() && !imageFile.delete()) {
                    throw IOException("Unable to delete profile image file")
                }
            }
        }
    }


}