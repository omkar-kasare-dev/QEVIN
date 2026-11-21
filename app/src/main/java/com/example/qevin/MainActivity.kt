package com.example.qevin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.qevin.presentation.navigation.QevinApp
import com.example.qevin.core.designsystem.theme.QEVINTheme
import dagger.hilt.android.AndroidEntryPoint

import android.util.Log
import com.google.firebase.FirebaseApp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val firebaseApp = FirebaseApp.getInstance()

        Log.d(
            "QEVIN_FIREBASE",
            "Firebase initialized: ${firebaseApp.name}"
        )

        Log.d(
            "QEVIN_FIREBASE",
            "Project ID: ${firebaseApp.options.projectId}"
        )
        setContent {
            QEVINTheme {
                QevinApp()
            }
        }
    }
}

