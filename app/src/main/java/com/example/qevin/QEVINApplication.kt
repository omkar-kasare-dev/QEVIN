package com.example.qevin

import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner

import com.example.qevin.domain.repository.AuthRepository
import com.example.qevin.domain.repository.PresenceRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class QEVINApplication : Application() {

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var presenceRepository: PresenceRepository

    // Lives as long as the process. Main.immediate because lifecycle observers
    // must be registered on the main thread.
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate() // Hilt injects the fields above here.
        startPresenceTracking()
    }

    /**
     * Presence is active only while a user is signed in AND the app is in the
     * foreground. Any change (background, sign-out, user switch) cancels the
     * previous run, which marks that user offline and closes the connection.
     */
    private fun startPresenceTracking() {
        val isForeground = ProcessLifecycleOwner.get().lifecycle.currentStateFlow
            .map { it.isAtLeast(Lifecycle.State.STARTED) }
            .distinctUntilChanged()

        val signedInUid = authRepository.currentUser
            .map { it?.id }
            .distinctUntilChanged()

        appScope.launch {
            combine(signedInUid, isForeground) { uid, foreground ->
                if (foreground) uid else null
            }
                .distinctUntilChanged()
                .catch { Log.w("QEVIN_PRESENCE", "Presence coordinator failed", it) }
                .collectLatest { activeUid ->
                    if (activeUid != null) {
                        presenceRepository.runOwnPresence(activeUid)
                    }
                }
        }
    }
}