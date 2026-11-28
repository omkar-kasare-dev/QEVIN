package com.example.qevin.presentation.people

import com.example.qevin.domain.model.PersonUi

data class PeopleUiState(
    val isLoading: Boolean = true,
    val currentUid: String? = null,
    val contacts: List<PersonUi> = emptyList(),
    val errorMessage: String? = null
)