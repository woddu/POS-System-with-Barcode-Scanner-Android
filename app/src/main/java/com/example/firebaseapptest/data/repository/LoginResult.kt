package com.example.firebaseapptest.data.repository

import com.example.firebaseapptest.data.local.entity.User

sealed interface LoginResult {
    data class Success(val user: User) : LoginResult
    object WrongPassword : LoginResult
    object UserNotFound : LoginResult
    object NetworkError : LoginResult
    data class UnknownError(val exception: Exception) : LoginResult
}
