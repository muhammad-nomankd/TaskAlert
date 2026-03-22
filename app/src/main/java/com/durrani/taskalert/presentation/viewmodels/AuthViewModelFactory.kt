package com.durrani.taskalert.presentation.viewmodels

import AuthRepositoryImpl
import AuthViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory to create AuthViewModel with its AuthRepository dependency.
 * Used in AuthenticationActivity like:
 *
 *   val viewModel: AuthViewModel by viewModels { AuthViewModelFactory(repository) }
 *
 * If you add Hilt later, delete this file and annotate the ViewModel with @HiltViewModel.
 */
class AuthViewModelFactory(
    private val repository: AuthRepositoryImpl
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}