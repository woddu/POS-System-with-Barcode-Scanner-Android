package com.example.firebaseapptest.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.firebaseapptest.data.local.entity.User

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?
}