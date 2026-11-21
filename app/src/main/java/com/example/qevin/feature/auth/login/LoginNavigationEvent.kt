package com.example.qevin.feature.auth.login


sealed interface LoginNavigationEvent {
    data object LoginSuccess : LoginNavigationEvent
}
